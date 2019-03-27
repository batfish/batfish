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

  public CumulusNcluConfiguration() {
    _bonds = new HashMap<>();
    _interfaces = new HashMap<>();
    _ipv4Nameservers = new LinkedList<>();
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

  public List<Ip> getIpv4Nameservers() {
    return _ipv4Nameservers;
  }

  private void markStructures() {
    markConcreteStructure(CumulusStructureType.BOND, CumulusStructureUsage.BOND_SELF_REFERENCE);
    markConcreteStructure(CumulusStructureType.INTERFACE, CumulusStructureUsage.BOND_SLAVE);
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
