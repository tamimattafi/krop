plugins {
    alias(libs.plugins.compose)
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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

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
