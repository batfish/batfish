package org.batfish.representation.huawei;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Vrf;

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

    // TODO: Convert other features (BGP, OSPF, static routes, ACLs, NAT, etc.)

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
   * <p>This method is a stub for future implementation.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationOspf(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    // TODO: Implement OSPF conversion
    HuaweiOspfProcess ospfProcess = huaweiCfg.getOspfProcess();
    if (ospfProcess != null) {
      // Convert OSPF process to Batfish format
    }
  }

  /**
   * Converts Huawei static routes to Batfish vendor-independent format.
   *
   * <p>This method is a stub for future implementation.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationStaticRoutes(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    // TODO: Implement static route conversion
  }

  /**
   * Converts Huawei ACLs to Batfish vendor-independent format.
   *
   * <p>This method is a stub for future implementation.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationAcls(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    // TODO: Implement ACL conversion
  }

  /**
   * Converts Huawei NAT rules to Batfish vendor-independent format.
   *
   * <p>This method is a stub for future implementation.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationNat(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    // TODO: Implement NAT conversion
  }

  /**
   * Converts Huawei VRF configurations to Batfish vendor-independent format.
   *
   * <p>This method is a stub for future implementation.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationVrfs(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    // TODO: Implement VRF conversion
  }

  private HuaweiConversions() {
    // Prevent instantiation
  }
}
