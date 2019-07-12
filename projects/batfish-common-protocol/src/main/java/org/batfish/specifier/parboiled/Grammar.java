package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.IpsecSessionStatus;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.OspfInterfacePropertySpecifier;
import org.batfish.datamodel.questions.OspfProcessPropertySpecifier;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.batfish.specifier.SpecifierContext;

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
  IPSEC_SESSION_STATUS_SPECIFIER("ipsecSessionStatusSpecifier", "ipsec-session-status-specifier"),
  LOCATION_SPECIFIER("locationSpecifier", "location-specifier"),
  MLAG_ID_SPECIFIER("mlagSpecifier", "mlag-id-specifier"),
  NAMED_STRUCTURE_SPECIFIER("namedStructureSpecifier", "named-structure-specifier"),
  NODE_PROPERTY_SPECIFIER("nodePropertySpecifier", "node-property-specifier"),
  NODE_SPECIFIER("nodeSpecifier", "node-specifier"),
  OSPF_INTERFACE_PROPERTY_SPECIFIER(
      "ospfInterfacePropertySpecifier", "ospf-interface-property-specifier"),
  OSPF_PROCESS_PROPERTY_SPECIFIER(
      "ospfProcessPropertySpecifier", "ospf-process-property-specifier"),
  ROUTING_POLICY_SPECIFIER("routingPolicySpecifier", "routing-policy-specifier"),
  ROUTING_PROTOCOL_SPECIFIER("routingProtocolSpecifier", "routing-protocol-specifier"),
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
      case IPSEC_SESSION_STATUS_SPECIFIER:
        return Arrays.asList(IpsecSessionStatus.values());
      case NAMED_STRUCTURE_SPECIFIER:
        return NamedStructurePropertySpecifier.ALL.getMatchingProperties();
      case NODE_PROPERTY_SPECIFIER:
        return NodePropertySpecifier.ALL.getMatchingProperties();
      case OSPF_INTERFACE_PROPERTY_SPECIFIER:
        return OspfInterfacePropertySpecifier.ALL.getMatchingProperties();
      case OSPF_PROCESS_PROPERTY_SPECIFIER:
        return OspfProcessPropertySpecifier.ALL.getMatchingProperties();
      case ROUTING_PROTOCOL_SPECIFIER:
        return RoutingProtocolSpecifier.getAllProtocolKeys();
      case VXLAN_VNI_PROPERTY_SPECIFIER:
        return VxlanVniPropertySpecifier.ALL.getMatchingProperties();
      default:
        throw new IllegalArgumentException(grammar + " is not an enum grammar");
    }
  }

  /**
   * Returns the set of names based on the grammar type, for grammars that use the shared name
   * grammar (NameSetSpec)
   */
  public static Set<String> getNames(SpecifierContext ctxt, Grammar grammar) {
    switch (grammar) {
      case MLAG_ID_SPECIFIER:
        return ctxt.getConfigs().values().stream()
            .flatMap(c -> c.getMlags().keySet().stream())
            .collect(ImmutableSet.toImmutableSet());
      default:
        throw new IllegalArgumentException("Cannot recover names for " + grammar);
    }
  }

  /**
   * Returns the set of names based on the grammar type, for grammars that use the shared name
   * grammar (NameSetSpec)
   */
  public static Set<String> getNames(CompletionMetadata completionMetadata, Grammar grammar) {
    switch (grammar) {
      case MLAG_ID_SPECIFIER:
        return completionMetadata.getMlagIds();
      default:
        throw new IllegalArgumentException("Cannot recover names for " + grammar);
    }
  }

  /**
   * Returns the type of names based on the grammar type, for grammars that use the shared name
   * grammar (NameSetSpec)
   */
  public static String getNameType(Grammar grammar) {
    switch (grammar) {
      case MLAG_ID_SPECIFIER:
        return "mlagId";
      default:
        throw new IllegalArgumentException("Cannot recover name type for " + grammar);
    }
  }
}
