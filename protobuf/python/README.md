
## Usage

```shell
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
protoc -I=schema --python_out=. schema/sdk.proto
python validate.py
```

## Observations

- Maps cannot have mixed/variant data types, e.g.

When defined as
```protobuf
message {
   map<string, string> headers = 1;
}
```

All values must be string, so timeout value has to be quoted:
```yaml
headers:
   api_key: abcd
   timeout: "1000"
```

- Maps do not support `oneof`, so something like this cannot be expressed as map of different types of messages. 

```yaml
exporters:
   otlp:
      endpoint: http://some_url
      protocol: http/protobuf
   zipkin:
      endpoint: http://another_url
      timeout: 1000
```
Instead, each exporter type will have to be defined as a separate field of the Exporters message:

```protobuf
message Exporters {
   OtlpExporter otlp = 1;
   ZipkinExporter zipkin = 2;
}
```

- There is no way to define required fields.
