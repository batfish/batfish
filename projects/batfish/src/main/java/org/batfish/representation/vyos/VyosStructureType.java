package org.batfish.representation.vyos;

import org.batfish.vendor.StructureType;

public enum VyosStructureType implements StructureType {
  PREFIX_LIST("prefix-list");

  private final String _description;

  VyosStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
