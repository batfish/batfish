package org.batfish.representation.frr;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureType;

public enum FrrStructureType implements StructureType {
  ABSTRACT_INTERFACE("abstract interface"),
  IP_AS_PATH_ACCESS_LIST("ip as-path access-list"),
  IP_COMMUNITY_LIST("ip community-list"),
  INTERFACE("interface"),
  IP_COMMUNITY_LIST_EXPANDED("ip community-list expanded"),
  IP_COMMUNITY_LIST_STANDARD("ip community-list standard"),
  IP_PREFIX_LIST("ip prefix-list"),
  IPV6_PREFIX_LIST("ipv6 prefix-list"),
  LOOPBACK("loopback"),
  ROUTE_MAP("route-map"),
  ROUTE_MAP_ENTRY("route-map entry"),
  VLAN("vlan"),
  VRF("vrf");

  private final @Nonnull String _description;

  private FrrStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
