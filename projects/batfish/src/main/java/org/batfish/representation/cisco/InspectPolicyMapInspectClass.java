package org.batfish.representation.cisco;

import java.io.Serializable;

public class InspectPolicyMapInspectClass implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private PolicyMapClassAction _action;

  public PolicyMapClassAction getAction() {
    return _action;
  }

  public void setAction(PolicyMapClassAction action) {
    _action = action;
  }
}
