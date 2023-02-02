package otel

import "strings"

// doesn't do anything but if writing custom tooling this is better
// than just using a comment
@title("OpenTelemetry SDK View Entry Configuration")

#Config: {
	scheme_version: string
	sdk: #SDK
}

#SDK: {
	disabled: bool | *true
	resource: #Resource
	propagators: [...#Propagator]

	attribute_limits: #AttributeLimits

	tracer_provider: #TracerProvider
	meter_provider: #MeterProvider
	logger_provider: #LoggerProvider
}

#Propagator: {
	name: "tracecontext" | "baggage" | "b3" | "b3multi" | "jaeger" | "xray" | "ottrace"
}

#AttributeLimits: {
	attribute_value_length_limit: int
	attribute_count_limit: int
}

#MeterProvider: {
	exporters: [Name=_]: #MetricExporter & {type: strings.Split(Name, "/")[0]}
	metric_readers: [...#MetricReader]
	views: [...#View]
	...
}

#View: {
	selector: {
		instrument_name: string
		instrument_type: string
		meter_name: string
		meter_version: string
		meter_schema_url: string
	}
	view: {
		name: string
		description: string
		aggregation: #Aggregation
		attribute_keys: [...string]
	}
}

#Aggregation: #SumAggregation | #LastValueAggregation | #ExplicitHistogramAggregation | #DropAggregation

#SumAggregation: {
	name: "sum"
}

#LastValueAggregation: {
	name: "last_value"
}

#DropAggregation: {
	name: "drop"
}

#ExplicitHistogramAggregation: {
	name: "explicit_bucket_histogram"
	args: {
		boundaries: [...float]
		record_min_max: bool
		max_size: int
	}
}

#MetricReader: #PeriodicReader | #PullReader

#PeriodicReader: {
	name: string
	args: {
	  interval: int
	  timeout: int
	  exporter: string
	}
}

#PullReader: {
	name: string
	args: {
		host: string
		port: int
	}
}

#LoggerProvider: {
	exporters:  [Name=_]: #Exporter & {type: strings.Split(Name, "/")[0]}
	log_record_processors: [...#LogRecordProcessors]
	log_record_limits: {
		attribute_value_length_limit: int
		attribute_count_limit: int
	}
}

#LogRecordProcessors: {
	name: "batch"
	args: {
		schedule_delay: int
		export_timeout: int
		max_queue_size: int
		max_export_batch_size: int
		exporter: string
	}
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

#MetricExporter: #MetricOTLPExporter

#Exporter: #OTLPExporter | #ZipkinExporter | #JaegerExporter

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

#MetricOTLPExporter: {
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
	...
}

#ZipkinExporter: {
	type: "zipkin"
    endpoint: string | *"http://localhost:9411/api/v2/spans"
    timeout: int | *10000
}

#JaegerExporter: {
	type: "jaeger"
	protocol: "http/thrift.binary" | "http/protobuf"
	endpoint: string | *"http://localhost:14268/api/traces"
	timeout: int | *10000
	user: string
	password: string
	agent_host: string
	agent_port: int
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
	jaeger_remote: {
		endpoint: string | *"http://localhost:14250"
		polling_interval: int | *5000
		initial_sampling_rate: float | *0.25
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
