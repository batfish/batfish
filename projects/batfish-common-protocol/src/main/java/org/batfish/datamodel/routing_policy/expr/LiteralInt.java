package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralInt extends IntExpr {

  private static final String PROP_VALUE = "value";

  /** */
  private static final long serialVersionUID = 1L;

  private int _value;

  @JsonCreator
  private LiteralInt() {}

  public LiteralInt(int value) {
    _value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LiteralInt other = (LiteralInt) obj;
    if (_value != other._value) {
      return false;
    }
    return true;
  }

  @Override
  public int evaluate(Environment environment) {
    return _value;
  }

  @JsonProperty(PROP_VALUE)
  public int getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _value;
    return result;
  }

  @JsonProperty(PROP_VALUE)
  public void setValue(int value) {
    _value = value;
  }
}
