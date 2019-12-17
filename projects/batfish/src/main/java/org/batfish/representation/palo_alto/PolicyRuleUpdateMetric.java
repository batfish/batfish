package org.batfish.representation.palo_alto;

import java.util.Objects;

/** A {@link PolicyRuleUpdate} that updates the metric/MED attribute of a route. */
public final class PolicyRuleUpdateMetric implements PolicyRuleUpdate {

  private final long _metric;

  public PolicyRuleUpdateMetric(long metric) {
    _metric = metric;
  }

  @Override
  public <T> T accept(PolicyRuleUpdateVisitior<T> visitor) {
    return visitor.visitPolicyRuleUpdateMetric(this);
  }

  public long getMetric() {
    return _metric;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PolicyRuleUpdateMetric)) {
      return false;
    }
    PolicyRuleUpdateMetric that = (PolicyRuleUpdateMetric) o;
    return _metric == that._metric;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_metric);
  }
}
