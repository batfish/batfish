package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NatRule} that matches on source address name */
@ParametersAreNonnullByDefault
public final class NatRuleMatchSrcAddrName implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public NatRuleMatchSrcAddrName(String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
  }
}
