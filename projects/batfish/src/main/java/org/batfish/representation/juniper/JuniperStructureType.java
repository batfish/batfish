package org.batfish.representation.juniper;

import org.batfish.vendor.StructureType;

public enum JuniperStructureType implements StructureType {
  ADDRESS_BOOK("address-book"),
  APPLICATION("application"),
  APPLICATION_OR_APPLICATION_SET("application or application-set"),
  APPLICATION_SET("application-set"),
  AS_PATH_GROUP("as-path-group"),
  AS_PATH_GROUP_AS_PATH("as-path-group as-path"),
  AUTHENTICATION_KEY_CHAIN("authentication-key-chain"),
  BGP_GROUP("bgp group"),
  DHCP_RELAY_SERVER_GROUP("dhcp-relay server-group"),
  FIREWALL_FILTER("firewall filter"),
  IKE_GATEWAY("ike gateway"),
  IKE_POLICY("ike policy"),
  IKE_PROPOSAL("ike proposal"),
  INTERFACE("interface"),
  IPSEC_POLICY("ipsec policy"),
  IPSEC_PROPOSAL("ipsec proposal"),
  LOGICAL_SYSTEM("logical-system"),
  NAT_POOL("nat pool"),
  NAT_RULE("nat rule"),
  NAT_RULE_SET("nat rule set"),
  POLICY_STATEMENT("policy-statement"),
  PREFIX_LIST("prefix-list"),
  RIB_GROUP("rib-group"),
  SECURE_TUNNEL_INTERFACE("secure tunnel interface"),
  SECURITY_PROFILE("security-profile"),
  VLAN("vlan");

  private final String _description;

  JuniperStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
