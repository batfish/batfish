package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.vendor.StructureType;

public enum CiscoXrStructureType implements StructureType {
  ACCESS_LIST("acl"),
  AS_PATH_SET("as-path-set"),
  BFD_TEMPLATE("bfd-template"),
  BGP_AF_GROUP("bgp af-group"),
  BGP_NEIGHBOR_GROUP("bgp neighbor-group"),
  BGP_PEER_GROUP("bgp peer-group"),
  BGP_SESSION_GROUP("bgp session-group"),
  BGP_TEMPLATE_PEER_POLICY("bgp template peer-policy"),
  BGP_TEMPLATE_PEER_SESSION("bgp template peer-session"),
  CLASS_MAP("class-map"),
  COMMUNITY_SET("community-set"),
  CRYPTO_DYNAMIC_MAP_SET("crypto-dynamic-map-set"),
  CRYPTO_MAP_SET("crypto-map-set"),
  DYNAMIC_TEMPLATE("dynamic-template"),
  ETHERNET_SERVICES_ACCESS_LIST("ethernet-services access-list"),
  EXTCOMMUNITY_SET_RT("extcommunity-set rt"),
  FLOW_EXPORTER_MAP("flow exporter-map"),
  FLOW_MONITOR_MAP("flow monitor-map"),
  INSPECT_CLASS_MAP("class-map type inspect"),
  INTERFACE("interface"),
  IP_ACCESS_LIST("ipv4/6 acl"),
  IPSEC_PROFILE("crypto ipsec profile"),
  IPSEC_TRANSFORM_SET("crypto ipsec transform-set"),
  IPV4_ACCESS_LIST("ipv4 access-list"),
  IPV4_ACCESS_LIST_LINE("ipv4 access-list line"),
  IPV6_ACCESS_LIST("ipv6 access-list"),
  ISAKMP_POLICY("crypto isakmp policy"),
  ISAKMP_PROFILE("crypto isakmp profile"),
  KEYRING("crypto keyring"),
  L2TP_CLASS("l2tp-class"),
  NAMED_RSA_PUB_KEY("crypto named rsa pubkey"),
  NETWORK_OBJECT("object network"),
  NETWORK_OBJECT_GROUP("object-group network"),
  POLICY_MAP("policy-map"),
  PREFIX_LIST("ipv4 prefix-list"),
  PREFIX_SET("prefix-set"),
  PREFIX6_LIST("ipv6 prefix-list"),
  RD_SET("rd-set"),
  ROUTE_POLICY("route-policy"),
  SAMPLER_MAP("sampler-map"),
  SERVICE_TEMPLATE("service-template"),
  TRACK("track");

  public static final Multimap<CiscoXrStructureType, CiscoXrStructureType> ABSTRACT_STRUCTURES =
      ImmutableListMultimap.<CiscoXrStructureType, CiscoXrStructureType>builder()
          .putAll(IP_ACCESS_LIST, IPV4_ACCESS_LIST, IPV6_ACCESS_LIST)
          .build();

  public static final Set<CiscoXrStructureType> CONCRETE_STRUCTURES =
      ImmutableSet.copyOf(
          Sets.difference(ImmutableSet.copyOf(values()), ABSTRACT_STRUCTURES.keySet()));

  private final String _description;

  CiscoXrStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
