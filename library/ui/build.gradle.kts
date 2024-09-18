plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id(libs.plugins.multiplatform.get().pluginId)
    id(libs.plugins.publish.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Library
            api(projects.library.core)

            // Compose
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(compose.components.resources)
        }
    }
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Testing
    desktopTestImplementation(libs.junit)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.kotlin.coroutines.test)
}
