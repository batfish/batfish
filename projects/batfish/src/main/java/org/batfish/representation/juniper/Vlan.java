package org.batfish.representation.juniper;

import org.batfish.common.util.ComparableStructure;

public class Vlan extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private int _vlanId;

  public Vlan(String vlanName, int vlanId) {
    super(vlanName);
    _vlanId = vlanId;
  }

  public int getVlanId() {
    return _vlanId;
  }
}
