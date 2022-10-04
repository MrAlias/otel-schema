package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpanProcessorSchemaTest {

  private static final SchemaValidationExtension validationExtension =
      new SchemaValidationExtension("span_processor.json");

  @Test
  void simpleAllFields() {
    assertThat(validationExtension.validateResource("/span_processor/simple-all-fields.yaml"))
        .isEmpty();
  }

  @Test
  void simpleInvalidTypes() {
    assertThat(validationExtension.validateResource("/span_processor/simple-invalid-types.yaml"))
        .containsExactlyInAnyOrder("$.args.exporter: string found, object expected");
  }

  @Test
  void batchAllFields() {
    assertThat(validationExtension.validateResource("/span_processor/batch-all-fields.yaml"))
        .isEmpty();
  }

  @Test
  void batchInvalidTypes() {
    assertThat(validationExtension.validateResource("/span_processor/batch-invalid-types.yaml"))
        .containsExactlyInAnyOrder(
            "$.args.exporter: string found, object expected",
            "$.args.max_queue_size: string found, integer expected",
            "$.args.scheduled_delay_millis: string found, integer expected",
            "$.args.export_timeout_millis: string found, integer expected",
            "$.args.max_export_batch_size: string found, integer expected");
  }
}
