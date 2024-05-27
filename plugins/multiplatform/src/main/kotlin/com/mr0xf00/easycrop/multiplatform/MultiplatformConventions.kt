package com.mr0xf00.easycrop.multiplatform

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
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
        compilations.all {
          it.kotlinOptions {
            jvmTarget = "17"
          }
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
      @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
      wasmJs {
        binaries.executable()
      }
    }

    val javaExtension = project.extensions.getByName("java") as JavaPluginExtension
    javaExtension.apply {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }

    val androidExtension = project.extensions.getByName("android") as LibraryExtension
    androidExtension.apply {
      namespace = "com.mr0xf00.easycrop.${project.name}"
      compileSdk = 34

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
