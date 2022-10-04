plugins {
  `java-library`

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

dependencies {
  implementation("org.yaml:snakeyaml:1.31")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.1")
  implementation("com.networknt:json-schema-validator:1.0.72")

  testImplementation(platform("org.junit:junit-bom:5.9.1"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testImplementation("org.assertj:assertj-core:3.23.1")
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
        "SCHEMA_DIR" to project.projectDir.parent.toString() + "/schema",
        "REPO_DIR" to File(project.projectDir.parent).parent.toString()
      )
    )
  }
}
