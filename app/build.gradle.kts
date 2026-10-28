plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.swipeclean.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.swipeclean.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signed with the debug key so the release APK installs for testing.
            // Replace with your own keystore before publishing to Google Play.
            signingConfig = signingConfigs.getByName("debug")
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
    }
}

// Every APK build is copied into <project>/release with a readable, versioned name,
// so that folder always holds the latest installable APKs.
val appVersion = android.defaultConfig.versionName
val copyApksToRelease by tasks.registering(Copy::class) {
    description = "Copies built APKs into the project's release/ folder."
    mustRunAfter("packageDebug", "packageRelease")
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("*.apk")
        rename { "SwipeClean-$appVersion.apk" }
    }
    from(layout.buildDirectory.dir("outputs/apk/debug")) {
        include("*.apk")
        rename { "SwipeClean-$appVersion-debug.apk" }
    }
    into(rootProject.layout.projectDirectory.dir("release"))
}
tasks.configureEach {
    if (name == "assembleRelease" || name == "assembleDebug") finalizedBy(copyApksToRelease)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
