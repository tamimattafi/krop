package com.attafitamim.krop.publish

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.api.publish.maven.MavenPomScm

private const val ROOT_MODULE_NAME = "library"

class PublishConventions : Plugin<Project> {

  private val version = "0.2.0-beta02"
  private val group = "com.attafitamim.krop"

  override fun apply(project: Project) {
    project.plugins.apply("com.vanniktech.maven.publish")

    val mavenPublishing = project.extensions.getByName("mavenPublishing")
            as MavenPublishBaseExtension

    val artifact = project.fullName
    mavenPublishing.apply {
      coordinates(group, artifact, version)
      pom(MavenPom::configure)
      publishToMavenCentral(
        com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL,
        automaticRelease = true
      )
      signAllPublications()
    }
  }
}

private fun MavenPom.configure() {
  name.set("Krop")
  description.set("Image cropper for Compose Multiplatform")
  url.set("https://github.com/tamimattafi/krop")

  licenses { licenseSpec ->
    licenseSpec.license(MavenPomLicense::configure)
  }

  developers { developerSpec ->
    developerSpec.developer(MavenPomDeveloper::configure)
  }

  scm(MavenPomScm::configure)
}

private fun MavenPomLicense.configure() {
  name.set("Apache-2.0")
  url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
}

private fun MavenPomDeveloper.configure() {
  name.set("Tamim Attafi")
  email.set("attafitamim@gmail.com")
}

private fun MavenPomScm.configure() {
  connection.set("scm:git:github.com/tamimattafi/krop.git")
  developerConnection.set("scm:git:ssh://github.com/tamimattafi/krop.git")
  url.set("https://github.com/tamimattafi/krop.git")
}

private val Project.fullName: String get() {
  val names = ArrayList<String>()
  names.add(name)

  var currentProject = this
  while (currentProject.parent != null && currentProject.parent?.name != ROOT_MODULE_NAME) {
    val parentProject = currentProject.parent!!
    names.add(parentProject.name)
    currentProject = parentProject
  }

  return names.reversed().joinToString("-")
}
