package com.attafitamim.krop.multiplatform

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

class MultiplatformConventions : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply {
      apply("org.jetbrains.kotlin.multiplatform")
      apply("com.android.library")
    }

    val multiplatformExtension = project.kotlinExtension as KotlinMultiplatformExtension
    multiplatformExtension.apply {
      applyDefaultHierarchyTemplate()
      jvmToolchain(17)

      // JS
      jvm("desktop")

      // Android
      androidTarget {
        compilerOptions {
          jvmTarget.set(JvmTarget.JVM_17)
        }
      }

      // iOS
      iosSimulatorArm64()
      iosX64()
      iosArm64()

      // JS
      js {
        browser()
      }

      // Wasm
      @OptIn(ExperimentalWasmDsl::class)
      wasmJs {
        browser()
        binaries.executable()
      }

      // Source sets
      sourceSets.apply {
        val commonMain = commonMain.get()

        // Skiko
        val skikoMain = create("skikoMain")
        skikoMain.dependsOn(commonMain)
        iosMain.get().dependsOn(skikoMain)
        jsMain.get().dependsOn(skikoMain)
        wasmJsMain.get().dependsOn(skikoMain)
        getByName("desktopMain").dependsOn(skikoMain)

        // Mobile
        val mobileMain = create("mobileMain")
        mobileMain.dependsOn(commonMain)
        iosMain.get().dependsOn(mobileMain)
        androidMain.get().dependsOn(mobileMain)
      }
    }

    val javaExtension = project.extensions.getByName("java") as JavaPluginExtension
    javaExtension.apply {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }

    val androidExtension = project.extensions.getByName("android") as LibraryExtension
    androidExtension.apply {
      namespace = "com.attafitamim.krop.${project.name}"
      compileSdk = 35

      defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      }

      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }
    }
  }
}
