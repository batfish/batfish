package org.batfish.representation.palo_alto;

import org.batfish.vendor.StructureType;

public enum PaloAltoStructureType implements StructureType {
  INTERFACE("interface");

  private final String _description;

  PaloAltoStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
