package org.batfish.vendor.nec_ix.representation;

import org.batfish.vendor.StructureType;

/** Named structures in an NEC IX configuration. */
public enum NecIxStructureType implements StructureType {
  ACCESS_LIST("ip access-list"),
  PREFIX_LIST("ip prefix-list"),
  ROUTE_MAP("route-map");

  private final String _description;

  NecIxStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
