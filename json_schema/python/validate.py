#!/usr/bin/env python

import yaml
from pathlib import Path
from jsonschema import validate, validators

path = Path(__file__).parent.resolve()
resolver = validators.RefResolver(
    base_uri=f"{path.as_uri()}/",
    referrer=True,
)

with open("../kitchen-sink.yaml", "r") as stream:
    try:
        parse_config = yaml.safe_load(stream)
        result = validate(
            instance=parse_config,
            schema={"$ref": "../schema/schema.json"},
            resolver=resolver,
        )
        print(result)
    except yaml.YAMLError as exc:
        print(exc)
