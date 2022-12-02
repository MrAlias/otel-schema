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

Explain the proposed change as though it was already implemented and you were explaining it to a user. Depending on which layer the proposal addresses, the "user" may vary, or there may even be multiple.

We encourage you to use examples, diagrams, or whatever else makes the most sense!

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

## Open questions (TBD)

What are some questions that you know aren't resolved yet by the OTEP? These may be questions that could be answered through further discussion, implementation experiments, or anything else that the future may bring.

## Future possibilities (TBD)

What are some future changes that this proposal would enable?
