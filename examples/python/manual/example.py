# import opentelemetry
import yaml


class Configuration:
    def __init__(self, conf):
        print(conf)

    @classmethod
    def load_from_file(cls, filename: str):
        print(f"Loading {filename}")
        with open(filename, "r") as file:
            conf = yaml.safe_load(file)
        print(conf)


cfg = Configuration(
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

Configuration.load_from_file("./config.yaml")
