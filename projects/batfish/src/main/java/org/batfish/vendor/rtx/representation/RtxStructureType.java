package org.batfish.vendor.rtx.representation;

import org.batfish.vendor.StructureType;

/** Named structures in a Yamaha RTX configuration. */
public enum RtxStructureType implements StructureType {
  BGP_EXPORT_FILTER("bgp export filter"),
  BGP_IMPORT_FILTER("bgp import filter"),
  FILTER("ip filter"),
  NAT_DESCRIPTOR("nat descriptor");

  private final String _description;

  RtxStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
