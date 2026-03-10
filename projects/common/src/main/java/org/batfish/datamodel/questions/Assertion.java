package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

/**
 * Represents an assertion that can be added to questions. It is evaluated by the corresponding
 * {@link org.batfish.common.Answerer}.
 */
public class Assertion {

  public enum AssertionType {
    countequals,
    countlessthan,
    countmorethan,
    equals
  }

  private static final String PROP_TYPE = "type";
  private static final String PROP_EXPECT = "expect";

  private AssertionType _assertionType;

  private JsonNode _expect;

  @JsonCreator
  public Assertion(
      @JsonProperty(PROP_TYPE) AssertionType assertionType,
      @JsonProperty(PROP_EXPECT) JsonNode expect) {
    switch (assertionType) {
      case countequals:
      case countlessthan:
      case countmorethan:
        if (!expect.isInt()) {
          throw new IllegalArgumentException(
              String.format(
                  "Value '%s' of assertion type '%s' is not an integer", expect, assertionType));
        }
        break;
      case equals:
        if (!expect.isArray()) {
          throw new IllegalArgumentException(
              String.format(
                  "Value '%s' of assertion type '%s' is not a list", expect, assertionType));
        }
        break;
    }
    _assertionType = assertionType;
    _expect = expect;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Assertion)) {
      return false;
    }
    Assertion other = (Assertion) o;
    return Objects.equals(_assertionType, other.getType())
        && Objects.equals(_expect, other.getExpect());
  }

  @JsonProperty(PROP_TYPE)
  public AssertionType getType() {
    return _assertionType;
  }

  @JsonProperty(PROP_EXPECT)
  public JsonNode getExpect() {
    return _expect;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_assertionType, _expect);
  }

  @Override
  public String toString() {
    try {
      return BatfishObjectMapper.writePrettyString(this);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Cannot serialize to Json", e);
    }
  }
}
