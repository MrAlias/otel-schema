const { Resource } = require('@opentelemetry/resources');
const { SemanticResourceAttributes } = require('@opentelemetry/semantic-conventions');
const fs = require('fs');
const path = require('path');
const jsonSchemaLibrary = require('json-schema-library');

class Configuration {
  constructor(schema) {
    this.schema = schema;
    this.draft = new jsonSchemaLibrary.Draft06(schema);
  }

  validate(configuration) {
    const errors = this.draft.validate(configuration);
    if (errors && errors.length > 0) {
      console.log(errors);
    }
  }

  async apply(config) {
    const resourceAttributes = config?.sdk?.resource?.attributes || {};
    const resource = new Resource(resourceAttributes);

    if (config.sdk.tracer_provider) {
      await this._configureTracerProvider(config.sdk.tracer_provider, resource);
    }
  }

  async _configureTracerProvider(traceConfig, resource) {
    const { BasicTracerProvider } = await import('@opentelemetry/sdk-trace-base');
    const provider = new BasicTracerProvider({
      resource: resource,
    });

    const exportersConfig = traceConfig.exporters;
    for (let i = 0; i < traceConfig.span_processors.length; i++) {
      let processorConfig = traceConfig.span_processors[i];
      const processor = await this._getTraceProcessor(processorConfig, exportersConfig);
      provider.addSpanProcessor(processor);
    }

    process.on('exit', (code) => {
      provider.shutdown();
    });

    provider.register();
  }

  async _getTraceProcessor(processorConfig, exportersConfig) {
    if (processorConfig.name === 'batch') {
      const { BatchSpanProcessor } = await import('@opentelemetry/sdk-trace-base');
      const exporterName = processorConfig.args.exporter;
      const exporter = await this._getTraceExporter(exporterName, exportersConfig[exporterName]);
      return new BatchSpanProcessor(exporter);

    } else if (processorConfig.name === 'simple') {
      const { SimpleSpanProcessor } = await import('@opentelemetry/sdk-trace-base');
      const exporterName = processorConfig.args.exporter;
      const exporter = await this._getTraceExporter(exporterName, exportersConfig[exporterName]);
      return new SimpleSpanProcessor(exporter);
    }
  }

  async _getTraceExporter(exporterType, exporterConfig) {
    if (exporterType === 'console') {
      const { ConsoleSpanExporter }  = await import('@opentelemetry/sdk-trace-base');
      return new ConsoleSpanExporter();

    } else if (exporterType === 'otlp') {
      if (exporterConfig.protocol === 'http/protobuf') {
        const { OTLPTraceExporter } = await import('@opentelemetry/exporter-trace-otlp-proto');
        return new OTLPTraceExporter({
          url: exporterConfig.endpoint,
          headers: exporterConfig.headers,
          timeoutMillis: exporterConfig.timeout
        });
      } else if (exporterConfig.protocol === 'http/json') {
        const { OTLPTraceExporter } = await import('@opentelemetry/exporter-trace-otlp-http');
        return new OTLPTraceExporter({
          url: exporterConfig.endpoint,
          headers: exporterConfig.headers,
          timeoutMillis: exporterConfig.timeout
        });
      }
    }
  }
}

module.exports = Configuration;
