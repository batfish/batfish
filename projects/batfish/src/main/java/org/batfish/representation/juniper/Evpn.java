package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;

/** Configuration of a VNI inside of p_evpn. */
@ParametersAreNonnullByDefault
public final class Evpn implements Serializable {

  private MulticastModeOptions _multicast_mode;
  private @Nullable Boolean _extended_vni_all;
  private @Nullable IntegerSpace _extended_vni_list;
  private @Nullable EvpnEncapsulation _encapsulation;

  public @Nullable MulticastModeOptions getMulticastMode() {
    return _multicast_mode;
  }

  public @Nullable EvpnEncapsulation getEncapsulation() {
    return _encapsulation;
  }

  public @Nullable Boolean getExtendedVniAll() {
    return _extended_vni_all;
  }

  public @Nullable IntegerSpace getExtendedVniList() {
    return _extended_vni_list;
  }

  public void setMulticastMode(MulticastModeOptions multicastMode) {
    _multicast_mode = multicastMode;
  }

  public void setExtendedVniAll(Boolean extendedVniAll) {
    _extended_vni_all = extendedVniAll;
  }

  public void setExtendedVniList(IntegerSpace extendedVniList) {
    _extended_vni_list = extendedVniList;
  }

  public void setEncapsulation(EvpnEncapsulation encapsulation) {
    _encapsulation = encapsulation;
  }
}
