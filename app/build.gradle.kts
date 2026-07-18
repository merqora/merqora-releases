plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("androidx.baselineprofile")
    id("com.google.gms.google-services")
    id("io.sentry.android.gradle")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.vinzay.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vinzay.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // NDK - C++ Media Optimizer Engine
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17 -O3 -ffast-math"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        create("release") {
            // Usar keystore de debug para testing de performance
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            
            // ═════════════════════════════════════════════════════════════
            // COLD START OPTIMIZATION: R8 Full Mode + Aggressive optimization
            // ═════════════════════════════════════════════════════════════
            
            // Baseline Profile optimization (run :app:generateBaselineProfile manually)
            // Disabled auto-gen to speed up builds
            // baselineProfile.automaticGenerationDuringBuild = true
            buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL") ?: "https://xyrpmmnegzjkbysoocpc.supabase.co"}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
            buildConfigField("String", "R2_PUBLIC_URL", "\"${project.findProperty("R2_PUBLIC_URL") ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.findProperty("CLOUDINARY_CLOUD_NAME") ?: ""}\"")
            buildConfigField("String", "IMAGEKIT_URL_ENDPOINT", "\"${project.findProperty("IMAGEKIT_URL_ENDPOINT") ?: ""}\"")
            buildConfigField("String", "IMAGEKIT_PUBLIC_KEY", "\"${project.findProperty("IMAGEKIT_PUBLIC_KEY") ?: ""}\"")
            buildConfigField("String", "IMAGEKIT_PRIVATE_KEY", "\"${project.findProperty("IMAGEKIT_PRIVATE_KEY") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_ACCOUNT_ID", "\"${project.findProperty("CLOUDFLARE_ACCOUNT_ID") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_ACCESS_KEY_ID", "\"${project.findProperty("CLOUDFLARE_ACCESS_KEY_ID") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_SECRET_ACCESS_KEY", "\"${project.findProperty("CLOUDFLARE_SECRET_ACCESS_KEY") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_BUCKET_NAME", "\"${project.findProperty("CLOUDFLARE_BUCKET_NAME") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_PUBLIC_DOMAIN", "\"${project.findProperty("CLOUDFLARE_PUBLIC_DOMAIN") ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.findProperty("CLOUDINARY_API_KEY") ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${project.findProperty("CLOUDINARY_API_SECRET") ?: ""}\"")
            buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"${project.findProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\"")
            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""}\"")
            // Mercado Pago Checkout API
            buildConfigField("String", "MP_PUBLIC_KEY", "\"${project.findProperty("MP_PUBLIC_KEY") ?: ""}\"")
            // HIGH-2: MP_ACCESS_TOKEN eliminado — solo se usa en Edge Functions del servidor
            buildConfigField("String", "MERCADOPAGO_CLIENT_ID", "\"${project.findProperty("MERCADOPAGO_CLIENT_ID") ?: ""}\"")
            // AI Support Backend URL - deploy to Railway/Render and set in gradle.properties
            buildConfigField("String", "AI_SUPPORT_URL", "\"${project.findProperty("AI_SUPPORT_URL") ?: "https://vinzay-ai.up.railway.app"}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL") ?: "https://xyrpmmnegzjkbysoocpc.supabase.co"}\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
            buildConfigField("String", "R2_PUBLIC_URL", "\"${project.findProperty("R2_PUBLIC_URL") ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.findProperty("CLOUDINARY_CLOUD_NAME") ?: ""}\"")
            buildConfigField("String", "IMAGEKIT_URL_ENDPOINT", "\"${project.findProperty("IMAGEKIT_URL_ENDPOINT") ?: ""}\"")
            buildConfigField("String", "IMAGEKIT_PUBLIC_KEY", "\"${project.findProperty("IMAGEKIT_PUBLIC_KEY") ?: ""}\"")
            buildConfigField("String", "IMAGEKIT_PRIVATE_KEY", "\"${project.findProperty("IMAGEKIT_PRIVATE_KEY") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_ACCOUNT_ID", "\"${project.findProperty("CLOUDFLARE_ACCOUNT_ID") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_ACCESS_KEY_ID", "\"${project.findProperty("CLOUDFLARE_ACCESS_KEY_ID") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_SECRET_ACCESS_KEY", "\"${project.findProperty("CLOUDFLARE_SECRET_ACCESS_KEY") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_BUCKET_NAME", "\"${project.findProperty("CLOUDFLARE_BUCKET_NAME") ?: ""}\"")
            buildConfigField("String", "CLOUDFLARE_PUBLIC_DOMAIN", "\"${project.findProperty("CLOUDFLARE_PUBLIC_DOMAIN") ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.findProperty("CLOUDINARY_API_KEY") ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${project.findProperty("CLOUDINARY_API_SECRET") ?: ""}\"")
            buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"${project.findProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\"")
            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""}\"")
            // Mercado Pago Checkout API - DEBUG usa credenciales TEST si están configuradas
            // Para obtener credenciales TEST: https://www.mercadopago.com.uy/developers/panel/app -> Credenciales de prueba
            // Agregar MP_TEST_PUBLIC_KEY y MP_TEST_ACCESS_TOKEN en gradle.properties
            buildConfigField("String", "MP_PUBLIC_KEY", "\"${project.findProperty("MP_TEST_PUBLIC_KEY") ?: project.findProperty("MP_PUBLIC_KEY") ?: ""}\"")
            // HIGH-2: MP_ACCESS_TOKEN eliminado
            buildConfigField("String", "MERCADOPAGO_CLIENT_ID", "\"${project.findProperty("MERCADOPAGO_CLIENT_ID") ?: ""}\"")
            // AI Support Backend URL - for debug, use local IP or deployed URL
            buildConfigField("String", "AI_SUPPORT_URL", "\"${project.findProperty("AI_SUPPORT_URL") ?: "https://vinzay-ai.up.railway.app"}\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}



composeCompiler {
    // Stability config: marca colecciones/fechas como estables para que
    // los items del feed sean skippable durante el scroll
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_compiler_config.conf"))
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.06.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose Core
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Compose Integration
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-process:2.9.1") // Para detectar app en primer plano

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.56.2")
    kapt("com.google.dagger:hilt-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Image Loading - Coil (Ultra optimizado)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    // Room Database
    val roomVersion = "2.7.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    
    // Networking
    val ktorVersion = "2.3.7"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    // Ktor OkHttp engine con soporte de WebSockets (requerido para Supabase Realtime)
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:2.0.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:functions-kt") // Edge Functions para Mercado Pago
    
    // Chrome Custom Tabs (Mercado Pago Checkout fallback)
    implementation("androidx.browser:browser:1.7.0")
    
    // WebView para Mercado Pago Checkout embebido (mejor que SDK externo)
    // El checkout se carga en una WebView dentro de la app - sin abrir navegador

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    
    // DataStore (mejor que SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Paging 3 (para scroll infinito optimizado)
    // DESHABILITADO: no se usa actualmente, se maneja con LazyColumn manual
    // implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    // implementation("androidx.paging:paging-compose:3.2.1")
    
    // WorkManager (for background cache sync)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Splash Screen (removido intencionalmente - se usa XML splash nativo)
    // implementation("androidx.core:core-splashscreen:1.0.1")
    
    // App Startup Library - optimizes ContentProvider initialization
    implementation("androidx.startup:startup-runtime:1.1.1")
    
    // Profile Installer - enables Baseline Profile on older devices
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    
    // Baseline Profile
    baselineProfile(project(":benchmark"))
    
    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    
    // QR Code Generation
    implementation("com.google.zxing:core:3.5.2")
    
    // Media3 ExoPlayer for video playback
    val media3Version = "1.6.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Google Play Services Location (GPS for AddressEngine)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    
    // LiveKit para streaming en vivo (reemplaza WebRTC directo)
    implementation("io.livekit:livekit-android:1.4.0")
    
    // Sentry (Error Tracking)
    implementation("io.sentry:sentry-android:7.20.0")
    
    // Firebase (Push Notifications)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation("org.robolectric:robolectric:4.14.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

kapt {
    correctErrorTypes = true
}
