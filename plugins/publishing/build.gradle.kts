plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
}

gradlePlugin {
    plugins.create("publish") {
        id = "com.mr0xf00.easycrop.publish"
        implementationClass = "com.mr0xf00.easycrop.publish.PublishConventions"
    }
}

dependencies {
    compileOnly(libs.kotlin.plugin)
    compileOnly(libs.maven.publish.plugin)
}