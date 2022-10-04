package io.opentelemetry.fileconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class SchemaValidationExtension {

  private final YamlJsonSchemaValidator validator;

  SchemaValidationExtension(String relativeSchemaFile) {
    File schemaDirectory = new File(System.getProperty("SCHEMA_DIR"));

    String baseUri = "https://opentelemetry.io/schemas/sdkconfig";
    Map<String, String> uriMapping = new HashMap<>();
    for (File schema : schemaDirectory.listFiles()) {
      uriMapping.put(
          baseUri + "/" + schema.getName().split("\\.")[0], schema.toURI().toASCIIString());
    }

    validator = new YamlJsonSchemaValidator(schemaDirectory, relativeSchemaFile, uriMapping);
  }

  Set<String> validateFile(File file) {
    FileInputStream fis;
    try {
      fis = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Unable to create input stream from file", e);
    }
    return validator.validate(fis);
  }

  Set<String> validateResource(String relativeResourcePath) {
    InputStream inputStream;
    try {
      URI uri = YamlJsonSchemaValidator.class.getResource(relativeResourcePath).toURI();
      inputStream = new FileInputStream(new File(uri));
    } catch (URISyntaxException | IOException e) {
      throw new IllegalArgumentException("Unable to load resource file", e);
    }
    return validator.validate(inputStream);
  }
}
