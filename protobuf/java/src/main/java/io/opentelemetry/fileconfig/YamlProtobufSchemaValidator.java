package io.opentelemetry.fileconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;

class YamlProtobufSchemaValidator {

  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  private static final Yaml YAML = new Yaml();

  YamlProtobufSchemaValidator() {}

  Message validate(InputStream yaml, Message.Builder builder) {
    Object yamlObj = YAML.load(yaml);
    try {
      String jsonStr = JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(yamlObj);
      JsonFormat.parser().ignoringUnknownFields().merge(jsonStr, builder);
      return builder.build();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to parse yaml", e);
    }
  }
}
