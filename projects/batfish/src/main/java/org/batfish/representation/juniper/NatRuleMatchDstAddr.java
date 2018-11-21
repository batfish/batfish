package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** A {@link NatRule} that matches on destination address */
@ParametersAreNonnullByDefault
public final class NatRuleMatchDstAddr implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public NatRuleMatchDstAddr(Prefix prefix) {
    _prefix = prefix;
  }

  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }
}
