package org.batfish.datamodel.answers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;

public final class SchemaUtils {

  private SchemaUtils() {}

  /**
   * Converts {@code jsonNode} to an object with Schema of {@code schema}
   *
   * @return The converted object
   * @throws ClassCastException if the conversion fails
   */
  public static @Nullable Object convertType(JsonNode jsonNode, Schema schema) {
    if (jsonNode == null || jsonNode.isNull()) {
      return null;
    }
    try {
      return switch (schema.getType()) {
        case BASE -> convertType(jsonNode, schema.getBaseType());
        case LIST -> {
          List<JsonNode> list =
              BatfishObjectMapper.mapper()
                  .readValue(
                      BatfishObjectMapper.mapper().treeAsTokens(jsonNode),
                      new TypeReference<List<JsonNode>>() {});
          yield list.stream()
              .map(in -> convertType(in, schema.getInnerSchema()))
              .collect(Collectors.toList());
        }
        case SET -> {
          Set<JsonNode> set =
              BatfishObjectMapper.mapper()
                  .readValue(
                      BatfishObjectMapper.mapper().treeAsTokens(jsonNode),
                      new TypeReference<Set<JsonNode>>() {});
          yield set.stream()
              .map(in -> convertType(in, schema.getInnerSchema()))
              .collect(Collectors.toSet());
        }
      };
    } catch (IOException e) {
      throw new ClassCastException(
          String.format(
              "Cannot recover object of schema %s from json %s: %s\n%s",
              schema, jsonNode, e.getMessage(), Throwables.getStackTraceAsString(e)));
    }
  }

  /**
   * Converts {@code jsonNode} to class of {@code valueType}
   *
   * @return The converted object
   * @throws ClassCastException if the conversion fails
   */
  private static <T> T convertType(JsonNode jsonNode, Class<T> valueType) {
    try {
      return BatfishObjectMapper.mapper().treeToValue(jsonNode, valueType);
    } catch (JsonProcessingException e) {
      throw new ClassCastException(
          String.format(
              "Cannot recover object of type '%s' from json %s: %s\n%s",
              valueType.getName(), jsonNode, e.getMessage(), Throwables.getStackTraceAsString(e)));
    }
  }

  /**
   * Checks if {@code object} can be cast to {@code schema}.
   *
   * <p>The function operates by converting the object to Json and then converting it back.
   */
  public static boolean isValidObject(Object object, Schema schema) {
    JsonNode jsonNode = BatfishObjectMapper.mapper().valueToTree(object);
    try {
      convertType(jsonNode, schema);
    } catch (ClassCastException e) {
      return false;
    }
    return true;
  }
}
