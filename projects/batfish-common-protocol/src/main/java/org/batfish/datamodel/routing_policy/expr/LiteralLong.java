package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.batfish.datamodel.routing_policy.Environment;

public final class LiteralLong extends LongExpr {
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
    } else if (!(obj instanceof LiteralLong)) {
      return false;
    }
    LiteralLong other = (LiteralLong) obj;
    return _value == other._value;
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
    return Long.hashCode(_value);
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
