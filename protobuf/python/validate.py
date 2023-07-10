import yaml
from google.protobuf.json_format import Parse, ParseDict
from google.protobuf.message import DecodeError
import sdk_pb2

with open("kitchen-sink.yaml") as yaml_in:
    yaml_object = yaml.safe_load(yaml_in)

    try:
        sdkConfig = ParseDict(yaml_object, sdk_pb2.Config())
        print("Success")
    except (DecodeError, yaml.YAMLError) as e:
        print(f"Error: {e}")
