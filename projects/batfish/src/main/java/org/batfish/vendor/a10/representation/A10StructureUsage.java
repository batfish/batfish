package org.batfish.vendor.a10.representation;

import org.batfish.vendor.StructureUsage;

/** Named structure-usage types for A10 devices */
public enum A10StructureUsage implements StructureUsage {
  INTERFACE_SELF_REF("interface"),
  INTERFACE_TRUNK_GROUP("interface trunk-group"),
  IP_NAT_POOL_VRID("ip nat pool vrid"),
  SERVICE_GROUP_MEMBER("service-group member"),
  // ACOS v2 member of a trunk
  TRUNK_INTERFACE("trunk interface"),
  VIRTUAL_SERVER_SELF_REF("virtual-server"),
  VIRTUAL_SERVER_VRID("virtual-server vrid"),
  VLAN_ROUTER_INTERFACE("vlan router-interface"),
  VLAN_TAGGED_INTERFACE("vlan tagged interface"),
  VLAN_UNTAGGED_INTERFACE("vlan untagged interface"),
  VRRP_A_INTERFACE("vrrp-a interface"),
  VRRP_A_VRID_BLADE_PARAMETERS_FAIL_OVER_POLICY_TEMPLATE(
      "vrrp-a vrid blade-parameters fail-over-policy-template"),
  VRRP_A_VRID_DEFAULT_SELF_REFERENCE("vrrp-a vrid 0");

  private final String _description;

  A10StructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
