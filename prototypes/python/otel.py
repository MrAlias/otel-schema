
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
        
    def set_tracer_provider(self):
        from opentelemetry.sdk.trace import TracerProvider
        provider = TracerProvider(resource=self._resource())
        from opentelemetry.sdk.trace.export import BatchSpanProcessor, ConsoleSpanExporter

        processor = BatchSpanProcessor(ConsoleSpanExporter())
        provider.add_span_processor(processor)
        set_tracer_provider(provider)

    def set_meter_provider(self):
        pass

    def apply(self):
        logging.debug("applying configuration %s", self._config)
        if self._config is None or self._config.get("sdk").get("disabled"):
            logging.debug("sdk disabled, nothing to apply")
            # do nothing
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