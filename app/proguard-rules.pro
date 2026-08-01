# ═══════════════════════════════════════════════════════════════════════════════
# Mercora ProGuard/R8 Rules - Cold Start Optimized
# Target: < 1000ms cold start
# ═══════════════════════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────────────────────────
# R8 AGGRESSIVE OPTIMIZATIONS
# ─────────────────────────────────────────────────────────────────────────────────
-optimizationpasses 2
-dontusemixedcaseclassnames
-allowaccessmodification
-repackageclasses ''
-flattenpackagehierarchy

# Aggressive inlining for startup performance
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static java.lang.String getStackTraceString(java.lang.Throwable);
}

# ─────────────────────────────────────────────────────────────────────────────────
# STARTUP CRITICAL - Keep startup path classes
# ─────────────────────────────────────────────────────────────────────────────────
-keep class com.mercora.app.MainActivity { *; }
-keep class com.mercora.app.MercoraApplication { *; }
-keep class com.mercora.app.startup.** { *; }

# ─────────────────────────────────────────────────────────────────────────────────
# NATIVE METHODS
# ─────────────────────────────────────────────────────────────────────────────────
-keepclasseswithmembernames class * {
    native <methods>;
}

# ─────────────────────────────────────────────────────────────────────────────────
# HILT / DAGGER
# ─────────────────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <methods>;
}

# ─────────────────────────────────────────────────────────────────────────────────
# COMPOSE
# ─────────────────────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─────────────────────────────────────────────────────────────────────────────────
# COIL
# ─────────────────────────────────────────────────────────────────────────────────
-dontwarn coil.**
-keep class coil.** { *; }

# ─────────────────────────────────────────────────────────────────────────────────
# KOTLIN SERIALIZATION
# ─────────────────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.mercora.app.**$$serializer { *; }
-keepclassmembers class com.mercora.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.mercora.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─────────────────────────────────────────────────────────────────────────────────
# KTOR
# ─────────────────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

# ─────────────────────────────────────────────────────────────────────────────────
# SUPABASE
# ─────────────────────────────────────────────────────────────────────────────────
-keep class io.github.jan.supabase.** { *; }
-keepclassmembers class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ─────────────────────────────────────────────────────────────────────────────────
# LIVEKIT
# ─────────────────────────────────────────────────────────────────────────────────
-keep class io.livekit.** { *; }
-keepclassmembers class io.livekit.** { *; }
-dontwarn io.livekit.**

# ─────────────────────────────────────────────────────────────────────────────────
# ROOM
# ─────────────────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ─────────────────────────────────────────────────────────────────────────────────
# OKHTTP / RETROFIT
# ─────────────────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ─────────────────────────────────────────────────────────────────────────────────
# MISSING CLASSES SUPPRESSIONS
# ─────────────────────────────────────────────────────────────────────────────────
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**

# ─────────────────────────────────────────────────────────────────────────────────
# BASELINE PROFILE SUPPORT
# ─────────────────────────────────────────────────────────────────────────────────
-keep class androidx.profileinstaller.** { *; }
