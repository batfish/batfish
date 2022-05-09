package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration of a VNI inside of p_evpn. */
@ParametersAreNonnullByDefault
public final class Evpn implements Serializable {

  private Enum _multicast_mode;
  private @Nullable Boolean _extended_vni_all;
  private @Nullable List<Integer> _extended_vni_list;
  private @Nullable Enum _encapsulation;

  public @Nullable Enum getMulticastMode() {
    return _multicast_mode;
  }

  public @Nullable Enum getEncapsulation() {
    return _encapsulation;
  }

  public @Nullable Boolean getExtendedVniAll() {
    return _extended_vni_all;
  }

  public @Nullable List<Integer> getExtendedVniList() {
    return _extended_vni_list;
  }

  public void setMulticastMode(Enum multicastMode) {
    _multicast_mode = multicastMode;
  }

  public void setExtendedVniAll(Boolean extendedVniAll) {
    _extended_vni_all = extendedVniAll;
  }

  public void setExtendedVniList(List<Integer> extendedVniList) {
    _extended_vni_list = extendedVniList;
  }

  public void setEncapsulation(Enum encapsulation) {
    _encapsulation = encapsulation;
  }
}
