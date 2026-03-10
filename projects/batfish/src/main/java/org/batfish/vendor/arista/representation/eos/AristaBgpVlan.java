package org.batfish.vendor.arista.representation.eos;

public final class AristaBgpVlan extends AristaBgpVlanBase {
  private final int _vlan;

  public AristaBgpVlan(int vlan) {
    _vlan = vlan;
  }

  public int getVlan() {
    return _vlan;
  }
}
