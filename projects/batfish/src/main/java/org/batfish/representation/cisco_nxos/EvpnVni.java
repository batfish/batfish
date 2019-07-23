package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration of a VNI inside of s_evpn. */
@ParametersAreNonnullByDefault
public final class EvpnVni implements Serializable {
  public EvpnVni(int vni) {
    _vni = vni;
  }

  public @Nullable RouteDistinguisherOrAuto getExportRt() {
    return _exportRt;
  }

  public void setExportRt(@Nullable RouteDistinguisherOrAuto exportRt) {
    _exportRt = exportRt;
  }

  @Nullable
  public RouteDistinguisherOrAuto getImportRt() {
    return _importRt;
  }

  public void setImportRt(@Nullable RouteDistinguisherOrAuto importRt) {
    _importRt = importRt;
  }

  public @Nullable RouteDistinguisherOrAuto getRd() {
    return _rd;
  }

  public void setRd(@Nullable RouteDistinguisherOrAuto rd) {
    _rd = rd;
  }

  public int getVni() {
    return _vni;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final int _vni;
  private @Nullable RouteDistinguisherOrAuto _rd;
  private @Nullable RouteDistinguisherOrAuto _exportRt;
  private @Nullable RouteDistinguisherOrAuto _importRt;
}
