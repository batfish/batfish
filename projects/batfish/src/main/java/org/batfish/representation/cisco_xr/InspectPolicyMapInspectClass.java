package org.batfish.representation.cisco_xr;

import java.io.Serializable;

public class InspectPolicyMapInspectClass implements Serializable {

  private PolicyMapClassAction _action;

  public PolicyMapClassAction getAction() {
    return _action;
  }

  public void setAction(PolicyMapClassAction action) {
    _action = action;
  }
}
