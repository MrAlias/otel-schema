import com.google.protobuf.gradle.*

plugins {
  `java-library`
  idea

  id("com.google.protobuf")
  id("com.diffplug.spotless") version "6.9.0"
}

spotless {
  java {
    googleJavaFormat()
  }
  kotlinGradle {
    ktlint().editorConfigOverride(mapOf("indent_size" to "2", "continuation_indent_size" to "2", "disabled_rules" to "no-wildcard-imports"))
  }
}

val protobufVersion = "3.19.4"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.google.protobuf:protobuf-java:$protobufVersion")
  implementation("org.yaml:snakeyaml:1.31")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.1")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")
  implementation("com.google.protobuf:protobuf-java-util:3.21.5")

  testImplementation(platform("org.junit:junit-bom:5.9.1"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testImplementation("org.assertj:assertj-core:3.23.1")
}

protobuf {
  protoc {
    // The artifact spec for the Protobuf Compiler
    artifact = "com.google.protobuf:protoc:$protobufVersion"
  }
}

sourceSets {
  main {
    proto {
      srcDir(project.projectDir.parent.toString() + "/schema")
    }
  }
}

tasks {
  withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
    }

    systemProperties(
      mapOf(
        "REPO_DIR" to File(project.projectDir.parent).parent.toString()
      )
    )
  }
}
