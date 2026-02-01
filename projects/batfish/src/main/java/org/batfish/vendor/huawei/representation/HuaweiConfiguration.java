package org.batfish.vendor.huawei.representation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.ArrayList;
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
  private @Nullable String _hostname;

  /** Map of interface names to interface configurations */
  private SortedMap<String, HuaweiInterface> _interfaces;

  /** Map of VLAN IDs to VLAN configurations */
  private SortedMap<Integer, HuaweiVlan> _vlans;

  /** List of static routes */
  private @Nonnull List<HuaweiStaticRoute> _staticRoutes;

  /** List of NAT rules */
  private @Nonnull List<HuaweiNatRule> _natRules;

  /** BGP process configuration (stub for future implementation) */
  private @Nullable HuaweiBgpProcess _bgpProcess;

  /** OSPF process configuration (stub for future implementation) */
  private @Nullable HuaweiOspfProcess _ospfProcess;

  /** VRF configurations (stub for future implementation) */
  private SortedMap<String, HuaweiVrf> _vrfs;

  /** Map of ACL names/numbers to ACL configurations */
  private SortedMap<String, HuaweiAcl> _acls;

  public HuaweiConfiguration() {
    _interfaces = ImmutableSortedMap.of();
    _vlans = ImmutableSortedMap.of();
    _staticRoutes = new ArrayList<>();
    _natRules = new ArrayList<>();
    _vrfs = ImmutableSortedMap.of();
    _acls = ImmutableSortedMap.of();
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
  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  /**
   * Sets the hostname of the device.
   *
   * @param hostname The hostname to set
   */
  @Override
  public void setHostname(@Nullable String hostname) {
    _hostname = hostname;
  }

  /**
   * Gets the map of interfaces.
   *
   * @return A sorted map of interface names to interface configurations
   */
  public @Nonnull SortedMap<String, HuaweiInterface> getInterfaces() {
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
  public @Nullable HuaweiInterface getInterface(String name) {
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
   * Gets the map of VLANs.
   *
   * @return A sorted map of VLAN IDs to VLAN configurations
   */
  public @Nonnull SortedMap<Integer, HuaweiVlan> getVlans() {
    return _vlans;
  }

  /**
   * Sets the VLANs map.
   *
   * @param vlans The map of VLAN IDs to VLAN configurations
   */
  public void setVlans(@Nonnull SortedMap<Integer, HuaweiVlan> vlans) {
    _vlans = vlans;
  }

  /**
   * Gets a specific VLAN by ID.
   *
   * @param vlanId The VLAN ID
   * @return The VLAN configuration, or null if not found
   */
  public @Nullable HuaweiVlan getVlan(int vlanId) {
    return _vlans.get(vlanId);
  }

  /**
   * Adds or updates a VLAN.
   *
   * @param vlanId The VLAN ID
   * @param vlan The VLAN configuration
   */
  public void addVlan(int vlanId, HuaweiVlan vlan) {
    ImmutableSortedMap.Builder<Integer, HuaweiVlan> builder =
        ImmutableSortedMap.<Integer, HuaweiVlan>naturalOrder().putAll(_vlans);
    builder.put(vlanId, vlan);
    _vlans = builder.build();
  }

  /**
   * Gets the list of static routes.
   *
   * @return A list of static routes
   */
  public @Nonnull List<HuaweiStaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  /**
   * Sets the list of static routes.
   *
   * @param staticRoutes The list of static routes to set
   */
  public void setStaticRoutes(@Nonnull List<HuaweiStaticRoute> staticRoutes) {
    _staticRoutes = staticRoutes;
  }

  /**
   * Adds a static route to the configuration.
   *
   * @param route The static route to add
   */
  public void addStaticRoute(HuaweiStaticRoute route) {
    _staticRoutes.add(route);
  }

  /**
   * Gets the list of NAT rules.
   *
   * @return A list of NAT rules
   */
  public @Nonnull List<HuaweiNatRule> getNatRules() {
    return _natRules;
  }

  /**
   * Sets the list of NAT rules.
   *
   * @param natRules The list of NAT rules to set
   */
  public void setNatRules(@Nonnull List<HuaweiNatRule> natRules) {
    _natRules = natRules;
  }

  /**
   * Adds a NAT rule to the configuration.
   *
   * @param natRule The NAT rule to add
   */
  public void addNatRule(HuaweiNatRule natRule) {
    _natRules.add(natRule);
  }

  /**
   * Gets the BGP process configuration.
   *
   * @return The BGP process, or null if not configured
   */
  public @Nullable HuaweiBgpProcess getBgpProcess() {
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
  public @Nullable HuaweiOspfProcess getOspfProcess() {
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
  public @Nonnull SortedMap<String, HuaweiVrf> getVrfs() {
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

  /**
   * Gets the map of ACLs.
   *
   * @return A sorted map of ACL names/numbers to ACL configurations
   */
  public @Nonnull SortedMap<String, HuaweiAcl> getAcls() {
    return _acls;
  }

  /**
   * Sets the ACLs map.
   *
   * @param acls The map of ACL names/numbers to ACL configurations
   */
  public void setAcls(@Nonnull SortedMap<String, HuaweiAcl> acls) {
    _acls = acls;
  }

  /**
   * Gets a specific ACL by name or number.
   *
   * @param name The ACL name or number
   * @return The ACL configuration, or null if not found
   */
  public @Nullable HuaweiAcl getAcl(String name) {
    return _acls.get(name);
  }

  /**
   * Adds or updates an ACL.
   *
   * @param name The ACL name or number
   * @param acl The ACL configuration
   */
  public void addAcl(String name, HuaweiAcl acl) {
    ImmutableSortedMap.Builder<String, HuaweiAcl> builder =
        ImmutableSortedMap.<String, HuaweiAcl>naturalOrder().putAll(_acls);
    builder.put(name, acl);
    _acls = builder.build();
  }
}
