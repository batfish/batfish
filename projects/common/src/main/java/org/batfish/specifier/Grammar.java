package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.ospf.OspfSessionStatus;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.batfish.datamodel.questions.BgpRouteStatus;
import org.batfish.datamodel.questions.BgpSessionStatus;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.IpsecSessionStatus;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.OspfInterfacePropertySpecifier;
import org.batfish.datamodel.questions.OspfProcessPropertySpecifier;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;

/** Contains information on various expressions supported by this package */
public enum Grammar {
  APPLICATION_SPECIFIER("applicationSpecifier", "application-specifier"),
  BGP_PEER_PROPERTY_SPECIFIER("bgpPeerPropertySpecifier", "bgp-peer-property-specifier"),
  BGP_PROCESS_PROPERTY_SPECIFIER("bgpProcessPropertySpecifier", "bgp-process-property-specifier"),
  BGP_ROUTE_STATUS_SPECIFIER("bgpRouteStatusSpecifier", "bgp-route-status-specifier"),
  BGP_SESSION_COMPAT_STATUS_SPECIFIER(
      "bgpSessionCompatStatusSpecifier", "bgp-session-compat-status-specifier"),
  BGP_SESSION_STATUS_SPECIFIER("bgpSessionStatusSpecifier", "bgp-session-status-specifier"),
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
  OSPF_SESSION_STATUS_SPECIFIER("ospfSessionStatusSpecifier", "ospf-session-status-specifier"),
  ROUTING_POLICY_SPECIFIER("routingPolicySpecifier", "routing-policy-specifier"),
  ROUTING_PROTOCOL_SPECIFIER("routingProtocolSpecifier", "routing-protocol-specifier"),
  SINGLE_APPLICATION_SPECIFIER("singleApplicationSpecifier", "single-application-specifier"),
  VXLAN_VNI_PROPERTY_SPECIFIER("vxlanVniPropertySpecifier", "vxlan-vni-property-specifier");

  static final String BASE_URL = "https://pybatfish.readthedocs.io/en/latest/specifiers.html#";

  public static final String GENERAL_NOTE =
      String.format(
          "Be sure to read the notes (%sgeneral-notes-on-the-grammar) on set operations, escaping"
              + " names, and regular expressions.",
          BASE_URL);

  /** What we call the grammar in user-facing documentation */
  private final String _friendlyName;

  /** Where the grammar is documented related to {@link Grammar#BASE_URL} */
  private final String _urlTail;

  Grammar(String friendlyName, String urlTail) {
    _friendlyName = friendlyName;
    _urlTail = urlTail;
  }

  public String getFullUrl() {
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

  /** Returns all values in the enum set for {@code grammar} */
  public static Collection<?> getEnumValues(Grammar grammar) {
    switch (grammar) {
      case BGP_PEER_PROPERTY_SPECIFIER:
        return BgpPeerPropertySpecifier.ALL.getMatchingProperties();
      case BGP_PROCESS_PROPERTY_SPECIFIER:
        return BgpProcessPropertySpecifier.ALL.getMatchingProperties();
      case BGP_ROUTE_STATUS_SPECIFIER:
        return Arrays.asList(BgpRouteStatus.values());
      case BGP_SESSION_COMPAT_STATUS_SPECIFIER:
        return Arrays.asList(ConfiguredSessionStatus.values());
      case BGP_SESSION_STATUS_SPECIFIER:
        return Arrays.asList(BgpSessionStatus.values());
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
      case OSPF_SESSION_STATUS_SPECIFIER:
        return Arrays.asList(OspfSessionStatus.values());
      case ROUTING_PROTOCOL_SPECIFIER:
        return RoutingProtocolSpecifier.getAllProtocolKeys();
      case VXLAN_VNI_PROPERTY_SPECIFIER:
        return VxlanVniPropertySpecifier.ALL.getMatchingProperties();
      default:
        throw new IllegalArgumentException(grammar + " is not an enum grammar");
    }
  }

  /**
   * Returns a mapping from group values in the enum set (which represent multiple atomic values) to
   * their atomic values.
   */
  public static Map<?, ? extends Set<?>> getGroupValues(Grammar grammar) {
    switch (grammar) {
      case ROUTING_PROTOCOL_SPECIFIER:
        return RoutingProtocolSpecifier.getGroupings();
      default:
        return ImmutableMap.of();
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
