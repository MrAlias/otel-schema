package otel

import "strings"

// doesn't do anything but if writing custom tooling this is better
// than just using a comment
@title("OpenTelemetry SDK View Entry Configuration")

#Config: {
	sdk: #SDK
	...
}

#SDK: {
	disabled: bool | *true
	resource: #Resource
	propagators: [...#Propagator]

	attribute_limits: #AttributeLimits

	tracer_provider: #TracerProvider
}

#Propagator: {
	name: "tracecontext" | "baggage" | "b3" | "b3multi" | "jaeger" | "xray" | "ottrace"
}

#AttributeLimits: {
	attribute_value_length_limit: int
	attribute_count_limit: int
}

#TracerProvider: {
	// adds field `type` that is the name of the exporter up to any / in the name
	exporters:  [Name=_]: #Exporter & {type: strings.Split(Name, "/")[0]}
	span_processors: [...#SpanProcessor]
	span_limits: #SpanLimits
	sampler_config: #SamplerConfig
	sampler: "parent_based"
}

#SpanLimits: {
	attribute_value_length_limit: int
	attribute_count_limit: int
	event_count_limit: int
	link_count_limit: int
	event_attribute_count_limit: int
	link_attribute_count_limit: int
}

#Exporter: #OTLPExporter | #ZipkinExporter

#OTLPExporter: {
	type: "otlp"
	protocol: *"grpc" | "http/protobuf"

	if protocol == "grpc" {
		endpoint: string | *"http://localost:4317"
	}

	if protocol == "http" {
		endpoint: string | *"http://localost:4318"
	}

	certificate: string
	client_key: string
	client_certificate: string
	headers: #Headers
	compression: *"gzip" | "none" | string
	timeout: int
}

#ZipkinExporter: {
	type: "zipkin"
    endpoint: string | *"http://localhost:9411/api/v2/spans"
    timeout: int | *10000
}

#SamplerConfig: {
	always_on: null
	always_off: null
	trace_id_ratio_based: {
		ratio: number
	}
	parent_based: {
		root: #Sampler
		remote_parent_sampled: #Sampler
		remote_parent_not_sampled: #Sampler
		local_parent_sampled: #Sampler
		local_parent_not_sampled: #Sampler
	}
}

#Sampler: "always_on" | "always_off" | "trace_id_ratio_based" | "parent_based"

#Resource: {
	attributes: [string]: string | int | float | bool
	schema_url?: string
}

#SimpleSpanProcessor: {
	name: "simple"
	args: {
		exporter: string
	}
}

#BatchSpanProcessor: {
	@description("OpenTelemetry SDK Batch Span Processor Configuration")
	name: "batch"
	args: {
		exporter: string
		max_queue_size: int & >0 | *2048
		schedule_delay: int & >0 | *5000
		export_timeout: int & >0 | *30000
		max_export_batch_size: int & >0 | *512
	}
}

#SpanProcessor: #SimpleSpanProcessor | #BatchSpanProcessor
