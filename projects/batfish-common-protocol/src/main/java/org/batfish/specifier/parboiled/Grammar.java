package org.batfish.specifier.parboiled;

import java.util.Arrays;
import java.util.Collection;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;

/** Contains information on various expressions supported by this package */
public enum Grammar {
  APPLICATION_SPECIFIER("applicationSpecifier", "application-specifier"),
  BGP_PEER_PROPERTY_SPECIFIER("bgpPeerPropertySpecifier", "bgp-peer-property-specifier"),
  BGP_PROCESS_PROPERTY_SPECIFIER("bgpProcessPropertySpecifier", "bgp-process-property-specifier"),
  BGP_SESSION_TYPE_SPECIFIER("bgpSessionTypeSpecifier", "bgp-session-type-specifier"),
  FILTER_SPECIFIER("filterSpecifier", "filter-specifier"),
  INTERFACE_PROPERTY_SPECIFIER("interfacePropertySpecifier", "interface-property-specifier"),
  INTERFACE_SPECIFIER("interfaceSpecifier", "interface-specifier"),
  IP_PROTOCOL_SPECIFIER("ipProtocolSpecifier", "ip-protocol-specifier"),
  IP_SPACE_SPECIFIER("ipSpecifier", "ip-specifier"),
  LOCATION_SPECIFIER("locationSpecifier", "location-specifier"),
  MLAG_ID_SPECIFIER("mlagSpecifier", "mlag-id-specifier"),
  NAMED_STRUCTURE_SPECIFIER("namedStructureSpecifier", "named-structure-specifier"),
  NODE_PROPERTY_SPECIFIER("nodePropertySpecifier", "node-property-specifier"),
  NODE_SPECIFIER("nodeSpecifier", "node-specifier"),
  ROUTING_POLICY_SPECIFIER("routingPolicySpecifier", "routing-policy-specifier"),
  VXLAN_VNI_PROPERTY_SPECIFIER("vxlanVniPropertySpecifier", "vxlan-vni-property-specifier");

  static final String BASE_URL =
      "https://github.com/batfish/batfish/blob/master/questions/Parameters.md#";

  /** What we call the grammar in user-facing documentation */
  private final String _friendlyName;

  /** Where the grammar is documented related to {@link Grammar#BASE_URL} */
  private final String _urlTail;

  Grammar(String friendlyName, String urlTail) {
    _friendlyName = friendlyName;
    _urlTail = urlTail;
  }

  String getFullUrl() {
    return BASE_URL + getUrlTail();
  }

  public String getFriendlyName() {
    return _friendlyName;
  }

  String getUrlTail() {
    return _urlTail;
  }

  @Override
  public String toString() {
    return _friendlyName;
  }

  public static Collection<?> getEnumValues(Grammar grammar) {
    switch (grammar) {
      case APPLICATION_SPECIFIER:
        return Arrays.asList(Protocol.values());
      case BGP_PEER_PROPERTY_SPECIFIER:
        return BgpPeerPropertySpecifier.ALL.getMatchingProperties();
      case BGP_PROCESS_PROPERTY_SPECIFIER:
        return BgpProcessPropertySpecifier.ALL.getMatchingProperties();
      case BGP_SESSION_TYPE_SPECIFIER:
        return Arrays.asList(SessionType.values());
      case INTERFACE_PROPERTY_SPECIFIER:
        return InterfacePropertySpecifier.ALL.getMatchingProperties();
      case NAMED_STRUCTURE_SPECIFIER:
        return NamedStructurePropertySpecifier.ALL.getMatchingProperties();
      case NODE_PROPERTY_SPECIFIER:
        return NodePropertySpecifier.ALL.getMatchingProperties();
      case VXLAN_VNI_PROPERTY_SPECIFIER:
        return VxlanVniPropertySpecifier.ALL.getMatchingProperties();
      default:
        throw new IllegalArgumentException(grammar + " is not an enum grammar");
    }
  }
}
