package org.batfish.representation.arista;

import org.batfish.vendor.StructureType;

public enum AristaStructureType implements StructureType {
  ACCESS_LIST("acl"),
  AS_PATH_ACCESS_LIST("as-path access-list"),
  BFD_TEMPLATE("bfd-template"),
  BGP_PEER_GROUP("bgp peer-group"),
  CLASS_MAP("class-map"),
  COMMUNITY_LIST("community-list"),
  COMMUNITY_LIST_EXPANDED("expanded community-list"),
  COMMUNITY_LIST_STANDARD("standard community-list"),
  CRYPTO_DYNAMIC_MAP_SET("crypto-dynamic-map-set"),
  CRYPTO_MAP_SET("crypto-map-set"),
  CSR_PARAMS("csr-params"),
  DEPI_CLASS("depi-class"),
  DEPI_TUNNEL("depi-tunnel"),
  DOCSIS_POLICY("docsis-policy"),
  DOCSIS_POLICY_RULE("docsis-policy-rule"),
  INSPECT_CLASS_MAP("class-map type inspect"),
  INSPECT_POLICY_MAP("policy-map type inspect"),
  INTERFACE("interface"),
  IP_ACCESS_LIST("ipv4/6 acl"),
  IPSEC_PROFILE("crypto ipsec profile"),
  IPSEC_TRANSFORM_SET("crypto ipsec transform-set"),
  IPV4_ACCESS_LIST("ipv4 acl"),
  IPV4_ACCESS_LIST_EXTENDED("extended ipv4 access-list"),
  IPV4_ACCESS_LIST_STANDARD("standard ipv4 access-list"),
  IPV6_ACCESS_LIST("ipv6 acl"),
  IPV6_ACCESS_LIST_EXTENDED("extended ipv6 access-list"),
  IPV6_ACCESS_LIST_STANDARD("standard ipv6 access-list"),
  ISAKMP_POLICY("crypto isakmp policy"),
  ISAKMP_PROFILE("crypto isakmp profile"),
  KEYRING("crypto keyring"),
  L2TP_CLASS("l2tp-class"),
  MAC_ACCESS_LIST("mac acl"),
  NAMED_RSA_PUB_KEY("crypto named rsa pubkey"),
  NAT_POOL("nat pool"),
  PEER_FILTER("peer-filter"),
  POLICY_MAP("policy-map"),
  PREFIX_LIST("ipv4 prefix-list"),
  PREFIX6_LIST("ipv6 prefix-list"),
  ROUTE_MAP("route-map"),
  ROUTE_MAP_CLAUSE("route-map-clause"),
  SERVICE_CLASS("cable service-class"),
  SERVICE_TEMPLATE("service-template"),
  TRACK("track"),
  VXLAN("vxlan");

  private final String _description;

  AristaStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
