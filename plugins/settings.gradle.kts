pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }

    versionCatalogs.create("libs") {
        from(files("../gradle/libs.versions.toml"))
    }
}

rootProject.name = "plugins"
include(":publishing")
include(":multiplatform")
