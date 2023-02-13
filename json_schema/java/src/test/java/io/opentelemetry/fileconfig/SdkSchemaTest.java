package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import io.opentelemetry.fileconf.schema.Attributes;
import io.opentelemetry.fileconf.schema.Headers;
import io.opentelemetry.fileconf.schema.Jaeger;
import io.opentelemetry.fileconf.schema.JaegerRemote;
import io.opentelemetry.fileconf.schema.Limits;
import io.opentelemetry.fileconf.schema.OpenTelemetryConfiguration;
import io.opentelemetry.fileconf.schema.Otlp;
import io.opentelemetry.fileconf.schema.ParentBased;
import io.opentelemetry.fileconf.schema.Processor;
import io.opentelemetry.fileconf.schema.ProcessorArgs;
import io.opentelemetry.fileconf.schema.Propagator;
import io.opentelemetry.fileconf.schema.Resource;
import io.opentelemetry.fileconf.schema.SamplerConfig;
import io.opentelemetry.fileconf.schema.Sdk;
import io.opentelemetry.fileconf.schema.SpanLimits;
import io.opentelemetry.fileconf.schema.TraceIDRatioBased;
import io.opentelemetry.fileconf.schema.TracerProvider;
import io.opentelemetry.fileconf.schema.TracerProviderExporters;
import io.opentelemetry.fileconf.schema.Zipkin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SdkSchemaTest {

  @Test
  void kitchenSink() throws FileNotFoundException {
    YamlJsonSchemaValidator validator =
        new YamlJsonSchemaValidator(new File(System.getenv("SCHEMA_FILE")));

    // Validate example kitchen-sink file in base of repository
    assertThat(
            validator.validate(
                new FileInputStream(System.getenv("REPO_DIR") + "/json_schema/kitchen-sink.yaml")))
        .isEmpty();

    OpenTelemetryConfiguration configuration =
        validator.parse(
            new FileInputStream(System.getenv("REPO_DIR") + "/json_schema/kitchen-sink.yaml"),
            new TypeReference<>() {});

    assertThat(configuration.getSchemeVersion()).isEqualTo(0.1);

    Sdk sdk = configuration.getSdk();
    assertThat(sdk).isNotNull();

    Resource resource = sdk.getResource();
    assertThat(resource).isNotNull();
    Attributes resourceAttributes = resource.getAttributes();
    assertThat(resourceAttributes).isNotNull();
    assertThat(resourceAttributes.getServiceName()).isEqualTo("unknown_service");

    List<Propagator> propagators = sdk.getPropagators();
    assertThat(propagators).hasSize(7);
    assertThat(propagators.get(0).getName()).isEqualTo("tracecontext");
    assertThat(propagators.get(1).getName()).isEqualTo("baggage");
    assertThat(propagators.get(2).getName()).isEqualTo("b3");
    assertThat(propagators.get(3).getName()).isEqualTo("b3multi");
    assertThat(propagators.get(4).getName()).isEqualTo("b3multijaeger");
    assertThat(propagators.get(5).getName()).isEqualTo("xray");
    assertThat(propagators.get(6).getName()).isEqualTo("ottrace");

    Limits attributeLimits = sdk.getAttributeLimits();
    assertThat(attributeLimits).isNotNull();
    assertThat(attributeLimits.getAttributeValueLengthLimit()).isEqualTo(4096);
    assertThat(attributeLimits.getAttributeCountLimit()).isEqualTo(128);

    TracerProvider tracerProvider = sdk.getTracerProvider();
    assertThat(tracerProvider).isNotNull();

    TracerProviderExporters exporters = tracerProvider.getExporters();
    assertThat(exporters).isNotNull();

    Otlp otlp = exporters.getOtlp();
    assertThat(otlp.getProtocol()).isEqualTo("http/protobuf");
    assertThat(otlp.getEndpoint()).isEqualTo(URI.create("http://localhost:4318/v1/metrics"));
    assertThat(otlp.getCertificate()).isEqualTo("/app/cert.pem");
    assertThat(otlp.getClientKey()).isEqualTo("/app/cert.pem");
    assertThat(otlp.getClientCertificate()).isEqualTo("/app/cert.pem");
    assertThat(otlp.getCompression()).isEqualTo("gzip");
    assertThat(otlp.getTimeout()).isEqualTo(10000);
    Headers headers = otlp.getHeaders();
    assertThat(headers).isNotNull();
    assertThat(headers.getAdditionalProperties()).isEqualTo(Map.of("api-key", 1234));

    Zipkin zipkin = exporters.getZipkin();
    assertThat(zipkin).isNotNull();
    assertThat(zipkin.getEndpoint()).isEqualTo(URI.create("http://localhost:9411/api/v2/spans"));
    assertThat(zipkin.getTimeout()).isEqualTo(10000);

    Jaeger jaeger = exporters.getJaeger();
    assertThat(jaeger).isNotNull();
    assertThat(jaeger.getProtocol()).isEqualTo("http/thrift.binary");
    assertThat(jaeger.getEndpoint()).isEqualTo(URI.create("http://localhost:14268/api/traces"));
    assertThat(jaeger.getTimeout()).isEqualTo(10000);
    assertThat(jaeger.getUser()).isEqualTo("user");
    assertThat(jaeger.getPassword()).isEqualTo("password");
    assertThat(jaeger.getAgentHost()).isEqualTo("localhost");
    assertThat(jaeger.getAgentPort()).isEqualTo(6832);

    List<Processor> spanProcessors = tracerProvider.getSpanProcessors();
    assertThat(spanProcessors).hasSize(3);

    Processor batchProcessor = spanProcessors.get(0);
    assertThat(batchProcessor).isNotNull();
    assertThat(batchProcessor.getName()).isEqualTo("batch");
    ProcessorArgs args = batchProcessor.getArgs();
    assertThat(args).isNotNull();
    assertThat(args.getScheduleDelay()).isEqualTo(5000);
    assertThat(args.getExportTimeout()).isEqualTo(30000);
    assertThat(args.getMaxQueueSize()).isEqualTo(2048);
    assertThat(args.getMaxExportBatchSize()).isEqualTo(512);
    assertThat(args.getExporter()).isEqualTo("otlp");

    batchProcessor = spanProcessors.get(1);
    assertThat(batchProcessor).isNotNull();
    assertThat(batchProcessor.getName()).isEqualTo("batch");
    args = batchProcessor.getArgs();
    assertThat(args.getExporter()).isEqualTo("zipkin");

    batchProcessor = spanProcessors.get(2);
    assertThat(batchProcessor).isNotNull();
    assertThat(batchProcessor.getName()).isEqualTo("batch");
    args = batchProcessor.getArgs();
    assertThat(args.getExporter()).isEqualTo("jaeger");

    SpanLimits spanLimits = tracerProvider.getSpanLimits();
    assertThat(spanLimits).isNotNull();
    assertThat(spanLimits.getAttributeValueLengthLimit()).isEqualTo(4096);
    assertThat(spanLimits.getAttributeCountLimit()).isEqualTo(128);
    assertThat(spanLimits.getEventCountLimit()).isEqualTo(128);
    assertThat(spanLimits.getLinkCountLimit()).isEqualTo(128);
    assertThat(spanLimits.getEventAttributeCountLimit()).isEqualTo(128);
    assertThat(spanLimits.getLinkAttributeCountLimit()).isEqualTo(128);

    SamplerConfig samplerConfig = tracerProvider.getSamplerConfig();
    assertThat(samplerConfig).isNotNull();
    // always_on and always_off are null because they have no properties.
    // assertThat(samplerConfig.getAlwaysOn()).isNotNull();
    // assertThat(samplerConfig.getAlwaysOff()).isNotNull();
    TraceIDRatioBased traceIdRatioBased = samplerConfig.getTraceIdRatioBased();
    assertThat(traceIdRatioBased).isNotNull();
    assertThat(traceIdRatioBased.getRatio()).isEqualTo(.0001);
    ParentBased parentBased = samplerConfig.getParentBased();
    assertThat(parentBased).isNotNull();
    assertThat(parentBased.getRoot()).isEqualTo("trace_id_ratio_based");
    assertThat(parentBased.getRemoteParentSampled()).isEqualTo("always_on");
    assertThat(parentBased.getRemoteParentNotSampled()).isEqualTo("always_off");
    assertThat(parentBased.getLocalParentSampled()).isEqualTo("always_on");
    assertThat(parentBased.getLocalParentNotSampled()).isEqualTo("always_off");
    JaegerRemote jaegerRemote = samplerConfig.getJaegerRemote();
    assertThat(jaegerRemote).isNotNull();
    assertThat(jaegerRemote.getEndpoint()).isEqualTo(URI.create("http://localhost:14250"));
    assertThat(jaegerRemote.getPollingInterval()).isEqualTo(5000);
    assertThat(jaegerRemote.getInitialSamplingRate()).isEqualTo(.25);

    assertThat(tracerProvider.getSampler()).isEqualTo("parent_based");

    // TODO: add assertions for MeterProvider, LoggerProvider

    System.out.print(configuration);
  }
}
