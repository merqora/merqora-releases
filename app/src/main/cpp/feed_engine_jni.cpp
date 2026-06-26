/**
 * Rendly Feed Engine v2 - JNI Bridge
 * 
 * Minimal JNI overhead: primitive arrays across boundary.
 * Engine singleton in native heap (zero GC pressure).
 * New v2: session tracking, user profile, bandit config, embeddings.
 */

#include <jni.h>
#include <android/log.h>
#include "feed_engine.h"

#define JNI_TAG "FeedEngineJNI"
#define JNI_LOG(...) __android_log_print(ANDROID_LOG_INFO, JNI_TAG, __VA_ARGS__)

static rendly::FeedEngine* g_engine = nullptr;

static rendly::FeedEngine* getEngine() {
    if (!g_engine) g_engine = new rendly::FeedEngine();
    return g_engine;
}

extern "C" {

// ═══════════════════════════════════════════════════════════════
// LIFECYCLE
// ═══════════════════════════════════════════════════════════════

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeInit(JNIEnv*, jobject) {
    getEngine();
    JNI_LOG("Feed engine v2 initialized");
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeResetSession(JNIEnv*, jobject) {
    getEngine()->resetSession();
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeDestroy(JNIEnv*, jobject) {
    if (g_engine) { delete g_engine; g_engine = nullptr; }
}

// ═══════════════════════════════════════════════════════════════
// POOL MANAGEMENT
// ═══════════════════════════════════════════════════════════════

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeClearPool(JNIEnv*, jobject) {
    getEngine()->clearPool();
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeAddItems(
    JNIEnv* env, jobject,
    jintArray jIndices, jintArray jTypes, jlongArray jTimestamps,
    jintArray jLikes, jintArray jViews, jintArray jShares,
    jintArray jSaves, jintArray jComments, jintArray jAuthorIds,
    jintArray jCategoryIds, jfloatArray jQuality, jfloatArray jCredibility,
    jfloatArray jCompletionRate, jint count
) {
    auto* engine = getEngine();

    jint*  indices     = env->GetIntArrayElements(jIndices, nullptr);
    jint*  types       = env->GetIntArrayElements(jTypes, nullptr);
    jlong* timestamps  = env->GetLongArrayElements(jTimestamps, nullptr);
    jint*  likes       = env->GetIntArrayElements(jLikes, nullptr);
    jint*  views       = env->GetIntArrayElements(jViews, nullptr);
    jint*  shares      = env->GetIntArrayElements(jShares, nullptr);
    jint*  saves       = env->GetIntArrayElements(jSaves, nullptr);
    jint*  comments    = env->GetIntArrayElements(jComments, nullptr);
    jint*  authorIds   = env->GetIntArrayElements(jAuthorIds, nullptr);
    jint*  categoryIds = env->GetIntArrayElements(jCategoryIds, nullptr);
    jfloat* quality    = env->GetFloatArrayElements(jQuality, nullptr);
    jfloat* credibility= env->GetFloatArrayElements(jCredibility, nullptr);
    jfloat* completion = env->GetFloatArrayElements(jCompletionRate, nullptr);

    for (int i = 0; i < count; i++) {
        engine->addItem(
            indices[i],
            static_cast<rendly::FeedItemType>(types[i]),
            static_cast<int64_t>(timestamps[i]),
            likes[i], views[i], shares[i],
            saves[i], comments[i], authorIds[i],
            static_cast<uint8_t>(categoryIds[i]),
            quality[i], credibility[i], completion[i]
        );
    }

    env->ReleaseIntArrayElements(jIndices, indices, JNI_ABORT);
    env->ReleaseIntArrayElements(jTypes, types, JNI_ABORT);
    env->ReleaseLongArrayElements(jTimestamps, timestamps, JNI_ABORT);
    env->ReleaseIntArrayElements(jLikes, likes, JNI_ABORT);
    env->ReleaseIntArrayElements(jViews, views, JNI_ABORT);
    env->ReleaseIntArrayElements(jShares, shares, JNI_ABORT);
    env->ReleaseIntArrayElements(jSaves, saves, JNI_ABORT);
    env->ReleaseIntArrayElements(jComments, comments, JNI_ABORT);
    env->ReleaseIntArrayElements(jAuthorIds, authorIds, JNI_ABORT);
    env->ReleaseIntArrayElements(jCategoryIds, categoryIds, JNI_ABORT);
    env->ReleaseFloatArrayElements(jQuality, quality, JNI_ABORT);
    env->ReleaseFloatArrayElements(jCredibility, credibility, JNI_ABORT);
    env->ReleaseFloatArrayElements(jCompletionRate, completion, JNI_ABORT);

    JNI_LOG("Added %d items to pool (v2)", count);
}

// ═══════════════════════════════════════════════════════════════
// FEED GENERATION
// ═══════════════════════════════════════════════════════════════

JNIEXPORT jintArray JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeGenerateInitialBatch(JNIEnv* env, jobject) {
    int32_t out[16];
    int count = getEngine()->generateInitialBatch(out, 16);
    jintArray result = env->NewIntArray(count);
    if (count > 0) env->SetIntArrayRegion(result, 0, count, out);
    return result;
}

JNIEXPORT jintArray JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeGenerateNextBatch(JNIEnv* env, jobject) {
    int32_t out[16];
    int count = getEngine()->generateNextBatch(out, 16);
    jintArray result = env->NewIntArray(count);
    if (count > 0) env->SetIntArrayRegion(result, 0, count, out);
    return result;
}

JNIEXPORT jintArray JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeGetPrefetchHints(JNIEnv* env, jobject) {
    int32_t out[16];
    int count = getEngine()->getPrefetchHints(out, 16);
    jintArray result = env->NewIntArray(count);
    if (count > 0) env->SetIntArrayRegion(result, 0, count, out);
    return result;
}

// ═══════════════════════════════════════════════════════════════
// USER PROFILE
// ═══════════════════════════════════════════════════════════════

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeSetUserEmbedding(
    JNIEnv* env, jobject, jfloatArray jEmbedding
) {
    int dim = env->GetArrayLength(jEmbedding);
    jfloat* data = env->GetFloatArrayElements(jEmbedding, nullptr);
    getEngine()->setUserEmbedding(data, dim);
    env->ReleaseFloatArrayElements(jEmbedding, data, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeSetUserTypeAffinity(
    JNIEnv* env, jobject, jfloatArray jAffinities
) {
    int count = env->GetArrayLength(jAffinities);
    jfloat* data = env->GetFloatArrayElements(jAffinities, nullptr);
    getEngine()->setUserTypeAffinity(data, count);
    env->ReleaseFloatArrayElements(jAffinities, data, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeSetUserStats(
    JNIEnv*, jobject, jfloat avgDwell, jfloat avgCompletion,
    jfloat sessionCount, jfloat interactionRate
) {
    getEngine()->setUserStats(avgDwell, avgCompletion, sessionCount, interactionRate);
}

// ═══════════════════════════════════════════════════════════════
// SESSION TRACKING
// ═══════════════════════════════════════════════════════════════

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeStartSession(JNIEnv*, jobject) {
    getEngine()->startSession();
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeReportDwell(
    JNIEnv*, jobject, jint sourceIndex, jfloat dwellMs, jboolean interacted
) {
    getEngine()->reportDwell(sourceIndex, dwellMs, interacted);
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeReportScrollSpeed(
    JNIEnv*, jobject, jfloat pxPerSec
) {
    getEngine()->reportScrollSpeed(pxPerSec);
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeReportVideoCompletion(
    JNIEnv*, jobject, jint sourceIndex, jfloat completionRate
) {
    getEngine()->reportVideoCompletion(sourceIndex, completionRate);
}

// ═══════════════════════════════════════════════════════════════
// CONFIG
// ═══════════════════════════════════════════════════════════════

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeSetTypeWeight(
    JNIEnv*, jobject, jint type, jfloat weight
) {
    getEngine()->setTypeWeight(static_cast<rendly::FeedItemType>(type), weight);
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeSetFactorPowers(
    JNIEnv*, jobject,
    jfloat affinity, jfloat quality, jfloat recency,
    jfloat sessionIntent, jfloat diversity, jfloat exploration
) {
    getEngine()->setFactorPowers(affinity, quality, recency,
                                  sessionIntent, diversity, exploration);
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeSetIntervals(
    JNIEnv*, jobject, jint suggestedInterval, jint specialInterval
) {
    auto& config = getEngine()->getConfig();
    config.suggestedInterval = suggestedInterval;
    config.specialInterval = specialInterval;
}

JNIEXPORT void JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeSetExplorationRate(
    JNIEnv*, jobject, jfloat rate
) {
    getEngine()->setExplorationRate(rate);
}

// ═══════════════════════════════════════════════════════════════
// STATE
// ═══════════════════════════════════════════════════════════════

JNIEXPORT jint JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeGetTotalServed(JNIEnv*, jobject) {
    return getEngine()->getTotalServed();
}

JNIEXPORT jint JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeGetAvailableCount(JNIEnv*, jobject) {
    return getEngine()->getAvailableCount();
}

// ═══════════════════════════════════════════════════════════════
// DEBUG
// ═══════════════════════════════════════════════════════════════

JNIEXPORT jfloatArray JNICALL
Java_com_rendly_app_feed_NativeFeedEngine_nativeGetScoreBreakdown(
    JNIEnv* env, jobject, jint sourceIndex
) {
    float factors[7] = {};
    getEngine()->getLastScoreBreakdown(sourceIndex, factors, 7);
    jfloatArray result = env->NewFloatArray(7);
    env->SetFloatArrayRegion(result, 0, 7, factors);
    return result;
}

} // extern "C"
