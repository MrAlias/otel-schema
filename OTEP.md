# OpenTelemetry Configuration

A new configuration interface is proposed here in the form of a configuration model, which can be expressed as a file, and validated through a published schema.

## Motivation

OpenTelemetry specifies code that can operate in a variety of ways based on the end-user’s desired mode of operation. This requires a configuration interface be provided to the user so they are able to communicate this information. Currently, OpenTelemetry specifies this interface in the form of the API exposed by the SDKs and environment variables. This environment variable interface is limited in the structure of information it can communicate and the primitives it can support.

### Environment Variable Interface Limitations

The environment variable interface suffers from the following identified limitations:

* **Flat**. Structured data is only allowed by using higher-level naming or data encoding schemes. Examples of configuration limited by lack of structured configuration include:
  * Configuring multiple span processors, periodic metric readers, or log record processors.
  * Configuring views.
  * Configuring arguments for parent based sampler (sampler parent is remote and sampled vs. not sampled, sampler when parent is local and sampled vs. not sampled).
* **Runtime dependent**. Different systems expose this interface differently (Linux, BSD, Windows). This usually means unique instructions are required to properly interact with the configuration interface on different systems.
* **Limited values**. Many systems only allow string values to be used, but OpenTelemetry specifies many configuration values other than this type. For example, OTEL_RESOURCE_ATTRIBUTES specifies a list of key value pairs to be used as resource attributes, but there is no way to specify array values, or indicate that the value should be interpreted as non-string type.
* **Limited validation**. Validation can only be performed by the receiver, there is no meta-configuration language to validate input.
* **Difficult to extend**. It’s difficult to anticipate the requirements of configuring custom extension components (processors, exporters, samplers, etc), and likely not practical to represent them in a flat structure. As a result, the environment variable interface is limited to components explicitly mentioned in the specification.

## Explanation

Using a configuration model or configuration file, users can configure all options currently available via environment variables.

### Goals

* The configuration must be language implementation agnostic. It must not contain structure or statements that only can be interpreted in a subset of languages. This does not preclude the possibility that the configuration can have specific extensions included for a subset of languages, but it does mean that the standard format must be interpretable by all implementation languages.
* Broadly supported format. Ideally, the information encoded in the file can be decoded using native tools for all OpenTelemetry implementation languages. However, it must be possible for languages that do not natively support an encoding format to write their own parsers. This means that the file encoding format must be specified in a language agnostic form.
* The configuration format must support structured data. At the minimum arrays and associative arrays.
* The format must support at least null, boolean, string, double precision floating point (IEEE 754-1985), or signed 64 bit integer value types.
* Custom span processors, exporters, samplers, or other user defined code can be configured using this format.
* Configure SDK, but also configure instrumentation.
* It needs to be able to version stability while evolving
* The file format can be validated client side.

## Internal details

The schema for OpenTelemetry configuration is to be published in a repository to allow language implementations to leverage that definition to automatically generate code and/or validate end-user configuration. This will ensure that all implementations provide a consistent experience for any version of the schema they support. An example of such a proposed schema is available [here](https://github.com/MrAlias/otel-schema/tree/main/json_schema/schema).

The working group proposes the use of [JSON Schema](https://json-schema.org/) as the language to define the schema. It provides:

* support for client-side validation
* code generation
* broad support across languages

In order to provide a minimal API surface area, implementations *MUST* support the following methods.

### Configure(config)

An API called `Configure` receives a configuration object. This method then applies the configuration object's details to the SDK. This method specifically applies the configuration object to allow for multiple configuration format providers to be supported in the future. This OTEP describes two such providers in a file and data structure formats below, but remote file formats *MAY* be implemented in the future.

### ParseAndValidateConfigurationFromFile(filepath, format) -> config

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
            ...
        }
    })
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
      ...
    },
  },
)
```

### Configuration file

The configuration model *MUST* also be configurable via the use of a configuration file. The working group proposes that all implementations *MUST* support JSON as a configuration file format, and *SHOULD* support YAML.

The following demonstrates an example of a configuration file format (full example [here](https://github.com/MrAlias/otel-schema/blob/main/config.yaml)):

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
  propagators: [tracecontext, baggage]
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
  ...
```

## Trade-offs and mitigations

### Additional method to configure OpenTelemetry

If the implementation suggested in this OTEP goes ahead, users will be presented with another mechanism for configuring OpenTelemetry. This may cause confusion for users who are new to the project. It may be possible to mitigate the confusion by providing users with best practices and documentation.

### Many ways to configure may result in users not knowing what is configured

As there are multiple mechanisms for configuration, it's possible that the active configuration isn't what was expected. This could happen today, and one way it could be mitigated would be by providing a mechanism to list the active OpenTelemetry configuration.


### Errors or difficulty in configuration files

Configuration files provide an opportunity for misconfiguration. A way to mitigate this would be to provide clear messaging and fail quickly when misconfiguration occurs.

## Prior art and alternatives

The working group looked to the OpenTelemetry Collector and OpenTelemetry Operator for inspiration and guidance.

### Alternative schema languages

In choosing to recommend JSON schema, the working group looked at the following options:

* [Cue](https://cuelang.org/) - A promising simpler language to define a schema, the working group decided against CUE because:
  * Tooling available for validating CUE files in languages outside of Go were limited.
  * Familiarity and learning curve would create problems for both users and contributors of OpenTelemetry.
* [Protobuf](https://developers.google.com/protocol-buffers) - With protobuf already used heavily in OpenTelemetry, the format was worth investigating as an option to define the schema. The working group decided against Protobuf because:
  * Validation of schema requires additional logic with custom serialization. This would need to be re-implemented in each language.
  * Validation errors are the result of serlization errors which can be difficult to interpret.

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

#### OpAmp

The configuration may be used in the future in conjunction with the OpAmp protocol to make remote configuration of SDKs available as a feature supported by OpenTelemetry.

## Related Spec issues address

* https://github.com/open-telemetry/opentelemetry-specification/issues/1773
* https://github.com/open-telemetry/opentelemetry-specification/issues/2857
* https://github.com/open-telemetry/opentelemetry-specification/issues/2746
* https://github.com/open-telemetry/opentelemetry-specification/issues/2860
