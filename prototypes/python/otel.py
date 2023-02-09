
import importlib
import logging

import yaml
from pathlib import Path
from jsonschema import validate, validators
from jsonschema.exceptions import ValidationError

from opentelemetry.trace import set_tracer_provider


class Config:
    def __init__(self, config=None) -> None:
        self._config = config
    
    def _resource(self):
        # resource detection
        # attributes
        from opentelemetry.sdk.resources import Resource
        return Resource.create(self._config.get("sdk").get("resource").get("attributes"))

    # _get_exporter returns a configured span
    def _get_exporter(self, signal, name):
        if name not in self._config.get("sdk").get(signal).get("exporters"):
            raise Exception(f"exporter {name} not specified for {signal} signal")
        
        # TODO: replace to use entrypoints
        _KNOWN_TRACE_EXPORTER_MAP = {
            "traces": {
                "console": {
                    "pkg": "opentelemetry.sdk.trace.export",
                    "class": "ConsoleSpanExporter",
                },
                "jaeger": {
                    "pkg": "opentelemetry.exporter.jaeger.thrift",
                    "class": "JaegerExporter",
                },
                "zipkin": {
                    "pkg": "opentelemetry.exporter.zipkin.json",
                    "class": "ZipkinExporter",
                },
            },
            "metrics": {
                "console": {
                    "pkg": "opentelemetry.sdk.metrics.export",
                    "class": "ConsoleExporter",
                },
            },
            "logs": {
                "console": {
                    "pkg": "opentelemetry.sdk.logs.export",
                    "class": "ConsoleExporter",
                },
            }
        }
        # look for known exporters
        if name in _KNOWN_TRACE_EXPORTER_MAP.get(signal):
            mod = importlib.__import__(_KNOWN_TRACE_EXPORTER_MAP.get(signal).get(name).get("pkg"), fromlist=[_KNOWN_TRACE_EXPORTER_MAP.get(signal).get(name).get("class")])
            _cls = getattr(mod, _KNOWN_TRACE_EXPORTER_MAP.get(signal).get(name).get("class"))
            return _cls()
        # handle case where a custom exporter is used

    def set_tracer_provider(self):
        from opentelemetry.sdk.trace import TracerProvider
        provider = TracerProvider(resource=self._resource())
        from opentelemetry.sdk.trace.export import BatchSpanProcessor

        for processor in self._config.get("sdk").get("traces").get("span_processors"):
            logging.debug("adding span processor %s", processor)
            try:
                processor = BatchSpanProcessor(self._get_exporter("traces", self._config.get("sdk").get("traces").get("span_processors").get(processor).get("args").get("exporter")))
                provider.add_span_processor(processor)
            except ModuleNotFoundError as exc:
                logging.error("module not found", exc)
        set_tracer_provider(provider)

    def set_meter_provider(self):
        pass

    def apply(self):
        logging.debug("applying configuration %s", self._config)
        if self._config is None or self._config.get("sdk").get("disabled"):
            logging.debug("sdk disabled, nothing to apply")
            return
        self.set_tracer_provider()
        self.set_meter_provider()


NoOpConfig = Config()


def parse_and_validate_from_config_file(filename: str, schema: str="../schema/schema.json") -> Config:
    logging.debug(f"Loading config file: {filename}")
    path = Path(__file__).parent.resolve()
    resolver = validators.RefResolver(
        base_uri=f"{path.as_uri()}/",
        referrer=True,
    )

    with open(filename, "r") as stream:
        try:
            parse_config = yaml.safe_load(stream)
            logging.debug("YAML parsed successfully")
            logging.debug(f"Validating using schema file: {schema}")
            validate(
                instance=parse_config,
                schema={"$ref": schema},
                resolver=resolver,
            )
            logging.debug("No validation errors")
            return Config(parse_config)
        except yaml.YAMLError as exc:
            logging.error(exc)
        except ValidationError as exc:
            logging.error(exc)
        return NoOpConfig