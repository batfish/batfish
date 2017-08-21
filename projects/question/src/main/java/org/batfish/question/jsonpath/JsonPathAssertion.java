package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

public class JsonPathAssertion {

  private static final String PROP_TYPE = "type";

  private static final String PROP_EXPECT = "expect";

  private JsonPathAssertionType _assertionType;

  private JsonNode _expect;

  public boolean evaluate(ArrayNode suffixes) {
    switch (getType()) {
    case count:
      if (!_expect.isInt()) {
        throw new BatfishException("Expected value of assertion type count ("
            + _expect.toString() + ") is not an integer");
      }
      return (suffixes.size() == _expect.asInt());
    case equal:
      if (!_expect.isArray()) {
        throw new BatfishException("Expected value of assertion type equal ("
            + _expect.toString() + ") is not an JSON list");
      }
      return _expect.equals(suffixes);
    case none:
      throw new BatfishException("Cannot evaluate assertion type none");
    default:
      throw new BatfishException("Unhandled assertion type: " + getType());
    }
  }

  @JsonProperty(PROP_TYPE)
  public JsonPathAssertionType getType() {
    return _assertionType;
  }

  @JsonProperty(PROP_EXPECT)
  public JsonNode getExpect() {
    return _expect;
  }

  @JsonProperty(PROP_TYPE)
  public void setType(JsonPathAssertionType assertionType) {
    _assertionType = assertionType;
  }

  @JsonProperty(PROP_EXPECT)
  public void setExpect(JsonNode expect) {
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
