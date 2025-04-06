import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.gaganyatris.gaganyatri"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gaganyatris.gaganyatri"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Enable code shrinking
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Securely fetch API keys from local.properties or environment variables.
            // DO NOT hardcode API keys in your release build.
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(localPropertiesFile.inputStream())
            }
            val apiKey = localProperties.getProperty("API_KEY", "")
            val geminiKey = localProperties.getProperty("GEMINI_API_KEY", "")

            if (apiKey.isNotEmpty() && geminiKey.isNotEmpty()) {
                buildConfigField("String", "API_KEY", "\"$apiKey\"")
                buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
            } else {
                // Handle the case where API keys are missing.
                // Consider throwing an error or using default values for testing.
                println("WARNING: API keys are missing in local.properties for release build!")
                buildConfigField("String", "API_KEY", "\"YOUR_API_KEY_HERE\"") //default value or error handling
                buildConfigField("String","GEMINI_API_KEY", "\"YOUR_GEMINI_API_KEY_HERE\"") //default value or error handling
            }
        }

        debug {
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(localPropertiesFile.inputStream())
            }
            val apiKey = localProperties.getProperty("API_KEY", "default_value")
            val geminiKey = localProperties.getProperty("GEMINI_API_KEY", "default_value")

            buildConfigField("String", "API_KEY", "\"$apiKey\"")
            buildConfigField("String","GEMINI_API_KEY", "\"$geminiKey\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.core.splashscreen)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.hbb20:ccp:2.6.0")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.libraries.places:places:4.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // Use the same version as Glide
    implementation("com.google.ai.client.generativeai:generativeai:0.4.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
}