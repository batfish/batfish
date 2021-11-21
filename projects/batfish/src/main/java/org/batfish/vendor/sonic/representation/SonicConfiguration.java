package org.batfish.vendor.sonic.representation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.representation.frr.FrrConfiguration;
import org.batfish.representation.frr.OutOfBandConfiguration;
import org.batfish.representation.frr.Vxlan;
import org.batfish.vendor.VendorConfiguration;

@ParametersAreNonnullByDefault
public class SonicConfiguration extends VendorConfiguration implements OutOfBandConfiguration {

  private @Nullable String _hostname;
  private @Nullable ConfigDb _configDb;
  private @Nonnull final FrrConfiguration _frr;

  public SonicConfiguration() {
    _frr = new FrrConfiguration();
  }

  public void setConfigDb(ConfigDb configDb) {
    _configDb = configDb;
  }

  public FrrConfiguration getFrrConfiguration() {
    return _frr;
  }

  @Override
  public boolean hasInterface(String ifaceName) {
    return false;
  }

  @Override
  public boolean hasVrf(String vrfName) {
    return false;
  }

  @Override
  public String getInterfaceVrf(String ifaceName) {
    return null;
  }

  @Nonnull
  @Override
  public List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName) {
    return null;
  }

  @Override
  public Map<String, Vxlan> getVxlans() {
    return null;
  }

  @Override
  public Optional<String> getVrfForVlan(Integer bridgeAccessVlan) {
    return Optional.empty();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    return null;
  }
}
