package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

/** Configuration for a VLAN ID to be used by switched virtual interface (SVI). */
public final class Vlan implements Serializable {

  private final int _id;

  public Vlan(int id) {
    _id = id;
  }

  public int getId() {
    return _id;
  }
}
