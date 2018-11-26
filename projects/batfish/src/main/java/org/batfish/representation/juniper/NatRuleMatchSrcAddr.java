package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** A {@link NatRule} that matches on source address */
@ParametersAreNonnullByDefault
public final class NatRuleMatchSrcAddr implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public NatRuleMatchSrcAddr(Prefix prefix) {
    _prefix = prefix;
  }

  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }
}
