# JSON Schema validation with Java

```shell
// Set environment variable pointing to location of schema file
export SCHEMA_FILE=/Users/jberg/code/rAlias/otel-schema/json_schema/schema/schema.json

// Run the validation, passing the location of the file to validate
./gradlew run --args="/Users/jberg/code/rAlias/otel-schema/config.yaml"
```