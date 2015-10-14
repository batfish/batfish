package org.batfish.representation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

public enum RoutingProtocol {
   AGGREGATE("aggregate"),
   BGP("bgp"),
   CONNECTED("connected"),
   EGP("egp"),
   IBGP("ibgp"),
   IGP("igp"),
   ISIS("isis"),
   ISIS_EL1("isisEL1"),
   ISIS_EL2("isisEL2"),
   ISIS_L1("isisL1"),
   ISIS_L2("isisL2"),
   LDP("ldp"),
   LOCAL("local"),
   MSDP("msdp"),
   OSPF("ospf"),
   OSPF_E1("ospfE1"),
   OSPF_E2("ospfE2"),
   OSPF_IA("ospfIA"),
   RSVP("rsvp"),
   STATIC("static");

   private final static Map<String, RoutingProtocol> _map = buildMap();

   private static Map<String, RoutingProtocol> buildMap() {
      Map<String, RoutingProtocol> map = new HashMap<String, RoutingProtocol>();
      for (RoutingProtocol protocol : RoutingProtocol.values()) {
         String protocolName = protocol._protocolName;
         map.put(protocolName, protocol);
      }
      return Collections.unmodifiableMap(map);
   }

   public static RoutingProtocol fromProtocolName(String name) {
      RoutingProtocol protocol = _map.get(name);
      if (protocol == null) {
         throw new BatfishException("No routing protocol with name: \"" + name
               + "\"");
      }
      return protocol;
   }

   private final String _protocolName;

   private RoutingProtocol(String protocolName) {
      _protocolName = protocolName;
   }

   public String protocolName() {
      return _protocolName;
   }

}
