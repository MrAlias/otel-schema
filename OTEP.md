# OpenTelemetry Configuration

A new configuration interface is proposed here in the form of a configuration model, which can be expressed as a file, and validated through a schema.

## Motivation

OpenTelemetry specifies code that can operate in a variety of ways based on the end-user’s desired mode of operation. This requires a configuration interface be provided to the user so they are able to communicate this information. Currently, OpenTelemetry specifies this interface in the form of the API exposed by the SDKs and environment variables. This environment variable interface is limited in the structure of information it can communicate and the primitives it can support.
Environment Variable Interface Limitations
The environment variable interface suffers from the following identified limitations.

* **Flat**. Structured data is only allowed by using higher-level naming or data encoding schemes. Examples of configuration limited by lack of structured configuration include:
  * Configuring multiple span processors, periodic metric readers, or log record processors.
  * Configuring views.
  * Configuring arguments for parent based sampler (sampler parent is remote and sampled vs. not sampled, sampler when parent is local and sampled vs. not sampled).
* **Runtime dependent**. Different systems expose this interface differently (Linux, BSD, Windows). This usually means unique instructions are required to properly interact with the configuration interface on different systems.
* **Limited values**. Many systems only allow string values to be used, but OpenTelemetry specifies many configuration values other than this type. For example, OTEL_RESOURCE_ATTRIBUTES specifies a list of key value pairs to be used as resource attributes, but there is no way to specify array values, or indicate that the value should be interpreted as non-string type.
* **Limited validation**. Validation can only be performed by the receiver, there is no meta-configuration language to validate input.
* **Difficult to extend**. It’s difficult to anticipate the requirements of configuring custom extension components (processors, exporters, samplers, etc), and likely not practical to represent them in a flat structure. As a result, the environment variable interface is limited to components explicitly mentioned in the specification.

## Explanation (TBD)

Using a configuration model or configuration file, users could configure all options currently available via environment variables.

### Goals

* The configuration file must be language implementation agnostic. It must not contain structure or statements that only can be interpreted in a subset of languages. This does not preclude the possibility that the configuration file can have specific extensions included for a subset of languages, but it does mean that the standard format of the file must be interpretable by all implementation languages.
* Broadly supported format. Ideally, the information encoded in the file can be decoded using native tools for all OpenTelemetry implementation languages. However, it must be possible for languages that do not natively support an encoding format to write their own parsers. This means that the file encoding format must be specified in a language agnostic form.
* The file format must support structured data. At the minimum arrays and associative arrays.
* The file format must support at least null, string, double precision floating point (IEEE 754-1985), or signed 64 bit integer value types.
* Extensible. Custom span processors, exporters, samplers, or other user defined code can be configured using this format.
* Configure SDK, but also configure instrumentation.
* Versioning: needs to be able to version stability while evolving
* (stretch) The file format can be validated client side.

## Internal details

In order to provide a minimal API surface area, implementations *MUST* support the following methods.

### ParseAndValidateConfigurationFromFile(filepath, format)

An API called `ParseAndValidateConfigurationFromFile` receives a string parameter indicating the file path containing the configuration to be parsed. An optional format parameter may be provided to indicate the format that this configuration uses. The default value for this parameter is `yaml`. The method returns a `Configuration` model that has been validated. This API *MAY* return an error or raise an exception, whichever is idiomatic to the implementation for the following reasons:

* file doesn't exist or is invalid
* configuration parsed is invalid

#### Python ParseAndValidateConfigurationFromFile example

```python

filename = "./config.yaml"

try:
  cfg = opentelemetry.ParseAndValidateConfigurationFromFile(filename)
except Exception as e:
  print(e)

filename = "./config.ini"

try:
  cfg = opentelemetry.ParseAndValidateConfigurationFromFile(filename, format="ini")
except Exception as e:
  raise e

```

#### Go ParseAndValidateConfigurationFromFile example

```go

filename := "./config.yaml"
cfg, err := otel.ParseAndValidateConfigurationFromFile(filename)
if err != nil {
  return err
}

filename := "./config.json"
cfg, err := otel.ParseAndValidateConfigurationFromFile(filename, otelconfig.WithFormat("json"))
if err != nil {
  return err
}

```

Implementations *MUST* allow users to specify an environment variable to set the configuration file. This gives flexibility to end users of implementations that do not support command line arguments. Some possible names for this variable:

* `OTEL_CONFIGURATION_FILE`
* `OTEL_CONFIG_FILE`
* `OTEL_CONF`

### Configuration model

To allow SDKs and instrumentation libraries to parse configuration without having to implement the parsing logic, a `Configuration` model *MUST* be provided by implementations. This object:

* has already been parsed from a file or data structure
* has been validated

(TBD what methods should this configuration model make available for SDKs/instrumentations?)

### Additional interface: ParseAndValidateConfiguration

Each language implementation supporting OpenTelemetry *MAY* support parsing a data structure instead of a file to produce a model. This allows implementations to provide a configuration interface without the expectation on users to parse a configuration file. The following demonstrates how Python and Go may provide a configuration interface to accomplish this:

#### Python ParseAndValidateConfiguration example

```python
opentelemetry.ParseAndValidateConfiguration(
    {
        "scheme_version": "0.0.1",
        "sdk": {
            "resource": {
                "attributes": {
                    "service.name": "unknown_service",
                }
            },
            "propagators": ["tracecontext", "baggage", "b3multi"],
            "tracer_provider": {
                "exporters": {
                    "zipkin": {
                        "endpoint": "http://localhost:9411/api/v2/spans",
                        "timeout": 10000,
                    },
                    "otlp": {},
                },
                "span_processors": [
                    {
                        "name": "batch",
                        "args": {
                            "schedule_delay": 5000,
                            "export_timeout": 30000,
                            "max_queue_size": 2048,
                            "max_export_batch_size": 512,
                            "exporter": "zipkin",
                        },
                    }
                ],
            },
            "meter_provider": {
                "exporters": {
                    "otlp": {
                        "protocol": "http/protobuf",
                        "endpoint": "http://localhost:4318/v1/metrics",
                        "certificate": "/app/cert.pem",
                        "client_key": "/app/cert.pem",
                        "client_certificate": "/app/cert.pem",
                        "headers": {
                            "api-key": "1234",
                        },
                        "compression": "gzip",
                        "timeout": 10000,
                        "temporality_preference": "delta",
                        "default_histogram_aggregation": "exponential_bucket_histogram",
                    }
                },
                "metric_readers": [
                    {
                        "name": "periodic",
                        "args": {
                            "interval": 5000,
                            "timeout": 30000,
                            "exporter": "otlp",
                        },
                    }
                ],
            },
            "logger_provider": {
                "exporters": {
                    "otlp": {
                        "protocol": "http/protobuf",
                        "endpoint": "http://localhost:4318/v1/logs",
                        "certificate": "/app/cert.pem",
                        "client_key": "/app/cert.pem",
                        "client_certificate": "/app/cert.pem",
                        "headers": {
                            "api-key": "1234",
                        },
                        "compression": "gzip",
                        "timeout": 10000,
                    },
                },
                "log_record_processors": [
                    {
                        "name": "batch",
                        "args": {
                            "schedule_delay": 5000,
                            "export_timeout": 30000,
                            "max_queue_size": 2048,
                            "max_export_batch_size": 512,
                            "exporter": "otlp",
                        },
                    },
                ],
            },
        },
    }
)
```

### Go ParseAndValidateConfiguration example

```go
type config map[string]interface{} // added for convenience

otel.ParseAndValidateConfiguration(config{
    "sdk": config{
      "resource": config{
        "attributes": config{
          "service.name": "unknown_service",
        },
      },
      "propagators": []string{"tracecontext", "baggage", "b3multi"},
      "tracer_provider": config{
        "exporters": config{
          "zipkin": config{
            "endpoint": "http://localhost:9411/api/v2/spans",
            "timeout":  10000,
          },
          "otlp": config{},
        },
        "span_processors": []config{
          {
            "name": "batch",
            "args": config{
              "schedule_delay":        5000,
              "export_timeout":        30000,
              "max_queue_size":        2048,
              "max_export_batch_size": 512,
              "exporter":              "zipkin",
            },
          },
        },
      },
      "meter_provider": config{
        "exporters": config{
          "otlp": config{
            "protocol":           "http/protobuf",
            "endpoint":           "http://localhost:4318/v1/metrics",
            "certificate":        "/app/cert.pem",
            "client_key":         "/app/cert.pem",
            "client_certificate": "/app/cert.pem",
            "headers": config{
            	"api-key": "1234",
            },
            "compression":                   "gzip",
            "timeout":                       10000,
            "temporality_preference":        "delta",
            "default_histogram_aggregation": "exponential_bucket_histogram",
          },
        },
        "metric_readers": []config{
          {
            "name": "periodic",
            "args": config{
              "interval": 5000,
              "timeout":  30000,
              "exporter": "otlp",
            },
          },
        },
      },
      "logger_provider": config{
        "exporters": config{
          "otlp": config{
            "protocol":           "http/protobuf",
            "endpoint":           "http://localhost:4318/v1/logs",
            "certificate":        "/app/cert.pem",
            "client_key":         "/app/cert.pem",
            "client_certificate": "/app/cert.pem",
            "headers": config{
              "api-key": "1234",
            },
            "compression": "gzip",
            "timeout":     10000,
          },
        },
        "log_record_processors": []config{
          {
            "name": "batch",
            "args": config{
              "schedule_delay":        5000,
              "export_timeout":        30000,
              "max_queue_size":        2048,
              "max_export_batch_size": 512,
              "exporter":              "otlp",
            },
          },
        },
      },
    },
  },
)
```

The configuration model *MAY* also be configured via the use of a configuration file. The following demonstrates an example configuration file format:

```yaml
# include version specification in configuration files to help with parsing and schema evolution.
scheme_version: 0.1
sdk:
  # Disable the SDK for all signals.
  #
  # Boolean value. If "true", a no-op SDK implementation will be used for all telemetry
  # signals. Any other value or absence of the variable will have no effect and the SDK
  # will remain enabled. This setting has no effect on propagators configured through
  # the OTEL_PROPAGATORS variable.
  #
  # Environment variable: OTEL_SDK_DISABLED
  disabled: false
  # Configure resource attributes and resource detection for all signals.
  resource:
    # Key-value pairs to be used as resource attributes.
    #
    # Environment variable: OTEL_RESOURCE_ATTRIBUTES
    attributes:
      # Sets the value of the `service.name` resource attribute
      #
      # Environment variable: OTEL_SERVICE_NAME
      service.name: !!str "unknown_service"
  # Configure context propagators. Each propagator has a name and args used to configure it. None of the propagators here have configurable options so args is not demonstrated.
  #
  # Environment variable: OTEL_PROPAGATORS
  propagators:
    - name: tracecontext
    - name: baggage
    - name: b3
    - name: b3multi
    - name: b3multijaeger
    - name: xray
    - name: ottrace
  # Configure the tracer provider.
  tracer_provider:
    # Span exporters. Each exporter key refers to the type of the exporter. Values configure the exporter. Exporters must be associated with a span processor.
    exporters:
      # Configure the zipkin exporter.
      zipkin:
        # Sets the endpoint.
        #
        # Environment variable: OTEL_EXPORTER_ZIPKIN_ENDPOINT
        endpoint: http://localhost:9411/api/v2/spans
        # Sets the max time to wait for each export.
        #
        # Environment variable: OTEL_EXPORTER_ZIPKIN_TIMEOUT
        timeout: 10000
      # TODO: OTLP exporter configuration.
      # TODO: Jaeger exporter configuration.
    # List of span processors. Each span processor has a name and args used to configure it.
    span_processors:
      # Add a batch span processor.
      #
      # Environment variable: OTEL_BSP_*, OTEL_TRACES_EXPORTER
      - name: batch
        # Configure the batch span processor.
        args:
          # Sets the delay interval between two consecutive exports.
          #
          # Environment variable: OTEL_BSP_SCHEDULE_DELAY
          schedule_delay: 5000
          # Sets the maximum allowed time to export data.
          #
          # Environment variable: OTEL_BSP_EXPORT_TIMEOUT
          export_timeout: 30000
          # Sets the maximum queue size.
          #
          # Environment variable: OTEL_BSP_MAX_QUEUE_SIZE
          max_queue_size: 2048
          # Sets the maximum batch size.
          #
          # Environment variable: OTEL_BSP_MAX_EXPORT_BATCH_SIZE
          max_export_batch_size: 512
          # Sets the exporter. Exporter must refer to a key in sdk.tracer_provider.exporters.
          #
          # Environment variable: OTEL_TRACES_EXPORTER
          exporter: zipkin
  # Configure the meter provider.
  meter_provider:
    # Metric exporters. Each exporter key refers to the type of the exporter. Values configure the exporter. Exporters must be associated with a metric reader.
    exporters:
      # Configure the otlp exporter.
      otlp:
        # Sets the protocol.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_PROTOCOL, OTEL_EXPORTER_OTLP_METRICS_PROTOCOL
        protocol: http/protobuf
        # Sets the endpoint.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_ENDPOINT, OTEL_EXPORTER_OTLP_METRICS_ENDPOINT
        endpoint: http://localhost:4318/v1/metrics
        # Sets the certificate.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_CERTIFICATE, OTEL_EXPORTER_OTLP_METRICS_CERTIFICATE
        certificate: /app/cert.pem
        # Sets the mTLS private client key.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_CLIENT_KEY, OTEL_EXPORTER_OTLP_METRICS_CLIENT_KEY
        client_key: /app/cert.pem
        # Sets the mTLS client certificate.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_CLIENT_CERTIFICATE, OTEL_EXPORTER_OTLP_METRICS_CLIENT_CERTIFICATE
        client_certificate: /app/cert.pem
        # Sets the headers.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_HEADERS, OTEL_EXPORTER_OTLP_METRICS_HEADERS
        headers:
          api-key: 1234
        # Sets the compression.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_COMPRESSION, OTEL_EXPORTER_OTLP_METRICS_COMPRESSION
        compression: gzip
        # Sets the max time to wait for each export.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_TIMEOUT, OTEL_EXPORTER_OTLP_METRICS_TIMEOUT
        timeout: 10000
        # Sets the temporality preference.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE
        temporality_preference: delta
        # Sets the default histogram aggregation.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION
        default_histogram_aggregation: exponential_bucket_histogram
    # List of metric readers. Each metric reader has a name and args used to configure it.
    metric_readers:
      # Add a periodic metric reader.
      #
      # Environment variable: OTEL_METRICS_EXPORT_*, OTEL_METRICS_EXPORTER
      - name: periodic
        args:
          # Sets delay interval between the start of two consecutive export attempts.
          #
          # Environment variable: OTEL_METRIC_EXPORT_INTERVAL
          interval: 5000
          # Sets the maximum allowed time to export data.
          #
          # Environment variable: OTEL_METRIC_EXPORT_TIMEOUT
          timeout: 30000
          # Sets the exporter. Exporter must refer to a key in sdk.meter_provider.exporters.
          #
          # Environment variable: OTEL_METRICS_EXPORTER
          exporter: otlp
  # Configure the logger provider.
  logger_provider:
    # Log record exporters. Each exporter key refers to the type of the exporter. Values configure the exporter. Exporters must be associated with a log record processor.
    exporters:
      # Configure the otlp exporter.
      otlp:
        # Sets the protocol.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_PROTOCOL, OTEL_EXPORTER_OTLP_LOGS_PROTOCOL
        protocol: http/protobuf
        # Sets the endpoint.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_ENDPOINT, OTEL_EXPORTER_OTLP_LOGS_ENDPOINT
        endpoint: http://localhost:4318/v1/logs
        # Sets the certificate.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_CERTIFICATE, OTEL_EXPORTER_OTLP_LOGS_CERTIFICATE
        certificate: /app/cert.pem
        # Sets the mTLS private client key.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_CLIENT_KEY, OTEL_EXPORTER_OTLP_LOGS_CLIENT_KEY
        client_key: /app/cert.pem
        # Sets the mTLS client certificate.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_CLIENT_CERTIFICATE, OTEL_EXPORTER_OTLP_LOGS_CLIENT_CERTIFICATE
        client_certificate: /app/cert.pem
        # Sets the headers.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_HEADERS, OTEL_EXPORTER_OTLP_LOGS_HEADERS
        headers:
          api-key: 1234
        # Sets the compression.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_COMPRESSION, OTEL_EXPORTER_OTLP_LOGS_COMPRESSION
        compression: gzip
        # Sets the max time to wait for each export.
        #
        # Environment variable: OTEL_EXPORTER_OTLP_TIMEOUT, OTEL_EXPORTER_OTLP_LOGS_TIMEOUT
        timeout: 10000
    # List of log record processors. Each log record processor has a name and args used to configure it.
    log_record_processors:
      # Add a batch log record processor.
      #
      # Environment variable: OTEL_BLRP_*, OTEL_LOGS_EXPORTER
      - name: batch
        # Configure the batch log record processor.
        args:
          # Sets the delay interval between two consecutive exports.
          #
          # Environment variable: OTEL_BLRP_SCHEDULE_DELAY
          schedule_delay: 5000
          # Sets the maximum allowed time to export data.
          #
          # Environment variable: OTEL_BLRP_EXPORT_TIMEOUT
          export_timeout: 30000
          # Sets the maximum queue size.
          #
          # Environment variable: OTEL_BLRP_MAX_QUEUE_SIZE
          max_queue_size: 2048
          # Sets the maximum batch size.
          #
          # Environment variable: OTEL_BLRP_MAX_EXPORT_BATCH_SIZE
          max_export_batch_size: 512
          # Sets the exporter. Exporter must refer to a key in sdk.loger_provider.exporters.
          #
          # Environment variable: OTEL_LOGS_EXPORTER
          exporter: otlp

```

From a technical perspective, how do you propose accomplishing the proposal? In particular, please explain:

* How the change would impact and interact with existing functionality
* Likely error modes (and how to handle them)
* Corner cases (and how to handle them)

While you do not need to prescribe a particular implementation - indeed, OTEPs should be about **behaviour**, not implementation! - it may be useful to provide at least one suggestion as to how the proposal *could* be implemented. This helps reassure reviewers that implementation is at least possible, and often helps them inspire them to think more deeply about trade-offs, alternatives, etc.

## Trade-offs and mitigations (TBD)

What are some (known!) drawbacks? What are some ways that they might be mitigated?

Note that mitigations do not need to be complete *solutions*, and that they do not need to be accomplished directly through your proposal. A suggested mitigation may even warrant its own OTEP!

## Prior art and alternatives (TBD)

What are some prior and/or alternative approaches? For instance, is there a corresponding feature in OpenTracing or OpenCensus? What are some ideas that you have rejected?

## Open questions

### How to handle environment variable / file config overlap?

How does file configuration interact with environment variable configuration when both are present?

* Solution 1: Ignore environment configuration when file configuration is present. Log a warning to the user indicating that multiple configuration modes were detected, but use the file configuration as the source of truth. 
* Solution 2: Superimpose environment configuration on top of file configuration when both are present. One problem with this is that environment variable configuration doesn’t map to file configuration in an intuitive way. For example, OTEL_TRACES_EXPORTER defines a list of span exporters to be paired with a batch span processor configured by the OTEL_BSP_* variables. What do we do if the file config already contains one or more processors with an exporter specified in OTEL_TRACES_EXPORTER? Essentially, do we merge or append the environment variable configuration?

### How to handle no-code vs programmatic configuration?

How should the SDK be configured when both no-code configuration (either environment variable or file config) and programmatic configuration are present? NOTE: this question exists today with only the environment variable interface available.

* Solution 1: Make it clear that interpretation of the environment shouldn’t be built into components. Instead, SDKs should have a component that explicitly interprets the environment and returns a configured instance of the SDK. This is how the java SDK works today and it nicely separates concerns.

### How to handle deprecation/breaking changes to the config?

How will breaking changes to the configuration be handled? What will the migration look like for users? Can it be consistent across implementations?

* Solution 1: Major scheme version should be bumped for any backward incompatible changes. Implementations must be aware of the current version they support.

## Future possibilities

### Additional configuration providers

Although the initial proposal for configuration supports only describes in-code and file representations, it's possible additional sources (remote, opamp, ...) for configuration will be desirable. The implementation of the configuration model and components should be extensible to allow for this.

### Integration with auto-instrumentation

The configuration model could be integrated to work with the existing auto-instrumentation tooling in each language implementation.

#### Java

The Java implementation provides a JAR that supports configuring various parameters via system properties. This implementation could leverage a configuration file by supporting its configuration a system property:

```bash
java -javaagent:path/to/opentelemetry-javaagent.jar \
     -Dotel.config=./config.yaml
     -jar myapp.jar
```

#### Python

The Python implementation has a command available that allows users to leverage auto-instrumentation. The `opentelemetry-instrument` command could use a `--config` flag to pass in a config file:

```bash
# install the instrumentation package
pip install opentelemetry-instrumentation
# use a --config parameter to pass in the configuration file
# NOTE: this parameter does not currently exist and would need to be added
opentelemetry-instrument --config ./config.yaml ./python/app.py
```

## Related Spec issues address

* https://github.com/open-telemetry/opentelemetry-specification/issues/1773
* https://github.com/open-telemetry/opentelemetry-specification/issues/2857
* https://github.com/open-telemetry/opentelemetry-specification/issues/2746
* https://github.com/open-telemetry/opentelemetry-specification/issues/2860

