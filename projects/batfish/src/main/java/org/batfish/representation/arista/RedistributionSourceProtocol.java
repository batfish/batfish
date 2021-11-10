package org.batfish.representation.arista;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

/**
 * Source protocol for redistribution, used to express redistribution for OSPF, RIP, and ISIS. BGP
 * is handled by {@link org.batfish.representation.arista.eos.AristaRedistributeType}.
 */
public enum RedistributionSourceProtocol {
  CONNECTED("connected"),
  BGP_ANY("bgp"),
  ISIS_ANY("isis"),
  STATIC("static");

  private static final Map<String, RedistributionSourceProtocol> _map = buildMap();

  private static Map<String, RedistributionSourceProtocol> buildMap() {
    ImmutableMap.Builder<String, RedistributionSourceProtocol> map = ImmutableMap.builder();
    for (RedistributionSourceProtocol protocol : RedistributionSourceProtocol.values()) {
      String protocolName = protocol._protocolName.toLowerCase();
      map.put(protocolName, protocol);
    }
    return map.build();
  }

  @JsonCreator
  public static RedistributionSourceProtocol fromProtocolName(String name) {
    RedistributionSourceProtocol protocol = _map.get(name.toLowerCase());
    if (protocol == null) {
      throw new BatfishException("No redistribution source protocol with name: \"" + name + "\"");
    }
    return protocol;
  }

  private final String _protocolName;

  RedistributionSourceProtocol(String protocolName) {
    _protocolName = protocolName;
  }

  @JsonValue
  public String protocolName() {
    return _protocolName;
  }
}
