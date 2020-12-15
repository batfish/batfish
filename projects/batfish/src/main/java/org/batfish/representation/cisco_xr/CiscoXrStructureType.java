package org.batfish.representation.cisco_xr;

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
  EXTCOMMUNITY_SET_RT("extcommunity-set rt"),
  ICMP_TYPE_OBJECT("object icmp-type"),
  ICMP_TYPE_OBJECT_GROUP("object-group icmp-type"),
  INSPECT_CLASS_MAP("class-map type inspect"),
  INTERFACE("interface"),
  IP_ACCESS_LIST("ipv4/6 acl"),
  IPSEC_PROFILE("crypto ipsec profile"),
  IPSEC_TRANSFORM_SET("crypto ipsec transform-set"),
  IPV4_ACCESS_LIST("ipv4 access-list"),
  IPV6_ACCESS_LIST("ipv6 access-list"),
  ISAKMP_POLICY("crypto isakmp policy"),
  ISAKMP_PROFILE("crypto isakmp profile"),
  KEYRING("crypto keyring"),
  L2TP_CLASS("l2tp-class"),
  MAC_ACCESS_LIST("mac acl"),
  NAMED_RSA_PUB_KEY("crypto named rsa pubkey"),
  NETWORK_OBJECT("object network"),
  NETWORK_OBJECT_GROUP("object-group network"),
  POLICY_MAP("policy-map"),
  PREFIX_LIST("ipv4 prefix-list"),
  PREFIX_SET("prefix-set"),
  PREFIX6_LIST("ipv6 prefix-list"),
  PROTOCOL_OBJECT("object protocol"),
  PROTOCOL_OBJECT_GROUP("object-group protocol"),
  PROTOCOL_OR_SERVICE_OBJECT_GROUP("object-group protocol or service"),
  ROUTE_POLICY("route-policy"),
  SERVICE_OBJECT("object service"),
  SERVICE_OBJECT_GROUP("object-group service"),
  SERVICE_TEMPLATE("service-template"),
  TRACK("track");

  private final String _description;

  CiscoXrStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
