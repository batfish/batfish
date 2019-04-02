package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureType;

public enum CumulusStructureType implements StructureType {
  BOND("bond"),
  INTERFACE("interface"),
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
