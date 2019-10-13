package org.batfish.representation.palo_alto;

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
}
