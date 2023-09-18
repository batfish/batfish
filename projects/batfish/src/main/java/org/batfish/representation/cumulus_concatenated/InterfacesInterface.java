package org.batfish.representation.cumulus_concatenated;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.representation.cumulus_concatenated.CumulusConcatenatedConfiguration.VLAN_INTERFACE_PATTERN;
import static org.batfish.representation.cumulus_concatenated.CumulusStructureType.BOND;
import static org.batfish.representation.cumulus_concatenated.CumulusStructureType.INTERFACE;
import static org.batfish.representation.cumulus_concatenated.CumulusStructureType.VLAN;
import static org.batfish.representation.cumulus_concatenated.CumulusStructureType.VRF;
import static org.batfish.representation.cumulus_concatenated.CumulusStructureType.VXLAN;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.batfish.representation.frr.FrrInterface;
import org.batfish.representation.frr.StaticRoute;

/**
 * Model of an iface block in a cumulus /etc/network/interfaces file.
 *
 * @see FrrInterface for interfaces defined in frr.conf
 */
@ParametersAreNonnullByDefault
public final class InterfacesInterface implements Serializable {

  private @Nonnull List<ConcreteInterfaceAddress> _addresses;
  private @Nullable Map<MacAddress, Set<InterfaceAddress>> _addressVirtuals;
  private @Nullable InterfaceBridgeSettings _bridgeSettings;
  private @Nullable InterfaceClagSettings _clagSettings;
  private @Nullable Integer _clagId;
  private @Nullable Ip _clagVxlanAnycastIp;
  private @Nullable String _description;
  private @Nullable String _vrfTable;
  private @Nullable Integer _linkSpeed;
  private @Nullable Integer _mtu;
  private final @Nonnull String _name;
  private @Nullable Integer _vlanId;
  private @Nullable String _vrf;
  private @Nullable String _vlanRawDevice;
  private @Nullable Ip _vxlanLocalTunnelIp;
  private @Nullable Integer _vxlanId;
  private @Nullable Set<String> _bondSlaves;
  private @Nullable Set<String> _bridgePorts;
  private @Nonnull List<StaticRoute> _postUpIpRoutes;

  public InterfacesInterface(@Nonnull String name) {
    _name = name;
    _addresses = ImmutableList.of();
    _postUpIpRoutes = ImmutableList.of();
  }

  public void addAddress(ConcreteInterfaceAddress address) {
    _addresses =
        ImmutableList.<ConcreteInterfaceAddress>builder().addAll(_addresses).add(address).build();
  }

  public @Nonnull InterfaceBridgeSettings createOrGetBridgeSettings() {
    if (_bridgeSettings == null) {
      _bridgeSettings = new InterfaceBridgeSettings();
    }
    return _bridgeSettings;
  }

  public @Nonnull InterfaceClagSettings createOrGetClagSettings() {
    if (_clagSettings == null) {
      _clagSettings = new InterfaceClagSettings();
    }
    return _clagSettings;
  }

  public @Nonnull List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }

  public @Nullable Map<MacAddress, Set<InterfaceAddress>> getAddressVirtuals() {
    return _addressVirtuals;
  }

  public @Nullable Set<String> getBondSlaves() {
    return _bondSlaves;
  }

  public @Nullable Set<String> getBridgePorts() {
    return _bridgePorts;
  }

  public @Nullable InterfaceBridgeSettings getBridgeSettings() {
    return _bridgeSettings;
  }

  public @Nullable Integer getClagId() {
    return _clagId;
  }

  public @Nullable InterfaceClagSettings getClagSettings() {
    return _clagSettings;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable String getVrfTable() {
    return _vrfTable;
  }

  public @Nullable Integer getLinkSpeed() {
    return _linkSpeed;
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull CumulusStructureType getType() {
    CumulusStructureType type = null;
    if (VLAN_INTERFACE_PATTERN.matcher(_name).matches()) {
      type = VLAN;
    }

    if (_vxlanId != null) {
      checkState(type == null, "ambiguous interface type: %s vs %s", type, VXLAN);
      type = VXLAN;
    }

    if (_vrfTable != null) {
      checkState(type == null, "ambiguous interface type: %s vs %s", type, VRF);
      type = VRF;
    }

    if (_bondSlaves != null && !_bondSlaves.isEmpty()) {
      checkState(type == null, "ambiguous interface type: %s vs %s", type, BOND);
      type = BOND;
    }

    return firstNonNull(type, INTERFACE);
  }

  public @Nullable Integer getVlanId() {
    return _vlanId;
  }

  public @Nullable String getVlanRawDevice() {
    return _vlanRawDevice;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public @Nullable Integer getVxlanId() {
    return _vxlanId;
  }

  public @Nullable Ip getVxlanLocalTunnelIp() {
    return _vxlanLocalTunnelIp;
  }

  public void setBondSlaves(Set<String> bondSlaves) {
    _bondSlaves = ImmutableSet.copyOf(bondSlaves);
  }

  public void setBridgePorts(Set<String> bridgePorts) {
    _bridgePorts = ImmutableSet.copyOf(bridgePorts);
  }

  public void setClagId(int clagId) {
    _clagId = clagId;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setVrfTable(String vrfTable) {
    _vrfTable = vrfTable;
  }

  public void setLinkSpeed(int linkSpeed) {
    _linkSpeed = linkSpeed;
  }

  public void setMtu(int mtu) {
    _mtu = mtu;
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

  public void setVxlanLocalTunnelIp(@Nullable Ip vxlanLocalTunnelIp) {
    _vxlanLocalTunnelIp = vxlanLocalTunnelIp;
  }

  public void setAddressVirtual(MacAddress macAddress, ConcreteInterfaceAddress address) {
    if (_addressVirtuals == null) {
      _addressVirtuals = new HashMap<>();
    }
    _addressVirtuals.computeIfAbsent(macAddress, k -> new HashSet<>()).add(address);
  }

  public static boolean isPhysicalInterfaceType(String ifaceName) {
    return CumulusConcatenatedConfiguration.PHYSICAL_INTERFACE_PATTERN.matcher(ifaceName).matches();
  }

  public static boolean isVlanInterfaceType(String ifaceName) {
    return VLAN_INTERFACE_PATTERN.matcher(ifaceName).matches();
  }

  public @Nonnull List<StaticRoute> getPostUpIpRoutes() {
    return _postUpIpRoutes;
  }

  public void addPostUpIpRoute(StaticRoute sr) {
    _postUpIpRoutes = ImmutableList.<StaticRoute>builder().addAll(_postUpIpRoutes).add(sr).build();
  }

  public @Nullable Ip getClagVxlanAnycastIp() {
    return _clagVxlanAnycastIp;
  }

  public void setClagVxlanAnycastIp(@Nullable Ip clagVxlanAnycastIp) {
    _clagVxlanAnycastIp = clagVxlanAnycastIp;
  }
}
