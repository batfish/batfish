package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

/** Represents a nat rule for Juniper */
public final class NatRule implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<NatRuleMatch> _matches;

  @Nullable private NatRuleThen _then;

  public NatRule() {
    _matches = new LinkedList<>();
    _then = null;
  }

  public List<NatRuleMatch> getMatches() {
    return _matches;
  }

  public void setThen(@Nullable NatRuleThen then) {
    _then = then;
  }

  @Nullable
  public NatRuleThen getThen() {
    return _then;
  }
}
