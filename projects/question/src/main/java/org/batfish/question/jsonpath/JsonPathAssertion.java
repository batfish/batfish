package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

public class JsonPathAssertion {

  private static final String PROP_ASSERTION_TYPE = "type";

  private static final String PROP_ASSERTION_EXPECT = "expect";

  private JsonPathAssertionType _assertionType;

  private JsonNode _expect;

  public boolean evaluate(ArrayNode suffixes) {
    switch (getAssertionType()) {
    case count:
      if (_expect.isInt()) {
        boolean match = (suffixes.size() == _expect.asInt());
        return match;
      } else {
        throw new BatfishException("Expected value of assertion type count ("
            + _expect.toString() + ") is not an integer");
      }
    case equal:
      if (_expect.isArray()) {
        boolean match = _expect.equals(suffixes);
        return match;
      } else {
        throw new BatfishException("Expected value of assertion type equal ("
            + _expect.toString() + ") is not an JSON list");
      }

    default:
      throw new BatfishException("Unhandled assertion type: " + getAssertionType());
    }
  }

  @JsonProperty(PROP_ASSERTION_TYPE)
  public JsonPathAssertionType getAssertionType() {
    return _assertionType;
  }

  @JsonProperty(PROP_ASSERTION_EXPECT)
  public JsonNode getExpect() {
    return _expect;
  }

  @JsonProperty(PROP_ASSERTION_TYPE)
  public void setAssertionType(JsonPathAssertionType assertionType) {
    _assertionType = assertionType;
  }

  @JsonProperty(PROP_ASSERTION_EXPECT)
  public void setRhs(JsonNode expect) {
    _expect = expect;
  }

  @Override
  public String toString() {
    BatfishObjectMapper mapper = new BatfishObjectMapper(false);
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Could not map JsonPathAssertion to JSON string", e);
    }
  }
}
