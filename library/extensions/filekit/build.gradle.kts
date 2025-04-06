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

            // FileKit
            api(libs.filekit.core)

            // Compose
            implementation(compose.ui)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }

        val webMain by creating {
            dependsOn(commonMain.get())
        }
        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }

        iosMain.get().dependsOn(nonAndroidMain)
        desktopMain.get().dependsOn(nonAndroidMain)
        jsMain.get().dependsOn(webMain)
        jsMain.get().dependsOn(nonAndroidMain)
        wasmJsMain.get().dependsOn(webMain)
        wasmJsMain.get().dependsOn(nonAndroidMain)
    }
}
