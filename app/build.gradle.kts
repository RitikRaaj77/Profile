plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.profile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.profile"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation for Compose
    implementation (libs.androidx.navigation.compose)
    // Coroutines core support
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

// Coroutines support for Android (Main Dispatcher, etc.)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    dependencies {
        // Firebase Realtime Database
        implementation ("com.google.firebase:firebase-database-ktx:21.0.0")
        // Firebase Storage
        implementation ("com.google.firebase:firebase-storage-ktx:21.0.0")
        // Coil for image loading in Compose
        implementation ("io.coil-kt:coil-compose:2.7.0")
        // Activity result API for image picker
        implementation ("androidx.activity:activity-compose:1.9.2")

        implementation ("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

        implementation("androidx.core:core-splashscreen:1.0.0")
    }

}