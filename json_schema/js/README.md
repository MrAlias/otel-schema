
This is a basic prototype of validating and applying an OTel configuration file.

It is limited to configuring simple and batch span processors with console or OTLP exporters. Most configuration options are not handled. This prototype also does not currently include metrics or logging.

## Run

```shell
npm i
node index.js
```

## Schema validation

There are a number of libraries for JSON schema validation. The most popular seems to be [ajv](https://www.npmjs.com/package/ajv), but I had trouble getting to validate the URI fields as defined in the schema. For now, this prototype is using [json-schema-library](https://www.npmjs.com/package/json-schema-library), which seems to work well.
