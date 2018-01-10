package org.batfish.representation.vyos;

import org.batfish.vendor.StructureUsage;

public enum VyosStructureUsage implements StructureUsage {
  ROUTE_MAP_MATCH_PREFIX_LIST("route-map match prefix-list");

  private final String _description;

  VyosStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
