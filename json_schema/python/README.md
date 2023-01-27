# json schema validation with Python

The code in this directory shows an example of using jsonschema validation in combination with a yaml parser in Python.

```bash
$ python3 -m venv .venv
$ source .venv/bin/activate
$ pip install -r requirements.txt
$ python validate.py
Using config file: ../kitchen-sink.yaml
YAML parsed successfully
Validating using schema file: ../schema/schema.json
No validation errors
```

To test the validation, update any values in [kitchen-sink.yaml](../kitchen-sink.yaml) to invalid values and an error will be printed.

```bash
# change a bool value to a string value
$ sed -i '' 's/disabled: false/disabled: invalid-val/g' ../kitchen-sink.yaml
$ python validate.py
Using config file: ../kitchen-sink.yaml
YAML parsed successfully
Validating using schema file: ../schema/schema.json
'invalid-val' is not of type 'boolean'

Failed validating 'type' in schema['properties']['sdk']['properties']['disabled']:
    {'type': 'boolean'}

On instance['sdk']['disabled']:
    'invalid-val'
```
