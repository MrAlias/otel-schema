#!/usr/bin/env python

import yaml
from pathlib import Path
from jsonschema import validate, validators
from jsonschema.exceptions import ValidationError

path = Path(__file__).parent.resolve()
resolver = validators.RefResolver(
    base_uri=f"{path.as_uri()}/",
    referrer=True,
)

config_file = "../kitchen-sink.yaml"
print(f"Using config file: {config_file}")
with open(config_file, "r") as stream:
    try:
        parse_config = yaml.safe_load(stream)
        print("YAML parsed successfully")
        schema_file = "../schema/schema.json"
        print(f"Validating using schema file: {schema_file}")
        result = validate(
            instance=parse_config,
            schema={"$ref": schema_file},
            resolver=resolver,
        )
        print("No validation errors")
    except yaml.YAMLError as exc:
        print(exc)
    except ValidationError as exc:
        print(exc)
