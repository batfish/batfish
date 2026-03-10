package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for a VLAN ID to be used by switched virtual interface (SVI). */
public final class Vlan implements Serializable {

  public Vlan(int id) {
    _id = id;
  }

  public int getId() {
    return _id;
  }

  public @Nullable Integer getVni() {
    return _vni;
  }

  public void setVni(@Nullable Integer vni) {
    _vni = vni;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final int _id;
  private @Nullable Integer _vni;
}
