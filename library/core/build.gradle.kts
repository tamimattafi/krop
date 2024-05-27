plugins {
    alias(libs.plugins.compose)
    id(libs.plugins.multiplatform.get().pluginId)
    id(libs.plugins.publish.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.animation)
            implementation(compose.foundation)
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
