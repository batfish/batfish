package org.batfish.vendor.huawei.representation;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;

/** Conversion helpers for converting Huawei VS model to VI model. */
public final class HuaweiConversions {

  // Default administrative distances for Huawei
  public static final int DEFAULT_EBGP_ADMIN_COST = 255;
  public static final int DEFAULT_IBGP_ADMIN_COST = 255;
  public static final int DEFAULT_LOCAL_ADMIN_COST = 255;
  public static final int DEFAULT_STATIC_ROUTE_ADMIN_COST = 60;

  private HuaweiConversions() {}

  /**
   * Convert a Huawei interface to a vendor-independent interface and attach it to the given VRF.
   */
  public static void convertInterface(
      Configuration c, Vrf vrf, HuaweiInterface iface, @Nullable org.batfish.common.Warnings w) {
    String name = iface.getName();

    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder().setName(name).setVrf(vrf).setOwner(c);

    // Set description
    if (iface.getDescription() != null) {
      newIface.setDescription(iface.getDescription());
    }

    // Set admin status (shutdown = !adminUp)
    newIface.setAdminUp(!iface.getShutdown());

    // Set interface type based on name
    newIface.setType(getInterfaceType(name));

    // Set IP address if present
    if (iface.getAddress() != null) {
      newIface.setAddress(iface.getAddress());
    }

    // Build the interface
    newIface.build();
  }

  /** Determine interface type from name. */
  public static InterfaceType getInterfaceType(String name) {
    if (name == null) {
      return InterfaceType.UNKNOWN;
    }
    String lowerName = name.toLowerCase();
    if (lowerName.startsWith("loopback")) {
      return InterfaceType.LOOPBACK;
    }
    if (lowerName.startsWith("vlanif")) {
      return InterfaceType.VLAN;
    }
    if (lowerName.startsWith("eth-trunk")) {
      return InterfaceType.AGGREGATED;
    }
    if (lowerName.startsWith("gigabitethernet")
        || lowerName.startsWith("ethernet")
        || lowerName.startsWith("ge")
        || lowerName.startsWith("xge")) {
      return InterfaceType.PHYSICAL;
    }
    return InterfaceType.UNKNOWN;
  }

  /** Convert Huawei BGP process to vendor-independent BGP process. */
  public static org.batfish.datamodel.BgpProcess convertBgpProcess(
      Configuration c,
      Vrf vrf,
      HuaweiBgpProcess bgpProcess,
      Map<String, HuaweiInterface> interfaces) {

    // Determine router ID
    Ip routerId = bgpProcess.getRouterId();
    if (routerId == null) {
      routerId = inferRouterId(interfaces);
    }

    // Build BGP process
    org.batfish.datamodel.BgpProcess newProcess =
        org.batfish.datamodel.BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(DEFAULT_EBGP_ADMIN_COST)
            .setIbgpAdminCost(DEFAULT_IBGP_ADMIN_COST)
            .setLocalAdminCost(DEFAULT_LOCAL_ADMIN_COST)
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
            .build();

    // Convert peers
    ImmutableSortedMap.Builder<Ip, BgpActivePeerConfig> neighborsBuilder =
        ImmutableSortedMap.naturalOrder();

    for (Map.Entry<Ip, HuaweiBgpPeer> entry : bgpProcess.getPeers().entrySet()) {
      Ip peerIp = entry.getKey();
      HuaweiBgpPeer peer = entry.getValue();

      BgpActivePeerConfig.Builder peerConfig = BgpActivePeerConfig.builder().setPeerAddress(peerIp);

      if (peer.getAsNum() != null) {
        peerConfig.setRemoteAsns(LongSpace.of(peer.getAsNum()));
      }

      // Set IPv4 address family
      peerConfig.setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build());

      neighborsBuilder.put(peerIp, peerConfig.build());
    }

    newProcess.setNeighbors(neighborsBuilder.build());

    return newProcess;
  }

  /** Infer router ID from interfaces (prefer Loopback0, then lowest IP). */
  public static @Nullable Ip inferRouterId(Map<String, HuaweiInterface> interfaces) {
    // Prefer Loopback0
    HuaweiInterface loopback0 = interfaces.get("LoopBack0");
    if (loopback0 != null && loopback0.getAddress() != null) {
      return loopback0.getAddress().getIp();
    }

    // Otherwise, find the lowest IP address
    Ip lowest = null;
    for (HuaweiInterface iface : interfaces.values()) {
      if (iface.getAddress() != null) {
        Ip ip = iface.getAddress().getIp();
        if (lowest == null || ip.asLong() < lowest.asLong()) {
          lowest = ip;
        }
      }
    }
    return lowest;
  }
}
