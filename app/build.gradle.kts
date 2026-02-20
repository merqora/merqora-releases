plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("androidx.baselineprofile")
    id("com.google.gms.google-services")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.rendly.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rendly.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            buildConfigField("String", "SUPABASE_URL", "\"https://xyrpmmnegzjkbysoocpc.supabase.co\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5cnBtbW5lZ3pqa2J5c29vY3BjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjczOTA1NDcsImV4cCI6MjA4Mjk2NjU0N30.RsQE2JCJbHoNzlqG95Hf4W0QNyx5Xzw5bwvYcfpWKI0\"")
            buildConfigField("String", "R2_PUBLIC_URL", "\"https://pub-40412c53d63945a3a1e5a19e946f5fba.r2.dev\"")
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"dz0clge3s\"")
            buildConfigField("String", "IMAGEKIT_URL_ENDPOINT", "\"https://ik.imagekit.io/4z6ezuoeb\"")
            buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"${project.findProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\"")
            // Mercado Pago Checkout API
            buildConfigField("String", "MP_PUBLIC_KEY", "\"${project.findProperty("MP_PUBLIC_KEY") ?: "APP_USR-f317b894-f344-4a2d-a430-5879dbd9cef2"}\"")
            buildConfigField("String", "MP_ACCESS_TOKEN", "\"${project.findProperty("MP_ACCESS_TOKEN") ?: "APP_USR-5371485033290040-020723-f545050d5138976f10770ec01b67ad38-1622183330"}\"")
            // AI Support Backend URL - deploy to Railway/Render and set in gradle.properties
            buildConfigField("String", "AI_SUPPORT_URL", "\"${project.findProperty("AI_SUPPORT_URL") ?: "https://rendly-ai.up.railway.app"}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "SUPABASE_URL", "\"https://xyrpmmnegzjkbysoocpc.supabase.co\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5cnBtbW5lZ3pqa2J5c29vY3BjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjczOTA1NDcsImV4cCI6MjA4Mjk2NjU0N30.RsQE2JCJbHoNzlqG95Hf4W0QNyx5Xzw5bwvYcfpWKI0\"")
            buildConfigField("String", "R2_PUBLIC_URL", "\"https://pub-40412c53d63945a3a1e5a19e946f5fba.r2.dev\"")
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"dz0clge3s\"")
            buildConfigField("String", "IMAGEKIT_URL_ENDPOINT", "\"https://ik.imagekit.io/4z6ezuoeb\"")
            buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"${project.findProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\"")
            // Mercado Pago Checkout API - DEBUG usa credenciales TEST para tarjetas de prueba
            // Para obtener credenciales TEST: https://www.mercadopago.com.uy/developers/panel/app -> Credenciales de prueba
            // Agregar MP_TEST_PUBLIC_KEY y MP_TEST_ACCESS_TOKEN en gradle.properties
            buildConfigField("String", "MP_PUBLIC_KEY", "\"${project.findProperty("MP_TEST_PUBLIC_KEY") ?: project.findProperty("MP_PUBLIC_KEY") ?: "TEST-f317b894-f344-4a2d-a430-5879dbd9cef2"}\"")
            buildConfigField("String", "MP_ACCESS_TOKEN", "\"${project.findProperty("MP_TEST_ACCESS_TOKEN") ?: project.findProperty("MP_ACCESS_TOKEN") ?: "TEST-5371485033290040-020723-f545050d5138976f10770ec01b67ad38-1622183330"}\"")
            // AI Support Backend URL - for debug, use local IP or deployed URL
            buildConfigField("String", "AI_SUPPORT_URL", "\"${project.findProperty("AI_SUPPORT_URL") ?: "https://rendly-ai.up.railway.app"}\"")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
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
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0") // Para detectar app en primer plano
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Image Loading - Coil (Ultra optimizado)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
    
    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    val ktorVersion = "2.3.7"
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:2.3.5")
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // DataStore (mejor que SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Paging 3 (para scroll infinito optimizado)
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
    
    // WorkManager (for background cache sync)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // App Startup Library - optimizes ContentProvider initialization
    implementation("androidx.startup:startup-runtime:1.1.1")
    
    // Profile Installer - enables Baseline Profile on older devices
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    
    // Baseline Profile
    baselineProfile(project(":benchmark"))
    
    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    
    // QR Code Generation & Scanning
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // Media3 ExoPlayer for video playback
    val media3Version = "1.2.0"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    
    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Google Play Services Location (GPS for AddressEngine)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // WebRTC para streaming en vivo
    implementation("io.getstream:stream-webrtc-android:1.1.1")
    
    // Firebase (Push Notifications)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

kapt {
    correctErrorTypes = true
}
