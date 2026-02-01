package org.batfish.vendor.huawei.representation;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.vendor_family.huawei.HuaweiFamily;

/**
 * Conversion utilities for transforming Huawei VRP configurations to Batfish's vendor-independent
 * format.
 *
 * <p>This class provides static methods to convert various aspects of Huawei configuration to
 * Batfish's abstract model, including interfaces, routing protocols, ACLs, and other features.
 */
public class HuaweiConversions {

  /**
   * Converts a Huawei configuration to a Batfish vendor-independent Configuration.
   *
   * @param huaweiConfig The Huawei configuration to convert
   * @return A Batfish Configuration object
   */
  public static @Nonnull Configuration toVendorIndependentConfiguration(
      @Nonnull HuaweiConfiguration huaweiConfig) {
    // Create new Configuration object with hostname and format
    Configuration c = new Configuration(huaweiConfig.getHostname(), ConfigurationFormat.HUAWEI);

    // Set Huawei vendor family
    c.getVendorFamily().setHuawei(new org.batfish.datamodel.vendor_family.huawei.HuaweiFamily());

    // Convert interfaces
    toConfigurationInterfaces(huaweiConfig, c);

    // Set default VRF
    c.getVrfs()
        .computeIfAbsent(
            DEFAULT_VRF_NAME,
            vrfName -> org.batfish.datamodel.Vrf.builder().setName(DEFAULT_VRF_NAME).build());

    // Convert static routes
    toConfigurationStaticRoutes(c, huaweiConfig);

    // Convert NAT rules
    toConfigurationNat(c, huaweiConfig);

    // Convert OSPF
    toConfigurationOspf(c, huaweiConfig);

    // Convert BGP
    toConfigurationBgp(c, huaweiConfig);

    // Convert VRFs
    toConfigurationVrfs(c, huaweiConfig);

    // Convert ACLs
    toConfigurationAcls(c, huaweiConfig);

    return c;
  }

  /**
   * Converts Huawei interface configurations to Batfish vendor-independent interfaces.
   *
   * @param huaweiConfig The Huawei configuration to convert from
   * @param c The Batfish Configuration object to populate
   */
  private static void toConfigurationInterfaces(
      @Nonnull HuaweiConfiguration huaweiConfig, @Nonnull Configuration c) {
    for (Entry<String, HuaweiInterface> e : huaweiConfig.getInterfaces().entrySet()) {
      String name = e.getKey();
      HuaweiInterface huaweiIface = e.getValue();
      Interface iface = toInterface(name, huaweiIface);
      c.getAllInterfaces().put(name, iface);
    }
  }

  /**
   * Converts a single Huawei interface to a Batfish vendor-independent interface.
   *
   * @param name The interface name
   * @param huaweiInterface The Huawei interface to convert
   * @return A Batfish Interface object
   */
  private static @Nonnull Interface toInterface(
      @Nonnull String name, @Nonnull HuaweiInterface huaweiInterface) {
    Interface.Builder builder = Interface.builder();

    // Set name and type based on interface name
    builder.setName(name).setType(getInterfaceType(name));

    // Set description
    if (huaweiInterface.getDescription() != null) {
      builder.setDescription(huaweiInterface.getDescription());
    }

    // Set admin status (active if not shutdown)
    builder.setAdminUp(!huaweiInterface.getShutdown());

    // Set address
    if (huaweiInterface.getAddress() != null) {
      builder.setAddress(huaweiInterface.getAddress());
    }

    // Set MTU
    if (huaweiInterface.getMtu() != 0) {
      builder.setMtu(huaweiInterface.getMtu());
    }

    // Set bandwidth (explicit or default)
    Double bandwidth = huaweiInterface.getBandwidth();
    if (bandwidth == null) {
      bandwidth = HuaweiInterface.getDefaultBandwidth(name);
    }
    if (bandwidth != null) {
      builder.setSpeed(bandwidth);
    }

    // Set incoming filter if present (TODO: Convert ACL name to IpAccessList)
    // if (huaweiInterface.getIncomingFilter() != null) {
    //   builder.setIncomingFilter(huaweiInterface.getIncomingFilter());
    // }

    // Set outgoing filter if present (TODO: Convert ACL name to IpAccessList)
    // if (huaweiInterface.getOutgoingFilter() != null) {
    //   builder.setOutgoingFilter(huaweiInterface.getOutgoingFilter());
    // }

    // Set DHCP relay addresses
    if (!huaweiInterface.getDhcpRelayAddresses().isEmpty()) {
      builder.setDhcpRelayAddresses(ImmutableList.copyOf(huaweiInterface.getDhcpRelayAddresses()));
    }

    return builder.build();
  }

  /**
   * Converts Huawei interface configurations to Batfish vendor-independent interfaces.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   * @param vrf The VRF to attach interfaces to
   */
  public static void toConfigurationInterfaces(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg, @Nonnull Vrf vrf) {

    for (Map.Entry<String, HuaweiInterface> entry : huaweiCfg.getInterfaces().entrySet()) {
      HuaweiInterface huaweiIface = entry.getValue();

      // Building with owner=c automatically adds to c.getAllInterfaces()
      toInterface(huaweiIface, vrf, c);
    }
  }

  /**
   * Converts a single Huawei interface to a Batfish vendor-independent interface.
   *
   * @param huaweiIface The Huawei interface to convert
   * @param vrf The VRF to attach the interface to
   * @param c The Configuration that owns the interface
   * @return A Batfish Interface object
   */
  public static @Nonnull Interface toInterface(
      @Nonnull HuaweiInterface huaweiIface, @Nonnull Vrf vrf, @Nonnull Configuration c) {
    String name = huaweiIface.getName();
    // Use builder pattern
    Interface.Builder builder =
        Interface.builder()
            .setName(name)
            .setType(getInterfaceType(name))
            .setVrf(vrf)
            .setVrf(vrf)
            .setOwner(c)
            .setAdminUp(!huaweiIface.getShutdown())
            .setMtu(huaweiIface.getMtu());

    // Set address if present
    if (huaweiIface.getAddress() != null) {
      builder.setAddress(huaweiIface.getAddress());
    }

    // Set description if present
    if (huaweiIface.getDescription() != null) {
      builder.setDescription(huaweiIface.getDescription());
    }

    // Set bandwidth if present
    if (huaweiIface.getBandwidth() != null) {
      builder.setSpeed(huaweiIface.getBandwidth());
    } else {
      // Set default bandwidth based on interface type
      Double defaultSpeed = HuaweiInterface.getDefaultBandwidth(huaweiIface.getName());
      if (defaultSpeed != null) {
        builder.setSpeed(defaultSpeed);
      }
    }

    // Set DHCP relay addresses
    if (!huaweiIface.getDhcpRelayAddresses().isEmpty()) {
      builder.setDhcpRelayAddresses(ImmutableList.copyOf(huaweiIface.getDhcpRelayAddresses()));
    }

    return builder.build();
    // Set DHCP relay client flag
  }

  /**
   * Determines the Batfish InterfaceType for a Huawei interface based on its name.
   *
   * <p>Maps Huawei interface types to Batfish vendor-independent interface types:
   *
   * <ul>
   *   <li>GigabitEthernet, 10GE, 25GE, 40GE, 100GE, Ethernet, Pos, Serial → PHYSICAL
   *   <li>Vlanif → VLAN
   *   <li>LoopBack → LOOPBACK
   *   <li>Eth-Trunk → AGGREGATED
   *   <li>Subinterfaces (GigabitEthernet0/0/0.1, etc.) → LOGICAL or AGGREGATE_CHILD
   *   <li>Tunnel → TUNNEL
   *   <li>Null → NULL
   * </ul>
   *
   * @param interfaceName The Huawei interface name
   * @return The corresponding Batfish InterfaceType
   */
  private static @Nonnull InterfaceType getInterfaceType(@Nonnull String interfaceName) {
    // Check for subinterfaces (contain a dot with number after)
    if (interfaceName.contains(".")) {
      String[] parts = interfaceName.split("\\\\.");
      if (parts.length == 2) {
        try {
          // If there's a number after the dot, it's a subinterface
          Integer.parseInt(parts[1]);
          // Determine if parent is aggregate
          String parentName = parts[0];
          if (parentName.startsWith("Eth-Trunk") || parentName.startsWith("Port-channel")) {
            return InterfaceType.AGGREGATE_CHILD;
          }
          return InterfaceType.LOGICAL;
        } catch (NumberFormatException e) {
          // Not a subinterface, continue checking
        }
      }
    }

    // Check for specific interface types
    if (interfaceName.startsWith("GigabitEthernet")
        || interfaceName.startsWith("10GE")
        || interfaceName.startsWith("25GE")
        || interfaceName.startsWith("40GE")
        || interfaceName.startsWith("100GE")
        || interfaceName.startsWith("GE")
        || interfaceName.startsWith("Ethernet")
        || interfaceName.startsWith("FastEthernet")
        || interfaceName.startsWith("Pos")
        || interfaceName.startsWith("Serial")
        || interfaceName.startsWith("XG")) {
      return InterfaceType.PHYSICAL;
    }

    if (interfaceName.startsWith("Vlanif")) {
      return InterfaceType.VLAN;
    }

    if (interfaceName.startsWith("LoopBack") || interfaceName.startsWith("Loopback")) {
      return InterfaceType.LOOPBACK;
    }

    if (interfaceName.startsWith("Eth-Trunk") || interfaceName.startsWith("Port-channel")) {
      return InterfaceType.AGGREGATED;
    }

    if (interfaceName.startsWith("Tunnel") || interfaceName.startsWith("Gre")) {
      return InterfaceType.TUNNEL;
    }

    if (interfaceName.startsWith("Inherit-Vlan") || interfaceName.startsWith("Dot1q")) {
      return InterfaceType.AGGREGATE_CHILD;
    }

    if (interfaceName.startsWith("Null")) {
      return InterfaceType.NULL;
    }

    // Default to PHYSICAL for unknown types
    return InterfaceType.PHYSICAL;
  }

  /**
   * Converts Huawei BGP process to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationBgp(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    HuaweiBgpProcess huaweiBgp = huaweiCfg.getBgpProcess();
    if (huaweiBgp == null) {
      return;
    }

    // Get default VRF
    Vrf vrf = c.getVrfs().get(DEFAULT_VRF_NAME);
    if (vrf == null) {
      return;
    }

    // Build BGP process
    BgpProcess.Builder bgpBuilder = BgpProcess.builder();

    // Set router ID (use loopback address if router ID not configured)
    Ip routerId = huaweiBgp.getRouterId();
    if (routerId == null) {
      // Try to find loopback interface
      routerId =
          c.getAllInterfaces().values().stream()
              .filter(iface -> iface.getName().contains("Loopback"))
              .filter(iface -> iface.getAddress() != null)
              .map(
                  iface -> {
                    InterfaceAddress addr = iface.getAddress();
                    if (addr instanceof org.batfish.datamodel.ConcreteInterfaceAddress) {
                      return ((org.batfish.datamodel.ConcreteInterfaceAddress) addr).getIp();
                    }
                    return Ip.ZERO;
                  })
              .findFirst()
              .orElse(Ip.ZERO);
    }
    bgpBuilder.setRouterId(routerId);

    // Set administrative costs (Huawei defaults: eBGP=20, iBGP=255)
    bgpBuilder.setEbgpAdminCost(20).setIbgpAdminCost(255).setLocalAdminCost(255);

    // Set tie-breakers
    bgpBuilder
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP);

    // Build and set BGP process
    BgpProcess bgpProcess = bgpBuilder.build();
    vrf.setBgpProcess(bgpProcess);

    // Convert BGP neighbors/peers
    // Note: HuaweiBgpProcess already stores neighbors as BgpPeerConfig objects
    // We need to convert them to BgpActivePeerConfig
    huaweiBgp
        .getNeighbors()
        .forEach(
            (peerIp, peerConfig) -> {
              // If it's already an active peer config, use it directly
              if (peerConfig instanceof BgpActivePeerConfig) {
                bgpProcess.getActiveNeighbors().put(peerIp, (BgpActivePeerConfig) peerConfig);
              } else {
                // Otherwise create a new active peer config
                BgpActivePeerConfig.Builder builder = BgpActivePeerConfig.builder();
                builder.setPeerAddress(peerIp);
                // Additional peer settings would be copied here if the peerConfig has them
                bgpProcess.getActiveNeighbors().put(peerIp, builder.build());
              }
            });

    // TODO: Convert peer groups
    // TODO: Convert network announcements
    // TODO: Convert address families
    // TODO: Convert route maps and policies
  }

  /**
   * Converts Huawei OSPF process to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationOspf(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    HuaweiOspfProcess huaweiOspf = huaweiCfg.getOspfProcess();
    if (huaweiOspf == null) {
      return;
    }

    // Get default VRF
    Vrf vrf = c.getVrfs().get(DEFAULT_VRF_NAME);
    if (vrf == null) {
      return;
    }

    // Build OSPF process
    OspfProcess.Builder ospfBuilder = OspfProcess.builder();

    // Set process ID (use string representation)
    ospfBuilder.setProcessId(String.valueOf(huaweiOspf.getProcessId()));

    // Set router ID (use loopback if not configured)
    Ip routerId = huaweiOspf.getRouterId();
    if (routerId == null) {
      // Try to find loopback interface
      routerId =
          c.getAllInterfaces().values().stream()
              .filter(iface -> iface.getName().contains("Loopback"))
              .filter(iface -> iface.getAddress() != null)
              .map(
                  iface -> {
                    InterfaceAddress addr = iface.getAddress();
                    if (addr instanceof org.batfish.datamodel.ConcreteInterfaceAddress) {
                      return ((org.batfish.datamodel.ConcreteInterfaceAddress) addr).getIp();
                    }
                    return Ip.ZERO;
                  })
              .findFirst()
              .orElse(Ip.ZERO);
    }
    ospfBuilder.setRouterId(routerId);

    // Set reference bandwidth (Huawei default: 100 Mbps)
    ospfBuilder.setReferenceBandwidth(100000000.0);

    // Set VRF
    ospfBuilder.setVrf(vrf);

    // Convert OSPF areas
    ImmutableMap.Builder<Long, OspfArea> areasBuilder = ImmutableMap.builder();
    for (HuaweiOspfProcess.HuaweiOspfArea huaweiArea : huaweiOspf.getAreas().values()) {
      OspfArea area = toOspfArea(huaweiArea, c);
      areasBuilder.put(huaweiArea.getAreaId(), area);
    }
    ospfBuilder.setAreas(areasBuilder.build());

    // Build and set OSPF process
    OspfProcess ospfProcess = ospfBuilder.build();
    vrf.setOspfProcesses(ImmutableSortedMap.of(ospfProcess.getProcessId(), ospfProcess));
  }

  /**
   * Converts Huawei static routes to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationStaticRoutes(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiStaticRoute huaweiRoute : huaweiCfg.getStaticRoutes()) {
      // Build the static route
      StaticRoute.Builder builder =
          StaticRoute.builder()
              .setNetwork(huaweiRoute.getDestination())
              .setNextHopIp(huaweiRoute.getNextHopIp());

      // Set next-hop interface if present
      if (huaweiRoute.getNextHopInterface() != null) {
        builder.setNextHopInterface(huaweiRoute.getNextHopInterface());
      }

      // Set administrative distance (preference)
      builder.setAdministrativeCost(huaweiRoute.getPreference());

      // Build the route
      StaticRoute route = builder.build();

      // Add to appropriate VRF
      String vrfName = huaweiRoute.getVrfName();
      if (vrfName == null) {
        vrfName = DEFAULT_VRF_NAME;
      }

      // Get or create VRF
      Vrf vrf = c.getVrfs().get(vrfName);
      if (vrf == null) {
        vrf = Vrf.builder().setName(vrfName).build();
        c.getVrfs().put(vrfName, vrf);
      }

      // Add static route to VRF
      vrf.getStaticRoutes().add(route);
    }
  }

  /**
   * Converts Huawei ACLs to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationAcls(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiAcl huaweiAcl : huaweiCfg.getAcls().values()) {
      IpAccessList ipAccessList = toIpAccessList(huaweiAcl);
      c.getIpAccessLists().put(ipAccessList.getName(), ipAccessList);
    }
  }

  /**
   * Converts a Huawei ACL to a Batfish IpAccessList.
   *
   * @param huaweiAcl The Huawei ACL to convert
   * @return A Batfish IpAccessList
   */
  private static @Nonnull IpAccessList toIpAccessList(@Nonnull HuaweiAcl huaweiAcl) {
    // Convert each ACL line to ExprAclLine
    ImmutableList.Builder<AclLine> linesBuilder = ImmutableList.builder();
    for (HuaweiAclLine huaweiLine : huaweiAcl.getLines()) {
      ExprAclLine line = toAclLine(huaweiLine);
      if (line != null) {
        linesBuilder.add(line);
      }
    }

    return IpAccessList.builder()
        .setName(huaweiAcl.getName())
        .setLines(linesBuilder.build())
        .setSourceName(huaweiAcl.getName())
        .setSourceType("Huawei ACL")
        .build();
  }

  /**
   * Converts a Huawei ACL line to a Batfish ExprAclLine.
   *
   * @param huaweiLine The Huawei ACL line to convert
   * @return A Batfish ExprAclLine, or null if conversion fails
   */
  private static @Nullable ExprAclLine toAclLine(@Nonnull HuaweiAclLine huaweiLine) {
    // Convert action (permit/deny)
    LineAction action;
    String actionStr = huaweiLine.getAction().toLowerCase();
    if ("permit".equals(actionStr)) {
      action = LineAction.PERMIT;
    } else if ("deny".equals(actionStr)) {
      action = LineAction.DENY;
    } else {
      // Unknown action, skip this line
      return null;
    }

    // Build match conditions
    ImmutableList.Builder<AclLineMatchExpr> matchConditions = ImmutableList.builder();

    // Match protocol if specified
    if (huaweiLine.getProtocol() != null) {
      IpProtocol ipProtocol = toIpProtocol(huaweiLine.getProtocol());
      if (ipProtocol != null) {
        matchConditions.add(matchIpProtocol(ipProtocol));
      }
    }

    // Build HeaderSpace for IP addresses and ports
    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();

    // Source IP
    if (huaweiLine.getSource() != null) {
      Prefix srcPrefix = parsePrefix(huaweiLine.getSource());
      if (srcPrefix != null) {
        headerSpaceBuilder.setSrcIps(srcPrefix.toIpSpace());
      }
    }

    // Destination IP
    if (huaweiLine.getDestination() != null) {
      Prefix dstPrefix = parsePrefix(huaweiLine.getDestination());
      if (dstPrefix != null) {
        headerSpaceBuilder.setDstIps(dstPrefix.toIpSpace());
      }
    }

    // Source port - convert to SubRange
    if (huaweiLine.getSourcePort() != null) {
      SubRange srcPortRange = parsePortSpecToSubRange(huaweiLine.getSourcePort());
      if (srcPortRange != null) {
        headerSpaceBuilder.setSrcPorts(srcPortRange);
      }
    }

    // Destination port - convert to SubRange
    if (huaweiLine.getDestinationPort() != null) {
      SubRange dstPortRange = parsePortSpecToSubRange(huaweiLine.getDestinationPort());
      if (dstPortRange != null) {
        headerSpaceBuilder.setDstPorts(dstPortRange);
      }
    }

    // If we have IP/port conditions, wrap in MatchHeaderSpace
    AclLineMatchExpr matchCondition;
    List<AclLineMatchExpr> conditions = matchConditions.build();

    if (headerSpaceBuilder.build().getSrcIps() != null
        || headerSpaceBuilder.build().getDstIps() != null
        || headerSpaceBuilder.build().getSrcPorts() != null
        || headerSpaceBuilder.build().getDstPorts() != null) {
      // Add HeaderSpace to conditions
      matchConditions.add(new MatchHeaderSpace(headerSpaceBuilder.build()));
    }

    // Combine all conditions with AND
    if (conditions.isEmpty()) {
      // No specific conditions, match all
      matchCondition = org.batfish.datamodel.acl.TrueExpr.INSTANCE;
    } else if (conditions.size() == 1) {
      matchCondition = conditions.get(0);
    } else {
      matchCondition = and(conditions);
    }

    // Build the ExprAclLine
    return ExprAclLine.builder()
        .setAction(action)
        .setMatchCondition(matchCondition)
        .setName(String.valueOf(huaweiLine.getSequenceNumber()))
        .build();
  }

  /**
   * Parses a Huawei ACL prefix string (e.g., "192.168.1.0 0.0.0.255" or "10.1.1.0/24").
   *
   * @param prefixStr The prefix string to parse
   * @return A Prefix object, or null if parsing fails
   */
  private static @Nullable Prefix parsePrefix(@Nonnull String prefixStr) {
    try {
      // Try CIDR notation first (e.g., "10.1.1.0/24")
      if (prefixStr.contains("/")) {
        String[] parts = prefixStr.split("/");
        if (parts.length == 2) {
          Ip addr = Ip.parse(parts[0]);
          int prefixLen = Integer.parseInt(parts[1]);
          return Prefix.create(addr, prefixLen);
        }
      }

      // Try wildcard mask notation (e.g., "192.168.1.0 0.0.0.255")
      String[] parts = prefixStr.trim().split("\\s+");
      if (parts.length == 2) {
        Ip addr = Ip.parse(parts[0]);
        Ip wildcard = Ip.parse(parts[1]);
        // Convert wildcard mask to prefix length
        long wildcardLong = wildcard.asLong() & 0xFFFFFFFFL;
        int prefixLen = 32 - Long.bitCount(wildcardLong);
        return Prefix.create(addr, prefixLen);
      }

      // Try simple IP address
      return Prefix.create(Ip.parse(prefixStr), 32);
    } catch (Exception e) {
      // Failed to parse, return null
      return null;
    }
  }

  /**
   * Parses a Huawei port specification (e.g., "eq 80", "range 100 200", "gt 1023") to a SubRange.
   *
   * @param portSpec The port specification string
   * @return A SubRange representing the ports, or null if parsing fails
   */
  private static @Nullable SubRange parsePortSpecToSubRange(@Nonnull String portSpec) {
    try {
      String spec = portSpec.trim().toLowerCase();

      if (spec.startsWith("eq ")) {
        // Equal to a specific port
        int port = Integer.parseInt(spec.substring(3).trim());
        return new SubRange(port, port);
      } else if (spec.startsWith("gt ")) {
        // Greater than a port
        int port = Integer.parseInt(spec.substring(3).trim()) + 1;
        return new SubRange(port, 65535);
      } else if (spec.startsWith("lt ")) {
        // Less than a port
        int port = Integer.parseInt(spec.substring(3).trim()) - 1;
        return new SubRange(0, port);
      } else if (spec.startsWith("range ")) {
        // Range of ports
        String[] rangeParts = spec.substring(6).trim().split("\\s+");
        if (rangeParts.length == 2) {
          int start = Integer.parseInt(rangeParts[0]);
          int end = Integer.parseInt(rangeParts[1]);
          return new SubRange(start, end);
        }
        // Invalid range format, return all ports
        return new SubRange(0, 65535);
      } else if (spec.startsWith("neq ")) {
        // Not equal to a port - this doesn't map well to SubRange
        // Return all ports as fallback
        return new SubRange(0, 65535);
      } else {
        // Try parsing as a single port number
        int port = Integer.parseInt(spec);
        return new SubRange(port, port);
      }
    } catch (Exception e) {
      // If parsing fails, return all ports
      return new SubRange(0, 65535);
    }
  }

  /**
   * Converts a Huawei protocol string to Batfish IpProtocol.
   *
   * @param protocol The protocol string (e.g., "tcp", "udp", "icmp", "ip")
   * @return An IpProtocol object, or null if unknown
   */
  private static @Nullable IpProtocol toIpProtocol(@Nonnull String protocol) {
    String proto = protocol.toLowerCase().trim();
    switch (proto) {
      case "tcp":
        return IpProtocol.TCP;
      case "udp":
        return IpProtocol.UDP;
      case "icmp":
        return IpProtocol.ICMP;
      case "ip":
      case "":
        return null; // IP protocol means "any", so return null to match all
      case "gre":
        return IpProtocol.GRE;
      case "ospf":
        return IpProtocol.OSPF;
      case "pim":
        return IpProtocol.PIM;
      case "sctp":
        return IpProtocol.SCTP;
      case "ah":
        // AH is not a standard constant in IpProtocol, use number 51
        return IpProtocol.fromNumber(51);
      default:
        // Try to parse as protocol number
        try {
          int protoNum = Integer.parseInt(proto);
          if (protoNum >= 0 && protoNum <= 255) {
            return IpProtocol.fromNumber(protoNum);
          }
        } catch (NumberFormatException e) {
          // Not a number, return null
        }
        return null;
    }
  }

  /**
   * Converts Huawei NAT rules to Batfish vendor-independent format.
   *
   * <p>Converts Huawei NAT rules including static NAT, dynamic NAT, Easy IP, and NAT server
   * configurations.
   *
   * <p>Note: Batfish doesn't have a generic NAT model in the vendor-independent Configuration
   * class. NAT rules are kept in their original form in the HuaweiConfiguration for reference. In
   * the future, specific NAT rules could be converted to generic Batfish structures (e.g., service
   * forwarding rules for NAT Server).
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationNat(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    if (huaweiCfg.getNatRules().isEmpty()) {
      return;
    }

    // Indicate that NAT is configured in the vendor family
    HuaweiFamily huaweiFamily = c.getVendorFamily().getHuawei();
    if (huaweiFamily == null) {
      huaweiFamily = new HuaweiFamily();
      c.getVendorFamily().setHuawei(huaweiFamily);
    }

    // TODO: In the future, consider converting some NAT rules to generic Batfish structures:
    // - Static NAT: Could be represented as static IP mappings in vendor-independent format
    // - NAT Server (port forwarding): Could be represented as service forwarding rules
    // - For now, NAT rules are preserved in their original Huawei-specific representation
    // and can be accessed via huaweiCfg.getNatRules()
  }

  /**
   * Converts Huawei VRF configurations to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationVrfs(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiVrf huaweiVrf : huaweiCfg.getVrfs().values()) {
      String vrfName = huaweiVrf.getName();

      // Skip default VRF as it's already created
      if (DEFAULT_VRF_NAME.equals(vrfName)) {
        continue;
      }

      // Create VRF builder
      Vrf.Builder vrfBuilder = Vrf.builder().setName(vrfName);

      // Set route distinguisher if present
      // TODO: Parse and convert RD string to RouteDistinguisher object
      // if (huaweiVrf.getRouteDistinguisher() != null) {
      //   RouteDistinguisher rd = RouteDistinguisher.parse(huaweiVrf.getRouteDistinguisher());
      //   vrfBuilder.setRouteDistinguisher(rd);
      // }

      // Build the VRF
      Vrf vrf = vrfBuilder.build();

      // Add to configuration
      c.getVrfs().put(vrfName, vrf);
    }
  }

  /**
   * Converts a Huawei OSPF area to Batfish OspfArea.
   *
   * @param huaweiArea The Huawei OSPF area
   * @param c The Batfish Configuration
   * @return A Batfish OspfArea
   */
  @SuppressWarnings("unused") // Configuration parameter will be used in future implementation
  private static @Nonnull OspfArea toOspfArea(
      @Nonnull HuaweiOspfProcess.HuaweiOspfArea huaweiArea, @Nonnull Configuration c) {

    OspfArea.Builder builder = OspfArea.builder();

    // Set area number
    builder.setNumber(huaweiArea.getAreaId());

    // TODO: Convert area type (stub, NSSA, normal)
    // TODO: Convert area ranges
    // TODO: Convert area authentication
    // TODO: Convert area summary settings

    return builder.build();
  }

  private HuaweiConversions() {
    // Prevent instantiation
  }
}
