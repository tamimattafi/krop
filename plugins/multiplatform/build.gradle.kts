plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
}

gradlePlugin {
    plugins.create("multiplatform") {
        id = "com.mr0xf00.easycrop.multiplatform"
        implementationClass = "com.mr0xf00.easycrop.multiplatform.MultiplatformConventions"
    }
}

dependencies {
    compileOnly(libs.kotlin.plugin)
    compileOnly(libs.android.build.tools)
}
