package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpanLimitsSchemaTest {

  private static final SchemaValidationExtension validationExtension =
      new SchemaValidationExtension("span_limits.json");

  @Test
  void allFields() {
    assertThat(validationExtension.validateResource("/span_limits/all-fields.yaml")).isEmpty();
  }

  @Test
  void invalidTypes() {
    assertThat(validationExtension.validateResource("/span_limits/invalid-types.yaml"))
        .containsExactlyInAnyOrder(
            "$.attribute_count_limit: string found, integer expected",
            "$.attribute_value_length_limit: string found, integer expected",
            "$.attribute_count_per_event_limit: string found, integer expected",
            "$.attribute_count_per_link_limit: string found, integer expected",
            "$.event_count_limit: string found, integer expected",
            "$.link_count_limit: string found, integer expected");
  }
}
