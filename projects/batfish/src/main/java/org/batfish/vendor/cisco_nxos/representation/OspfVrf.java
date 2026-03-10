package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

public final class OspfVrf extends OspfProcess {

  public OspfVrf(String vrf) {
    _vrf = vrf;
  }

  public @Nonnull String getVrf() {
    return _vrf;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nonnull String _vrf;
}
