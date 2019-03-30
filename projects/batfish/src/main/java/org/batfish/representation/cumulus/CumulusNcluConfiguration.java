package org.batfish.representation.cumulus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;
import org.parboiled.common.ImmutableList;

/** A {@link VendorConfiguration} for the Cumulus NCLU configuration language. */
public class CumulusNcluConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Map<String, Bond> _bonds;
  private transient Configuration _c;
  private @Nullable String _hostname;
  private final @Nonnull Map<String, Interface> _interfaces;
  private final @Nonnull List<Ip> _ipv4Nameservers;
  private final @Nonnull List<Ip6> _ipv6Nameservers;
  private final @Nonnull Loopback _loopback;
  private final @Nonnull Map<String, Vlan> _vlans;
  private final @Nonnull Map<String, Vrf> _vrfs;

  public CumulusNcluConfiguration() {
    _bonds = new HashMap<>();
    _interfaces = new HashMap<>();
    _ipv4Nameservers = new LinkedList<>();
    _ipv6Nameservers = new LinkedList<>();
    _loopback = new Loopback();
    _vlans = new HashMap<>();
    _vrfs = new HashMap<>();
  }

  public @Nonnull Map<String, Bond> getBonds() {
    return _bonds;
  }

  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  public @Nonnull Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull List<Ip> getIpv4Nameservers() {
    return _ipv4Nameservers;
  }

  public @Nonnull List<Ip6> getIpv6Nameservers() {
    return _ipv6Nameservers;
  }

  public @Nonnull Loopback getLoopback() {
    return _loopback;
  }

  public @Nonnull Map<String, Vlan> getVlans() {
    return _vlans;
  }

  public @Nonnull Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  private void markStructures() {
    markConcreteStructure(CumulusStructureType.BOND, CumulusStructureUsage.BOND_SELF_REFERENCE);
    markConcreteStructure(
        CumulusStructureType.INTERFACE,
        CumulusStructureUsage.BOND_SLAVE,
        CumulusStructureUsage.INTERFACE_SELF_REFERENCE);
    markConcreteStructure(CumulusStructureType.VLAN, CumulusStructureUsage.VLAN_SELF_REFERENCE);
    markConcreteStructure(
        CumulusStructureType.VRF,
        CumulusStructureUsage.INTERFACE_CLAG_BACKUP_IP_VRF,
        CumulusStructureUsage.INTERFACE_VRF,
        CumulusStructureUsage.VLAN_VRF,
        CumulusStructureUsage.VRF_SELF_REFERENCE);
  }

  @Override
  public void setHostname(@Nullable String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    _c = new Configuration(getHostname(), ConfigurationFormat.CUMULUS_NCLU);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    markStructures();
    return _c;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }
}
