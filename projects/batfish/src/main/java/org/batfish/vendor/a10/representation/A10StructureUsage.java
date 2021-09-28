package org.batfish.vendor.a10.representation;

import org.batfish.vendor.StructureUsage;

/** Named structure-usage types for A10 devices */
public enum A10StructureUsage implements StructureUsage {
  INTERFACE_SELF_REF("interface"),
  INTERFACE_TRUNK_GROUP("interface trunk-group"),
  // ACOS v2 member of a trunk
  TRUNK_INTERFACE("trunk interface"),
  VLAN_ROUTER_INTERFACE("vlan router-interface"),
  VLAN_TAGGED_INTERFACE("vlan tagged interface"),
  VLAN_UNTAGGED_INTERFACE("vlan untagged interface");

  private final String _description;

  A10StructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
