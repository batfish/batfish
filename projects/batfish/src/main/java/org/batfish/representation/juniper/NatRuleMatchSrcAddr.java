package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix;

public class NatRuleMatchSrcAddr implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public NatRuleMatchSrcAddr(Prefix prefix) {
    _prefix = prefix;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
