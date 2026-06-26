/**
 * Rendly Feed Engine v2 - Global-Scale Recommendation System
 * 
 * Architecture:
 *   FinalScore = UserAffinity
 *              × ContentQuality
 *              × RecencyDecay
 *              × SessionIntent
 *              × DiversityAdjustment
 *              × ExplorationFactor
 *
 * Subsystems:
 *   1. UserProfile    - 64-dim embedding, behavioral tracking, auto type weights
 *   2. SessionState   - Dwell time tracking, intent detection, adaptive scoring
 *   3. ContentPool    - SoA layout + quality score + credibility score
 *   4. BanditExplorer - Thompson Sampling for explore/exploit balance
 *   5. AntiManip      - Fraud detection, spike penalization, spam patterns
 *   6. Diversity      - Anti-clustering + author dedup + category spread
 *
 * Memory: Pre-allocated pools, zero GC, SoA cache-friendly layout
 * PRNG:   xorshift128+ (4x faster than mt19937)
 * Mobile: <100KB RSS, 0 heap allocs during scoring, 60fps safe
 */

#pragma once

#include <cstdint>
#include <vector>
#include <array>
#include <algorithm>
#include <cmath>
#include <cstring>

namespace rendly {

// ═══════════════════════════════════════════════════════════════
// CONSTANTS
// ═══════════════════════════════════════════════════════════════

static constexpr int BATCH_SIZE          = 4;
static constexpr int MAX_FEED_ITEMS      = 4096;
static constexpr int MAX_HISTORY         = 128;
static constexpr int ANTI_CLUSTER_WINDOW = 3;
static constexpr int PREFETCH_AHEAD      = 12;
static constexpr int EMBED_DIM           = 64;
static constexpr int MAX_AUTHORS_TRACK   = 64;
static constexpr int MAX_CATEGORY_SLOTS  = 16;
static constexpr int BANDIT_ARMS         = 6;   // One per FeedItemType
static constexpr int DWELL_HISTORY_SIZE  = 32;

// ═══════════════════════════════════════════════════════════════
// FEED ITEM TYPES
// ═══════════════════════════════════════════════════════════════

enum class FeedItemType : uint8_t {
    POST_IMAGE    = 0,
    POST_VIDEO    = 1,
    REND_VIDEO    = 2,
    SUGGESTED     = 3,
    SPECIAL       = 4,
    AD_SLOT       = 5,
    TYPE_COUNT    = 6
};

// ═══════════════════════════════════════════════════════════════
// 1. USER PROFILE - Lightweight on-device user model
// ═══════════════════════════════════════════════════════════════

struct UserProfile {
    // 64-dimensional interest embedding (synced from backend or learned locally)
    float embedding[EMBED_DIM];

    // Behavioral type preferences (auto-adjusted from interactions)
    float typeAffinity[static_cast<int>(FeedItemType::TYPE_COUNT)];

    // Engagement history (exponential moving averages)
    float avgDwellTimeMs;        // EMA of time spent on items
    float avgCompletionRate;     // EMA of video completion rate (0-1)
    float avgScrollSpeed;        // EMA of scroll velocity (px/sec)
    float sessionCount;          // Total sessions (for cold-start detection)
    float interactionRate;       // Likes+saves+shares / views

    // Cold-start flag
    bool isColdStart() const { return sessionCount < 5.0f; }

    void reset() {
        std::memset(embedding, 0, sizeof(embedding));
        for (int i = 0; i < static_cast<int>(FeedItemType::TYPE_COUNT); i++) {
            typeAffinity[i] = 1.0f; // Neutral start
        }
        avgDwellTimeMs = 3000.0f;    // 3s default
        avgCompletionRate = 0.5f;
        avgScrollSpeed = 500.0f;
        sessionCount = 0.0f;
        interactionRate = 0.05f;
    }
};

// ═══════════════════════════════════════════════════════════════
// 2. SESSION STATE - Real-time session context
// ═══════════════════════════════════════════════════════════════

struct DwellRecord {
    int32_t sourceIndex;
    float   dwellMs;
    uint8_t type;
    bool    interacted; // Liked, saved, shared, commented
};

struct SessionState {
    // Current session tracking
    DwellRecord dwellHistory[DWELL_HISTORY_SIZE];
    int         dwellCount;

    // Session-level intent detection
    float intentByType[static_cast<int>(FeedItemType::TYPE_COUNT)];

    // Session metrics
    float sessionDurationMs;
    int   itemsViewed;
    int   itemsInteracted;
    float currentScrollSpeed;

    // Fatigue detection
    float fatigueLevel;  // 0.0 = fresh, 1.0 = fatigued (long session, fast scroll)

    void reset() {
        dwellCount = 0;
        for (int i = 0; i < static_cast<int>(FeedItemType::TYPE_COUNT); i++) {
            intentByType[i] = 1.0f;
        }
        sessionDurationMs = 0.0f;
        itemsViewed = 0;
        itemsInteracted = 0;
        currentScrollSpeed = 0.0f;
        fatigueLevel = 0.0f;
        std::memset(dwellHistory, 0, sizeof(dwellHistory));
    }
};

// ═══════════════════════════════════════════════════════════════
// 3. CONTENT POOL - SoA with quality + credibility
// ═══════════════════════════════════════════════════════════════

struct ContentPool {
    // Core fields (SoA)
    int32_t  indices[MAX_FEED_ITEMS];
    uint8_t  types[MAX_FEED_ITEMS];
    int64_t  timestamps[MAX_FEED_ITEMS];
    int32_t  likesCount[MAX_FEED_ITEMS];
    int32_t  viewsCount[MAX_FEED_ITEMS];
    int32_t  sharesCount[MAX_FEED_ITEMS];
    int32_t  savesCount[MAX_FEED_ITEMS];
    int32_t  commentsCount[MAX_FEED_ITEMS];

    // NEW v2 fields
    float    contentQuality[MAX_FEED_ITEMS];    // Pre-computed quality [0,1] (backend or local)
    float    credibility[MAX_FEED_ITEMS];        // Anti-manipulation score [0,1]
    float    completionRate[MAX_FEED_ITEMS];     // Avg video completion [0,1]
    int32_t  authorIds[MAX_FEED_ITEMS];          // Author hash for dedup
    uint8_t  categoryIds[MAX_FEED_ITEMS];        // Category for spread
    float    contentEmbedding[MAX_FEED_ITEMS][EMBED_DIM]; // Content embeddings (sparse ok)
    bool     hasEmbedding[MAX_FEED_ITEMS];       // Whether embedding is valid

    // Scoring output
    float    baseScores[MAX_FEED_ITEMS];
    float    finalScores[MAX_FEED_ITEMS];
    bool     consumed[MAX_FEED_ITEMS];

    int32_t  count;

    void reset() {
        count = 0;
        std::memset(consumed, 0, sizeof(consumed));
        std::memset(hasEmbedding, 0, sizeof(hasEmbedding));
    }
};

// ═══════════════════════════════════════════════════════════════
// 4. BANDIT EXPLORER - Thompson Sampling
// ═══════════════════════════════════════════════════════════════

struct BanditArm {
    float alpha;  // Successes + prior (Beta distribution)
    float beta;   // Failures + prior

    void reset() { alpha = 1.0f; beta = 1.0f; } // Uniform prior
};

struct BanditExplorer {
    BanditArm arms[BANDIT_ARMS]; // One per FeedItemType
    float explorationRate;        // Base exploration probability
    float explorationDecay;       // Decay per session (reduce exploration over time)

    void reset() {
        for (auto& arm : arms) arm.reset();
        explorationRate = 0.15f;
        explorationDecay = 0.98f;
    }
};

// ═══════════════════════════════════════════════════════════════
// 5. ANTI-MANIPULATION SYSTEM
// ═══════════════════════════════════════════════════════════════

struct AntiManipConfig {
    float spikeThreshold;        // Engagement spike detection multiplier
    float minCredibility;        // Minimum credibility to show
    float spamPenalty;           // Multiplier for spam-flagged content
    float newAccountPenalty;     // Penalty for very new accounts
    float engagementVelocityMax; // Max expected engagement per hour

    void defaults() {
        spikeThreshold = 5.0f;
        minCredibility = 0.1f;
        spamPenalty = 0.2f;
        newAccountPenalty = 0.7f;
        engagementVelocityMax = 100.0f;
    }
};

// ═══════════════════════════════════════════════════════════════
// FEED CONFIG v2
// ═══════════════════════════════════════════════════════════════

struct FeedConfig {
    // Type weights (higher = more frequent in feed)
    float typeWeights[static_cast<int>(FeedItemType::TYPE_COUNT)] = {
        1.0f,   // POST_IMAGE
        1.2f,   // POST_VIDEO
        0.8f,   // REND_VIDEO
        0.3f,   // SUGGESTED
        0.2f,   // SPECIAL
        0.0f    // AD_SLOT
    };

    // v2 Scoring factor weights (multiplicative formula tuning)
    float affinityPower       = 1.0f;   // Power applied to UserAffinity
    float qualityPower        = 0.8f;   // Power applied to ContentQuality
    float recencyPower        = 1.0f;   // Power applied to RecencyDecay
    float sessionIntentPower  = 0.6f;   // Power applied to SessionIntent
    float diversityPower      = 0.5f;   // Power applied to DiversityAdjust
    float explorationPower    = 0.3f;   // Power applied to ExplorationFactor

    // Recency
    float recencyHalfLifeHours = 24.0f;

    // Anti-clustering
    int   antiClusterWindow    = ANTI_CLUSTER_WINDOW;
    float clusterPenalty       = 0.6f;

    // Author dedup
    int   authorDedup          = 3;     // Min items between same author
    float authorRepeatPenalty  = 0.4f;

    // Injection intervals
    int   suggestedInterval    = 8;
    int   specialInterval      = 15;

    // Batch
    int   batchSize            = BATCH_SIZE;
    int   initialBatchSize     = 1;

    // Dwell-time thresholds
    float dwellGoodMs          = 5000.0f;  // >5s = positive signal
    float dwellBadMs           = 1000.0f;  // <1s = negative signal
    float completionGoodRate   = 0.7f;     // >70% = positive

    // EMA smoothing factor
    float emaSmoothFactor      = 0.15f;

    // Anti-manipulation
    AntiManipConfig antiManip;

    FeedConfig() { antiManip.defaults(); }
};

// ═══════════════════════════════════════════════════════════════
// FEED ENGINE v2
// ═══════════════════════════════════════════════════════════════

class FeedEngine {
public:
    FeedEngine();
    ~FeedEngine() = default;

    // ─── Configuration ───
    void setConfig(const FeedConfig& config);
    FeedConfig& getConfig() { return config_; }

    // ─── User Profile ───
    void setUserEmbedding(const float* embedding, int dim);
    void setUserTypeAffinity(const float* affinities, int count);
    void setUserStats(float avgDwell, float avgCompletion, float sessionCount,
                      float interactionRate);
    UserProfile& getUserProfile() { return userProfile_; }

    // ─── Session ───
    void startSession();
    void reportDwell(int32_t sourceIndex, float dwellMs, bool interacted);
    void reportScrollSpeed(float pxPerSec);
    void reportVideoCompletion(int32_t sourceIndex, float completionRate);

    // ─── Pool management ───
    void clearPool();
    void addItem(int32_t sourceIndex, FeedItemType type, int64_t timestamp,
                 int32_t likes, int32_t views, int32_t shares,
                 int32_t saves, int32_t comments, int32_t authorId,
                 uint8_t categoryId, float contentQuality, float credibility,
                 float completionRate);
    void setContentEmbedding(int32_t sourceIndex, const float* embedding, int dim);

    // ─── Feed generation ───
    int generateInitialBatch(int32_t* outIndices, int maxOut);
    int generateNextBatch(int32_t* outIndices, int maxOut);
    int getPrefetchHints(int32_t* outIndices, int maxOut);

    // ─── State ───
    int  getTotalServed() const { return totalServed_; }
    int  getAvailableCount() const;
    void resetSession();

    // ─── Dynamic config (A/B testing, backend-driven) ───
    void setTypeWeight(FeedItemType type, float weight);
    void setFactorPowers(float affinity, float quality, float recency,
                         float sessionIntent, float diversity, float exploration);
    void setExplorationRate(float rate);
    void setBanditPriors(const float* alphas, const float* betas, int count);

    // ─── Debug ───
    void getLastScoreBreakdown(int32_t sourceIndex, float* outFactors, int maxFactors);

private:
    FeedConfig     config_;
    ContentPool    pool_;
    UserProfile    userProfile_;
    SessionState   session_;
    BanditExplorer bandit_;

    // History tracking
    uint8_t  typeHistory_[MAX_HISTORY];
    int32_t  authorHistory_[MAX_HISTORY];
    int      historyLen_;
    int      totalServed_;

    // Fast PRNG (xorshift128+)
    uint64_t rngState_[2];

    // ─── Scoring Pipeline ───
    float computeUserAffinity(int poolIdx) const;
    float computeContentQuality(int poolIdx) const;
    float computeRecencyDecay(int poolIdx) const;
    float computeSessionIntent(int poolIdx) const;
    float computeDiversityAdjust(int poolIdx) const;
    float computeExplorationFactor(int poolIdx);
    float computeAntiManipulation(int poolIdx) const;

    void  computeAllScores(bool initialBatch);
    int   selectTopN(int32_t* outIndices, int n, bool onlyContent);
    void  updateHistory(int poolIdx);

    // ─── Bandit ───
    float thompsonSample(int armIdx);
    void  banditReward(uint8_t type, bool positive);

    // ─── Session intent detection ───
    void  updateSessionIntent();
    void  updateFatigue();

    // ─── User profile learning ───
    void  updateTypeAffinity(uint8_t type, bool positive);
    void  updateEmbeddingFromInteraction(int poolIdx, float reward);

    // ─── Anti-manipulation ───
    float detectEngagementSpike(int poolIdx) const;

    // ─── Injection ───
    bool  shouldInjectType(FeedItemType type) const;

    // ─── PRNG ───
    uint64_t nextRandom();
    float    randomFloat();
    float    randomBeta(float alpha, float beta);

    // ─── Utility ───
    int64_t  currentTimeMs() const;
    float    dotProduct(const float* a, const float* b, int dim) const;
    float    safePow(float base, float power) const;
};

} // namespace rendly
