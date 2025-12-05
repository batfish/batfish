package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.AbstractRoute.MAX_ADMIN_DISTANCE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * Expression representing an incremented administrative cost. The result is clipped to the
 * specified maximum value (typically vendor-specific).
 */
public final class IncrementAdministrativeCost extends AdministrativeCostExpr {

  private static final String PROP_ADDEND = "addend";
  private static final String PROP_MAX = "max";

  private final long _addend;
  private final long _max;

  @JsonCreator
  private static IncrementAdministrativeCost create(
      @JsonProperty(PROP_ADDEND) long addend, @JsonProperty(PROP_MAX) long max) {
    return new IncrementAdministrativeCost(addend, max);
  }

  public IncrementAdministrativeCost(long addend, long max) {
    checkArgument(
        addend >= 0, "To add a negative number (%s), use DecrementAdministrativeCost", addend);
    checkArgument(
        addend <= MAX_ADMIN_DISTANCE, "Value (%s) is out of range for administrative cost", addend);
    checkArgument(
        max >= 0 && max <= MAX_ADMIN_DISTANCE,
        "Max value (%s) is out of range [0,%s]",
        max,
        MAX_ADMIN_DISTANCE);
    _addend = addend;
    _max = max;
  }

  @Override
  public long evaluate(Environment environment) {
    AbstractRoute originalRoute = environment.getOriginalRoute();
    if (originalRoute == null) {
      return Math.min(_addend, _max);
    }
    long oldCost = originalRoute.getAdministrativeCost();
    // Clip to _max to prevent overflow
    if (oldCost > _max - _addend) {
      return _max;
    }
    return oldCost + _addend;
  }

  @JsonProperty(PROP_ADDEND)
  public long getAddend() {
    return _addend;
  }

  @JsonProperty(PROP_MAX)
  public long getMax() {
    return _max;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof IncrementAdministrativeCost)) {
      return false;
    }
    IncrementAdministrativeCost other = (IncrementAdministrativeCost) obj;
    return _addend == other._addend && _max == other._max;
  }

  @Override
  public int hashCode() {
    int result = Long.hashCode(_addend);
    result = 31 * result + Long.hashCode(_max);
    return result;
  }
}
