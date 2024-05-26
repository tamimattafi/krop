plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.mr0xf00.easycrop"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = libs.versions.kotlin.jvm.target.get()
    }
}

dependencies {
    // Compose
    implementation(compose.runtime)
    implementation(compose.material)
    implementation(compose.ui)

    // Android
    implementation(libs.android.core.ktx)
    implementation(libs.android.lifecycle.runtime.ktx)
    implementation(libs.android.activity.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.espresso)
    androidTestImplementation(libs.kotlin.coroutines.test)
    androidTestImplementation(libs.android.test.ext.junit)
}
