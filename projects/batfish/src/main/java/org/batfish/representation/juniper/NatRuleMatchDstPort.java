package org.batfish.representation.juniper;

public class NatRuleMatchDstPort implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final int _from;

  private final int _to;

  public NatRuleMatchDstPort(int from, int to) {
    _from = from;
    _to = to;
  }

  public int getFrom() {
    return _from;
  }

  public int getTo() {
    return _to;
  }
}
