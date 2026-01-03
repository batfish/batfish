package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum RoutingProtocol {
  AGGREGATE("aggregate"),
  BGP("bgp"),
  CONNECTED("connected"),
  EGP("egp"),
  EIGRP("eigrp"),
  EIGRP_EX("eigrpEX"),
  EVPN("evpn"),
  IBGP("ibgp"),
  IGP("igp"),
  ISIS_ANY("isis"),
  ISIS_EL1("isisEL1"),
  ISIS_EL2("isisEL2"),
  ISIS_L1("isisL1"),
  ISIS_L2("isisL2"),
  KERNEL("kernel"),
  LDP("ldp"),
  LISP("lisp"),
  LOCAL("local"),
  HMM("hmm"),
  MSDP("msdp"),
  /** OSPF Intra-area */
  OSPF("ospf"),
  /** OSPF External type 1 */
  OSPF_E1("ospfE1"),
  /** OSPF External type 2 */
  OSPF_E2("ospfE2"),
  /** OSPF Inter-area */
  OSPF_IA("ospfIA"),
  /** OSPF Internal summary */
  OSPF_IS("ospfIS"),
  OSPF3("ospf3"),
  RIP("rip"),
  RSVP("rsvp"),
  STATIC("static");

  private static final Map<String, RoutingProtocol> _map = buildMap();

  private static Map<String, RoutingProtocol> buildMap() {
    ImmutableMap.Builder<String, RoutingProtocol> map = ImmutableMap.builder();
    for (RoutingProtocol protocol : RoutingProtocol.values()) {
      String protocolName = protocol._protocolName.toLowerCase();
      map.put(protocolName, protocol);
    }
    return map.build();
  }

  @JsonCreator
  public static RoutingProtocol fromProtocolName(String name) {
    RoutingProtocol protocol = _map.get(name.toLowerCase());
    if (protocol == null) {
      throw new BatfishException("No routing protocol with name: \"" + name + "\"");
    }
    return protocol;
  }

  private final String _protocolName;

  RoutingProtocol(String protocolName) {
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
          case ARUBAOS:
            break;
          case AWS:
            break;
          case CADANT:
            break;
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case HOST:
          case IGNORED:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
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
          case AWS:
            return 20;
          case CADANT:
            return 20;
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case CUMULUS_CONCATENATED:
          case CUMULUS_NCLU:
          case FORCE10:
          case FORTIOS:
          case FOUNDRY:
          case F5:
          case F5_BIGIP_STRUCTURED:
            return 20;
          case FLAT_JUNIPER:
          case JUNIPER:
          case JUNIPER_SWITCH:
            return 170;
          case FLAT_VYOS:
          case VYOS:
            return 20;
          case ARUBAOS: // aruba controllers don't support BGP
          case EMPTY:
          case IGNORED:
          case BLADENETWORK:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case CONNECTED:
        return 0;

      case EIGRP:
        switch (vendor) {
          case ALCATEL_AOS:
          case ARISTA:
          case ARUBAOS:
          case AWS:
          case BLADENETWORK:
          case CADANT:
            break;
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
            return 90;
          case EMPTY:
          case F5:
          case FLAT_JUNIPER:
          case FLAT_VYOS:
          case FORCE10:
          case FOUNDRY:
          case HOST:
          case IGNORED:
          case IPTABLES:
          case JUNIPER:
          case JUNIPER_SWITCH:
          case METAMAKO:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case PALO_ALTO:
          case PALO_ALTO_NESTED:
          case UNKNOWN:
          case VXWORKS:
          case VYOS:
            break;
          default:
            break;
        }
        break;

      case EIGRP_EX:
        switch (vendor) {
          case ALCATEL_AOS:
          case ARISTA:
          case ARUBAOS:
          case AWS:
          case BLADENETWORK:
          case CADANT:
            break;
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
            return 170;
          case EMPTY:
          case F5:
          case FLAT_JUNIPER:
          case FLAT_VYOS:
          case FORCE10:
          case FOUNDRY:
          case HOST:
          case IGNORED:
          case IPTABLES:
          case JUNIPER:
          case JUNIPER_SWITCH:
          case METAMAKO:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case PALO_ALTO:
          case PALO_ALTO_NESTED:
          case UNKNOWN:
          case VXWORKS:
          case VYOS:
            break;
          default:
            break;
        }
        break;

      case IBGP:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case ARISTA:
            return 200;
          case AWS:
            return 200;
          case CADANT:
            return 20;
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case CUMULUS_CONCATENATED:
          case CUMULUS_NCLU:
          case FORCE10:
          case FORTIOS:
          case FOUNDRY:
          case F5:
          case F5_BIGIP_STRUCTURED:
            return 200;
          case FLAT_JUNIPER:
          case JUNIPER:
          case JUNIPER_SWITCH:
            return 170;
          case FLAT_VYOS:
          case VYOS:
            return 200;
          case ARUBAOS: // aruba controllers don't support bgp
          case EMPTY:
          case IGNORED:
          case BLADENETWORK:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case ISIS_EL1:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case AWS:
            return 115;
          case CADANT:
            return 117;
          case ARISTA:
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case ISIS_EL2:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case AWS:
            return 115;
          case CADANT:
            return 118;
          case ARISTA:
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case BLADENETWORK:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case ISIS_L1:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case AWS:
            return 115;
          case CADANT:
            return 115;
          case ARISTA:
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case BLADENETWORK:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case ISIS_L2:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case AWS:
            return 115;
          case CADANT:
            return 116;
          case ARISTA:
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case BLADENETWORK:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case KERNEL:
        return 0;

      case OSPF:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case ARISTA:
            return 110;
          case AWS:
            return 110;
          case CADANT:
            // TODO: verify. assumption due to missing information in manual.
            return 110;
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case OSPF_E1:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case ARISTA:
            return 110;
          case AWS:
            return 110;
          case CADANT:
            // TODO: verify. assumption based on incrementing IS-IS costs in manual.
            return 112;
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case OSPF_E2:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case ARISTA:
            return 110;
          case AWS:
            return 110;
          case CADANT:
            // TODO: verify. assumption based on incrementing IS-IS costs in manual.
            return 113;
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case OSPF_IA:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case ARISTA:
            return 110;
          case AWS:
            return 110;
          case CADANT:
            // TODO: verify. assumption based on incrementing IS-IS costs in manual.
            return 111;
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case OSPF_IS:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case ARISTA:
            return 110;
          case AWS:
            return 110;
          case CADANT: // TODO: verify
            return 110;
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
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
          case F5:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case RIP:
        switch (vendor) {
          case ALCATEL_AOS:
            break;
          case ARISTA:
            return 120;
          case AWS:
            return 120;
          case CADANT:
            return 120;
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
            return 120;
          case FLAT_JUNIPER:
          case JUNIPER:
          case JUNIPER_SWITCH:
            return 100;
          case FLAT_VYOS:
          case VYOS:
            return 120;
          case EMPTY:
          case IGNORED:
          case BLADENETWORK:
          case F5:
          case HOST:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
            break;
          default:
            break;
        }
        break;

      case STATIC:
        return 1;

      case EGP:
      case IGP:
      case ISIS_ANY:
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
            + _protocolName
            + "' for vendor '"
            + vendor
            + "'");
  }

  public int getSummaryAdministrativeCost(ConfigurationFormat vendor) {
    switch (this) {
      case OSPF_IA:
        switch (vendor) {
          case ARISTA:
          case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
          case CADANT:
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
          case FORCE10:
          case FOUNDRY:
            return 254;

          case FLAT_JUNIPER:
          case JUNIPER:
          case JUNIPER_SWITCH:
            return 10;

          case ALCATEL_AOS:
          case AWS:
          case BLADENETWORK:
          case EMPTY:
          case F5:
          case FLAT_VYOS:
          case HOST:
          case IGNORED:
          case IPTABLES:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case UNKNOWN:
          case VXWORKS:
          case VYOS:
          default:
            throw new BatfishException(
                "Unsupported vendor for OSPF inter-area summary administrative cost: " + vendor);
        }

      case AGGREGATE:
      case BGP:
      case CONNECTED:
      case EIGRP:
        switch (vendor) {
          case ALCATEL_AOS:
          case ARISTA:
          case ARUBAOS:
          case AWS:
          case BLADENETWORK:
          case CADANT:
            break;
          case CISCO_ASA:
          case CISCO_IOS:
          case CISCO_IOS_XR:
          case CISCO_NX:
            return 5;
          case EMPTY:
          case F5:
          case FLAT_JUNIPER:
          case FLAT_VYOS:
          case FORCE10:
          case FOUNDRY:
          case HOST:
          case IGNORED:
          case IPTABLES:
          case JUNIPER:
          case JUNIPER_SWITCH:
          case METAMAKO:
          case MRV:
          case MRV_COMMANDS:
          case MSS:
          case PALO_ALTO:
          case PALO_ALTO_NESTED:
          case UNKNOWN:
          case VXWORKS:
          case VYOS:
          default:
            break;
        }
        throw new BatfishException(
            "Unsupported vendor for EIGRP summary administrative cost: " + vendor);
      case EGP:
      case EIGRP_EX:
      case IBGP:
      case IGP:
      case ISIS_ANY:
      case ISIS_EL1:
      case ISIS_EL2:
      case ISIS_L1:
      case ISIS_L2:
      case LDP:
      case LOCAL:
      case MSDP:
      case OSPF:
      case OSPF3:
      case OSPF_E1:
      case OSPF_E2:
      case RSVP:
      case RIP:
      case STATIC:
      default:
        throw new BatfishException(
            "Unuspported protocol for summary administrative cost: " + protocolName());
    }
  }

  @JsonValue
  public String protocolName() {
    return _protocolName;
  }

  private static RoutingProtocol[] VALUES = RoutingProtocol.values();

  public static RoutingProtocol fromOrdinal(int ordinal) {
    return VALUES[ordinal];
  }
}
