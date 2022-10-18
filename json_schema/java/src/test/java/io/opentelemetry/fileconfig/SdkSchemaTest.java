package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;

class SdkSchemaTest {

  private static final SchemaValidationExtension validationExtension =
      new SchemaValidationExtension("sdk.json");

  @Test
  void kitchenSink() {
    // Validate example kitchen-sink file in base of repository
    assertThat(
            validationExtension.validateFile(
                new File(System.getProperty("REPO_DIR") + "/json_schema/kitchen-sink.yaml")))
        .isEmpty();
  }

  @Test
  void simple() {
    assertThat(validationExtension.validateResource("/sdk/simple.yaml")).isEmpty();
  }
}
