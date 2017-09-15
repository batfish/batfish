package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.question.jsonpath.JsonPathResult.JsonPathResultEntry;

public class JsonPathAssertion {

  private static final String PROP_TYPE = "type";

  private static final String PROP_EXPECT = "expect";

  private JsonPathAssertionType _assertionType;

  private JsonNode _expect;

  public boolean evaluate(Set<JsonPathResultEntry> resultEntries) {
    switch (getType()) {
      case count:
        if (!_expect.isInt()) {
          throw new BatfishException(
              "Expected value of assertion type count ("
                  + _expect.toString()
                  + ") is not an integer");
        }
        return resultEntries.size() == _expect.asInt();
      case equals:
        if (!_expect.isArray()) {
          throw new BatfishException(
              "Expected value of assertion type equals (" + _expect.toString() + ") is not a list");
        }
        Set<JsonPathResultEntry> expectedEntries = new HashSet<>();
        for (final JsonNode nodeEntry : _expect) {
          try {
            BatfishObjectMapper mapper = new BatfishObjectMapper();
            JsonPathResultEntry expectedEntry =
                mapper.readValue(nodeEntry.toString(), JsonPathResultEntry.class);
            expectedEntries.add(expectedEntry);
          } catch (IOException e) {
            throw new BatfishException(
                "Could not convert '" + nodeEntry.toString() + "' to JsonPathResultEntry", e);
          }
        }
        SetView<JsonPathResultEntry> difference1 = Sets.difference(expectedEntries, resultEntries);
        SetView<JsonPathResultEntry> difference2 = Sets.difference(resultEntries, expectedEntries);
        return difference1.isEmpty() && difference2.isEmpty();
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
