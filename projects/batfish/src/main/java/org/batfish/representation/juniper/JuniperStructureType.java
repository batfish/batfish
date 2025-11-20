package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.vendor.StructureType;

public enum JuniperStructureType implements StructureType {
  ADDRESS_BOOK("address-book"),
  ADMIN_GROUP("admin-group"),
  APPLICATION("application"),
  APPLICATION_OR_APPLICATION_SET("application or application-set"),
  APPLICATION_SET("application-set"),
  AS_PATH("as-path"),
  AS_PATH_GROUP("as-path-group"),
  AS_PATH_GROUP_AS_PATH("as-path-group as-path"),
  AUTHENTICATION_KEY_CHAIN("authentication-key-chain"),
  BGP_GROUP("bgp group"),
  BGP_NEIGHBOR("bgp neighbor"),
  BRIDGE_DOMAIN("bridge-domain"),
  CLASS_OF_SERVICE_CLASSIFIER("class-of-service classifier"),
  CLASS_OF_SERVICE_DSCP_CODE_POINT_ALIAS("class-of-service dscp code-point-alias"),
  CLASS_OF_SERVICE_DSCP_IPV6_CODE_POINT_ALIAS("class-of-service dscp-ipv6 code-point-alias"),
  CLASS_OF_SERVICE_EXP_CODE_POINT_ALIAS("class-of-service exp code-point-alias"),
  CLASS_OF_SERVICE_FORWARDING_CLASS("class-of-service forwarding-class"),
  CLASS_OF_SERVICE_IEEE_802_1_CODE_POINT_ALIAS("class-of-service ieee-802.1 code-point-alias"),
  CLASS_OF_SERVICE_INET_PRECEDENCE_CODE_POINT_ALIAS(
      "class-of-service inet-precedence code-point-alias"),
  CLASS_OF_SERVICE_REWRITE_RULE("class-of-service rewrite-rule"),
  CLASS_OF_SERVICE_SCHEDULER("class-of-service scheduler"),
  CLASS_OF_SERVICE_SCHEDULER_MAP("class-of-service scheduler-map"),
  COMMUNITY("community"),
  CONDITION("condition"),
  DHCP_RELAY_SERVER_GROUP("dhcp-relay server-group"),
  FIREWALL_FILTER("firewall filter"),
  FIREWALL_INET6_FILTER("firewall family inet6 filter"),
  FIREWALL_FILTER_TERM("firewall filter term"),
  FIREWALL_INTERFACE_SET("firewall interface-set"),
  IKE_GATEWAY("ike gateway"),
  IKE_POLICY("ike policy"),
  IKE_PROPOSAL("ike proposal"),
  INTERFACE("interface"),
  IPSEC_POLICY("ipsec policy"),
  IPSEC_PROPOSAL("ipsec proposal"),
  LOGICAL_SYSTEM("logical-system"),
  MPLS_PATH("mpls path"),
  NAT_POOL("nat pool"),
  NAT_RULE("nat rule"),
  NAT_RULE_SET("nat rule set"),
  POLICY_STATEMENT("policy-statement"),
  POLICY_STATEMENT_TERM("policy-statement term"),
  PREFIX_LIST("prefix-list"),
  RIB_GROUP("rib-group"),
  ROUTING_INSTANCE("routing-instance"),
  RTF_PREFIX_LIST("rtf-prefix-list"),
  SECURE_TUNNEL_INTERFACE("secure tunnel interface"),
  /**
   * A security policy. In config like {@code security policies from-zone A to-zone B policy P}, the
   * entire A-B structure.
   */
  SECURITY_POLICY("security policy"),
  /**
   * One part of a security policy. In config like {@code security policies from-zone A to-zone B
   * policy P}, structure P.
   */
  SECURITY_POLICY_TERM("security policy term"),
  SECURITY_PROFILE("security-profile"),
  SNMP_CLIENT_LIST("snmp client-list"),
  SNMP_CLIENT_LIST_OR_PREFIX_LIST("snmp client-list or prefix-list"),
  SRLG("srlg"),
  TUNNEL_ATTRIBUTE("tunnel-attribute"),
  VLAN("vlan");

  private final String _description;

  JuniperStructureType(String description) {
    _description = description;
  }

  public static final Multimap<JuniperStructureType, JuniperStructureType> ABSTRACT_STRUCTURES =
      ImmutableListMultimap.<JuniperStructureType, JuniperStructureType>builder()
          .putAll(APPLICATION_OR_APPLICATION_SET, APPLICATION, APPLICATION_SET)
          .putAll(SNMP_CLIENT_LIST_OR_PREFIX_LIST, SNMP_CLIENT_LIST, PREFIX_LIST)
          .build();

  /**
   * Built-in structures that don't appear in configurations but can be referenced. Maps structure
   * types to their built-in names.
   */
  public static final ImmutableSetMultimap<JuniperStructureType, String> BUILT_IN_STRUCTURES =
      ImmutableSetMultimap.<JuniperStructureType, String>builder()
          .put(CLASS_OF_SERVICE_CLASSIFIER, "default")
          .putAll(CLASS_OF_SERVICE_FORWARDING_CLASS, ForwardingClassUtil.builtinNames())
          .putAll(CLASS_OF_SERVICE_DSCP_CODE_POINT_ALIAS, DscpUtil.builtinNames())
          .putAll(CLASS_OF_SERVICE_EXP_CODE_POINT_ALIAS, ExpUtil.builtinNames())
          .putAll(CLASS_OF_SERVICE_IEEE_802_1_CODE_POINT_ALIAS, Ieee8021pUtil.builtinNames())
          .putAll(
              CLASS_OF_SERVICE_INET_PRECEDENCE_CODE_POINT_ALIAS, InetPrecedenceUtil.builtinNames())
          .put(CLASS_OF_SERVICE_REWRITE_RULE, "default")
          .put(CLASS_OF_SERVICE_SCHEDULER, "default-be")
          .put(CLASS_OF_SERVICE_SCHEDULER_MAP, "default")
          .build();

  public static final Set<JuniperStructureType> CONCRETE_STRUCTURES =
      ImmutableSet.copyOf(
          Sets.difference(ImmutableSet.copyOf(values()), ABSTRACT_STRUCTURES.keySet()));

  @Override
  public String getDescription() {
    return _description;
  }
}
