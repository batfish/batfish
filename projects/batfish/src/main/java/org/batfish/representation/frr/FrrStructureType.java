package org.batfish.representation.frr;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureType;

public enum FrrStructureType implements StructureType {
  ABSTRACT_INTERFACE("abstract interface"),
  BOND("bond"),
  IP_AS_PATH_ACCESS_LIST("ip as-path access-list"),
  IP_COMMUNITY_LIST("ip community-list"),
  INTERFACE("interface"),
  IP_COMMUNITY_LIST_EXPANDED("ip community-list expanded"),
  IP_COMMUNITY_LIST_STANDARD("ip community-list standard"),
  IP_PREFIX_LIST("ip_prefix_list"),
  LOOPBACK("loopback"),
  ROUTE_MAP("route-map"),
  ROUTE_MAP_ENTRY("route-map entry"),
  VLAN("vlan"),
  VRF("vrf"),
  VXLAN("vxlan");

  private final @Nonnull String _description;

  private FrrStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }

  public FrrStructureUsage selfReference() {
    switch (this) {
      case BOND:
        return FrrStructureUsage.BOND_SELF_REFERENCE;
      case INTERFACE:
        return FrrStructureUsage.INTERFACE_SELF_REFERENCE;
      case LOOPBACK:
        return FrrStructureUsage.LOOPBACK_SELF_REFERENCE;
      case VLAN:
        return FrrStructureUsage.VLAN_SELF_REFERENCE;
      case VRF:
        return FrrStructureUsage.VRF_SELF_REFERENCE;
      case VXLAN:
        return FrrStructureUsage.VXLAN_SELF_REFERENCE;
      default:
        throw new IllegalArgumentException(
            String.format("CumulusStructureType %s has no self-reference usage", _description));
    }
  }
}
