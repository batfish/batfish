package org.batfish.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.batfish.common.BatfishException;

/**
 * Captures one "row" of the result when a JsonPath query is evaluated. Each row has a prefix
 * portion and a suffix portion. The prefix portion is what the sequence of components that the
 * query matched and the suffix is the value of the last matched component.
 */
public class JsonPathResult {

  private final List<String> _concretePath;

  private final JsonNode _suffix;

  public JsonPathResult(List<String> concretePath, JsonNode suffix) {
    _concretePath = concretePath;
    if (suffix != null && suffix.isNull()) {
      _suffix = null;
    } else {
      _suffix = suffix;
    }
  }

  public JsonPathResult(JsonNode prefix, JsonNode suffix) {
    this(getPrefixParts(prefix), suffix);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JsonPathResult)) {
      return false;
    }
    return Objects.equals(_concretePath, ((JsonPathResult) o)._concretePath)
        && Objects.equals(_suffix, ((JsonPathResult) o)._suffix);
  }

  /**
   * Return a component of the prefix part of the result
   *
   * @param index The index of the requested component
   * @return The component
   */
  public String getPrefixPart(int index) {
    if (_concretePath.size() <= index) {
      throw new BatfishException(
          "No valid part at index " + index + "for concrete path " + _concretePath);
    }
    // remove the single quotes around the string
    return _concretePath.get(index).replaceAll("^\'|\'$", "");
  }

  private static List<String> getPrefixParts(JsonNode prefix) {
    String text = prefix.textValue();
    if (text.equals("$")) {
      return Collections.singletonList("$");
    }
    if (text.length() < 2) {
      throw new BatfishException("Unexpected prefix " + text);
    }
    String endsCut = text.substring(2, text.length() - 1);
    return Arrays.asList(endsCut.split("\\]\\["));
  }

  /**
   * Returns the suffix of the result
   *
   * @return The suffix
   */
  public JsonNode getSuffix() {
    return _suffix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_concretePath, _suffix);
  }

  /** Tells us if the suffix is null or empty */
  public boolean isNullOrEmptySuffix() {
    return _suffix == null || (_suffix.isArray() && _suffix.size() == 0);
  }
}
