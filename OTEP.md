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

## Internal details (TBD)

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

## Future possibilities (TBD)

What are some future changes that this proposal would enable?

## Related Spec issues address

* https://github.com/open-telemetry/opentelemetry-specification/issues/1773
* https://github.com/open-telemetry/opentelemetry-specification/issues/2857
* https://github.com/open-telemetry/opentelemetry-specification/issues/2746

