package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;

/** Configuration of a VNI inside of p_evpn. */
@ParametersAreNonnullByDefault
public final class Evpn implements Serializable {

  private MulticastModeOptions _multicastMode;
  private @Nullable Boolean _extendedVniAll;
  private @Nullable IntegerSpace _extendedVniList;
  private @Nullable EvpnEncapsulation _encapsulation;

  public @Nullable MulticastModeOptions getMulticastMode() {
    return _multicastMode;
  }

  public @Nullable EvpnEncapsulation getEncapsulation() {
    return _encapsulation;
  }

  public @Nullable Boolean getExtendedVniAll() {
    return _extendedVniAll;
  }

  public @Nullable IntegerSpace getExtendedVniList() {
    return _extendedVniList;
  }

  public void setMulticastMode(MulticastModeOptions multicastMode) {
    _multicastMode = multicastMode;
  }

  public void setExtendedVniAll(Boolean extendedVniAll) {
    _extendedVniAll = extendedVniAll;
  }

  public void setExtendedVniList(IntegerSpace extendedVniList) {
    _extendedVniList = extendedVniList;
  }

  public void setEncapsulation(EvpnEncapsulation encapsulation) {
    _encapsulation = encapsulation;
  }
}
