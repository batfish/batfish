package org.batfish.representation.palo_alto;

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
}
