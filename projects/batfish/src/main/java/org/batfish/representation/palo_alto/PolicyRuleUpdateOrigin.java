package org.batfish.representation.palo_alto;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.OriginType;

/** A {@link PolicyRuleUpdateOrigin} that sets the origin type. */
public final class PolicyRuleUpdateOrigin implements PolicyRuleUpdate {

  private final @Nonnull OriginType _origin;

  public PolicyRuleUpdateOrigin(OriginType origin) {
    _origin = origin;
  }

  @Override
  public <T> T accept(PolicyRuleUpdateVisitior<T> visitor) {
    return visitor.visitPolicyRuleUpdateOrigin(this);
  }

  public @Nonnull OriginType getOrigin() {
    return _origin;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PolicyRuleUpdateOrigin)) {
      return false;
    }
    PolicyRuleUpdateOrigin that = (PolicyRuleUpdateOrigin) o;
    return _origin == that._origin;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_origin);
  }
}
