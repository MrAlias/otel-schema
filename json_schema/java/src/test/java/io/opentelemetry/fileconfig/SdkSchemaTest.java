package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;

class SdkSchemaTest {

  @Test
  void kitchenSink() throws FileNotFoundException {
    YamlJsonSchemaValidator validator =
        new YamlJsonSchemaValidator(new File(System.getenv("SCHEMA_FILE")));

    FileInputStream fis =
        new FileInputStream(System.getenv("REPO_DIR") + "/json_schema/kitchen-sink.yaml");

    // Validate example kitchen-sink file in base of repository
    assertThat(validator.validate(fis)).isEmpty();
  }
}
