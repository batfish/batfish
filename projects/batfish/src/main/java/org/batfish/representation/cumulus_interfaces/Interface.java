package org.batfish.representation.cumulus_interfaces;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.batfish.representation.cumulus.InterfaceClagSettings;

/** Model of an iface block in a cumulus /etc/network/interfaces file. */
@ParametersAreNonnullByDefault
public final class Interface {
  private @Nullable List<ConcreteInterfaceAddress> _addresses;
  private @Nullable Map<MacAddress, Set<InterfaceAddress>> _addressVirtuals;
  private @Nullable Integer _bridgeAccess;
  private @Nullable InterfaceClagSettings _clagSettings;
  private @Nullable List<String> _bridgePorts;
  private @Nullable IntegerSpace _bridgeVids;
  private @Nullable Integer _clagId;
  private @Nullable String _description;
  private boolean _isVrf = false;
  private @Nullable Integer _linkSpeed;
  private final @Nonnull String _name;
  private @Nullable Integer _vlanId;
  private @Nullable String _vrf;
  private @Nullable String _vlanRawDevice;
  private @Nullable Ip _vxlanLocalTunnelIp;
  private @Nullable Integer _vxlanId;

  public Interface(@Nonnull String name) {
    _name = name;
  }

  public void addAddress(ConcreteInterfaceAddress address) {
    if (_addresses == null) {
      _addresses = new LinkedList<>();
    }
    _addresses.add(address);
  }

  @Nonnull
  public InterfaceClagSettings createOrGetClagSettings() {
    if (_clagSettings == null) {
      _clagSettings = new InterfaceClagSettings();
    }
    return _clagSettings;
  }

  @Nullable
  public List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }

  @Nullable
  public Map<MacAddress, Set<InterfaceAddress>> getAddressVirtuals() {
    return _addressVirtuals;
  }

  @Nullable
  public Integer getBridgeAccess() {
    return _bridgeAccess;
  }

  @Nullable
  public List<String> getBridgePorts() {
    return _bridgePorts;
  }

  @Nullable
  public IntegerSpace getBridgeVids() {
    return _bridgeVids;
  }

  @Nullable
  public Integer getClagId() {
    return _clagId;
  }

  @Nullable
  public InterfaceClagSettings getClagSettings() {
    return _clagSettings;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  public boolean getIsVrf() {
    return _isVrf;
  }

  @Nullable
  public Integer getLinkSpeed() {
    return _linkSpeed;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public Integer getVlanId() {
    return _vlanId;
  }

  @Nullable
  public String getVlanRawDevice() {
    return _vlanRawDevice;
  }

  @Nullable
  public String getVrf() {
    return _vrf;
  }

  @Nullable
  public Integer getVxlanId() {
    return _vxlanId;
  }

  @Nullable
  public Ip getVxlanLocalTunnelIp() {
    return _vxlanLocalTunnelIp;
  }

  public void setBridgeAccess(int vlanId) {
    _bridgeAccess = vlanId;
  }

  public void setBridgePorts(List<String> bridgePorts) {
    _bridgePorts = ImmutableList.copyOf(bridgePorts);
  }

  public void setBridgeVids(IntegerSpace bridgeVids) {
    _bridgeVids = bridgeVids;
  }

  public void setClagId(int clagId) {
    _clagId = clagId;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIsVrf() {
    _isVrf = true;
  }

  public void setLinkSpeed(int linkSpeed) {
    _linkSpeed = linkSpeed;
  }

  public void setVlanId(int vlanId) {
    _vlanId = vlanId;
  }

  public void setVlanRawDevice(String vlanRawDevice) {
    _vlanRawDevice = vlanRawDevice;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }

  public void setVxlanId(int vxlanId) {
    _vxlanId = vxlanId;
  }

  public void setVxlanLocalTunnelIp(Ip vxlanLocalTunnelIp) {
    _vxlanLocalTunnelIp = vxlanLocalTunnelIp;
  }

  public void setAddressVirtual(MacAddress macAddress, ConcreteInterfaceAddress address) {
    if (_addressVirtuals == null) {
      _addressVirtuals = new HashMap<>();
    }
    _addressVirtuals.computeIfAbsent(macAddress, k -> new HashSet<>()).add(address);
  }
}
