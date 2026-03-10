package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class VrfAddressFamily implements Serializable {
  public VrfAddressFamily(AddressFamily type) {
    _type = type;
  }

  public @Nullable ExtendedCommunityOrAuto getExportRt() {
    return _exportRt;
  }

  public void setExportRt(@Nullable ExtendedCommunityOrAuto exportRt) {
    _exportRt = exportRt;
  }

  public @Nullable ExtendedCommunityOrAuto getExportRtEvpn() {
    return _exportRtEvpn;
  }

  public void setExportRtEvpn(@Nullable ExtendedCommunityOrAuto exportRtEvpn) {
    _exportRtEvpn = exportRtEvpn;
  }

  public @Nullable ExtendedCommunityOrAuto getImportRt() {
    return _importRt;
  }

  public void setImportRt(@Nullable ExtendedCommunityOrAuto importRt) {
    _importRt = importRt;
  }

  public @Nullable ExtendedCommunityOrAuto getImportRtEvpn() {
    return _importRtEvpn;
  }

  public void setImportRtEvpn(@Nullable ExtendedCommunityOrAuto importRtEvpn) {
    _importRtEvpn = importRtEvpn;
  }

  public AddressFamily getType() {
    return _type;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final AddressFamily _type;
  private @Nullable ExtendedCommunityOrAuto _exportRt;
  private @Nullable ExtendedCommunityOrAuto _exportRtEvpn;
  private @Nullable ExtendedCommunityOrAuto _importRt;
  private @Nullable ExtendedCommunityOrAuto _importRtEvpn;
}
