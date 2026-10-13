package org.batfish.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Checks requests and responses against {@code openapi.yaml}. Covers the JSON Schema subset the
 * contract uses: {@code $ref}, {@code type}, {@code required}, {@code properties}, {@code
 * additionalProperties}, {@code items}, {@code minItems}, {@code enum} and {@code pattern}.
 */
final class OpenApiContract {

  private final JsonNode _spec;

  private OpenApiContract(JsonNode spec) {
    _spec = spec;
  }

  static OpenApiContract load() {
    Path path = Paths.get("projects/engine/openapi.yaml");
    try {
      return new OpenApiContract(new ObjectMapper(new YAMLFactory()).readTree(path.toFile()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Errors in {@code body} as the response of {@code method template} with {@code status}. */
  List<String> checkResponse(String template, String method, int status, JsonNode body) {
    JsonNode op = operation(template, method);
    JsonNode response = op.path("responses").path(Integer.toString(status));
    if (response.isMissingNode()) {
      return List.of(method + " " + template + " does not document status " + status);
    }
    response = resolve(response);
    JsonNode schema = response.path("content").path("application/json").path("schema");
    if (schema.isMissingNode()) {
      return body == null || body.isMissingNode()
          ? List.of()
          : List.of(method + " " + template + " " + status + " documents no body");
    }
    List<String> errors = new ArrayList<>();
    validate(schema, body, "$", errors);
    return errors;
  }

  /** Errors in {@code body} as the request of {@code method template}. */
  List<String> checkRequest(String template, String method, JsonNode body) {
    JsonNode schema =
        operation(template, method)
            .path("requestBody")
            .path("content")
            .path("application/json")
            .path("schema");
    List<String> errors = new ArrayList<>();
    validate(schema, body, "$", errors);
    return errors;
  }

  private JsonNode operation(String template, String method) {
    JsonNode op = _spec.path("paths").path(template).path(method.toLowerCase());
    if (op.isMissingNode()) {
      throw new IllegalArgumentException("no operation " + method + " " + template);
    }
    return op;
  }

  private JsonNode resolve(JsonNode node) {
    while (node.has("$ref")) {
      String[] parts = node.get("$ref").asText().substring(2).split("/");
      JsonNode target = _spec;
      for (String p : parts) {
        target = target.path(p);
      }
      node = target;
    }
    return node;
  }

  private void validate(JsonNode schema, JsonNode value, String at, List<String> errors) {
    schema = resolve(schema);
    if (value == null || value.isNull()) {
      errors.add(at + ": null");
      return;
    }
    if (schema.has("enum")) {
      boolean match = false;
      for (JsonNode e : schema.get("enum")) {
        match |= e.equals(value);
      }
      if (!match) {
        errors.add(at + ": " + value + " not in " + schema.get("enum"));
      }
    }
    String type = schema.path("type").asText("");
    switch (type) {
      case "object" -> validateObject(schema, value, at, errors);
      case "array" -> {
        if (!value.isArray()) {
          errors.add(at + ": expected array");
          return;
        }
        if (schema.has("minItems") && value.size() < schema.get("minItems").asInt()) {
          errors.add(at + ": fewer than " + schema.get("minItems") + " items");
        }
        for (int i = 0; i < value.size(); i++) {
          validate(schema.path("items"), value.get(i), at + "[" + i + "]", errors);
        }
      }
      case "string" -> {
        if (!value.isTextual()) {
          errors.add(at + ": expected string");
        } else if (schema.has("pattern")
            && !Pattern.compile(schema.get("pattern").asText()).matcher(value.asText()).find()) {
          errors.add(at + ": does not match " + schema.get("pattern").asText());
        }
      }
      case "integer" -> {
        if (!value.isIntegralNumber()) {
          errors.add(at + ": expected integer");
        }
      }
      case "number" -> {
        if (!value.isNumber()) {
          errors.add(at + ": expected number");
        }
      }
      case "boolean" -> {
        if (!value.isBoolean()) {
          errors.add(at + ": expected boolean");
        }
      }
      default -> {}
    }
  }

  private void validateObject(JsonNode schema, JsonNode value, String at, List<String> errors) {
    if (!value.isObject()) {
      errors.add(at + ": expected object");
      return;
    }
    for (JsonNode required : schema.path("required")) {
      if (!value.has(required.asText())) {
        errors.add(at + ": missing " + required.asText());
      }
    }
    JsonNode properties = schema.path("properties");
    JsonNode additional = schema.path("additionalProperties");
    Iterator<Map.Entry<String, JsonNode>> fields = value.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> f = fields.next();
      String child = at + "." + f.getKey();
      if (properties.has(f.getKey())) {
        validate(properties.get(f.getKey()), f.getValue(), child, errors);
      } else if (additional.isBoolean() && !additional.asBoolean()) {
        errors.add(child + ": not allowed");
      } else if (additional.isObject()) {
        validate(additional, f.getValue(), child, errors);
      }
    }
  }
}
