package org.batfish.representation.juniper;

import org.batfish.common.util.DefinedStructure;

public class Vlan extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private int _vlanId;

  public Vlan(String vlanName, int definitionLine, int vlanId) {
    super(vlanName, definitionLine);
    _vlanId = vlanId;
  }

  public int getVlanId() {
    return _vlanId;
  }
}
