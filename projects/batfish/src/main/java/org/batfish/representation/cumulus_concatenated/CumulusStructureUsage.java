package org.batfish.representation.cumulus_concatenated;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.batfish.representation.frr.FrrStructureUsage;
import org.batfish.vendor.StructureUsage;

public enum CumulusStructureUsage implements StructureUsage {
  BOND_SELF_REFERENCE("bond self-reference"),
  BOND_SLAVE("bond slave"),
  BOND_VRF("bond vrf"),
  BRIDGE_PORT("bridge ports"),
  INTERFACE_CLAG_BACKUP_IP_VRF("interface clag backup-ip vrf"),
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  INTERFACE_VRF("interface vrf"),
  LOOPBACK_SELF_REFERENCE("loopback self-reference"),
  PORT_BREAKOUT("port breakout"),
  PORT_DISABLED("port disabled"),
  PORT_SPEED("port speed"),
  VLAN_SELF_REFERENCE("vlan self-reference"),
  VLAN_VRF("vlan vrf"),
  VRF_SELF_REFERENCE("vrf self-reference"),
  VXLAN_SELF_REFERENCE("vxlan self-reference"),

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

  CumulusStructureUsage(@Nonnull String description) {
    _description = description;
  }

  CumulusStructureUsage(@Nonnull FrrStructureUsage frrStructureUsage) {
    checkArgument(name().equals(frrStructureUsage.name())); // enforce 1:1 name mapping
    _description = frrStructureUsage.getDescription();
  }

  private static final Map<FrrStructureUsage, CumulusStructureUsage> FRR_TO_CUMULUS_MAP = initMap();

  public static @Nonnull CumulusStructureUsage fromFrrStructureUsage(
      @Nonnull FrrStructureUsage frrStructureUsage) {
    // initMap ensures that all keys exists
    return FRR_TO_CUMULUS_MAP.get(frrStructureUsage);
  }

  private static Map<FrrStructureUsage, CumulusStructureUsage> initMap() {
    ImmutableMap.Builder<FrrStructureUsage, CumulusStructureUsage> map = ImmutableMap.builder();
    for (FrrStructureUsage frrUsage : FrrStructureUsage.values()) {
      CumulusStructureUsage matchingCumulusUsage =
          Arrays.stream(CumulusStructureUsage.values())
              .filter(cType -> cType.name().equals(frrUsage.name()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new NoSuchElementException(
                          "No CumulusStructureUsage exists for FrrStructureUsage " + frrUsage));
      map.put(frrUsage, matchingCumulusUsage);
    }
    return map.build();
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
