import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pawranger"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pawranger"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load MAPS_API_KEY from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    installation {
        timeOutInMs = 300000
    }
}

dependencies {
    // Material Design 3
    implementation(libs.material)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Google Maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.browser)

    // Import Firebase BoM (Bill of Materials) agar versinya selalu sinkron
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // Import Firebase Cloud Messaging untuk urusan notifikasi
    implementation("com.google.firebase:firebase-messaging")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
