plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
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

        androidMain.dependencies {
            implementation(libs.android.exif)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.metadata.extractor)
            }
        }
    }
}

android {
    buildFeatures {
        compose = true
    }
}
