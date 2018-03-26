package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.table.Rows;

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
      default:
        throw new IllegalArgumentException("Unhandled assertion type: " + assertionType);
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
    return Objects.equal(_assertionType, other.getType())
        && Objects.equal(_expect, other.getExpect());
  }

  public boolean evaluate(Rows rows) {
    switch (getType()) {
      case countequals:
        return rows.size() == _expect.asInt();
      case countlessthan:
        return rows.size() < _expect.asInt();
      case countmorethan:
        return rows.size() > _expect.asInt();
      case equals:
        Rows expectedEntries;
        try {
          expectedEntries = BatfishObjectMapper.mapper().readValue(_expect.toString(), Rows.class);
        } catch (IOException e) {
          throw new BatfishException("Could not recover Set<ObjectNode> from expect", e);
        }
        return rows.equals(expectedEntries);
      default:
        throw new BatfishException("Unhandled assertion type: " + getType());
    }
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
    return java.util.Objects.hash(_assertionType, _expect);
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
