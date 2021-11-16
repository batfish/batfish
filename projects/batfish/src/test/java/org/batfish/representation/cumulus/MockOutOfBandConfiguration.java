package org.batfish.representation.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;

public class MockOutOfBandConfiguration implements OutOfBandConfiguration {

  private Set<String> _interfaces;
  private Map<String, String> _interfaceVrf;
  private Map<String, List<ConcreteInterfaceAddress>> _interfaceAddresses;
  private Set<String> _vrfs;
  private Map<String, Vxlan> _vxlans;
  private Map<Integer, String> _vlanVrfs;
  private Map<String, InterfaceClagSettings> _clagSettings;
  private Map<String, Ip> _clagVxlanAnycastIps;
  private Map<String, Ip> _vxlanLocalTunnelIps;

  @Override
  public boolean hasInterface(String ifaceName) {
    return _interfaces.contains(ifaceName);
  }

  @Override
  public String getInterfaceVrf(String ifaceName) {
    return _interfaceVrf.get(ifaceName);
  }

  @Override
  public List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName) {
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
  public Optional<String> getVrfForVlan(Integer bridgeAccessVlan) {
    return Optional.of(_vlanVrfs.get(bridgeAccessVlan));
  }

  @Override
  public Map<String, InterfaceClagSettings> getClagSettings() {
    return _clagSettings;
  }

  @Override
  public Ip getClagVxlanAnycastIp(String ifaceName) {
    return _clagVxlanAnycastIps.get(ifaceName);
  }

  @Override
  public Ip getVxlanLocalTunnelIp(String ifaceName) {
    return _vxlanLocalTunnelIps.get(ifaceName);
  }

  public static Builder builder() {
    return new Builder();
  }

  // always build via the builder
  private MockOutOfBandConfiguration() {}

  public static final class Builder {
    private Map<String, String> _superInterfaceNames;
    private Set<String> _interfaces;
    private Map<String, String> _interfaceVrf;
    private Map<String, List<ConcreteInterfaceAddress>> _interfaceAddresses;
    private Set<String> _vrfs;
    private Map<String, Vxlan> _vxlans;
    private Map<Integer, String> _vlanVrfs;
    private Map<String, InterfaceClagSettings> _clagSettings;
    private Map<String, Ip> _clagVxlanAnycastIps;
    private Map<String, Ip> _vxlanLocalTunnelIps;

    private Builder() {}

    public Builder setSuperInterfaceNames(Map<String, String> superInterfaceNames) {
      this._superInterfaceNames = superInterfaceNames;
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

    public Builder setVlanVrfs(Map<Integer, String> vlanVrfs) {
      this._vlanVrfs = vlanVrfs;
      return this;
    }

    public Builder setClagSettings(Map<String, InterfaceClagSettings> clagSettings) {
      this._clagSettings = clagSettings;
      return this;
    }

    public Builder setClagVxlanAnycastIps(Map<String, Ip> clagVxlanAnycastIps) {
      this._clagVxlanAnycastIps = clagVxlanAnycastIps;
      return this;
    }

    public Builder setVxlanLocalTunnelIps(Map<String, Ip> vxlanLocalTunnelIps) {
      this._vxlanLocalTunnelIps = vxlanLocalTunnelIps;
      return this;
    }

    public MockOutOfBandConfiguration build() {
      MockOutOfBandConfiguration mockOutOfBandConfiguration = new MockOutOfBandConfiguration();
      mockOutOfBandConfiguration._vxlans = firstNonNull(this._vxlans, ImmutableMap.of());
      mockOutOfBandConfiguration._vlanVrfs = firstNonNull(this._vlanVrfs, ImmutableMap.of());
      mockOutOfBandConfiguration._clagSettings =
          firstNonNull(this._clagSettings, ImmutableMap.of());
      mockOutOfBandConfiguration._interfaceVrf =
          firstNonNull(this._interfaceVrf, ImmutableMap.of());
      mockOutOfBandConfiguration._interfaceAddresses =
          firstNonNull(this._interfaceAddresses, ImmutableMap.of());
      mockOutOfBandConfiguration._vrfs = firstNonNull(this._vrfs, ImmutableSet.of());
      mockOutOfBandConfiguration._interfaces = firstNonNull(this._interfaces, ImmutableSet.of());
      mockOutOfBandConfiguration._clagVxlanAnycastIps =
          firstNonNull(this._clagVxlanAnycastIps, ImmutableMap.of());
      mockOutOfBandConfiguration._vxlanLocalTunnelIps =
          firstNonNull(this._vxlanLocalTunnelIps, ImmutableMap.of());
      return mockOutOfBandConfiguration;
    }
  }
}
