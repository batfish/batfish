package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class NatRule implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private List<NatRuleMatch> _matches;

  private NatRuleThen _then;

  public NatRule() {
    _matches = new LinkedList<>();
    _then = null;
  }

  public List<NatRuleMatch> getMatches() {
    return _matches;
  }

  public void setThen(NatRuleThen then) {
    _then = then;
  }
}
