/**
 * Mercora Feed Engine v2 - Global-Scale Recommendation System
 *
 * Scoring Formula (multiplicative):
 *   FinalScore = safePow(UserAffinity, affinityPower)
 *              × safePow(ContentQuality, qualityPower)
 *              × safePow(RecencyDecay, recencyPower)
 *              × safePow(SessionIntent, sessionIntentPower)
 *              × safePow(DiversityAdjust, diversityPower)
 *              × safePow(ExplorationFactor, explorationPower)
 *              × AntiManipulation
 *
 * All factors in [0.01, 2.0] range to avoid zeroing out.
 * Powers allow A/B testing by adjusting factor importance.
 * Zero heap allocations during scoring loop.
 */

#include "feed_engine.h"
#include <android/log.h>
#include <chrono>
#include <climits>

#define FE_TAG "MercoraFeedEngineV2"
#define FE_LOG(...) __android_log_print(ANDROID_LOG_INFO, FE_TAG, __VA_ARGS__)
#define FE_ERR(...) __android_log_print(ANDROID_LOG_ERROR, FE_TAG, __VA_ARGS__)

namespace mercora {

// ═══════════════════════════════════════════════════════════════
// CONSTRUCTOR / RESET
// ═══════════════════════════════════════════════════════════════

FeedEngine::FeedEngine()
    : historyLen_(0)
    , totalServed_(0)
{
    pool_.reset();
    userProfile_.reset();
    session_.reset();
    bandit_.reset();

    // Seed PRNG with high-resolution clock
    auto now = std::chrono::high_resolution_clock::now().time_since_epoch();
    uint64_t seed = static_cast<uint64_t>(
        std::chrono::duration_cast<std::chrono::nanoseconds>(now).count()
    );
    rngState_[0] = seed;
    rngState_[1] = seed ^ 0x6A09E667F3BCC908ULL;
    for (int i = 0; i < 8; i++) nextRandom();

    std::memset(typeHistory_, 0, sizeof(typeHistory_));
    std::memset(authorHistory_, 0, sizeof(authorHistory_));

    FE_LOG("FeedEngine v2 initialized (pool=%d, embed=%d, batch=%d)",
           MAX_FEED_ITEMS, EMBED_DIM, BATCH_SIZE);
}

void FeedEngine::setConfig(const FeedConfig& config) {
    config_ = config;
    FE_LOG("Config v2: aff=%.2f qual=%.2f rec=%.2f sess=%.2f div=%.2f expl=%.2f",
           config_.affinityPower, config_.qualityPower, config_.recencyPower,
           config_.sessionIntentPower, config_.diversityPower, config_.explorationPower);
}

void FeedEngine::resetSession() {
    pool_.reset();
    session_.reset();
    historyLen_ = 0;
    totalServed_ = 0;
    std::memset(typeHistory_, 0, sizeof(typeHistory_));
    std::memset(authorHistory_, 0, sizeof(authorHistory_));
    // Decay exploration rate across sessions
    bandit_.explorationRate *= bandit_.explorationDecay;
    if (bandit_.explorationRate < 0.02f) bandit_.explorationRate = 0.02f;
    userProfile_.sessionCount += 1.0f;
    FE_LOG("Session reset (sessions=%.0f, explRate=%.3f)",
           userProfile_.sessionCount, bandit_.explorationRate);
}

// ═══════════════════════════════════════════════════════════════
// USER PROFILE
// ═══════════════════════════════════════════════════════════════

void FeedEngine::setUserEmbedding(const float* embedding, int dim) {
    int d = std::min(dim, EMBED_DIM);
    std::memcpy(userProfile_.embedding, embedding, d * sizeof(float));
    if (d < EMBED_DIM) std::memset(userProfile_.embedding + d, 0, (EMBED_DIM - d) * sizeof(float));
    FE_LOG("User embedding set (%d dims)", d);
}

void FeedEngine::setUserTypeAffinity(const float* affinities, int count) {
    int c = std::min(count, static_cast<int>(FeedItemType::TYPE_COUNT));
    for (int i = 0; i < c; i++) userProfile_.typeAffinity[i] = affinities[i];
}

void FeedEngine::setUserStats(float avgDwell, float avgCompletion,
                               float sessionCount, float interactionRate) {
    userProfile_.avgDwellTimeMs = avgDwell;
    userProfile_.avgCompletionRate = avgCompletion;
    userProfile_.sessionCount = sessionCount;
    userProfile_.interactionRate = interactionRate;
}

// ═══════════════════════════════════════════════════════════════
// SESSION TRACKING
// ═══════════════════════════════════════════════════════════════

void FeedEngine::startSession() {
    session_.reset();
    FE_LOG("Session started");
}

void FeedEngine::reportDwell(int32_t sourceIndex, float dwellMs, bool interacted) {
    // Record in ring buffer
    int idx = session_.dwellCount % DWELL_HISTORY_SIZE;
    session_.dwellHistory[idx].sourceIndex = sourceIndex;
    session_.dwellHistory[idx].dwellMs = dwellMs;
    session_.dwellHistory[idx].interacted = interacted;

    // Find pool index for type
    for (int i = 0; i < pool_.count; i++) {
        if (pool_.indices[i] == sourceIndex) {
            session_.dwellHistory[idx].type = pool_.types[i];
            uint8_t t = pool_.types[i];

            // Update type affinity based on dwell quality
            bool positive = dwellMs >= config_.dwellGoodMs || interacted;
            bool negative = dwellMs < config_.dwellBadMs && !interacted;
            if (positive) {
                updateTypeAffinity(t, true);
                banditReward(t, true);
            } else if (negative) {
                updateTypeAffinity(t, false);
                banditReward(t, false);
            }

            // Update embedding from interaction
            if (interacted && pool_.hasEmbedding[i]) {
                float reward = interacted ? 1.0f : (positive ? 0.3f : -0.1f);
                updateEmbeddingFromInteraction(i, reward);
            }
            break;
        }
    }

    session_.dwellCount++;
    session_.itemsViewed++;
    if (interacted) session_.itemsInteracted++;

    // Update EMA of user dwell time
    float alpha = config_.emaSmoothFactor;
    userProfile_.avgDwellTimeMs = alpha * dwellMs + (1.0f - alpha) * userProfile_.avgDwellTimeMs;

    // Update session intent and fatigue
    updateSessionIntent();
    updateFatigue();
}

void FeedEngine::reportScrollSpeed(float pxPerSec) {
    session_.currentScrollSpeed = pxPerSec;
    float alpha = config_.emaSmoothFactor;
    userProfile_.avgScrollSpeed = alpha * pxPerSec + (1.0f - alpha) * userProfile_.avgScrollSpeed;
    updateFatigue();
}

void FeedEngine::reportVideoCompletion(int32_t sourceIndex, float completionRate) {
    float alpha = config_.emaSmoothFactor;
    userProfile_.avgCompletionRate = alpha * completionRate + (1.0f - alpha) * userProfile_.avgCompletionRate;

    // Positive signal if completion > threshold
    if (completionRate >= config_.completionGoodRate) {
        for (int i = 0; i < pool_.count; i++) {
            if (pool_.indices[i] == sourceIndex) {
                updateTypeAffinity(pool_.types[i], true);
                banditReward(pool_.types[i], true);
                break;
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// POOL MANAGEMENT
// ═══════════════════════════════════════════════════════════════

void FeedEngine::clearPool() {
    pool_.reset();
    FE_LOG("Pool cleared");
}

void FeedEngine::addItem(int32_t sourceIndex, FeedItemType type, int64_t timestamp,
                          int32_t likes, int32_t views, int32_t shares,
                          int32_t saves, int32_t comments, int32_t authorId,
                          uint8_t categoryId, float contentQuality, float credibility,
                          float completionRate) {
    if (pool_.count >= MAX_FEED_ITEMS) {
        FE_ERR("Pool full (max=%d)", MAX_FEED_ITEMS);
        return;
    }

    int i = pool_.count;
    pool_.indices[i]        = sourceIndex;
    pool_.types[i]          = static_cast<uint8_t>(type);
    pool_.timestamps[i]     = timestamp;
    pool_.likesCount[i]     = likes;
    pool_.viewsCount[i]     = views;
    pool_.sharesCount[i]    = shares;
    pool_.savesCount[i]     = saves;
    pool_.commentsCount[i]  = comments;
    pool_.authorIds[i]      = authorId;
    pool_.categoryIds[i]    = categoryId;
    pool_.contentQuality[i] = contentQuality;
    pool_.credibility[i]    = credibility;
    pool_.completionRate[i] = completionRate;
    pool_.hasEmbedding[i]   = false;
    pool_.consumed[i]       = false;
    pool_.baseScores[i]     = 0.0f;
    pool_.finalScores[i]    = 0.0f;
    pool_.count++;
}

void FeedEngine::setContentEmbedding(int32_t sourceIndex, const float* embedding, int dim) {
    for (int i = 0; i < pool_.count; i++) {
        if (pool_.indices[i] == sourceIndex) {
            int d = std::min(dim, EMBED_DIM);
            std::memcpy(pool_.contentEmbedding[i], embedding, d * sizeof(float));
            if (d < EMBED_DIM) std::memset(pool_.contentEmbedding[i] + d, 0, (EMBED_DIM - d) * sizeof(float));
            pool_.hasEmbedding[i] = true;
            return;
        }
    }
}

int FeedEngine::getAvailableCount() const {
    int avail = 0;
    for (int i = 0; i < pool_.count; i++) {
        if (!pool_.consumed[i]) avail++;
    }
    return avail;
}

// ═══════════════════════════════════════════════════════════════
// PRNG - xorshift128+
// ═══════════════════════════════════════════════════════════════

uint64_t FeedEngine::nextRandom() {
    uint64_t s1 = rngState_[0];
    uint64_t s0 = rngState_[1];
    uint64_t result = s0 + s1;
    rngState_[0] = s0;
    s1 ^= s1 << 23;
    rngState_[1] = s1 ^ s0 ^ (s1 >> 17) ^ (s0 >> 26);
    return result;
}

float FeedEngine::randomFloat() {
    return static_cast<float>(nextRandom() >> 11) * (1.0f / 9007199254740992.0f);
}

// Approximation of Beta distribution sample using Jorgensen method
// For Thompson Sampling — cheap, sufficient quality
float FeedEngine::randomBeta(float alpha, float beta) {
    // Use the ratio of Gamma variates approximation
    // For alpha,beta >= 1 this is fast and accurate enough
    // Simplified: use mean + noise scaled by variance
    float mean = alpha / (alpha + beta);
    float var = (alpha * beta) / ((alpha + beta) * (alpha + beta) * (alpha + beta + 1.0f));
    float stddev = std::sqrt(var);
    // Box-Muller-ish: use uniform -> approximate normal
    float u1 = randomFloat();
    float u2 = randomFloat();
    float z = std::sqrt(-2.0f * std::log(u1 + 1e-10f)) * std::cos(6.2831853f * u2);
    float sample = mean + stddev * z;
    return std::max(0.001f, std::min(0.999f, sample));
}

// ═══════════════════════════════════════════════════════════════
// UTILITY
// ═══════════════════════════════════════════════════════════════

int64_t FeedEngine::currentTimeMs() const {
    auto now = std::chrono::system_clock::now().time_since_epoch();
    return std::chrono::duration_cast<std::chrono::milliseconds>(now).count();
}

float FeedEngine::dotProduct(const float* a, const float* b, int dim) const {
    float sum = 0.0f;
    // Manual unroll by 4 for NEON-friendly codegen
    int i = 0;
    for (; i + 3 < dim; i += 4) {
        sum += a[i] * b[i] + a[i+1] * b[i+1] + a[i+2] * b[i+2] + a[i+3] * b[i+3];
    }
    for (; i < dim; i++) {
        sum += a[i] * b[i];
    }
    return sum;
}

float FeedEngine::safePow(float base, float power) const {
    if (base <= 0.0f) return 0.01f;
    if (power == 1.0f) return base;
    if (power == 0.0f) return 1.0f;
    return std::pow(base, power);
}

// ═══════════════════════════════════════════════════════════════
// SCORING PIPELINE - Each factor computed independently
// ═══════════════════════════════════════════════════════════════

float FeedEngine::computeUserAffinity(int idx) const {
    uint8_t type = pool_.types[idx];

    // Component 1: Type affinity (user's learned preference)
    float typeAff = 1.0f;
    if (type < static_cast<uint8_t>(FeedItemType::TYPE_COUNT)) {
        typeAff = userProfile_.typeAffinity[type];
    }

    // Component 2: Embedding similarity (if available)
    float embedSim = 0.5f; // Neutral default
    if (pool_.hasEmbedding[idx]) {
        // Cosine similarity via dot product (assuming normalized embeddings)
        float dot = dotProduct(userProfile_.embedding, pool_.contentEmbedding[idx], EMBED_DIM);
        embedSim = (dot + 1.0f) * 0.5f; // Map [-1,1] to [0,1]
    }

    // For cold-start users, weight embedding less
    float embedWeight = userProfile_.isColdStart() ? 0.2f : 0.6f;
    float typeWeight = 1.0f - embedWeight;

    float affinity = typeAff * typeWeight + embedSim * embedWeight;

    // Type weight from config (backward compat)
    float configWeight = (type < static_cast<uint8_t>(FeedItemType::TYPE_COUNT))
                         ? config_.typeWeights[type] : 0.5f;

    return std::max(0.01f, affinity * configWeight);
}

float FeedEngine::computeContentQuality(int idx) const {
    // Use pre-computed quality if available (from backend)
    float quality = pool_.contentQuality[idx];

    if (quality <= 0.0f) {
        // Fallback: compute locally from engagement signals
        float likes  = std::log2(static_cast<float>(pool_.likesCount[idx] + 1)) / 15.0f;
        float views  = std::log2(static_cast<float>(pool_.viewsCount[idx] + 1)) / 20.0f;
        float shares = std::log2(static_cast<float>(pool_.sharesCount[idx] + 1)) / 10.0f;
        float saves  = std::log2(static_cast<float>(pool_.savesCount[idx] + 1)) / 12.0f;
        float comments = std::log2(static_cast<float>(pool_.commentsCount[idx] + 1)) / 12.0f;

        // Saves and shares are higher-quality signals than likes
        quality = likes * 0.15f + views * 0.15f + shares * 0.25f + saves * 0.25f + comments * 0.20f;

        // Boost by completion rate for video content
        uint8_t type = pool_.types[idx];
        if (type == static_cast<uint8_t>(FeedItemType::POST_VIDEO) ||
            type == static_cast<uint8_t>(FeedItemType::REND_VIDEO)) {
            quality *= (0.5f + pool_.completionRate[idx] * 0.5f);
        }
    }

    return std::max(0.01f, std::min(quality, 1.5f));
}

float FeedEngine::computeRecencyDecay(int idx) const {
    int64_t now = currentTimeMs();
    int64_t ageMs = now - pool_.timestamps[idx];
    if (ageMs < 0) ageMs = 0;

    float halfLifeMs = config_.recencyHalfLifeHours * 3600000.0f;
    float lambda = 0.693147f / halfLifeMs;
    float decay = std::exp(-lambda * static_cast<float>(ageMs));

    return std::max(0.01f, decay);
}

float FeedEngine::computeSessionIntent(int idx) const {
    uint8_t type = pool_.types[idx];
    if (type >= static_cast<uint8_t>(FeedItemType::TYPE_COUNT)) return 1.0f;

    // Intent = how much user engages with this type in current session
    float intent = session_.intentByType[type];

    // Fatigue adjustment: when fatigued, boost novel types
    if (session_.fatigueLevel > 0.5f) {
        // If user is fatigued on this type, penalize; boost others
        float fatigueBoost = (1.0f - intent) * session_.fatigueLevel * 0.3f;
        intent = intent * (1.0f - session_.fatigueLevel * 0.2f) + fatigueBoost;
    }

    return std::max(0.1f, std::min(intent, 2.0f));
}

float FeedEngine::computeDiversityAdjust(int idx) const {
    uint8_t type = pool_.types[idx];
    int32_t author = pool_.authorIds[idx];
    float score = 1.0f;

    // 1. Type clustering penalty (same as v1 but enhanced)
    int typeCount = 0;
    int window = std::min(historyLen_, config_.antiClusterWindow * 2);
    for (int i = historyLen_ - window; i < historyLen_ && i >= 0; i++) {
        if (typeHistory_[i] == type) typeCount++;
    }
    if (window > 0) {
        float ratio = static_cast<float>(typeCount) / static_cast<float>(window);
        score *= (1.0f - ratio * 0.7f);
    }

    // 2. Consecutive type penalty
    int consecutive = 0;
    for (int i = historyLen_ - 1; i >= 0 && i >= historyLen_ - config_.antiClusterWindow; i--) {
        if (typeHistory_[i] == type) consecutive++;
        else break;
    }
    if (consecutive >= config_.antiClusterWindow) {
        score *= config_.clusterPenalty;
    } else if (consecutive >= 2) {
        score *= 0.8f;
    }

    // 3. Author dedup: penalize if same author appeared recently
    for (int i = historyLen_ - 1; i >= 0 && i >= historyLen_ - config_.authorDedup; i--) {
        if (authorHistory_[i] == author && author != 0) {
            score *= config_.authorRepeatPenalty;
            break;
        }
    }

    // 4. Category spread bonus: boost underrepresented categories
    uint8_t cat = pool_.categoryIds[idx];
    int catCount = 0;
    int catWindow = std::min(historyLen_, 12);
    for (int i = historyLen_ - catWindow; i < historyLen_ && i >= 0; i++) {
        // Check what category was at that history position
        // We can't easily track this without more storage, so skip for now
        // This will be enhanced in Phase 2 with category history
    }
    (void)catCount; (void)cat; // Suppress unused warnings

    return std::max(0.1f, score);
}

float FeedEngine::computeExplorationFactor(int idx) {
    uint8_t type = pool_.types[idx];
    if (type >= BANDIT_ARMS) return 1.0f;

    // Thompson Sampling: sample from Beta(alpha, beta) for each arm
    float sample = thompsonSample(type);

    // Cold-start boost: explore more aggressively for new users
    if (userProfile_.isColdStart()) {
        sample = 0.5f + sample * 0.5f; // Floor at 0.5, stretch to [0.5, 1.0]
        // Add extra randomness
        sample += randomFloat() * 0.2f;
    }

    // Base exploration: occasionally boost random content to break echo chambers
    if (randomFloat() < bandit_.explorationRate) {
        sample = std::max(sample, 0.7f + randomFloat() * 0.3f);
    }

    return std::max(0.1f, std::min(sample * 1.5f, 2.0f));
}

float FeedEngine::computeAntiManipulation(int idx) const {
    float cred = pool_.credibility[idx];

    // If credibility is pre-computed (from backend), use it
    if (cred > 0.0f) {
        if (cred < config_.antiManip.minCredibility) {
            return config_.antiManip.spamPenalty;
        }
        return cred;
    }

    // Local heuristic: detect engagement spikes
    float spikePenalty = detectEngagementSpike(idx);

    // Combine
    float score = 1.0f * spikePenalty;
    return std::max(0.01f, score);
}

float FeedEngine::detectEngagementSpike(int idx) const {
    // Heuristic: if likes/views ratio is suspiciously high, penalize
    int32_t views = pool_.viewsCount[idx];
    int32_t likes = pool_.likesCount[idx];
    int32_t shares = pool_.sharesCount[idx];

    if (views <= 10) return 1.0f; // Not enough data

    float likeRate = static_cast<float>(likes) / static_cast<float>(views);
    float shareRate = static_cast<float>(shares) / static_cast<float>(views);

    // Suspicious if >50% like rate or >20% share rate
    float penalty = 1.0f;
    if (likeRate > 0.5f) {
        penalty *= 0.6f;
    }
    if (shareRate > 0.2f) {
        penalty *= 0.7f;
    }

    // Age-based velocity check: too much engagement too fast
    int64_t now = currentTimeMs();
    int64_t ageMs = now - pool_.timestamps[idx];
    if (ageMs > 0 && ageMs < 3600000) { // Less than 1 hour old
        float hoursOld = static_cast<float>(ageMs) / 3600000.0f;
        float velocity = static_cast<float>(likes + shares) / hoursOld;
        if (velocity > config_.antiManip.engagementVelocityMax) {
            float overshoot = velocity / config_.antiManip.engagementVelocityMax;
            penalty *= 1.0f / overshoot;
        }
    }

    return std::max(0.1f, penalty);
}

// ═══════════════════════════════════════════════════════════════
// BANDIT SYSTEM
// ═══════════════════════════════════════════════════════════════

float FeedEngine::thompsonSample(int armIdx) {
    if (armIdx < 0 || armIdx >= BANDIT_ARMS) return 0.5f;
    return randomBeta(bandit_.arms[armIdx].alpha, bandit_.arms[armIdx].beta);
}

void FeedEngine::banditReward(uint8_t type, bool positive) {
    if (type >= BANDIT_ARMS) return;
    if (positive) {
        bandit_.arms[type].alpha += 1.0f;
    } else {
        bandit_.arms[type].beta += 1.0f;
    }
    // Cap to prevent overflow and allow forgetting
    if (bandit_.arms[type].alpha + bandit_.arms[type].beta > 100.0f) {
        bandit_.arms[type].alpha *= 0.9f;
        bandit_.arms[type].beta *= 0.9f;
    }
}

// ═══════════════════════════════════════════════════════════════
// SESSION INTENT + FATIGUE
// ═══════════════════════════════════════════════════════════════

void FeedEngine::updateSessionIntent() {
    // Compute intent per type from recent dwell history
    float typeSum[static_cast<int>(FeedItemType::TYPE_COUNT)] = {};
    float typeCount[static_cast<int>(FeedItemType::TYPE_COUNT)] = {};

    int records = std::min(session_.dwellCount, DWELL_HISTORY_SIZE);
    for (int i = 0; i < records; i++) {
        int ri = (session_.dwellCount - 1 - i) % DWELL_HISTORY_SIZE;
        uint8_t t = session_.dwellHistory[ri].type;
        if (t >= static_cast<uint8_t>(FeedItemType::TYPE_COUNT)) continue;

        // Recency-weighted: more recent = more weight
        float weight = 1.0f / (1.0f + static_cast<float>(i) * 0.1f);
        float quality = 0.0f;
        if (session_.dwellHistory[ri].interacted) quality = 1.0f;
        else if (session_.dwellHistory[ri].dwellMs >= config_.dwellGoodMs) quality = 0.6f;
        else if (session_.dwellHistory[ri].dwellMs >= config_.dwellBadMs) quality = 0.3f;
        else quality = -0.2f;

        typeSum[t] += quality * weight;
        typeCount[t] += weight;
    }

    for (int t = 0; t < static_cast<int>(FeedItemType::TYPE_COUNT); t++) {
        if (typeCount[t] > 0.0f) {
            float intent = typeSum[t] / typeCount[t];
            // Map to [0.3, 1.7] range
            session_.intentByType[t] = 1.0f + intent * 0.7f;
        } else {
            session_.intentByType[t] = 1.0f; // Neutral
        }
    }
}

void FeedEngine::updateFatigue() {
    // Fatigue increases with: session length, fast scrolling, low dwell times
    float sessionMinutes = session_.sessionDurationMs / 60000.0f;
    float lengthFatigue = std::min(sessionMinutes / 30.0f, 1.0f); // Max at 30 min

    float speedFatigue = 0.0f;
    if (session_.currentScrollSpeed > 1500.0f) {
        speedFatigue = std::min((session_.currentScrollSpeed - 1500.0f) / 3000.0f, 1.0f);
    }

    float dwellFatigue = 0.0f;
    if (userProfile_.avgDwellTimeMs < 1500.0f) {
        dwellFatigue = 1.0f - (userProfile_.avgDwellTimeMs / 1500.0f);
    }

    session_.fatigueLevel = lengthFatigue * 0.4f + speedFatigue * 0.3f + dwellFatigue * 0.3f;
    session_.fatigueLevel = std::min(session_.fatigueLevel, 1.0f);
}

// ═══════════════════════════════════════════════════════════════
// USER PROFILE LEARNING (lightweight on-device)
// ═══════════════════════════════════════════════════════════════

void FeedEngine::updateTypeAffinity(uint8_t type, bool positive) {
    if (type >= static_cast<uint8_t>(FeedItemType::TYPE_COUNT)) return;
    float alpha = config_.emaSmoothFactor;
    float target = positive ? 1.3f : 0.7f;
    userProfile_.typeAffinity[type] = alpha * target + (1.0f - alpha) * userProfile_.typeAffinity[type];
    // Clamp to [0.3, 2.0]
    userProfile_.typeAffinity[type] = std::max(0.3f, std::min(userProfile_.typeAffinity[type], 2.0f));
}

void FeedEngine::updateEmbeddingFromInteraction(int poolIdx, float reward) {
    if (!pool_.hasEmbedding[poolIdx]) return;
    // Lightweight embedding update: nudge user embedding toward interacted content
    // This is a simplified version of gradient descent on cosine similarity
    float lr = config_.emaSmoothFactor * 0.1f * reward; // Very small learning rate
    for (int d = 0; d < EMBED_DIM; d++) {
        userProfile_.embedding[d] += lr * (pool_.contentEmbedding[poolIdx][d] - userProfile_.embedding[d]);
    }
    // L2 normalize to prevent drift
    float norm = 0.0f;
    for (int d = 0; d < EMBED_DIM; d++) norm += userProfile_.embedding[d] * userProfile_.embedding[d];
    norm = std::sqrt(norm);
    if (norm > 0.01f) {
        for (int d = 0; d < EMBED_DIM; d++) userProfile_.embedding[d] /= norm;
    }
}

// ═══════════════════════════════════════════════════════════════
// INJECTION
// ═══════════════════════════════════════════════════════════════

bool FeedEngine::shouldInjectType(FeedItemType type) const {
    if (type == FeedItemType::SUGGESTED && totalServed_ > 0) {
        return (totalServed_ % config_.suggestedInterval) == 0;
    }
    if (type == FeedItemType::SPECIAL && totalServed_ > 0) {
        return (totalServed_ % config_.specialInterval) == 0;
    }
    return false;
}

// ═══════════════════════════════════════════════════════════════
// MAIN SCORING PIPELINE
// ═══════════════════════════════════════════════════════════════

void FeedEngine::computeAllScores(bool initialBatch) {
    for (int i = 0; i < pool_.count; i++) {
        if (pool_.consumed[i]) {
            pool_.finalScores[i] = -1.0f;
            continue;
        }

        uint8_t type = pool_.types[i];

        // For initial batch: only content items
        if (initialBatch) {
            if (type != static_cast<uint8_t>(FeedItemType::POST_IMAGE) &&
                type != static_cast<uint8_t>(FeedItemType::POST_VIDEO) &&
                type != static_cast<uint8_t>(FeedItemType::REND_VIDEO)) {
                pool_.finalScores[i] = -1.0f;
                continue;
            }
        }

        // ═══ 6-FACTOR MULTIPLICATIVE SCORE ═══
        float affinity    = computeUserAffinity(i);
        float quality     = computeContentQuality(i);
        float recency     = computeRecencyDecay(i);
        float intent      = computeSessionIntent(i);
        float diversity   = computeDiversityAdjust(i);
        float exploration = computeExplorationFactor(i);
        float antiManip   = computeAntiManipulation(i);

        // Base = stable factors (don't change between picks in same batch)
        float base = safePow(affinity, config_.affinityPower)
                   * safePow(quality, config_.qualityPower)
                   * safePow(recency, config_.recencyPower)
                   * safePow(intent, config_.sessionIntentPower)
                   * antiManip;

        if (initialBatch) base *= 1.5f;

        pool_.baseScores[i] = base;

        // Final = base × volatile factors (diversity + exploration change per pick)
        pool_.finalScores[i] = base
                             * safePow(diversity, config_.diversityPower)
                             * safePow(exploration, config_.explorationPower);
    }
}

// ═══════════════════════════════════════════════════════════════
// SELECTION + HISTORY
// ═══════════════════════════════════════════════════════════════

void FeedEngine::updateHistory(int poolIdx) {
    if (historyLen_ < MAX_HISTORY) {
        typeHistory_[historyLen_] = pool_.types[poolIdx];
        authorHistory_[historyLen_] = pool_.authorIds[poolIdx];
        historyLen_++;
    } else {
        std::memmove(typeHistory_, typeHistory_ + 1, MAX_HISTORY - 1);
        typeHistory_[MAX_HISTORY - 1] = pool_.types[poolIdx];
        std::memmove(authorHistory_, authorHistory_ + 1, (MAX_HISTORY - 1) * sizeof(int32_t));
        authorHistory_[MAX_HISTORY - 1] = pool_.authorIds[poolIdx];
    }
}

int FeedEngine::selectTopN(int32_t* outIndices, int n, bool onlyContent) {
    int selected = 0;

    for (int sel = 0; sel < n; sel++) {
        float bestScore = -2.0f;
        int bestIdx = -1;

        for (int i = 0; i < pool_.count; i++) {
            if (pool_.consumed[i]) continue;
            if (pool_.finalScores[i] <= -1.0f) continue;

            if (onlyContent) {
                uint8_t t = pool_.types[i];
                if (t != static_cast<uint8_t>(FeedItemType::POST_IMAGE) &&
                    t != static_cast<uint8_t>(FeedItemType::POST_VIDEO) &&
                    t != static_cast<uint8_t>(FeedItemType::REND_VIDEO)) {
                    continue;
                }
            }

            if (pool_.finalScores[i] > bestScore) {
                bestScore = pool_.finalScores[i];
                bestIdx = i;
            }
        }

        if (bestIdx < 0) break;

        pool_.consumed[bestIdx] = true;
        outIndices[selected] = pool_.indices[bestIdx];
        updateHistory(bestIdx);
        totalServed_++;
        selected++;

        // Re-score remaining: diversity + exploration change after each pick
        // Other factors (affinity, quality, recency, intent, antiManip) are stable
        if (sel < n - 1) {
            for (int i = 0; i < pool_.count; i++) {
                if (pool_.consumed[i] || pool_.finalScores[i] <= -1.0f) continue;
                float diversity   = computeDiversityAdjust(i);
                float exploration = computeExplorationFactor(i);
                // Re-use cached base score (affinity * quality * recency * intent * antiManip)
                // and apply updated diversity + exploration
                float base = pool_.baseScores[i]; // Includes all stable factors
                pool_.finalScores[i] = base
                    * safePow(diversity, config_.diversityPower)
                    * safePow(exploration, config_.explorationPower);
            }
        }
    }

    return selected;
}

// ═══════════════════════════════════════════════════════════════
// PUBLIC API
// ═══════════════════════════════════════════════════════════════

int FeedEngine::generateInitialBatch(int32_t* outIndices, int maxOut) {
    if (pool_.count == 0 || maxOut <= 0) return 0;
    int batchSize = std::min(config_.initialBatchSize, maxOut);

    FE_LOG("v2: Initial batch (size=%d, pool=%d, coldStart=%d)",
           batchSize, pool_.count, userProfile_.isColdStart() ? 1 : 0);

    computeAllScores(true);
    int count = selectTopN(outIndices, batchSize, true);

    FE_LOG("v2: Initial batch -> %d items", count);
    return count;
}

int FeedEngine::generateNextBatch(int32_t* outIndices, int maxOut) {
    if (pool_.count == 0 || maxOut <= 0) return 0;
    int batchSize = std::min(config_.batchSize, maxOut);

    bool needsSuggested = shouldInjectType(FeedItemType::SUGGESTED);
    bool needsSpecial = shouldInjectType(FeedItemType::SPECIAL);

    FE_LOG("v2: Next batch (size=%d, avail=%d, served=%d, fatigue=%.2f)",
           batchSize, getAvailableCount(), totalServed_, session_.fatigueLevel);

    computeAllScores(false);

    // Boost injection types
    if (needsSuggested) {
        for (int i = 0; i < pool_.count; i++) {
            if (!pool_.consumed[i] &&
                pool_.types[i] == static_cast<uint8_t>(FeedItemType::SUGGESTED)) {
                pool_.finalScores[i] += 100.0f;
            }
        }
    }
    if (needsSpecial) {
        for (int i = 0; i < pool_.count; i++) {
            if (!pool_.consumed[i] &&
                pool_.types[i] == static_cast<uint8_t>(FeedItemType::SPECIAL)) {
                pool_.finalScores[i] += 100.0f;
            }
        }
    }

    int count = selectTopN(outIndices, batchSize, false);

    FE_LOG("v2: Next batch -> %d items (total=%d)", count, totalServed_);
    return count;
}

int FeedEngine::getPrefetchHints(int32_t* outIndices, int maxOut) {
    if (pool_.count == 0 || maxOut <= 0) return 0;
    int prefetchCount = std::min(PREFETCH_AHEAD, maxOut);

    computeAllScores(false);

    struct ScoredIdx { int32_t idx; float score; };
    // Stack-allocated for zero-alloc path when count is small
    ScoredIdx candidates[128];
    int candCount = 0;

    for (int i = 0; i < pool_.count && candCount < 128; i++) {
        if (!pool_.consumed[i] && pool_.finalScores[i] > -1.0f) {
            candidates[candCount++] = {pool_.indices[i], pool_.finalScores[i]};
        }
    }

    int n = std::min(prefetchCount, candCount);
    if (n > 0) {
        std::partial_sort(candidates, candidates + n, candidates + candCount,
                          [](const ScoredIdx& a, const ScoredIdx& b) {
                              return a.score > b.score;
                          });
    }

    for (int i = 0; i < n; i++) outIndices[i] = candidates[i].idx;
    return n;
}

// ═══════════════════════════════════════════════════════════════
// DYNAMIC CONFIG (A/B TESTING)
// ═══════════════════════════════════════════════════════════════

void FeedEngine::setTypeWeight(FeedItemType type, float weight) {
    int idx = static_cast<int>(type);
    if (idx >= 0 && idx < static_cast<int>(FeedItemType::TYPE_COUNT)) {
        config_.typeWeights[idx] = weight;
    }
}

void FeedEngine::setFactorPowers(float affinity, float quality, float recency,
                                  float sessionIntent, float diversity, float exploration) {
    config_.affinityPower = affinity;
    config_.qualityPower = quality;
    config_.recencyPower = recency;
    config_.sessionIntentPower = sessionIntent;
    config_.diversityPower = diversity;
    config_.explorationPower = exploration;
    FE_LOG("v2: Factor powers updated: aff=%.2f qual=%.2f rec=%.2f sess=%.2f div=%.2f expl=%.2f",
           affinity, quality, recency, sessionIntent, diversity, exploration);
}

void FeedEngine::setExplorationRate(float rate) {
    bandit_.explorationRate = std::max(0.01f, std::min(rate, 0.5f));
}

void FeedEngine::setBanditPriors(const float* alphas, const float* betas, int count) {
    int c = std::min(count, BANDIT_ARMS);
    for (int i = 0; i < c; i++) {
        bandit_.arms[i].alpha = std::max(0.1f, alphas[i]);
        bandit_.arms[i].beta = std::max(0.1f, betas[i]);
    }
}

// ═══════════════════════════════════════════════════════════════
// DEBUG
// ═══════════════════════════════════════════════════════════════

void FeedEngine::getLastScoreBreakdown(int32_t sourceIndex, float* outFactors, int maxFactors) {
    if (maxFactors < 7) return;
    for (int i = 0; i < pool_.count; i++) {
        if (pool_.indices[i] == sourceIndex) {
            outFactors[0] = computeUserAffinity(i);
            outFactors[1] = computeContentQuality(i);
            outFactors[2] = computeRecencyDecay(i);
            outFactors[3] = computeSessionIntent(i);
            outFactors[4] = computeDiversityAdjust(i);
            outFactors[5] = computeExplorationFactor(i);
            outFactors[6] = computeAntiManipulation(i);
            return;
        }
    }
    std::memset(outFactors, 0, maxFactors * sizeof(float));
}

} // namespace mercora
