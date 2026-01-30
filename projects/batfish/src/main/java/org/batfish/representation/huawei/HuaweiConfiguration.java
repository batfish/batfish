package org.batfish.representation.huawei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

/**
 * Top-level configuration class for Huawei VRP devices.
 *
 * <p>This class stores the parsed configuration data from Huawei VRP configuration files. It serves
 * as the vendor-specific representation that will later be converted to Batfish's
 * vendor-independent Configuration format.
 */
public class HuaweiConfiguration extends VendorConfiguration implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Hostname of the device */
  @Nullable private String _hostname;

  /** Map of interface names to interface configurations */
  private SortedMap<String, HuaweiInterface> _interfaces;

  /** BGP process configuration (stub for future implementation) */
  @Nullable private HuaweiBgpProcess _bgpProcess;

  /** OSPF process configuration (stub for future implementation) */
  @Nullable private HuaweiOspfProcess _ospfProcess;

  /** VRF configurations (stub for future implementation) */
  private SortedMap<String, HuaweiVrf> _vrfs;

  public HuaweiConfiguration() {
    _interfaces = ImmutableSortedMap.of();
    _vrfs = ImmutableSortedMap.of();
  }

  /**
   * Converts this Huawei configuration to Batfish's vendor-independent Configuration format.
   *
   * @return A Batfish Configuration object
   */
  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    return ImmutableList.of(HuaweiConversions.toVendorIndependentConfiguration(this));
  }

  /**
   * Sets the vendor format for this configuration.
   *
   * @param format The configuration format
   */
  @Override
  public void setVendor(ConfigurationFormat format) {
    // Huawei configurations always use HUAWEI format
    // This method is required by VendorConfiguration but doesn't need to do anything
  }

  /**
   * Gets the hostname of the device.
   *
   * @return The hostname, or null if not set
   */
  @Nullable
  public String getHostname() {
    return _hostname;
  }

  /**
   * Sets the hostname of the device.
   *
   * @param hostname The hostname to set
   */
  public void setHostname(@Nullable String hostname) {
    _hostname = hostname;
  }

  /**
   * Gets the map of interfaces.
   *
   * @return A sorted map of interface names to interface configurations
   */
  @Nonnull
  public SortedMap<String, HuaweiInterface> getInterfaces() {
    return _interfaces;
  }

  /**
   * Sets the interfaces map.
   *
   * @param interfaces The map of interface names to interface configurations
   */
  public void setInterfaces(@Nonnull SortedMap<String, HuaweiInterface> interfaces) {
    _interfaces = interfaces;
  }

  /**
   * Gets a specific interface by name.
   *
   * @param name The interface name
   * @return The interface configuration, or null if not found
   */
  @Nullable
  public HuaweiInterface getInterface(String name) {
    return _interfaces.get(name);
  }

  /**
   * Adds or updates an interface.
   *
   * @param name The interface name
   * @param iface The interface configuration
   */
  public void addInterface(String name, HuaweiInterface iface) {
    ImmutableSortedMap.Builder<String, HuaweiInterface> builder =
        ImmutableSortedMap.<String, HuaweiInterface>naturalOrder().putAll(_interfaces);
    builder.put(name, iface);
    _interfaces = builder.build();
  }

  /**
   * Gets the BGP process configuration.
   *
   * @return The BGP process, or null if not configured
   */
  @Nullable
  public HuaweiBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  /**
   * Sets the BGP process configuration.
   *
   * @param bgpProcess The BGP process to set
   */
  public void setBgpProcess(@Nullable HuaweiBgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  /**
   * Gets the OSPF process configuration.
   *
   * @return The OSPF process, or null if not configured
   */
  @Nullable
  public HuaweiOspfProcess getOspfProcess() {
    return _ospfProcess;
  }

  /**
   * Sets the OSPF process configuration.
   *
   * @param ospfProcess The OSPF process to set
   */
  public void setOspfProcess(@Nullable HuaweiOspfProcess ospfProcess) {
    _ospfProcess = ospfProcess;
  }

  /**
   * Gets the map of VRF configurations.
   *
   * @return A sorted map of VRF names to VRF configurations
   */
  @Nonnull
  public SortedMap<String, HuaweiVrf> getVrfs() {
    return _vrfs;
  }

  /**
   * Sets the VRF configurations.
   *
   * @param vrfs The map of VRF names to VRF configurations
   */
  public void setVrfs(@Nonnull SortedMap<String, HuaweiVrf> vrfs) {
    _vrfs = vrfs;
  }

  /**
   * Adds or updates a VRF.
   *
   * @param name The VRF name
   * @param vrf The VRF configuration
   */
  public void addVrf(String name, HuaweiVrf vrf) {
    ImmutableSortedMap.Builder<String, HuaweiVrf> builder =
        ImmutableSortedMap.<String, HuaweiVrf>naturalOrder().putAll(_vrfs);
    builder.put(name, vrf);
    _vrfs = builder.build();
  }
}
