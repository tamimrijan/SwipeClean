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
    buildFeatures {
        compose = true
    }
    lint {
        // Lifecycle's LiveData lint check crashes on Kotlin 2.2 sources with this AGP; the app uses no LiveData.
        disable += "NullSafeMutableLiveData"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        // Material 3 Expressive components are still marked experimental.
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
        )
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
    // Material 3 Expressive ships in the material3 1.5 line.
    val compose = "1.11.4"
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.foundation:foundation:$compose")
    implementation("androidx.compose.animation:animation:$compose")
    implementation("androidx.compose.material3:material3:1.5.0-alpha18")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.graphics:graphics-shapes:1.0.1")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling:$compose")
}
