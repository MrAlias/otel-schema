package main

import "fmt"

func NewConfiguration(cfg config) {
	fmt.Println(cfg)
}

type config map[string]interface{}

func main() {
	NewConfiguration(config{
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
}
