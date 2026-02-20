# ═══════════════════════════════════════════════════════════════════════════════
# Rendly ProGuard/R8 Rules - Cold Start Optimized
# Target: < 1000ms cold start
# ═══════════════════════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────────────────────────
# R8 AGGRESSIVE OPTIMIZATIONS
# ─────────────────────────────────────────────────────────────────────────────────
-optimizationpasses 7
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
}

# ─────────────────────────────────────────────────────────────────────────────────
# STARTUP CRITICAL - Keep startup path classes
# ─────────────────────────────────────────────────────────────────────────────────
-keep class com.rendly.app.MainActivity { *; }
-keep class com.rendly.app.RendlyApplication { *; }
-keep class com.rendly.app.startup.** { *; }

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
-keep,includedescriptorclasses class com.rendly.app.**$$serializer { *; }
-keepclassmembers class com.rendly.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.rendly.app.** {
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
