package io.opentelemetry.fileconfig;

import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

class YamlJsonSchemaValidator {

  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
  private static final Yaml YAML = new Yaml();

  private final JsonSchema jsonSchema;

  /**
   * Create a schema validator for a JSON schema.
   *
   * @param schemaDirectory the directory with JSON Schema files
   * @param schemaFile the specific schema to validate with {@link #validate(InputStream)}
   * @param uriMapping map of URI lookup overrides
   */
  YamlJsonSchemaValidator(File schemaDirectory, String schemaFile, Map<String, String> uriMapping) {
    JsonSchemaFactory jsonSchemaFactory =
        JsonSchemaFactory.builder(
                // V202012 has a bug where items are not validated
                JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909))
            .objectMapper(MAPPER)
            .addUriMappings(uriMapping)
            .build();
    try {
      jsonSchema =
          jsonSchemaFactory.getSchema(
              new FileInputStream(schemaDirectory.getAbsolutePath() + "/" + schemaFile));
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to initialize validator", e);
    }
  }

  /**
   * Validate the input {@code yaml} against the JSON schema, returning a set of validation issues.
   * A valid schema will return an empty set.
   */
  Set<String> validate(InputStream yaml) {
    // Load yaml and write it as string to resolve anchors
    Object yamlObj = YAML.load(yaml);
    try {
      String yamlStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(yamlObj);
      return jsonSchema.validate(MAPPER.readTree(yamlStr)).stream()
          .map(ValidationMessage::toString)
          .collect(toSet());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to parse yaml", e);
    }
  }
}
