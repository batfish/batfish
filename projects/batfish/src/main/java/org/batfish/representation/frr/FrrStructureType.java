package org.batfish.representation.frr;

import javax.annotation.Nonnull;

/**
 * An enum with structure types that can appear in frr.conf files. It does not inherit {@link
 * org.batfish.vendor.StructureType} because FRR is not an independent, stand-alone vendor. Vendors
 * that are based on FRR (e.g., Cumulus, Sonic) "cast" these types into their own structure types.
 */
public enum FrrStructureType {
  ABSTRACT_INTERFACE("abstract interface"),
  BGP_AS_PATH_ACCESS_LIST("bgp as-path access-list"),
  BGP_COMMUNITY_LIST("bgp community-list"),
  BGP_COMMUNITY_LIST_EXPANDED("bgp community-list expanded"),
  BGP_COMMUNITY_LIST_STANDARD("bgp community-list standard"),
  INTERFACE("interface"),
  IP_PREFIX_LIST("ip prefix-list"),
  IPV6_PREFIX_LIST("ipv6 prefix-list"),
  LOOPBACK("loopback"),
  ROUTE_MAP("route-map"),
  ROUTE_MAP_ENTRY("route-map entry"),
  VLAN("vlan"),
  VRF("vrf");

  private final @Nonnull String _description;

  FrrStructureType(String description) {
    _description = description;
  }

  public @Nonnull String getDescription() {
    return _description;
  }
}
