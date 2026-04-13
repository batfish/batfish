package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration of EVPN IP prefix routes. */
@ParametersAreNonnullByDefault
public final class EvpnIpPrefixRoutes implements Serializable {

  private @Nullable EvpnIpPrefixRoutesAdvertise _advertise;
  private @Nullable EvpnEncapsulation _encapsulation;
  private @Nullable String _exportPolicy;
  private @Nullable String _importPolicy;
  private @Nullable Integer _vni;

  public @Nullable EvpnIpPrefixRoutesAdvertise getAdvertise() {
    return _advertise;
  }

  public void setAdvertise(@Nullable EvpnIpPrefixRoutesAdvertise advertise) {
    _advertise = advertise;
  }

  public @Nullable EvpnEncapsulation getEncapsulation() {
    return _encapsulation;
  }

  public void setEncapsulation(@Nullable EvpnEncapsulation encapsulation) {
    _encapsulation = encapsulation;
  }

  public @Nullable String getExportPolicy() {
    return _exportPolicy;
  }

  public void setExportPolicy(@Nullable String exportPolicy) {
    _exportPolicy = exportPolicy;
  }

  public @Nullable String getImportPolicy() {
    return _importPolicy;
  }

  public void setImportPolicy(@Nullable String importPolicy) {
    _importPolicy = importPolicy;
  }

  public @Nullable Integer getVni() {
    return _vni;
  }

  public void setVni(@Nullable Integer vni) {
    _vni = vni;
  }
}
