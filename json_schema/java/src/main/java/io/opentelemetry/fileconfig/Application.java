package io.opentelemetry.fileconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

public class Application {

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length != 1) {
      throw new IllegalArgumentException("Missing file to validate.");
    }

    File file = new File(args[0]);
    if (!file.exists()) {
      throw new IllegalArgumentException("File does not exist: " + args[0]);
    }

    String schemaFilePath = System.getenv("SCHEMA_FILE");
    if (schemaFilePath == null) {
      throw new IllegalArgumentException("Env var is required: SCHEMA_FILE");
    }
    File schemaFile = new File(schemaFilePath);
    if (!schemaFile.exists()) {
      throw new IllegalArgumentException("Specified SCHEMA_FILE does not exist");
    }

    YamlJsonSchemaValidator validator = new YamlJsonSchemaValidator(schemaFile);
    Set<String> results = validator.validate(new FileInputStream(file));
    if (results.isEmpty()) {
      System.out.println("Schema successfully validated.");
    } else {
      System.out.println("Error(s) detected validating schema: ");
      results.stream().map(r -> "\t" + r).forEach(System.out::println);
    }
  }
}
