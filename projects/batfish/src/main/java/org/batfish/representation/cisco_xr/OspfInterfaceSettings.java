package org.batfish.representation.cisco_xr;

import java.io.Serializable;

/** Represents the OSPF settings on an interface as specified in router OSPF stanza */
public class OspfInterfaceSettings implements Serializable {

  public OspfInterfaceSettings() {
    _ospfSettings = new OspfSettings();
  }

  public OspfSettings getOspfSettings() {
    return _ospfSettings;
  }

  private final OspfSettings _ospfSettings;
}
