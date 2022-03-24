package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A single explicit vlan-id. */
public final class BridgeDomainVlanIdNumber implements BridgeDomainVlanId {

  public static @Nonnull BridgeDomainVlanIdNumber of(int vlan) {
    return new BridgeDomainVlanIdNumber(vlan);
  }

  @Override
  public void accept(BridgeDomainVlanIdVoidVisitor visitor) {
    visitor.visitBridgeDomainVlanIdNumber(this);
  }

  public int getVlan() {
    return _vlan;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BridgeDomainVlanIdNumber)) {
      return false;
    }
    BridgeDomainVlanIdNumber that = (BridgeDomainVlanIdNumber) o;
    return _vlan == that._vlan;
  }

  @Override
  public int hashCode() {
    return _vlan;
  }

  private BridgeDomainVlanIdNumber(int vlan) {
    _vlan = vlan;
  }

  private final int _vlan;
}
