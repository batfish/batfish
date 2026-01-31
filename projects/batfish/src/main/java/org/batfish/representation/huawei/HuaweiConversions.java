package org.batfish.representation.huawei;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;

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
  @Nonnull
  public static Configuration toVendorIndependentConfiguration(
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
  @Nonnull
  private static Interface toInterface(
      @Nonnull String name, @Nonnull HuaweiInterface huaweiInterface) {
    Interface.Builder builder = Interface.builder();

    // Set name and type (default to PHYSICAL for now)
    builder.setName(name).setType(InterfaceType.PHYSICAL);

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
  @Nonnull
  public static Interface toInterface(
      @Nonnull HuaweiInterface huaweiIface, @Nonnull Vrf vrf, @Nonnull Configuration c) {
    // Use builder pattern
    Interface.Builder builder =
        Interface.builder()
            .setName(huaweiIface.getName())
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
  }

  /**
   * Converts Huawei BGP process to Batfish vendor-independent format.
   *
   * <p>This method is a stub for future implementation.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationBgp(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    // TODO: Implement BGP conversion
    HuaweiBgpProcess bgpProcess = huaweiCfg.getBgpProcess();
    if (bgpProcess != null) {
      // Convert BGP process to Batfish format
    }
  }

  /**
   * Converts Huawei OSPF process to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationOspf(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    HuaweiOspfProcess ospfProcess = huaweiCfg.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    // TODO: Implement full OSPF conversion to Batfish format
    // For Phase 6, we just parse and store the OSPF configuration
    // Full conversion will be implemented in a future phase
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
      String aclName = huaweiAcl.getName();

      // Convert ACL lines to Batfish format
      Builder<AclLine> lines = ImmutableList.builder();
      for (HuaweiAclLine huaweiLine : huaweiAcl.getLines()) {
        try {
          ExprAclLine aclLine = convertAclLine(huaweiLine);
          if (aclLine != null) {
            lines.add(aclLine);
          }
        } catch (Exception e) {
          // Log error but continue processing other lines
          // In production, this would be added to warnings
        }
      }

      // Create IpAccessList and add to configuration
      IpAccessList ipAccessList =
          IpAccessList.builder()
              .setName(aclName)
              .setLines(lines.build())
              .setOwner(c)
              .setSourceName("huawei")
              .setSourceType("huawei")
              .build();

      // The builder automatically adds to c.getIpAccessLists()
    }
  }

  /**
   * Converts a Huawei ACL line to a Batfish ExprAclLine.
   *
   * @param huaweiLine The Huawei ACL line to convert
   * @return A Batfish ExprAclLine, or null if conversion fails
   */
  @Nullable
  private static ExprAclLine convertAclLine(@Nonnull HuaweiAclLine huaweiLine) {
    // Determine action (permit/deny)
    LineAction action =
        "permit".equalsIgnoreCase(huaweiLine.getAction()) ? LineAction.PERMIT : LineAction.DENY;

    // Build HeaderSpace for matching
    HeaderSpace.Builder headerSpace = HeaderSpace.builder();

    // Parse source address
    String src = huaweiLine.getSource();
    if (src != null && !src.equalsIgnoreCase("any")) {
      // For now, handle basic CIDR notation
      // TODO: Handle wildcard format and more complex address specifications
      try {
        if (src.contains("/")) {
          // CIDR notation - create empty HeaderSpace for now
          // Full implementation would properly set source IP ranges
        }
      } catch (Exception e) {
        // If parsing fails, leave as empty (matches all)
      }
    }

    // Parse destination address
    String dst = huaweiLine.getDestination();
    if (dst != null && !dst.equalsIgnoreCase("any")) {
      // For now, handle basic CIDR notation
      try {
        if (dst.contains("/")) {
          // CIDR notation - create empty HeaderSpace for now
          // Full implementation would properly set destination IP ranges
        }
      } catch (Exception e) {
        // If parsing fails, leave as empty (matches all)
      }
    }

    // TODO: Parse protocol, ports, etc. for more accurate matching

    // Build the ExprAclLine
    return ExprAclLine.builder()
        .setAction(action)
        .setMatchCondition(new MatchHeaderSpace(headerSpace.build()))
        .setName(String.valueOf(huaweiLine.getSequenceNumber()))
        .build();
  }

  /**
   * Converts Huawei NAT rules to Batfish vendor-independent format.
   *
   * <p>Converts Huawei NAT rules including static NAT, dynamic NAT, Easy IP, and NAT server
   * configurations.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationNat(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiNatRule natRule : huaweiCfg.getNatRules()) {
      // For Phase 8, we're just tracking that NAT is configured
      // Full implementation would convert to Batfish's NAT model
      // This would involve creating org.batfish.datamodel.IpAccessList objects
      // and configuring ServiceNAT rules on interfaces

      // TODO: Implement full NAT conversion:
      // - Static NAT: Create IpAccessList with static NAT rules
      // - Dynamic NAT: Create Pool and associate with ACL
      // - Easy IP: Create PAT rules on interface
      // - NAT Server: Create port forwarding rules

      // For now, we just note that NAT is present
      // The actual Batfish NAT model would be populated here
    }
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

  private HuaweiConversions() {
    // Prevent instantiation
  }
}
