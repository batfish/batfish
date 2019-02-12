package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.vendor.StructureUsage;

/** Named structure-usage types for F5 BIG-IP device */
@ParametersAreNonnullByDefault
public enum F5BigipStructureUsage implements StructureUsage {
  BGP_ADDRESS_FAMILY_REDISTRIBUTE_KERNEL_ROUTE_MAP(
      "bgp address-family redistribute kernel route-map"),
  BGP_NEIGHBOR_IPV4_ROUTE_MAP_OUT("bgp neighbor address-family ipv4 route-map out"),
  BGP_NEIGHBOR_IPV6_ROUTE_MAP_OUT("bgp neighbor address-family ipv6 route-map out"),
  BGP_NEIGHBOR_UPDATE_SOURCE("bgp neighbor update-source"),
  BGP_PROCESS_SELF_REFERENCE("bgp process self-reference"),
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  ROUTE_MAP_MATCH_IPV4_ADDRESS_PREFIX_LIST("route-map match ipv4 address prefix-list"),
  SELF_SELF_REFERENCE("self self-reference"),
  SELF_VLAN("self vlan"),
  VLAN_INTERFACE("vlan interface");

  private final @Nonnull String _description;

  private F5BigipStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
