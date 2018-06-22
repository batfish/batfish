package org.batfish.representation.palo_alto;

import org.batfish.vendor.StructureType;

public enum PaloAltoStructureType implements StructureType {
  INTERFACE("interface"),

  // Borrowed from Juniper
  APPLICATION("application"),
  APPLICATION_OR_APPLICATION_SET("application or application-set"),
  APPLICATION_SET("application-set"),
  AUTHENTICATION_KEY_CHAIN("authentication-key-chain"),
  BGP_GROUP("bgp group"),
  DHCP_RELAY_SERVER_GROUP("dhcp-relay server-group"),
  FIREWALL_FILTER("firewall filter"),
  IKE_GATEWAY("ike gateway"),
  IKE_POLICY("ike policy"),
  IKE_PROPOSAL("ike proposal"),
  IPSEC_POLICY("ipsec policy"),
  IPSEC_PROPOSAL("ipsec proposal"),
  POLICY_STATEMENT("policy-statement"),
  PREFIX_LIST("prefix-list"),
  SECURE_TUNNEL_INTERFACE("secure tunnel interface"),
  VLAN("vlan");

  private final String _description;

  PaloAltoStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
