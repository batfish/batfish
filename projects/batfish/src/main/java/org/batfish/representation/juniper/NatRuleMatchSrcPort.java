package org.batfish.representation.juniper;

import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NatRule} that matches on source port */
@ParametersAreNonnullByDefault
public final class NatRuleMatchSrcPort implements NatRuleMatch {

  private static final long serialVersionUID = 1L;

  private final int _from;

  private final int _to;

  public NatRuleMatchSrcPort(int from, int to) {
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
