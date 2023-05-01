package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralLong extends LongExpr {
  private static final String PROP_VALUE = "value";

  private long _value;

  @JsonCreator
  private LiteralLong() {}

  public LiteralLong(long value) {
    _value = value;
  }

  @Override
  public <T, U> T accept(LongExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLiteralLong(this, arg);
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
    LiteralLong other = (LiteralLong) obj;
    if (_value != other._value) {
      return false;
    }
    return true;
  }

  @Override
  public long evaluate(Environment environment) {
    return _value;
  }

  @JsonProperty(PROP_VALUE)
  public long getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_value ^ (_value >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("value", _value).toString();
  }

  @JsonProperty(PROP_VALUE)
  public void setValue(long value) {
    _value = value;
  }
}
