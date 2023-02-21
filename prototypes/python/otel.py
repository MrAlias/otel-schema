
import logging
from typing import List, Sequence, Tuple
from pkg_resources import iter_entry_points


import yaml
from pathlib import Path
from jsonschema import validate, validators
from jsonschema.exceptions import ValidationError

from opentelemetry.metrics import set_meter_provider
from opentelemetry.trace import set_tracer_provider
from opentelemetry.sdk.trace.export import SpanExporter
from opentelemetry.sdk.metrics.export import MetricExporter


# borromed from opentelemetry/sdk/_configuration
def _import_config_component(
    selected_component: str, entry_point_name: str
) -> object:
    component_entry_points = {
        ep.name: ep for ep in iter_entry_points(entry_point_name)
    }
    entry_point = component_entry_points.get(selected_component, None)
    if not entry_point:
        raise RuntimeError(
            f"Requested component '{selected_component}' not found in entry points for '{entry_point_name}'"
        )

    return entry_point.load()

class Config:
    def __init__(self, config=None) -> None:
        self._config = config
    
    def _resource(self):
        # resource detection
        # attributes
        from opentelemetry.sdk.resources import Resource
        return Resource.create(self._config.get("sdk").get("resource").get("attributes"))

    # _get_exporter returns an exporter class for the signal
    def _get_exporter(self, signal: str, name: str):
        if name not in self._config.get("sdk").get(signal).get("exporters"):
            raise Exception(f"exporter {name} not specified for {signal} signal")
        
        exporter = _import_config_component(name, f"opentelemetry_{signal}_exporter")
        if signal == "metrics":
            cls_type = MetricExporter
        elif signal == "traces":
            cls_type = SpanExporter
        elif signal == "logs":
            cls_type = SpanExporter
        if issubclass(exporter, cls_type):
            if self._config.get("sdk").get(signal).get("exporters").get(name) is not None:
                return exporter(**self._config.get("sdk").get(signal).get("exporters").get(name))
            return exporter()
        raise RuntimeError(f"{name} is not a {signal} exporter")

    def configure_tracing(self, cfg):
        if cfg is None:
            return
        from opentelemetry.sdk.trace import TracerProvider
        provider = TracerProvider(resource=self._resource())
        from opentelemetry.sdk.trace.export import BatchSpanProcessor

        for processor in cfg.get("span_processors"):
            logging.debug("adding span processor %s", processor)
            try:
                processor = BatchSpanProcessor(self._get_exporter("traces", processor.get("args").get("exporter")))
                provider.add_span_processor(processor)
            except ModuleNotFoundError as exc:
                logging.error("module not found", exc)
        set_tracer_provider(provider)

    def configure_metrics(self, cfg):
        if cfg is None:
            return
        from opentelemetry.sdk.metrics import MeterProvider
        readers = []
        for reader in cfg.get("metric_readers"):
            if reader.get("type") == "periodic":
                from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader
                readers.append(PeriodicExportingMetricReader(self._get_exporter("metrics", reader.get("args").get("exporter"))))
        provider = MeterProvider(resource=self._resource(), metric_readers=readers)
        set_meter_provider(provider)

    def apply(self):
        logging.debug("applying configuration %s", self._config)
        if self._config is None or self._config.get("sdk").get("disabled"):
            logging.debug("sdk disabled, nothing to apply")
            return
        self.configure_tracing(self._config.get("sdk").get("traces"))
        self.configure_metrics(self._config.get("sdk").get("metrics"))


NoOpConfig = Config()

def configure(configuration: Config) -> None:
    configuration.apply()

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