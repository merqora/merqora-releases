#include <jni.h>
#include <cmath>
#include <chrono>
#include <mutex>
#include <android/log.h>

#define LOG_TAG "ScrollPhysics"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

// Physics constants tuned for a responsive, natural fling feel.
constexpr float kFrictionPerSecond = 6.0f;          // exponential decay rate (1/s)
constexpr float kSettleVelocity = 8.0f;             // px/s under which we consider scroll settled
constexpr float kFlingThreshold = 120.0f;           // px/s below which a release does not fling
constexpr float kPrefetchVelocityThreshold = 400.0f;// px/s above which prefetch is suggested
constexpr float kMaxDeltaTime = 0.05f;              // seconds, clamp to avoid jump after pauses
constexpr float kMaxVelocity = 12000.0f;            // px/s safety clamp

struct ScrollPhysics {
    std::mutex mtx;
    float position = 0.0f;          // simulated scroll position (px)
    float velocity = 0.0f;          // simulated velocity (px/s, positive = forward/down)
    float viewport = 0.0f;
    float content = 1.0e7f;         // large default: bounds clamped only when content set
    bool dragging = false;
    bool initialized = false;
    std::chrono::steady_clock::time_point lastUpdate;
};

ScrollPhysics g_physics;

float clampf(float v, float lo, float hi) {
    return v < lo ? lo : (v > hi ? hi : v);
}

float maxScroll() {
    return std::max(0.0f, g_physics.content - g_physics.viewport);
}

void ensureClockStarted() {
    if (!g_physics.initialized) {
        g_physics.lastUpdate = std::chrono::steady_clock::now();
        g_physics.initialized = true;
    }
}

float secondsSinceLastUpdate() {
    auto now = std::chrono::steady_clock::now();
    float dt = std::chrono::duration<float>(now - g_physics.lastUpdate).count();
    g_physics.lastUpdate = now;
    return clampf(dt, 0.0f, kMaxDeltaTime);
}

// Integrates one physics step: exponential friction decay + position advance.
// Returns the position delta for this step (px).
float integrateStep(float dt) {
    if (g_physics.dragging || std::fabs(g_physics.velocity) <= kSettleVelocity) {
        g_physics.velocity = 0.0f;
        return 0.0f;
    }
    float prev = g_physics.position;
    g_physics.velocity *= std::exp(-kFrictionPerSecond * dt);
    g_physics.position += g_physics.velocity * dt;
    float maxOff = maxScroll();
    if (g_physics.position < 0.0f) {
        g_physics.position = 0.0f;
        g_physics.velocity = 0.0f;
    } else if (g_physics.position > maxOff) {
        g_physics.position = maxOff;
        g_physics.velocity = 0.0f;
    }
    return g_physics.position - prev;
}

}  // namespace

extern "C" {

JNIEXPORT void JNICALL
Java_com_mercora_app_native_FeedEngine_nativeInit(JNIEnv* env, jobject thiz, jfloat viewportHeight, jfloat contentHeight) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    g_physics.viewport = viewportHeight;
    g_physics.content = contentHeight > 0.0f ? contentHeight : 1.0e7f;
    g_physics.position = 0.0f;
    g_physics.velocity = 0.0f;
    g_physics.dragging = false;
    g_physics.initialized = false;
    ensureClockStarted();
    LOGI("nativeInit viewport=%.0f content=%.0f", viewportHeight, g_physics.content);
}

JNIEXPORT void JNICALL
Java_com_mercora_app_native_FeedEngine_nativeOnDragStart(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    ensureClockStarted();
    g_physics.dragging = true;
    g_physics.velocity = 0.0f;
}

JNIEXPORT void JNICALL
Java_com_mercora_app_native_FeedEngine_nativeOnDrag(JNIEnv* env, jobject thiz, jfloat delta, jfloat velocity) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    ensureClockStarted();
    g_physics.velocity = clampf(velocity, -kMaxVelocity, kMaxVelocity);
    g_physics.position = clampf(g_physics.position + delta, 0.0f, maxScroll());
}

JNIEXPORT void JNICALL
Java_com_mercora_app_native_FeedEngine_nativeOnDragEnd(JNIEnv* env, jobject thiz, jfloat velocity) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    ensureClockStarted();
    g_physics.dragging = false;
    g_physics.velocity = std::fabs(velocity) >= kFlingThreshold
        ? clampf(velocity, -kMaxVelocity, kMaxVelocity)
        : 0.0f;
    g_physics.lastUpdate = std::chrono::steady_clock::now();
}

JNIEXPORT void JNICALL
Java_com_mercora_app_native_FeedEngine_nativeUpdate(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    float dt = secondsSinceLastUpdate();
    integrateStep(dt);
}

JNIEXPORT jfloat JNICALL
Java_com_mercora_app_native_FeedEngine_nativeGetScrollOffset(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    return g_physics.position;
}

JNIEXPORT jfloat JNICALL
Java_com_mercora_app_native_FeedEngine_nativeGetVelocity(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    return g_physics.velocity;
}

JNIEXPORT jboolean JNICALL
Java_com_mercora_app_native_FeedEngine_nativeIsSettled(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    return !g_physics.dragging && std::fabs(g_physics.velocity) <= kSettleVelocity;
}

JNIEXPORT jboolean JNICALL
Java_com_mercora_app_native_FeedEngine_nativeShouldPrefetch(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    return !g_physics.dragging &&
           std::fabs(g_physics.velocity) >= kPrefetchVelocityThreshold;
}

JNIEXPORT jint JNICALL
Java_com_mercora_app_native_FeedEngine_nativeGetPrefetchCount(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    float speed = std::fabs(g_physics.velocity);
    if (speed < kPrefetchVelocityThreshold) return 0;
    int count = static_cast<int>(2.0f + speed / 1200.0f);
    return count > 8 ? 8 : count;
}

JNIEXPORT jint JNICALL
Java_com_mercora_app_native_FeedEngine_nativeGetPrefetchDirection(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    float speed = std::fabs(g_physics.velocity);
    if (speed < kPrefetchVelocityThreshold) return 0;
    return g_physics.velocity > 0.0f ? 1 : -1;
}

JNIEXPORT void JNICALL
Java_com_mercora_app_native_FeedEngine_nativeSetContentHeight(JNIEnv* env, jobject thiz, jfloat height) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    g_physics.content = height > 0.0f ? height : 1.0e7f;
    g_physics.position = clampf(g_physics.position, 0.0f, maxScroll());
}

JNIEXPORT void JNICALL
Java_com_mercora_app_native_FeedEngine_nativeSetScrollOffset(JNIEnv* env, jobject thiz, jfloat offset) {
    std::lock_guard<std::mutex> lock(g_physics.mtx);
    g_physics.position = clampf(offset, 0.0f, maxScroll());
}

}  // extern "C"
