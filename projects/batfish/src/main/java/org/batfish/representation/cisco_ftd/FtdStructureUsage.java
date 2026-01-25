package org.batfish.representation.cisco_ftd;

import org.batfish.vendor.StructureUsage;

/** Structure usages for Cisco FTD configurations. */
public enum FtdStructureUsage implements StructureUsage {
  ACCESS_GROUP_ACCESS_LIST("access-group"),
  ACCESS_LIST_NETWORK_OBJECT("access-list network object reference"),
  ACCESS_LIST_NETWORK_OBJECT_GROUP("access-list network object-group reference"),
  ACCESS_LIST_SERVICE_OBJECT("access-list service object reference"),
  ACCESS_LIST_SERVICE_OBJECT_GROUP("access-list service object-group reference"),
  NETWORK_OBJECT_GROUP_OBJECT("object-group network object reference"),
  NETWORK_OBJECT_GROUP_GROUP("object-group network group-object reference"),
  SERVICE_OBJECT_GROUP_OBJECT("object-group service object reference"),
  CLASS_MAP_MATCH("class-map match"),
  CLASS_MAP_ACCESS_LIST("class-map match access-list"),
  POLICY_MAP_CLASS("policy-map class"),
  SERVICE_POLICY_POLICY_MAP("service-policy policy-map"),
  INTERFACE_SELF_REF("interface definition"),
  OSPF_PASSIVE_INTERFACE("router ospf passive-interface"),
  OSPF_REDISTRIBUTE_CONNECTED_ROUTE_MAP("router ospf redistribute connected route-map"),
  OSPF_REDISTRIBUTE_STATIC_ROUTE_MAP("router ospf redistribute static route-map"),
  BGP_ROUTE_MAP_IN("router bgp neighbor route-map in"),
  BGP_ROUTE_MAP_OUT("router bgp neighbor route-map out"),
  NAT_SOURCE_OBJECT("nat source object"),
  NAT_DESTINATION_OBJECT("nat destination object"),
  NAT_SERVICE_OBJECT("nat service object"),
  CRYPTO_MAP_ACL("crypto map match address"),
  CRYPTO_MAP_TRANSFORM_SET("crypto map set transform-set"),
  CRYPTO_MAP_IPSEC_PROFILE("crypto map set ipsec-profile"),
  IPSEC_PROFILE_TRANSFORM_SET("crypto ipsec profile set transform-set"),
  IKEV2_POLICY("ikev2 policy"),
  TUNNEL_GROUP_IKEV2_POLICY("tunnel-group ikev2-policy");

  private final String _description;

  FtdStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
