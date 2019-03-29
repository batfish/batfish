package org.batfish.representation.cumulus;

import org.batfish.vendor.StructureType;

public enum CumulusStructureType implements StructureType {
  BOND("bond"),
  INTERFACE("interface"),
  VRF("vrf");

  private final String _description;

  private CumulusStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
