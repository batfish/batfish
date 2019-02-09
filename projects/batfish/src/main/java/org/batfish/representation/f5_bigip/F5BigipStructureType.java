package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.vendor.StructureType;

@ParametersAreNonnullByDefault
public enum F5BigipStructureType implements StructureType {
  BGP_PROCESS("bgp process"),
  INTERFACE("interface"),
  PREFIX_LIST("prefix-list"),
  ROUTE_MAP("route-map"),
  SELF("self"),
  VLAN("vlan");

  private final String _description;

  F5BigipStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
