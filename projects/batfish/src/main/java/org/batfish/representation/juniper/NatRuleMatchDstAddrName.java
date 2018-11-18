package org.batfish.representation.juniper;

public class NatRuleMatchDstAddrName implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public NatRuleMatchDstAddrName(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
