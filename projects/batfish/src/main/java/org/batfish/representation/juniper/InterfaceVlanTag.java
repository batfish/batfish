package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

/** A VLAN tag configured on a Junos logical interface. */
public final class InterfaceVlanTag implements Serializable {

  private final @Nullable Integer _tpid;
  private final int _vlanId;

  public InterfaceVlanTag(@Nullable Integer tpid, int vlanId) {
    _tpid = tpid;
    _vlanId = vlanId;
  }

  public @Nullable Integer getTpid() {
    return _tpid;
  }

  public int getVlanId() {
    return _vlanId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceVlanTag)) {
      return false;
    }
    InterfaceVlanTag that = (InterfaceVlanTag) o;
    return _vlanId == that._vlanId && Objects.equals(_tpid, that._tpid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_tpid, _vlanId);
  }
}
