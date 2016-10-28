package org.batfish.datamodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
   OSPF3("ospf3"),
   RSVP("rsvp"),
   STATIC("static");

   private final static Map<String, RoutingProtocol> _map = buildMap();

   private synchronized static Map<String, RoutingProtocol> buildMap() {
      Map<String, RoutingProtocol> map = new HashMap<>();
      for (RoutingProtocol protocol : RoutingProtocol.values()) {
         String protocolName = protocol._protocolName.toLowerCase();
         map.put(protocolName, protocol);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static RoutingProtocol fromProtocolName(String name) {
      RoutingProtocol protocol = _map.get(name.toLowerCase());
      if (protocol == null) {
         throw new BatfishException(
               "No routing protocol with name: \"" + name + "\"");
      }
      return protocol;
   }

   private final String _protocolName;

   private RoutingProtocol(String protocolName) {
      _protocolName = protocolName;
   }

   public int getDefaultAdministrativeCost(ConfigurationFormat vendor) {
      switch (this) {
      case AGGREGATE:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            break;
         case AWS_VPC:
            break;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case FORCE10:
            break;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            break;
         case FLAT_VYOS:
         case VYOS:
            break;
         case EMPTY:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         default:
            break;

         }
         break;

      case BGP:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            return 200;
         case AWS_VPC:
            return 20;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 20;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 170;
         case FLAT_VYOS:
         case VYOS:
            return 20;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case CONNECTED:
         return 0;

      case IBGP:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            return 200;
         case AWS_VPC:
            return 200;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 200;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 170;
         case FLAT_VYOS:
         case VYOS:
            return 200;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case ISIS_EL1:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            break;
         case AWS_VPC:
            return 115;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 115;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 160;
         case FLAT_VYOS:
         case VYOS:
            return 115;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case ISIS_EL2:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            break;
         case AWS_VPC:
            return 115;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 115;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 165;
         case FLAT_VYOS:
         case VYOS:
            return 115;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case ISIS_L1:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            break;
         case AWS_VPC:
            return 115;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 115;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 15;
         case FLAT_VYOS:
         case VYOS:
            return 115;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case ISIS_L2:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            break;
         case AWS_VPC:
            return 115;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 115;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 18;
         case FLAT_VYOS:
         case VYOS:
            return 115;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case OSPF:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            return 110;
         case AWS_VPC:
            return 110;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 110;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 10;
         case FLAT_VYOS:
         case VYOS:
            return 110;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case OSPF_E1:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            return 110;
         case AWS_VPC:
            return 110;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 110;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 150;
         case FLAT_VYOS:
         case VYOS:
            return 110;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case OSPF_E2:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            return 110;
         case AWS_VPC:
            return 110;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 110;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 150;
         case FLAT_VYOS:
         case VYOS:
            return 110;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case OSPF_IA:
         switch (vendor) {
         case ALCATEL_AOS:
            break;
         case ARISTA:
            return 110;
         case AWS_VPC:
            return 110;
         case CISCO_IOS:
         case CISCO_IOS_XR:
         case CISCO_NX:
         case FORCE10:
            return 110;
         case FLAT_JUNIPER:
         case JUNIPER:
         case JUNIPER_SWITCH:
            return 10;
         case FLAT_VYOS:
         case VYOS:
            return 110;
         case EMPTY:
         case IGNORED:
         case BLADENETWORK:
         case HOST:
         case IPTABLES:
         case MRV:
         case MSS:
         case UNKNOWN:
         case VXWORKS:
            break;
         }
         break;

      case STATIC:
         return 1;

      case EGP:
      case IGP:
      case ISIS:
      case LDP:
      case LOCAL:
      case MSDP:
      case OSPF3:
      case RSVP:
      default:
         break;

      }
      throw new BatfishException(
            "Missing default administrative cost for protocol: '"
                  + _protocolName + "' for vendor '" + vendor.toString() + "'");
   }

   @JsonValue
   public String protocolName() {
      return _protocolName;
   }

}
