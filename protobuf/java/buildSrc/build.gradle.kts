plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation("gradle.plugin.com.google.protobuf:protobuf-gradle-plugin:0.8.17")
}