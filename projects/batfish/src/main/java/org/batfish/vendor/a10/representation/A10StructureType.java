package org.batfish.vendor.a10.representation;

import org.batfish.vendor.StructureType;

public enum A10StructureType implements StructureType {
  INTERFACE("interface");

  private final String _description;

  A10StructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
