package org.batfish.vendor.a10.representation;

import org.batfish.vendor.StructureUsage;

public enum A10StructureUsage implements StructureUsage {
  INTERFACE_SELF_REF("interface"),
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
