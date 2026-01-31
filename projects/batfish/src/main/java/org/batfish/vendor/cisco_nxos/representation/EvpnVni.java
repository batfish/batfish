package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration of a VNI inside of s_evpn. */
@ParametersAreNonnullByDefault
public final class EvpnVni implements Serializable {
  public EvpnVni(int vni) {
    _vni = vni;
  }

  public @Nullable ExtendedCommunityOrAuto getExportRt() {
    return _exportRt;
  }

  public void setExportRt(@Nullable ExtendedCommunityOrAuto exportRt) {
    _exportRt = exportRt;
  }

  public @Nullable ExtendedCommunityOrAuto getImportRt() {
    return _importRt;
  }

  public void setImportRt(@Nullable ExtendedCommunityOrAuto importRt) {
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
  private @Nullable ExtendedCommunityOrAuto _exportRt;
  private @Nullable ExtendedCommunityOrAuto _importRt;
}
