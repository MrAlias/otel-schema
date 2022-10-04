package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ViewEntrySchemaTest {

  private static final SchemaValidationExtension validationExtension =
      new SchemaValidationExtension("view_entry.json");

  @Test
  void allFields() {
    assertThat(validationExtension.validateResource("/view_entry/all-fields.yaml")).isEmpty();
  }

  @Test
  void invalidTypes() {
    assertThat(validationExtension.validateResource("/view_entry/invalid-types.yaml"))
        .containsExactlyInAnyOrder(
            "$.selector.instrument_name: integer found, string expected",
            "$.selector.instrument_type: integer found, string expected",
            "$.selector.meter_name: integer found, string expected",
            "$.selector.meter_version: integer found, string expected",
            "$.selector.meter_schema_url: integer found, string expected",
            "$.view.name: integer found, string expected",
            "$.view.description: integer found, string expected",
            "$.view.aggregation.name: integer found, string expected",
            "$.view.aggregation.args: integer found, object expected");
  }
}
