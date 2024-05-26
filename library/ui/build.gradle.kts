plugins {
    alias(libs.plugins.compose)
    id(libs.plugins.multiplatform.get().pluginId)
    id(libs.plugins.publish.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Library
            implementation(project(":library:core"))

            // Compose
            implementation(compose.runtime)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }

        androidMain.dependencies {
            // Android
            implementation(libs.android.core.ktx)
            implementation(libs.android.lifecycle.runtime.ktx)
            implementation(libs.android.activity.compose)
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.espresso)
    androidTestImplementation(libs.kotlin.coroutines.test)
    androidTestImplementation(libs.android.test.ext.junit)
}
