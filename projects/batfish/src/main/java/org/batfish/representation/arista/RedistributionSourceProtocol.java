package org.batfish.representation.arista;

/**
 * Source protocol for redistribution, used to express redistribution for OSPF, RIP, and ISIS. BGP
 * is handled by {@link org.batfish.representation.arista.eos.AristaRedistributeType}.
 */
public enum RedistributionSourceProtocol {
  CONNECTED("connected"),
  BGP_ANY("bgp"),
  ISIS_ANY("isis"),
  ISIS_L1("isisL1"),
  STATIC("static");

  private final String _protocolName;

  RedistributionSourceProtocol(String protocolName) {
    _protocolName = protocolName;
  }

  public String protocolName() {
    return _protocolName;
  }
}
