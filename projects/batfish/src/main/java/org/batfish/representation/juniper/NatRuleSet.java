package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class NatRuleSet implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private List<NatRule> _rules;

  public NatRuleSet() {
    _rules = new LinkedList<>();
  }

  public List<NatRule> getRules() {
    return _rules;
  }

  public void setFromInterface(String name) {}

  public void setFromRoutingInstance(String name) {}

  public void setFromZone(String name) {}

  public void setToInterface(String name) {}

  public void setToRoutingInstance(String name) {}

  public void setToZone(String name) {}
}
