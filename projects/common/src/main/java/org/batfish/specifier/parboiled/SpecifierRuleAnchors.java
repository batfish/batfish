package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.specifier.parboiled.grammar.SpecifierParser;

/**
 * Maps ANTLR specifier grammar rule indices to {@link Anchor.Type}s, reproducing the
 * {@code @Anchor} annotations that the parboiled {@link Parser} placed on its rules. Used by the
 * antlr4-c3-based autocomplete to classify candidate rules into completion categories.
 */
final class SpecifierRuleAnchors {

  private SpecifierRuleAnchors() {}

  /**
   * Rule index -> anchor type. Rules absent from this map have no anchor (like unannotated rules).
   */
  static final Map<Integer, Anchor.Type> ANCHORS = buildAnchors();

  private static Map<Integer, Anchor.Type> buildAnchors() {
    ImmutableMap.Builder<Integer, Anchor.Type> b = ImmutableMap.builder();
    put(b, SpecifierParser.RULE_referenceBook, Anchor.Type.REFERENCE_BOOK_NAME);
    put(b, SpecifierParser.RULE_oneAppSpec, Anchor.Type.ONE_APP);
    put(b, SpecifierParser.RULE_oneAppIcmp, Anchor.Type.ONE_APP_ICMP);
    put(b, SpecifierParser.RULE_oneAppIcmpType, Anchor.Type.ONE_APP_ICMP_TYPE);
    put(b, SpecifierParser.RULE_oneAppTcp, Anchor.Type.ONE_APP_TCP);
    put(b, SpecifierParser.RULE_oneAppUdp, Anchor.Type.ONE_APP_UDP);
    put(b, SpecifierParser.RULE_appSpec, Anchor.Type.APP_SET_OP);
    put(b, SpecifierParser.RULE_appName, Anchor.Type.APP_NAME);
    put(b, SpecifierParser.RULE_appNameRegex, Anchor.Type.DEPRECATED);
    put(b, SpecifierParser.RULE_appIcmpTerm, Anchor.Type.APP_ICMP);
    put(b, SpecifierParser.RULE_appIcmpType, Anchor.Type.APP_ICMP_TYPE);
    put(b, SpecifierParser.RULE_appIcmpTypeCode, Anchor.Type.APP_ICMP_TYPE_CODE);
    put(b, SpecifierParser.RULE_appTcpTerm, Anchor.Type.APP_TCP);
    put(b, SpecifierParser.RULE_appUdpTerm, Anchor.Type.APP_UDP);
    put(b, SpecifierParser.RULE_appPortSpec, Anchor.Type.APP_PORTS);
    put(b, SpecifierParser.RULE_appPort, Anchor.Type.APP_PORT);
    put(b, SpecifierParser.RULE_appPortRange, Anchor.Type.APP_PORT_RANGE);
    put(b, SpecifierParser.RULE_enumSetSpec, Anchor.Type.ENUM_SET_SET_OP);
    put(b, SpecifierParser.RULE_enumSetValue, Anchor.Type.ENUM_SET_VALUE);
    put(b, SpecifierParser.RULE_enumSetRegex, Anchor.Type.ENUM_SET_REGEX);
    put(b, SpecifierParser.RULE_enumSetRegexDeprecated, Anchor.Type.DEPRECATED);
    put(b, SpecifierParser.RULE_enumSetNotTerm, Anchor.Type.ENUM_SET_NOT);
    put(b, SpecifierParser.RULE_filterSpec, Anchor.Type.FILTER_SET_OP);
    put(b, SpecifierParser.RULE_filterWithNode, Anchor.Type.NODE_AND_FILTER);
    put(b, SpecifierParser.RULE_filterWithNodeTail, Anchor.Type.NODE_AND_FILTER_TAIL);
    put(b, SpecifierParser.RULE_filterWithoutNode, Anchor.Type.FILTER_SET_OP);
    put(b, SpecifierParser.RULE_filterInterfaceIn, Anchor.Type.FILTER_INTERFACE_IN);
    put(b, SpecifierParser.RULE_filterInterfaceOut, Anchor.Type.FILTER_INTERFACE_OUT);
    put(b, SpecifierParser.RULE_filterName, Anchor.Type.FILTER_NAME);
    put(b, SpecifierParser.RULE_filterNameRegex, Anchor.Type.FILTER_NAME_REGEX);
    put(b, SpecifierParser.RULE_filterNameRegexDeprecated, Anchor.Type.DEPRECATED);
    put(b, SpecifierParser.RULE_filterParens, Anchor.Type.FILTER_PARENS);
    put(b, SpecifierParser.RULE_filterWithoutNodeParens, Anchor.Type.FILTER_PARENS);
    put(b, SpecifierParser.RULE_interfaceSpec, Anchor.Type.INTERFACE_SET_OP);
    put(b, SpecifierParser.RULE_interfaceWithNode, Anchor.Type.NODE_AND_INTERFACE);
    put(b, SpecifierParser.RULE_interfaceWithNodeTail, Anchor.Type.NODE_AND_INTERFACE_TAIL);
    put(b, SpecifierParser.RULE_interfaceWithoutNode, Anchor.Type.INTERFACE_SET_OP);
    put(b, SpecifierParser.RULE_interfaceConnectedTo, Anchor.Type.INTERFACE_CONNECTED_TO);
    put(
        b,
        SpecifierParser.RULE_interfaceGroupAndReferenceBook,
        Anchor.Type.REFERENCE_BOOK_AND_INTERFACE_GROUP);
    put(
        b,
        SpecifierParser.RULE_interfaceGroupAndReferenceBookTail,
        Anchor.Type.REFERENCE_BOOK_AND_INTERFACE_GROUP_TAIL);
    put(b, SpecifierParser.RULE_interfaceGroup, Anchor.Type.INTERFACE_GROUP_NAME);
    put(b, SpecifierParser.RULE_interfaceType, Anchor.Type.INTERFACE_TYPE);
    put(b, SpecifierParser.RULE_interfaceVrf, Anchor.Type.INTERFACE_VRF);
    put(b, SpecifierParser.RULE_vrfName, Anchor.Type.VRF_NAME);
    put(b, SpecifierParser.RULE_interfaceZone, Anchor.Type.INTERFACE_ZONE);
    put(b, SpecifierParser.RULE_zoneName, Anchor.Type.ZONE_NAME);
    put(b, SpecifierParser.RULE_interfaceName, Anchor.Type.INTERFACE_NAME);
    put(b, SpecifierParser.RULE_interfaceNameRegex, Anchor.Type.INTERFACE_NAME_REGEX);
    put(b, SpecifierParser.RULE_interfaceNameRegexDeprecated, Anchor.Type.DEPRECATED);
    put(b, SpecifierParser.RULE_interfaceParens, Anchor.Type.INTERFACE_PARENS);
    put(b, SpecifierParser.RULE_interfaceWithoutNodeParens, Anchor.Type.INTERFACE_PARENS);
    put(b, SpecifierParser.RULE_ipProtocolSpec, Anchor.Type.IP_PROTOCOL_SET_OP);
    put(b, SpecifierParser.RULE_ipProtocolNot, Anchor.Type.IP_PROTOCOL_NOT);
    put(b, SpecifierParser.RULE_ipProtocolName, Anchor.Type.IP_PROTOCOL_NAME);
    put(b, SpecifierParser.RULE_ipProtocolNumber, Anchor.Type.IP_PROTOCOL_NUMBER);
    put(b, SpecifierParser.RULE_ipSpaceSpec, Anchor.Type.IP_SPACE_SET_OP);
    put(b, SpecifierParser.RULE_ipSpaceParens, Anchor.Type.IP_SPACE_PARENS);
    put(
        b,
        SpecifierParser.RULE_addressGroupAndReferenceBook,
        Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP);
    put(
        b,
        SpecifierParser.RULE_addressGroupAndReferenceBookTail,
        Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL);
    put(b, SpecifierParser.RULE_addressGroup, Anchor.Type.ADDRESS_GROUP_NAME);
    put(b, SpecifierParser.RULE_ipAddress, Anchor.Type.IP_ADDRESS);
    put(b, SpecifierParser.RULE_ipAddressMask, Anchor.Type.IP_ADDRESS_MASK);
    put(b, SpecifierParser.RULE_ipPrefix, Anchor.Type.IP_PREFIX);
    put(b, SpecifierParser.RULE_ipRange, Anchor.Type.IP_RANGE);
    put(b, SpecifierParser.RULE_ipWildcard, Anchor.Type.IP_WILDCARD);
    put(b, SpecifierParser.RULE_locationSpec, Anchor.Type.LOCATION_SET_OP);
    put(b, SpecifierParser.RULE_locationInternet, Anchor.Type.HIDDEN);
    put(b, SpecifierParser.RULE_locationEnter, Anchor.Type.LOCATION_ENTER);
    put(b, SpecifierParser.RULE_locationParens, Anchor.Type.LOCATION_PARENS);
    put(b, SpecifierParser.RULE_nameSetSpec, Anchor.Type.NAME_SET_SET_OP);
    put(b, SpecifierParser.RULE_nameSetName, Anchor.Type.NAME_SET_NAME);
    put(b, SpecifierParser.RULE_nameSetRegex, Anchor.Type.NAME_SET_REGEX);
    put(b, SpecifierParser.RULE_nameSetRegexDeprecated, Anchor.Type.DEPRECATED);
    put(b, SpecifierParser.RULE_nodeSpec, Anchor.Type.NODE_SET_OP);
    put(b, SpecifierParser.RULE_nodeRoleAndDimension, Anchor.Type.NODE_ROLE_AND_DIMENSION);
    put(b, SpecifierParser.RULE_nodeRoleAndDimensionTail, Anchor.Type.NODE_ROLE_AND_DIMENSION_TAIL);
    put(b, SpecifierParser.RULE_nodeRoleDimensionName, Anchor.Type.NODE_ROLE_DIMENSION_NAME);
    put(b, SpecifierParser.RULE_nodeRoleName, Anchor.Type.NODE_ROLE_NAME);
    put(b, SpecifierParser.RULE_nodeType, Anchor.Type.NODE_TYPE);
    put(b, SpecifierParser.RULE_nodeName, Anchor.Type.NODE_NAME);
    put(b, SpecifierParser.RULE_nodeNameRegex, Anchor.Type.NODE_NAME_REGEX);
    put(b, SpecifierParser.RULE_nodeNameRegexDeprecated, Anchor.Type.DEPRECATED);
    put(b, SpecifierParser.RULE_nodeParens, Anchor.Type.NODE_PARENS);
    put(b, SpecifierParser.RULE_routingPolicySpec, Anchor.Type.ROUTING_POLICY_SET_OP);
    put(b, SpecifierParser.RULE_routingPolicyName, Anchor.Type.ROUTING_POLICY_NAME);
    put(b, SpecifierParser.RULE_routingPolicyNameRegex, Anchor.Type.ROUTING_POLICY_NAME_REGEX);
    put(b, SpecifierParser.RULE_routingPolicyNameRegexDeprecated, Anchor.Type.DEPRECATED);
    put(b, SpecifierParser.RULE_routingPolicyParens, Anchor.Type.ROUTING_POLICY_PARENS);
    return b.build();
  }

  private static void put(
      ImmutableMap.Builder<Integer, Anchor.Type> b, int ruleIndex, Anchor.Type type) {
    b.put(ruleIndex, type);
  }
}
