package org.batfish.vendor.nec_ix.representation;

import org.batfish.vendor.StructureUsage;

/** Places where an NEC IX configuration refers to a named structure. */
public enum NecIxStructureUsage implements StructureUsage {
  BGP_NEIGHBOR_ROUTE_MAP_IN("router bgp neighbor route-map in"),
  BGP_NEIGHBOR_ROUTE_MAP_OUT("router bgp neighbor route-map out"),
  BGP_REDISTRIBUTE_ROUTE_MAP("router bgp redistribute route-map"),
  INTERFACE_IP_FILTER_IN("interface ip filter in"),
  INTERFACE_IP_FILTER_OUT("interface ip filter out"),
  INTERFACE_IP_NAPT_INSIDE_LIST("interface ip napt inside list"),
  ROUTE_MAP_MATCH_PREFIX_LIST("route-map match ip address prefix-list");

  private final String _description;

  NecIxStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
