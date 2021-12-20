package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.batfish.representation.frr.FrrStructureUsage;
import org.batfish.vendor.StructureUsage;

public enum SonicStructureUsage implements StructureUsage {

  // delegated to FRR
  BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF(FrrStructureUsage.BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF),
  BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF(FrrStructureUsage.BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF),
  BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV4_UNICAST(
      FrrStructureUsage.BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV4_UNICAST),
  BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV6_UNICAST(
      FrrStructureUsage.BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV6_UNICAST),
  BGP_AGGREGATE_ADDRESS_ROUTE_MAP(FrrStructureUsage.BGP_AGGREGATE_ADDRESS_ROUTE_MAP),
  BGP_AGGREGATE_ADDRESS_SUPPRESS_MAP(FrrStructureUsage.BGP_AGGREGATE_ADDRESS_SUPPRESS_MAP),
  BGP_IPV4_UNICAST_REDISTRIBUTE_CONNECTED_ROUTE_MAP(
      FrrStructureUsage.BGP_IPV4_UNICAST_REDISTRIBUTE_CONNECTED_ROUTE_MAP),
  BGP_IPV4_UNICAST_REDISTRIBUTE_STATIC_ROUTE_MAP(
      FrrStructureUsage.BGP_IPV4_UNICAST_REDISTRIBUTE_STATIC_ROUTE_MAP),
  BGP_IPV4_UNICAST_REDISTRIBUTE_OSPF_ROUTE_MAP(
      FrrStructureUsage.BGP_IPV4_UNICAST_REDISTRIBUTE_OSPF_ROUTE_MAP),
  BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_IN(FrrStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_IN),
  BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_OUT(
      FrrStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_OUT),
  BGP_IPV4_UNICAST_NEIGHBOR_DEFAULT_ORIGINATE_ROUTE_MAP(
      FrrStructureUsage.BGP_IPV4_UNICAST_NEIGHBOR_DEFAULT_ORIGINATE_ROUTE_MAP),
  BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_IN(FrrStructureUsage.BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_IN),
  BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_OUT(FrrStructureUsage.BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_OUT),
  BGP_NEIGHBOR_INTERFACE(FrrStructureUsage.BGP_NEIGHBOR_INTERFACE),
  BGP_NETWORK(FrrStructureUsage.BGP_NETWORK),
  BGP_VRF(FrrStructureUsage.BGP_VRF),
  OSPF_REDISTRIBUTE_BGP_ROUTE_MAP(FrrStructureUsage.OSPF_REDISTRIBUTE_BGP_ROUTE_MAP),
  OSPF_REDISTRIBUTE_CONNECTED_ROUTE_MAP(FrrStructureUsage.OSPF_REDISTRIBUTE_CONNECTED_ROUTE_MAP),
  OSPF_REDISTRIBUTE_STATIC_ROUTE_MAP(FrrStructureUsage.OSPF_REDISTRIBUTE_STATIC_ROUTE_MAP),
  STATIC_ROUTE_VRF(FrrStructureUsage.STATIC_ROUTE_VRF),
  ROUTE_MAP_CALL(FrrStructureUsage.ROUTE_MAP_CALL),
  ROUTE_MAP_ENTRY_SELF_REFERENCE(FrrStructureUsage.ROUTE_MAP_ENTRY_SELF_REFERENCE),
  ROUTE_MAP_MATCH_COMMUNITY_LIST(FrrStructureUsage.ROUTE_MAP_MATCH_COMMUNITY_LIST),
  ROUTE_MAP_MATCH_INTERFACE(FrrStructureUsage.ROUTE_MAP_MATCH_INTERFACE),
  ROUTE_MAP_MATCH_AS_PATH(FrrStructureUsage.ROUTE_MAP_MATCH_AS_PATH),
  ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST(FrrStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST),
  ROUTE_MAP_SET_COMM_LIST_DELETE(FrrStructureUsage.ROUTE_MAP_SET_COMM_LIST_DELETE);

  private final @Nonnull String _description;

  SonicStructureUsage(@Nonnull String description) {
    _description = description;
  }

  SonicStructureUsage(@Nonnull FrrStructureUsage frrStructureUsage) {
    checkArgument(name().equals(frrStructureUsage.name())); // enforce 1:1 name mapping
    _description = frrStructureUsage.getDescription();
  }

  private static final Map<FrrStructureUsage, SonicStructureUsage> FRR_TO_SONIC_MAP = initMap();

  public static @Nonnull SonicStructureUsage fromFrrStructureUsage(
      @Nonnull FrrStructureUsage frrStructureUsage) {
    // initMap ensures that all keys exists
    return FRR_TO_SONIC_MAP.get(frrStructureUsage);
  }

  private static Map<FrrStructureUsage, SonicStructureUsage> initMap() {
    ImmutableMap.Builder<FrrStructureUsage, SonicStructureUsage> map = ImmutableMap.builder();
    for (FrrStructureUsage frrUsage : FrrStructureUsage.values()) {
      SonicStructureUsage matchingSonicType =
          Arrays.stream(SonicStructureUsage.values())
              .filter(cType -> cType.name().equals(frrUsage.name()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new NoSuchElementException(
                          "No SonicStructureUsage exists for FrrStructureUsage " + frrUsage));
      map.put(frrUsage, matchingSonicType);
    }
    return map.build();
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
