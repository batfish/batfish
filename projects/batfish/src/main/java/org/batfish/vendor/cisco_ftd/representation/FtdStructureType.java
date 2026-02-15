package org.batfish.vendor.cisco_ftd.representation;

import org.batfish.vendor.StructureType;

/** Structure types for Cisco FTD configurations. */
public enum FtdStructureType implements StructureType {
  ACCESS_LIST("access-list"),
  NETWORK_OBJECT("object network"),
  NETWORK_OBJECT_GROUP("object-group network"),
  SERVICE_OBJECT("object service"),
  SERVICE_OBJECT_GROUP("object-group service"),
  CLASS_MAP("class-map"),
  POLICY_MAP("policy-map"),
  CRYPTO_MAP("crypto map"),
  IPSEC_TRANSFORM_SET("crypto ipsec transform-set"),
  IPSEC_PROFILE("crypto ipsec profile"),
  IKEV2_POLICY("crypto ikev2 policy"),
  TUNNEL_GROUP("tunnel-group"),
  TIME_RANGE("time-range"),
  INTERFACE("interface"),
  OSPF_PROCESS("router ospf"),
  ROUTE_MAP("route-map");

  private final String _description;

  FtdStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
