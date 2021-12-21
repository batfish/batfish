package org.batfish.representation.frr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;

public class MockFrrVendorConfiguration extends FrrVendorConfiguration {

  private FrrConfiguration _frr;
  private Set<String> _interfaces;
  private Map<String, String> _interfaceVrf;
  private Map<String, List<ConcreteInterfaceAddress>> _interfaceAddresses;
  private Set<String> _vrfs;
  private Map<String, Vxlan> _vxlans;

  @Override
  public FrrConfiguration getFrrConfiguration() {
    return _frr;
  }

  @Override
  public boolean hasInterface(String ifaceName) {
    return _interfaces.contains(ifaceName);
  }

  @Override
  public String getInterfaceVrf(String ifaceName) {
    return _interfaceVrf.get(ifaceName);
  }

  @Override
  public @Nonnull List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName) {
    return _interfaceAddresses.get(ifaceName);
  }

  @Override
  public boolean hasVrf(String vrfName) {
    return _vrfs.contains(vrfName);
  }

  @Override
  public Map<String, Vxlan> getVxlans() {
    return _vxlans;
  }

  @Override
  public void referenceStructure(
      FrrStructureType type, String name, FrrStructureUsage usage, int line) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void defineStructure(FrrStructureType type, String name, ParserRuleContext ctx) {
    throw new UnsupportedOperationException();
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String getHostname() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHostname(String hostname) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    throw new UnsupportedOperationException();
  }

  // always build via the builder
  private MockFrrVendorConfiguration() {}

  public static final class Builder {
    private FrrConfiguration _frr;
    private Set<String> _interfaces;
    private Map<String, String> _interfaceVrf;
    private Map<String, List<ConcreteInterfaceAddress>> _interfaceAddresses;
    private Set<String> _vrfs;
    private Map<String, Vxlan> _vxlans;

    private Builder() {}

    public Builder setFrrConfiguration(FrrConfiguration frr) {
      this._frr = frr;
      return this;
    }

    public Builder setInterfaces(Set<String> interfaces) {
      this._interfaces = interfaces;
      return this;
    }

    public Builder setInterfaceVrf(Map<String, String> interfaceVrf) {
      this._interfaceVrf = interfaceVrf;
      return this;
    }

    public Builder setInterfaceAddresses(
        Map<String, List<ConcreteInterfaceAddress>> interfaceAddresses) {
      this._interfaceAddresses = interfaceAddresses;
      return this;
    }

    public Builder setHasVrf(Set<String> vrfs) {
      this._vrfs = vrfs;
      return this;
    }

    public Builder setVxlans(Map<String, Vxlan> vxlans) {
      this._vxlans = vxlans;
      return this;
    }

    public MockFrrVendorConfiguration build() {
      MockFrrVendorConfiguration mockOutOfBandConfiguration = new MockFrrVendorConfiguration();
      mockOutOfBandConfiguration._frr = firstNonNull(this._frr, new FrrConfiguration());
      mockOutOfBandConfiguration._vxlans = firstNonNull(this._vxlans, ImmutableMap.of());
      mockOutOfBandConfiguration._interfaceVrf =
          firstNonNull(this._interfaceVrf, ImmutableMap.of());
      mockOutOfBandConfiguration._interfaceAddresses =
          firstNonNull(this._interfaceAddresses, ImmutableMap.of());
      mockOutOfBandConfiguration._vrfs = firstNonNull(this._vrfs, ImmutableSet.of());
      mockOutOfBandConfiguration._interfaces = firstNonNull(this._interfaces, ImmutableSet.of());
      return mockOutOfBandConfiguration;
    }
  }
}
