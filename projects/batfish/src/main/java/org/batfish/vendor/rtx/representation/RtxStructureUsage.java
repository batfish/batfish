package org.batfish.vendor.rtx.representation;

import org.batfish.vendor.StructureUsage;

/** Places where a Yamaha RTX configuration refers to a named structure. */
public enum RtxStructureUsage implements StructureUsage {
  BGP_EXPORT_FILTER("bgp export <remote-as> filter"),
  BGP_IMPORT_FILTER("bgp import <remote-as> <protocol> filter"),
  NAT_DESCRIPTOR_SELF_REF("nat descriptor"),
  INTERFACE_NAT_DESCRIPTOR("ip <interface> nat descriptor"),
  SECURE_FILTER_IN("ip <interface> secure filter in"),
  SECURE_FILTER_OUT("ip <interface> secure filter out");

  private final String _description;

  RtxStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
