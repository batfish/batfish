package org.batfish.representation.palo_alto;

import java.util.Objects;
import javax.annotation.Nullable;

public final class PolicyRuleUpdateWeight implements PolicyRuleUpdate {

  public PolicyRuleUpdateWeight(int weight) {
    _weight = weight;
  }

  public int getWeight() {
    return _weight;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PolicyRuleUpdateWeight)) {
      return false;
    }
    PolicyRuleUpdateWeight that = (PolicyRuleUpdateWeight) o;
    return _weight == that._weight;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_weight);
  }

  @Override
  public <T> T accept(PolicyRuleUpdateVisitior<T> visitor) {
    return visitor.visitPolicyRuleUpdateWeight(this);
  }

  private final int _weight;
}
