package org.batfish.representation.juniper;

import java.io.Serializable;

public class Vlan implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private int _vlanId;

  public Vlan(int vlanId) {
    _vlanId = vlanId;
  }

  public int getVlanId() {
    return _vlanId;
  }
}
