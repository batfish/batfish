package org.batfish.representation.cumulus_concatenated;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.representation.frr.FrrStructureType;
import org.batfish.vendor.StructureType;

public enum CumulusStructureType implements StructureType {
  BOND("bond"),
  VXLAN("vxlan"),

  // delegated to FRR
  ABSTRACT_INTERFACE(FrrStructureType.ABSTRACT_INTERFACE),
  IP_AS_PATH_ACCESS_LIST(FrrStructureType.IP_AS_PATH_ACCESS_LIST),
  IP_COMMUNITY_LIST(FrrStructureType.IP_COMMUNITY_LIST),
  INTERFACE(FrrStructureType.INTERFACE),
  IP_COMMUNITY_LIST_EXPANDED(FrrStructureType.IP_COMMUNITY_LIST_EXPANDED),
  IP_COMMUNITY_LIST_STANDARD(FrrStructureType.IP_COMMUNITY_LIST_STANDARD),
  IP_PREFIX_LIST(FrrStructureType.IP_PREFIX_LIST),
  IPV6_PREFIX_LIST(FrrStructureType.IPV6_PREFIX_LIST),
  LOOPBACK(FrrStructureType.LOOPBACK),
  ROUTE_MAP(FrrStructureType.ROUTE_MAP),
  ROUTE_MAP_ENTRY(FrrStructureType.ROUTE_MAP_ENTRY),
  VLAN(FrrStructureType.VLAN),
  VRF(FrrStructureType.VRF);

  private final @Nonnull String _description;

  CumulusStructureType(@Nonnull String description) {
    _description = description;
  }

  CumulusStructureType(@Nonnull FrrStructureType frrStructureType) {
    _description = frrStructureType.getDescription();
  }

  private static final Map<String, CumulusStructureType> MAP = initMap();

  public static CumulusStructureType fromFrrStructureType(
      @Nonnull FrrStructureType frrStructureType) {
    checkArgument(MAP.containsKey(frrStructureType.getDescription()));
    return MAP.get(frrStructureType.getDescription());
  }

  private static Map<String, CumulusStructureType> initMap() {
    ImmutableMap.Builder<String, CumulusStructureType> map = ImmutableMap.builder();
    for (CumulusStructureType value : CumulusStructureType.values()) {
      map.put(value.getDescription(), value);
    }
    return map.build();
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }

  public CumulusStructureUsage selfReference() {
    switch (this) {
      case BOND:
        return CumulusStructureUsage.BOND_SELF_REFERENCE;
      case INTERFACE:
        return CumulusStructureUsage.INTERFACE_SELF_REFERENCE;
      case LOOPBACK:
        return CumulusStructureUsage.LOOPBACK_SELF_REFERENCE;
      case VLAN:
        return CumulusStructureUsage.VLAN_SELF_REFERENCE;
      case VRF:
        return CumulusStructureUsage.VRF_SELF_REFERENCE;
      case VXLAN:
        return CumulusStructureUsage.VXLAN_SELF_REFERENCE;
      default:
        throw new IllegalArgumentException(
            String.format("CumulusStructureType %s has no self-reference usage", _description));
    }
  }
}
