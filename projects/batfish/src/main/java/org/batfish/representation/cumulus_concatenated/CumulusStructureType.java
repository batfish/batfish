package org.batfish.representation.cumulus_concatenated;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.batfish.representation.frr.FrrStructureType;
import org.batfish.vendor.StructureType;

public enum CumulusStructureType implements StructureType {
  BOND("bond"),
  VXLAN("vxlan"),

  // these are defined in FRR files, so convert from there
  ABSTRACT_INTERFACE(FrrStructureType.ABSTRACT_INTERFACE),
  BGP_AS_PATH_ACCESS_LIST(FrrStructureType.BGP_AS_PATH_ACCESS_LIST),
  BGP_COMMUNITY_LIST(FrrStructureType.BGP_COMMUNITY_LIST),
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
    checkArgument(name().equals(frrStructureType.name())); // enforce 1:1 name mapping
    _description = frrStructureType.getDescription();
  }

  private static final Map<FrrStructureType, CumulusStructureType> FRR_TO_CUMULUS_MAP = initMap();

  public static @Nonnull CumulusStructureType fromFrrStructureType(
      @Nonnull FrrStructureType frrStructureType) {
    // initMap ensures that all keys exists
    return FRR_TO_CUMULUS_MAP.get(frrStructureType);
  }

  private static Map<FrrStructureType, CumulusStructureType> initMap() {
    ImmutableMap.Builder<FrrStructureType, CumulusStructureType> map = ImmutableMap.builder();
    for (FrrStructureType frrType : FrrStructureType.values()) {
      CumulusStructureType matchingCumulusType =
          Arrays.stream(CumulusStructureType.values())
              .filter(cType -> cType.name().equals(frrType.name()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new NoSuchElementException(
                          "No CumulusStructureType exists for FrrStructureType " + frrType));
      map.put(frrType, matchingCumulusType);
    }
    return map.build();
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
