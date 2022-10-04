package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExporterSchemaTest {

  private static final SchemaValidationExtension validationExtension =
      new SchemaValidationExtension("exporter.json");

  @Test
  void otlpAllFields() {
    assertThat(validationExtension.validateResource("/exporter/otlp-all-fields.yaml")).isEmpty();
  }

  @Test
  void otlpInvalidTypes() {
    assertThat(validationExtension.validateResource("/exporter/otlp-invalid-types.yaml"))
        .containsExactlyInAnyOrder(
            "$.args.endpoint: integer found, string expected",
            "$.args.insecure: integer found, boolean expected",
            "$.args.certificate: integer found, string expected",
            "$.args.client_key: integer found, string expected",
            "$.args.client_certificate: integer found, string expected",
            "$.args.headers: integer found, object expected",
            "$.args.compression: integer found, string expected",
            "$.args.timeout: integer found, string expected",
            "$.args.protocol: integer found, string expected");
  }

  @Test
  void zipkinAllFields() {
    assertThat(validationExtension.validateResource("/exporter/zipkin-all-fields.yaml")).isEmpty();
  }

  @Test
  void zipkinInvalidTypes() {
    assertThat(validationExtension.validateResource("/exporter/zipkin-invalid-types.yaml"))
        .containsExactlyInAnyOrder(
            "$.args.endpoint: integer found, string expected",
            "$.args.timeout: integer found, string expected");
  }

  @Test
  void jaegerAllFields() {
    assertThat(validationExtension.validateResource("/exporter/jaeger-all-fields.yaml")).isEmpty();
  }

  @Test
  void jaegerInvalidTypes() {
    assertThat(validationExtension.validateResource("/exporter/jaeger-invalid-types.yaml"))
        .containsExactlyInAnyOrder(
            "$.args.protocol: integer found, string expected",
            "$.args.endpoint: integer found, string expected",
            "$.args.timeout: integer found, string expected",
            "$.args.user: integer found, string expected",
            "$.args.agent_host: integer found, string expected",
            "$.args.agent_port: integer found, string expected");
  }
}
