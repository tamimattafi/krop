package com.mr0xf00.easycrop.publish

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.api.publish.maven.MavenPomScm

class PublishConventions : Plugin<Project> {

  private val version = "0.1.1"
  private val group = "io.github.mr0xf00"

  override fun apply(project: Project) {
    project.plugins.apply("com.vanniktech.maven.publish")

    val mavenPublishing = project.extensions.getByName("mavenPublishing")
            as MavenPublishBaseExtension

    val artifact = project.name
    mavenPublishing.apply {
      coordinates(group, artifact, version)
      pom(MavenPom::configure)
      publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
      signAllPublications()
    }
  }
}

private fun MavenPom.configure() {
  name.set("EasyCrop")
  description.set("Image cropper for jetpack compose")
  url.set("https://github.com/mr0xf00/easycrop")

  licenses { licenseSpec ->
    licenseSpec.license(MavenPomLicense::configure)
  }

  developers { developerSpec ->
    developerSpec.developer(MavenPomDeveloper::configure)
  }

  scm(MavenPomScm::configure)
}

private fun MavenPomLicense.configure() {
  name.set("Apache License 2.0")
  url.set("https://github.com/mr0xf00/easycrop/blob/main/LICENSE.md")
}

private fun MavenPomDeveloper.configure() {
  name.set("mr0xf00")
  email.set("mr0xf00@proton.me")
}

private fun MavenPomScm.configure() {
  connection.set("scm:git:github.com/mr0xf00/easycrop.git")
  developerConnection.set("scm:git:ssh://github.com/mr0xf00/easycrop.git")
  url.set("https://github.com/mr0xf00/easycrop.git")
}
