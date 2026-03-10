package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.AbstractRoute.MAX_ADMIN_DISTANCE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.routing_policy.Environment;

/** Expression representing a literal administrative cost value. */
public final class LiteralAdministrativeCost extends AdministrativeCostExpr {

  private static final String PROP_VALUE = "value";

  private final long _value;

  @JsonCreator
  private static LiteralAdministrativeCost create(@JsonProperty(PROP_VALUE) long value) {
    return new LiteralAdministrativeCost(value);
  }

  public LiteralAdministrativeCost(long value) {
    checkArgument(
        value >= 0 && value <= MAX_ADMIN_DISTANCE,
        "Value (%s) is out of range for administrative cost [0,%s]",
        value,
        MAX_ADMIN_DISTANCE);
    _value = value;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof LiteralAdministrativeCost)) {
      return false;
    }
    LiteralAdministrativeCost other = (LiteralAdministrativeCost) obj;
    return _value == other._value;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_value);
  }
}
