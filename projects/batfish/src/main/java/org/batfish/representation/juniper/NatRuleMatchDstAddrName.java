package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NatRule} that matches on destination address name */
@ParametersAreNonnullByDefault
public final class NatRuleMatchDstAddrName implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public NatRuleMatchDstAddrName(String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
  }
}
