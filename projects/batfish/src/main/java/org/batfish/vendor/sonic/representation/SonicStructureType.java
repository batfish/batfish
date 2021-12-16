package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.representation.frr.FrrStructureType;
import org.batfish.vendor.StructureType;

public enum SonicStructureType implements StructureType {
  // these are defined in FRR files, so convert from there
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

  SonicStructureType(@Nonnull String description) {
    _description = description;
  }

  SonicStructureType(@Nonnull FrrStructureType frrStructureType) {
    _description = frrStructureType.getDescription();
  }

  private static final Map<String, SonicStructureType> MAP = initMap();

  public static SonicStructureType fromFrrStructureType(
      @Nonnull FrrStructureType frrStructureType) {
    checkArgument(MAP.containsKey(frrStructureType.getDescription()));
    return MAP.get(frrStructureType.getDescription());
  }

  private static Map<String, SonicStructureType> initMap() {
    ImmutableMap.Builder<String, SonicStructureType> map = ImmutableMap.builder();
    for (SonicStructureType value : SonicStructureType.values()) {
      map.put(value.getDescription(), value);
    }
    return map.build();
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
