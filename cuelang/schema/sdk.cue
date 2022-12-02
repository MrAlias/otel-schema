package otel

// doesn't do anything but if writing custom tooling this is better
// than just using a comment
@title("OpenTelemetry SDK Configuration")

#SDK: {
	disabled: bool | *true
	resource: #Resource
	span_processors: [...#SpanProcessor]
}

#Resource: {
	attributes: [string]: string | int | float | bool
	schema_url?: string
}

#OTLPExporter: #Exporter & {
	type: "otlp"
	args: {
		protocol: *"grpc" | "http"

		if protocol == "grpc" {
			endpoint: string | *"http://localost:4317"
		}

		if protocol == "http" {
			endpoint: string | *"http://localost:4318"
		}

		headers:        #Headers
		compression?:   "gzip"
		timeout_millis: int & >0 | *30000
	}
}

#ExtensionExporter: #Exporter & {
	type: "extension_exporter"
	args: {
		compression: *"gzip" | "none" | string
	}
}

#ZipkinExporter: #Exporter & {
	type: "zipkin"
	args: {}
}

#Exporter: {
	type: string
	args: {...}
}

#SimpleSpanProcessor: {
	type:     "simple"
	exporter: #Exporter
}

#BatchSpanProcessor: {
	@description("OpenTelemetry SDK Batch Span Processor Configuration")
	type:                   "batch"
	exporter:               #Exporter | *#OTLPExporter
	max_queue_size:         int & >0 | *2048
	scheduled_delay_millis: int & >0 | *5000
	export_timeout_millis:  int & >0 | *30000
	max_export_batch_size:  int & >0 | *512
}

#SpanProcessor: #SimpleSpanProcessor | #BatchSpanProcessor
