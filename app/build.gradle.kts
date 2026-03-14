plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.github.triplet.play")
}

fun propertyOrDefault(name: String, defaultValue: String): String =
    providers.gradleProperty(name).orNull?.takeIf { it.isNotBlank() } ?: defaultValue

val debugAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
val debugBannerId = "ca-app-pub-3940256099942544/6300978111"
val debugInterstitialId = "ca-app-pub-3940256099942544/1033173712"

val releaseAdmobAppId = propertyOrDefault("ADMOB_APP_ID", debugAdmobAppId)
val releaseBannerId = propertyOrDefault("ADMOB_BANNER_UNIT_ID", debugBannerId)
val releaseInterstitialId = propertyOrDefault("ADMOB_INTERSTITIAL_UNIT_ID", debugInterstitialId)
val privacyPolicyUrl = propertyOrDefault("PRIVACY_POLICY_URL", "https://example.com/privacy-policy")
val playServiceAccountJson = providers.gradleProperty("PLAY_SERVICE_ACCOUNT_JSON").orNull
val playTrack = propertyOrDefault("PLAY_TRACK", "production")

android {
    namespace = "com.hardik.access"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hardik.access"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["admobAppId"] = releaseAdmobAppId
            resValue("string", "admob_banner_unit_id", releaseBannerId)
            resValue("string", "admob_interstitial_unit_id", releaseInterstitialId)
            buildConfigField("String", "PRIVACY_POLICY_URL", "\"$privacyPolicyUrl\"")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["admobAppId"] = debugAdmobAppId
            resValue("string", "admob_banner_unit_id", debugBannerId)
            resValue("string", "admob_interstitial_unit_id", debugInterstitialId)
            buildConfigField("String", "PRIVACY_POLICY_URL", "\"$privacyPolicyUrl\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

play {
    defaultToAppBundles.set(true)
    track.set(playTrack)
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.DRAFT)
    if (!playServiceAccountJson.isNullOrBlank()) {
        serviceAccountCredentials.set(file(playServiceAccountJson))
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation(platform("androidx.compose:compose-bom:2024.09.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.android.gms:play-services-ads:23.4.0")
    implementation("com.google.android.ump:user-messaging-platform:2.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
