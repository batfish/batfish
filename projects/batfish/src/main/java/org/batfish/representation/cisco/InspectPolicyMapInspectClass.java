package org.batfish.representation.cisco;

import java.io.Serializable;

public class InspectPolicyMapInspectClass implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _inspect;

  public boolean getInspect() {
    return _inspect;
  }

  public void setInspect(boolean inspect) {
    _inspect = inspect;
  }
}
