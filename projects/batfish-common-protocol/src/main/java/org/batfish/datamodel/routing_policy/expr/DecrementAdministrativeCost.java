package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.AbstractRoute.MAX_ADMIN_DISTANCE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * Expression representing a decremented administrative cost. The result is clipped to the specified
 * minimum value (typically 0).
 */
public final class DecrementAdministrativeCost extends AdministrativeCostExpr {

  private static final String PROP_SUBTRAHEND = "subtrahend";
  private static final String PROP_MIN = "min";

  private final long _subtrahend;
  private final long _min;

  @JsonCreator
  private static DecrementAdministrativeCost create(
      @JsonProperty(PROP_SUBTRAHEND) long subtrahend, @JsonProperty(PROP_MIN) long min) {
    return new DecrementAdministrativeCost(subtrahend, min);
  }

  public DecrementAdministrativeCost(long subtrahend, long min) {
    checkArgument(
        subtrahend >= 0,
        "To subtract a negative number (%s), use IncrementAdministrativeCost",
        subtrahend);
    checkArgument(
        subtrahend <= MAX_ADMIN_DISTANCE,
        "Value (%s) is out of range for administrative cost",
        subtrahend);
    checkArgument(
        min >= 0 && min <= MAX_ADMIN_DISTANCE,
        "Min value (%s) is out of range [0,%s]",
        min,
        MAX_ADMIN_DISTANCE);
    _subtrahend = subtrahend;
    _min = min;
  }

  @Override
  public long evaluate(Environment environment) {
    long oldCost = environment.getOriginalRoute().getAdministrativeCost();
    // Clip to _min to prevent underflow
    if (oldCost < _min + _subtrahend) {
      return _min;
    }
    return oldCost - _subtrahend;
  }

  @JsonProperty(PROP_SUBTRAHEND)
  public long getSubtrahend() {
    return _subtrahend;
  }

  @JsonProperty(PROP_MIN)
  public long getMin() {
    return _min;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof DecrementAdministrativeCost)) {
      return false;
    }
    DecrementAdministrativeCost other = (DecrementAdministrativeCost) obj;
    return _subtrahend == other._subtrahend && _min == other._min;
  }

  @Override
  public int hashCode() {
    int result = Long.hashCode(_subtrahend);
    result = 31 * result + Long.hashCode(_min);
    return result;
  }
}
