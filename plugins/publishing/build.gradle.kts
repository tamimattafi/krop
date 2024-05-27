plugins {
    alias(libs.plugins.kotlin.jvm)
    id(libs.plugins.java.gradle.plugin.get().pluginId)
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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