package org.batfish.representation.cumulus;

import org.batfish.vendor.StructureType;

public enum CumulusNcluStructureType implements StructureType {
  BOND("bond"),
  INTERFACE("interface");

  private final String _description;

  private CumulusNcluStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
