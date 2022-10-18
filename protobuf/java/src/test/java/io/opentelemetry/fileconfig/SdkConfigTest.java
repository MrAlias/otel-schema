package io.opentelemetry.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Message;
import io.opentelemetry.proto.sdk.v1.ArgsValue;
import io.opentelemetry.proto.sdk.v1.AttributeLimits;
import io.opentelemetry.proto.sdk.v1.Config;
import io.opentelemetry.proto.sdk.v1.LoggerProvider;
import io.opentelemetry.proto.sdk.v1.Logging;
import io.opentelemetry.proto.sdk.v1.MeterProvider;
import io.opentelemetry.proto.sdk.v1.Propagator;
import io.opentelemetry.proto.sdk.v1.Resource;
import io.opentelemetry.proto.sdk.v1.SdkConfig;
import io.opentelemetry.proto.sdk.v1.TracerProvider;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SdkConfigTest {

  @Test
  void parseYaml() throws FileNotFoundException {
    InputStream inputStream =
        new FileInputStream(System.getProperty("REPO_DIR") + "/protobuf/kitchen-sink.yaml");

    YamlProtobufSchemaValidator validator = new YamlProtobufSchemaValidator();
    Message message = validator.validate(inputStream, Config.newBuilder());

    assertThat(message).isInstanceOf(Config.class);

    SdkConfig actualSdk = ((Config) message).getSdk();

    System.out.println(message);

    Resource expectedResource =
        Resource.newBuilder()
            .addDetectors(Resource.ResourceDetector.newBuilder().setName("com.domain.resources.CustomResourceProvider").build())
            .addDetectors(Resource.ResourceDetector.newBuilder().setName("io.opentelemetry.sdk.extension.resources.*")
                .addExcludedAttributeKeys("process.command_line").build())
            .putAttributes(
                "service.name",
                Resource.AttributeValue.newBuilder().setStringValue("my-service").build())
            .putAttributes(
                "service.instance.id",
                Resource.AttributeValue.newBuilder().setStringValue("1234").build())
            .setSchemaUrl("http://schema.com")
            .build();
    assertThat(actualSdk.getResource()).isEqualTo(expectedResource);

    AttributeLimits expectedAttributeLimits =
        AttributeLimits.newBuilder()
            .setAttributeCountLimit(10)
            .setAttributeValueLengthLimit(100)
            .build();
    assertThat(actualSdk.getAttributeLimits()).isEqualTo(expectedAttributeLimits);

    Logging expectedLogging = Logging.newBuilder()
        .setLevel("info")
        .build();
    assertThat(actualSdk.getLogging()).isEqualTo(expectedLogging);

    TracerProvider expectedTracerProvider =
        TracerProvider.newBuilder()
            .addSpanProcessors(
                TracerProvider.SpanProcessor.newBuilder()
                    .setProcessor(TracerProvider.SpanProcessor.SpanProcessorType.SIMPLE)
                    .setSimpleProcessorArgs(
                        TracerProvider.SpanProcessor.SimpleSpanProcessorArgs.newBuilder()
                            .setExporter(
                                TracerProvider.SpanProcessor.SpanExporterType.EXTENSION_EXPORTER)
                            .setExtensionExporterArgs(
                                TracerProvider.SpanProcessor.ExtensionSpanExporterArgs.newBuilder()
                                    .setName("foo-exporter")
                                    .putArgs(
                                        "key",
                                        ArgsValue.newBuilder().setStringValue("value").build())
                                    .build())
                            .build())
                    .build())
            .addSpanProcessors(
                TracerProvider.SpanProcessor.newBuilder()
                    .setProcessor(TracerProvider.SpanProcessor.SpanProcessorType.BATCH)
                    .setBatchProcessorArgs(
                        TracerProvider.SpanProcessor.BatchSpanProcessorArgs.newBuilder()
                            .setExporter(TracerProvider.SpanProcessor.SpanExporterType.OTLP)
                            .setOtlpExporterArgs(
                                TracerProvider.SpanProcessor.OtlpSpanExporterArgs.newBuilder()
                                    .setEndpoint("https://my-remote-otlp-host.com:4317")
                                    .putHeaders("api-key", "1234")
                                    .setCompression("gzip")
                                    .setProtocol("grpc")
                                    .build())
                            .setMaxQueueSize(10)
                            .build())
                    .build())
            .addSpanProcessors(
                TracerProvider.SpanProcessor.newBuilder()
                    .setProcessor(
                        TracerProvider.SpanProcessor.SpanProcessorType.EXTENSION_PROCESSOR)
                    .setExtensionProcessorArgs(
                        TracerProvider.SpanProcessor.ExtensionSpanProcessorArgs.newBuilder()
                            .setName("my-processor")
                            .putArgs("key", ArgsValue.newBuilder().setStringValue("value").build())
                            .build())
                    .build())
            .setSampler(TracerProvider.SamplerType.PARENT_BASED)
            .setParentBasedSamplerArgs(
                TracerProvider.ParentBasedSamplerArgs.newBuilder()
                    .setRootSampler(TracerProvider.SamplerType.TRACE_ID_RATIO_BASED)
                    .setTraceIdRatioBasedSamplerArgs(
                        TracerProvider.TraceIdRatioBasedSamplerArgs.newBuilder()
                            .setRatio(.005)
                            .build())
                    .setRemoteParentSampled(true)
                    .build())
            .setSpanLimits(
                TracerProvider.SpanLimits.newBuilder()
                    .setAttributeCountLimit(20)
                    .setAttributeValueLengthLimit(200)
                    .setAttributeCountPerEventLimit(5)
                    .setAttributeCountPerLinkLimit(5)
                    .setEventCountLimit(10)
                    .setLinkCountLimit(4)
                    .build())
            .build();
    assertThat(actualSdk.getTracerProvider()).isEqualTo(expectedTracerProvider);

    MeterProvider expectedMeterProvider =
        MeterProvider.newBuilder()
            .addMetricReaders(
                MeterProvider.MetricReader.newBuilder()
                    .setReader(MeterProvider.MetricReader.MetricReaderType.PERIODIC)
                    .setPeriodicReaderArgs(
                        MeterProvider.MetricReader.PeriodicMetricReaderArgs.newBuilder()
                            .setExporter(
                                MeterProvider.MetricReader.PeriodicMetricReaderArgs
                                    .MetricExporterType.OTLP)
                            .setOtlpExporterArgs(
                                MeterProvider.MetricReader.PeriodicMetricReaderArgs
                                    .OtlpMetricExporterArgs.newBuilder()
                                    .setEndpoint("https://my-remote-otlp-host.com:4317")
                                    .putHeaders("api-key", "1234")
                                    .setCompression("gzip")
                                    .setProtocol("grpc")
                                    .build())
                            .setIntervalMillis(100)
                            .build())
                    .build())
            .addMetricReaders(
                MeterProvider.MetricReader.newBuilder()
                    .setReader(MeterProvider.MetricReader.MetricReaderType.EXTENSION_READER)
                    .setExtensionReaderArgs(
                        MeterProvider.MetricReader.ExtensionMetricReaderArgs.newBuilder()
                            .setName("my-reader")
                            .putArgs("key", ArgsValue.newBuilder().setStringValue("value").build())
                            .build())
                    .build())
            .addViews(MeterProvider.RegisteredView.newBuilder()
                .setSelector(MeterProvider.RegisteredView.Selector.newBuilder()
                    .setInstrumentName("my-instrument")
                    .setInstrumentType(MeterProvider.RegisteredView.Selector.InstrumentType.COUNTER)
                    .setMeterName("my-meter")
                    .setMeterVersion("1.0.0")
                    .setMeterSchemaUrl("http://example.com")
                    .build())
                .setView(MeterProvider.RegisteredView.View.newBuilder()
                    .setName("new-instrument-name")
                    .setDescription("new-description")
                    .setAggregation(MeterProvider.RegisteredView.View.Aggregation.EXPLICIT_BUCKET_HISTOGRAM)
                    .setExplicitBucketHistogramAggregationArgs(
                        MeterProvider.RegisteredView.View.ExplicitBucketHistogramArgs.newBuilder()
                            .addBucketBoundaries(1.0).addBucketBoundaries(2.0).addBucketBoundaries(5.0).build())
                    .addAttributeKeys("foo")
                    .addAttributeKeys("bar")
                    .build())
                .build())
            .addViews(
                MeterProvider.RegisteredView.newBuilder()
                    .setSelector(
                        MeterProvider.RegisteredView.Selector.newBuilder()
                            .setInstrumentName("*.server.duration")
                            .build())
                    .setView(
                        MeterProvider.RegisteredView.View.newBuilder()
                            .setAggregation(MeterProvider.RegisteredView.View.Aggregation.DROP)
                            .build())
                    .build())
            .build();
    assertThat(actualSdk.getMeterProvider()).isEqualTo(expectedMeterProvider);

    LoggerProvider expectedLoggerProvider =
        LoggerProvider.newBuilder()
            .addLogRecordProcessors(
                LoggerProvider.LogRecordProcessor.newBuilder()
                    .setProcessor(LoggerProvider.LogRecordProcessor.LogRecordProcessorType.SIMPLE)
                    .setSimpleProcessorArgs(
                        LoggerProvider.LogRecordProcessor.SimpleLogRecordProcessorArgs.newBuilder()
                            .setExporter(
                                LoggerProvider.LogRecordProcessor.LogRecordExporterType
                                    .EXTENSION_EXPORTER)
                            .setExtensionExporterArgs(
                                LoggerProvider.LogRecordProcessor.ExtensionLogRecordExporterArgs
                                    .newBuilder()
                                    .setName("foo-exporter")
                                    .putArgs(
                                        "key",
                                        ArgsValue.newBuilder().setStringValue("value").build())
                                    .build())
                            .build())
                    .build())
            .addLogRecordProcessors(
                LoggerProvider.LogRecordProcessor.newBuilder()
                    .setProcessor(LoggerProvider.LogRecordProcessor.LogRecordProcessorType.BATCH)
                    .setBatchProcessorArgs(
                        LoggerProvider.LogRecordProcessor.BatchLogRecordProcessorArgs.newBuilder()
                            .setExporter(
                                LoggerProvider.LogRecordProcessor.LogRecordExporterType.OTLP)
                            .setOtlpExporterArgs(
                                LoggerProvider.LogRecordProcessor.OtlpLogRecordExporterArgs
                                    .newBuilder()
                                    .setEndpoint("https://my-remote-otlp-host.com:4317")
                                    .putHeaders("api-key", "1234")
                                    .setCompression("gzip")
                                    .setProtocol("grpc")
                                    .build())
                            .setMaxQueueSize(10)
                            .build())
                    .build())
            .addLogRecordProcessors(
                LoggerProvider.LogRecordProcessor.newBuilder()
                    .setProcessor(
                        LoggerProvider.LogRecordProcessor.LogRecordProcessorType
                            .EXTENSION_PROCESSOR)
                    .setExtensionProcessorArgs(
                        LoggerProvider.LogRecordProcessor.ExtensionLogRecordProcessorArgs
                            .newBuilder()
                            .setName("my-processor")
                            .putArgs("key", ArgsValue.newBuilder().setStringValue("value").build())
                            .build())
                    .build())
            .build();
    assertThat(actualSdk.getLoggerProvider()).isEqualTo(expectedLoggerProvider);

    List<Propagator> expectedPropagators =
        Collections.singletonList(
            Propagator.newBuilder().setPropagator(Propagator.PropagatorType.TRACECONTEXT).build());
    assertThat(actualSdk.getPropagatorsList()).isEqualTo(expectedPropagators);

    SdkConfig expectedSdk =
        SdkConfig.newBuilder()
            .setDisabled(false)
            .setResource(expectedResource)
            .setLogging(expectedLogging)
            .setAttributeLimits(expectedAttributeLimits)
            .setTracerProvider(expectedTracerProvider)
            .setMeterProvider(expectedMeterProvider)
            .setLoggerProvider(expectedLoggerProvider)
            .addAllPropagators(expectedPropagators)
            .build();
    assertThat(actualSdk).isEqualTo(expectedSdk);
  }
}
