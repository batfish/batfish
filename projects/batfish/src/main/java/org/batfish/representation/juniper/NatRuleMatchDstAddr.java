package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix;

/** A {@link NatRule} that matches on destination address */
public final class NatRuleMatchDstAddr implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public NatRuleMatchDstAddr(Prefix prefix) {
    _prefix = prefix;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
