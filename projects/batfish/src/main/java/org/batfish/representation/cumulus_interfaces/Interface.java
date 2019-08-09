package org.batfish.representation.cumulus_interfaces;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.representation.cumulus.CumulusStructureType.INTERFACE;
import static org.batfish.representation.cumulus.CumulusStructureType.VLAN;
import static org.batfish.representation.cumulus.CumulusStructureType.VRF;
import static org.batfish.representation.cumulus.CumulusStructureType.VXLAN;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.InterfaceBridgeSettings;
import org.batfish.representation.cumulus.InterfaceClagSettings;

/** Model of an iface block in a cumulus /etc/network/interfaces file. */
@ParametersAreNonnullByDefault
public final class Interface {
  private static final Pattern VLAN_INTERFACE_PATTERN = Pattern.compile("^vlan([0-9]+)$");

  private @Nullable List<ConcreteInterfaceAddress> _addresses;
  private @Nullable Map<MacAddress, Set<InterfaceAddress>> _addressVirtuals;
  private @Nullable InterfaceBridgeSettings _bridgeSettings;
  private @Nullable InterfaceClagSettings _clagSettings;
  private @Nullable Integer _clagId;
  private @Nullable String _description;
  private @Nullable String _vrfTable;
  private @Nullable Integer _linkSpeed;
  private final @Nonnull String _name;
  private @Nullable Integer _vlanId;
  private @Nullable String _vrf;
  private @Nullable String _vlanRawDevice;
  private @Nullable Ip _vxlanLocalTunnelIp;
  private @Nullable Integer _vxlanId;
  private @Nullable Set<String> _bridgePorts;

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
  public InterfaceBridgeSettings createOrGetBridgeSettings() {
    if (_bridgeSettings == null) {
      _bridgeSettings = new InterfaceBridgeSettings();
    }
    return _bridgeSettings;
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

  public @Nullable Set<String> getBridgePorts() {
    return _bridgePorts;
  }

  @Nullable
  public InterfaceBridgeSettings getBridgeSettings() {
    return _bridgeSettings;
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

  @Nullable
  public String getVrfTable() {
    return _vrfTable;
  }

  @Nullable
  public Integer getLinkSpeed() {
    return _linkSpeed;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public CumulusStructureType getType() {
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

    return firstNonNull(type, INTERFACE);
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
