package org.batfish.representation.juniper;

/** A {@link NatRule} that matches on source address name */
public class NatRuleMatchSrcAddrName implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public NatRuleMatchSrcAddrName(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
