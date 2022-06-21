package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.representation.frr.FrrStructureType;
import org.batfish.vendor.StructureType;

public enum SonicStructureType implements StructureType {
  // these are defined in FRR files, so convert from there
  ABSTRACT_INTERFACE(FrrStructureType.ABSTRACT_INTERFACE),
  BGP_AS_PATH_ACCESS_LIST(FrrStructureType.BGP_AS_PATH_ACCESS_LIST),
  BGP_COMMUNITY_LIST(FrrStructureType.BGP_COMMUNITY_LIST),
  INTERFACE(FrrStructureType.INTERFACE),
  BGP_COMMUNITY_LIST_EXPANDED(FrrStructureType.BGP_COMMUNITY_LIST_EXPANDED),
  BGP_COMMUNITY_LIST_STANDARD(FrrStructureType.BGP_COMMUNITY_LIST_STANDARD),
  BGP_LISTEN_RANGE(FrrStructureType.BGP_LISTEN_RANGE),
  BGP_NEIGHBOR(FrrStructureType.BGP_NEIGHBOR),
  BGP_NEIGHBOR_INTERFACE(FrrStructureType.BGP_NEIGHBOR_INTERFACE),
  IP_PREFIX_LIST(FrrStructureType.IP_PREFIX_LIST),
  IPV6_PREFIX_LIST(FrrStructureType.IPV6_PREFIX_LIST),
  LOOPBACK(FrrStructureType.LOOPBACK),
  ROUTE_MAP(FrrStructureType.ROUTE_MAP),
  ROUTE_MAP_ENTRY(FrrStructureType.ROUTE_MAP_ENTRY),
  VLAN(FrrStructureType.VLAN),
  VRF(FrrStructureType.VRF);

  // TODO: Add abstract structures here after adding structures from configdb
  public static final Multimap<SonicStructureType, SonicStructureType> ABSTRACT_STRUCTURES =
      ImmutableListMultimap.<SonicStructureType, SonicStructureType>builder()
          .putAll(
              BGP_COMMUNITY_LIST,
              ImmutableSet.of(BGP_COMMUNITY_LIST_STANDARD, BGP_COMMUNITY_LIST_EXPANDED))
          .build();

  public static final Set<SonicStructureType> CONCRETE_STRUCTURES =
      ImmutableSet.copyOf(
          Sets.difference(ImmutableSet.copyOf(values()), ABSTRACT_STRUCTURES.keySet()));

  private final @Nonnull String _description;

  SonicStructureType(@Nonnull String description) {
    _description = description;
  }

  SonicStructureType(@Nonnull FrrStructureType frrStructureType) {
    checkArgument(name().equals(frrStructureType.name())); // enforce 1:1 name mapping
    _description = frrStructureType.getDescription();
  }

  private static final Map<FrrStructureType, SonicStructureType> FRR_TO_SONIC_MAP = initMap();

  public static SonicStructureType fromFrrStructureType(
      @Nonnull FrrStructureType frrStructureType) {
    // initMap ensures that all keys exists
    return FRR_TO_SONIC_MAP.get(frrStructureType);
  }

  private static Map<FrrStructureType, SonicStructureType> initMap() {
    ImmutableMap.Builder<FrrStructureType, SonicStructureType> map = ImmutableMap.builder();
    for (FrrStructureType frrType : FrrStructureType.values()) {
      SonicStructureType matchingSonicType =
          Arrays.stream(SonicStructureType.values())
              .filter(cType -> cType.name().equals(frrType.name()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new NoSuchElementException(
                          "No SonicStructureType exists for FrrStructureType " + frrType));
      map.put(frrType, matchingSonicType);
    }
    return map.build();
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
