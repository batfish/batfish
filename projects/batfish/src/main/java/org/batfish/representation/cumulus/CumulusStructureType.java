package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureType;

public enum CumulusStructureType implements StructureType {
  ABSTRACT_INTERFACE("abstract interface"),
  BOND("bond"),
  INTERFACE("interface"),
  LOOPBACK("loopback"),
  ROUTE_MAP("route-map"),
  VLAN("vlan"),
  VRF("vrf"),
  VXLAN("vxlan");

  private final @Nonnull String _description;

  private CumulusStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
