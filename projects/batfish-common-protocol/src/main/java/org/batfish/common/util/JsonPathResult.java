package org.batfish.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.batfish.common.BatfishException;

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
      return Arrays.asList("$");
    }
    if (text.length() < 2) {
      throw new BatfishException("Unexpected prefix " + text);
    }
    String endsCut = text.substring(2, text.length() - 1);
    return Arrays.asList(endsCut.split("\\]\\["));
  }

  public JsonNode getSuffix() {
    return _suffix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_concretePath, _suffix);
  }
}
