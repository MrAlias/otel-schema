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
