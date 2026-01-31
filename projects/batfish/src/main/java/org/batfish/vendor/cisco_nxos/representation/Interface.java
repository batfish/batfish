package org.batfish.vendor.cisco_nxos.representation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Range;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;

/** A layer-2- or layer-3-capable network interface */
public final class Interface implements Serializable {

  public static final double BANDWIDTH_CONVERSION_FACTOR = 1000D; // kbits

  private static final double DEFAULT_INTERFACE_BANDWIDTH = 1E12D;

  /** Loopback bandwidth (bits/s) */
  private static final double DEFAULT_LOOPBACK_BANDWIDTH = 8E9D;

  /** Management bandwidth (bits/s) */
  private static final double DEFAULT_MGMT_BANDWIDTH = 1E9D;

  /**
   * Default interface delay in tens of microseconds: "10 microseconds for all interfaces except
   * loopback ports"
   * https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus7000/sw/interfaces/command/cisco_nexus7000_interfaces_command_ref/d_commands.html#wp1043520423
   */
  private static final int DEFAULT_INTERFACE_DELAY = 1;

  /** Default loopback delay in tens of microseconds */
  private static final int DEFAULT_LOOPBACK_DELAY = 500;

  /** NX-OS Ethernet 802.3z - may not apply for non-NX-OS */
  private static final double DEFAULT_NXOS_ETHERNET_SPEED = 1E9D;

  private static final double DEFAULT_VLAN_BANDWIDTH = 1E9D;
  public static final IntegerSpace VLAN_RANGE = IntegerSpace.of(Range.closed(1, 4094));

  /**
   * Returns the shutdown status for an interface when shutdown is not explicitly configured. Once
   * explicitly configured, inference no longer applies for the life of the interface.
   *
   * <ul>
   *   <li>Ethernet parent interface default shutdown varies by platform and whether interface is in
   *       switchport mode.
   *   <li>port-channel parent interfaces are not shutdown by default.
   *   <li>Ethernet and port-channel subinterfaces are shut down by default.
   *   <li>loopback interfaces are not shutdown by default.
   *   <li>mgmt interfaces are not shutdown by default.
   *   <li>vlan interfaces are shutdown by default.
   * </ul>
   */
  private static boolean defaultShutdown(
      SwitchportMode switchportMode,
      CiscoNxosInterfaceType type,
      boolean subinterface,
      boolean systemDefaultSwitchportShutdown,
      boolean nonSwitchportDefaultShutdown) {
    return switch (type) {
      case ETHERNET ->
          switchportMode == SwitchportMode.NONE
              ? nonSwitchportDefaultShutdown
              : systemDefaultSwitchportShutdown;
      case PORT_CHANNEL -> subinterface;
      case VLAN -> true;
      case LOOPBACK, MGMT -> false;
    };
  }

  /** Default bandwidth in bits/second */
  public static @Nullable Double getDefaultBandwidth(CiscoNxosInterfaceType type) {
    Double defaultSpeed = getDefaultSpeed(type);
    if (defaultSpeed != null) {
      return defaultSpeed;
    }
    switch (type) {
      case LOOPBACK:
        return DEFAULT_LOOPBACK_BANDWIDTH;
      case MGMT:
        return DEFAULT_MGMT_BANDWIDTH;
      case PORT_CHANNEL:
        return null;
      case VLAN:
        return DEFAULT_VLAN_BANDWIDTH;
      default:
        // Use default bandwidth for other interface types that have no speed
        return DEFAULT_INTERFACE_BANDWIDTH;
    }
  }

  /** Default interface delay in tens of microseconds */
  public static int defaultDelayTensOfMicroseconds(CiscoNxosInterfaceType type) {
    // TODO Is this actually correct for EIGRP delays?
    switch (type) {
      case LOOPBACK:
        return DEFAULT_LOOPBACK_DELAY;
      case MGMT:
      case PORT_CHANNEL:
      case VLAN:
      default:
        return DEFAULT_INTERFACE_DELAY;
    }
  }

  public static @Nullable Double getDefaultSpeed(CiscoNxosInterfaceType type) {
    if (type == CiscoNxosInterfaceType.ETHERNET) {
      return DEFAULT_NXOS_ETHERNET_SPEED;
    } else {
      // loopback
      // port-Channel
      // Vlan
      // ... others
      return null;
    }
  }

  /**
   * Construct a non-Vlan interface with given {@code name}, {@code parentInterface}, and {@code
   * type}.
   */
  public static @Nonnull Interface newNonVlanInterface(
      String name, @Nullable String parentInterface, CiscoNxosInterfaceType type) {
    checkArgument(type != CiscoNxosInterfaceType.VLAN, "Expected non-VLAN interface type");
    return new Interface(name, parentInterface, type, null);
  }

  /** Construct a Vlan interface with given {@code name} and {@code vlan} ID. */
  public static @Nonnull Interface newVlanInterface(String name, int vlan) {
    return new Interface(name, null, CiscoNxosInterfaceType.VLAN, vlan);
  }

  private @Nullable Integer _accessVlan;
  private @Nullable InterfaceAddressWithAttributes _address;
  private @Nonnull IntegerSpace _allowedVlans;
  private boolean _autostate;
  private @Nullable Long _bandwidth;
  private @Nullable String _channelGroup;
  private final @Nonnull Set<String> _declaredNames;
  private @Nullable Integer _delayTensOfMicroseconds;
  private @Nullable String _description;
  private final @Nonnull SortedSet<Ip> _dhcpRelayAddresses;
  private @Nullable String _eigrp;
  private @Nullable Long _eigrpBandwidth;
  private @Nullable Integer _eigrpDelay;
  private boolean _eigrpPassive;
  private @Nullable DistributeList _eigrpInboundDistributeList;
  private @Nullable DistributeList _eigrpOutboundDistributeList;
  private @Nullable Integer _encapsulationVlan;
  private boolean _fabricForwardingModeAnycastGateway;
  private @Nullable InterfaceHsrp _hsrp;
  private @Nullable String _ipAccessGroupIn;
  private @Nullable String _ipAccessGroupOut;
  private boolean _ipAddressDhcp;
  private @Nullable Boolean _ipForward;
  private @Nullable InterfaceIpv6AddressWithAttributes _ipv6Address;
  private boolean _ipv6AddressDhcp;
  private final @Nonnull Set<InterfaceIpv6AddressWithAttributes> _ipv6AddressSecondaries;
  private @Nullable Boolean _ipProxyArp;
  private @Nullable Lacp _lacp;
  private @Nullable Integer _mtu;
  private final @Nonnull String _name;
  private @Nullable Integer _nativeVlan;
  private @Nullable OspfInterface _ospf;
  private final @Nullable String _parentInterface;
  private @Nullable String _pbrPolicy;
  private final @Nonnull Set<InterfaceAddressWithAttributes> _secondaryAddresses;
  private @Nullable Boolean _shutdown;
  private @Nullable Integer _speedMbps;
  private @Nullable SwitchportMode _switchportMode;
  private boolean _switchportMonitor;
  private final @Nonnull CiscoNxosInterfaceType _type;
  private final @Nullable Integer _vlan;
  private @Nullable String _vrfMember;

  private Interface(
      String name,
      @Nullable String parentInterface,
      CiscoNxosInterfaceType type,
      @Nullable Integer vlan) {
    _name = name;
    _parentInterface = parentInterface;
    _declaredNames = new HashSet<>();
    _dhcpRelayAddresses = new TreeSet<>();
    _secondaryAddresses = new HashSet<>();
    _ipv6AddressSecondaries = new HashSet<>();
    _type = type;
    _vlan = vlan;
    _autostate = true;

    // Set defaults for individual switchport modes
    // - only effective when corresponding switchport mode is active
    _accessVlan = 1;
    _nativeVlan = 1;
    _allowedVlans = VLAN_RANGE;
  }

  public @Nullable Integer getAccessVlan() {
    return _accessVlan;
  }

  /** The primary IPv4 address of the interface. */
  public @Nullable InterfaceAddressWithAttributes getAddress() {
    return _address;
  }

  public @Nonnull IntegerSpace getAllowedVlans() {
    return _allowedVlans;
  }

  public boolean getAutostate() {
    return _autostate;
  }

  /** Bandwidth in kbits */
  public @Nullable Long getBandwidth() {
    return _bandwidth;
  }

  public @Nullable String getChannelGroup() {
    return _channelGroup;
  }

  public @Nonnull Set<String> getDeclaredNames() {
    return _declaredNames;
  }

  public @Nullable Integer getDelayTensOfMicroseconds() {
    return _delayTensOfMicroseconds;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull SortedSet<Ip> getDhcpRelayAddresses() {
    return _dhcpRelayAddresses;
  }

  public @Nullable String getEigrp() {
    return _eigrp;
  }

  public void setEigrp(@Nullable String eigrp) {
    _eigrp = eigrp;
  }

  /** Configured bandwidth for EIGRP, in kb/s */
  public @Nullable Long getEigrpBandwidth() {
    return _eigrpBandwidth;
  }

  /** Configured delay for EIGRP, in tens of microseconds */
  public @Nullable Integer getEigrpDelay() {
    return _eigrpDelay;
  }

  public @Nullable DistributeList getEigrpInboundDistributeList() {
    return _eigrpInboundDistributeList;
  }

  public @Nullable DistributeList getEigrpOutboundDistributeList() {
    return _eigrpOutboundDistributeList;
  }

  public boolean getEigrpPassive() {
    return _eigrpPassive;
  }

  public @Nullable Integer getEncapsulationVlan() {
    return _encapsulationVlan;
  }

  public boolean isFabricForwardingModeAnycastGateway() {
    return _fabricForwardingModeAnycastGateway;
  }

  public void setFabricForwardingModeAnycastGateway(boolean fabricForwardingModeAnycastGateway) {
    _fabricForwardingModeAnycastGateway = fabricForwardingModeAnycastGateway;
  }

  public @Nullable InterfaceHsrp getHsrp() {
    return _hsrp;
  }

  public @Nonnull InterfaceHsrp getOrCreateHsrp() {
    if (_hsrp == null) {
      _hsrp = new InterfaceHsrp();
    }
    return _hsrp;
  }

  public @Nullable String getIpAccessGroupIn() {
    return _ipAccessGroupIn;
  }

  public @Nullable String getIpAccessGroupOut() {
    return _ipAccessGroupOut;
  }

  public boolean getIpAddressDhcp() {
    return _ipAddressDhcp;
  }

  public @Nullable Boolean getIpForward() {
    return _ipForward;
  }

  public @Nullable InterfaceIpv6AddressWithAttributes getIpv6Address() {
    return _ipv6Address;
  }

  public boolean getIpv6AddressDhcp() {
    return _ipv6AddressDhcp;
  }

  public @Nonnull Set<InterfaceIpv6AddressWithAttributes> getIpv6AddressSecondaries() {
    return _ipv6AddressSecondaries;
  }

  public @Nullable Boolean getIpProxyArp() {
    return _ipProxyArp;
  }

  public void setIpProxyArp(@Nullable Boolean ipProxyArp) {
    _ipProxyArp = ipProxyArp;
  }

  public @Nullable Lacp getLacp() {
    return _lacp;
  }

  public @Nonnull Lacp getOrCreateLacp() {
    if (_lacp == null) {
      _lacp = new Lacp();
    }
    return _lacp;
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getNativeVlan() {
    return _nativeVlan;
  }

  public @Nullable OspfInterface getOspf() {
    return _ospf;
  }

  public @Nonnull OspfInterface getOrCreateOspf() {
    if (_ospf == null) {
      _ospf = new OspfInterface();
    }
    return _ospf;
  }

  public @Nullable String getParentInterface() {
    return _parentInterface;
  }

  public @Nullable String getPbrPolicy() {
    return _pbrPolicy;
  }

  public void setPbrPolicy(@Nullable String pbrPolicy) {
    _pbrPolicy = pbrPolicy;
  }

  /** The set of secondary IPv4 addresses of the interface. */
  public @Nonnull Set<InterfaceAddressWithAttributes> getSecondaryAddresses() {
    return _secondaryAddresses;
  }

  /**
   * Returns {@code true} if this interface is explicitly administratively shutdown; {@code false}
   * if this interface is explicitly administratively up; or {@code null} if administrative status
   * has not been explicitly configured.
   */
  public @Nullable Boolean getShutdown() {
    return _shutdown;
  }

  /**
   * Returns {@code true} iff this interface is explictly or implicitly administratively shutdown.
   */
  public boolean getShutdownEffective(
      boolean systemDefaultSwitchport,
      boolean systemDefaultSwitchportShutdown,
      boolean nonSwitchportDefaultShutdown) {
    return _shutdown != null
        ? _shutdown
        : defaultShutdown(
            getSwitchportModeEffective(systemDefaultSwitchport),
            _type,
            _parentInterface != null,
            systemDefaultSwitchportShutdown,
            nonSwitchportDefaultShutdown);
  }

  public @Nullable Integer getSpeedMbps() {
    return _speedMbps;
  }

  public void setSpeed(@Nullable Integer speedMbps) {
    _speedMbps = speedMbps;
  }

  public @Nullable SwitchportMode getSwitchportMode() {
    return _switchportMode;
  }

  /** Returns explicit or implicit {@link SwitchportMode}. */
  public @Nonnull SwitchportMode getSwitchportModeEffective(boolean systemDefaultSwitchport) {
    return _switchportMode != null
        ? _switchportMode
        : defaultSwitchportMode(systemDefaultSwitchport, _parentInterface != null, _type);
  }

  private @Nonnull SwitchportMode defaultSwitchportMode(
      boolean systemDefaultSwitchport, boolean isSubinterface, CiscoNxosInterfaceType type) {
    switch (type) {
      case ETHERNET:
      case PORT_CHANNEL:
        if (isSubinterface) {
          // this is a subinterface
          return SwitchportMode.NONE;
        } else {
          // this is a parent interface
          return systemDefaultSwitchport ? SwitchportMode.ACCESS : SwitchportMode.NONE;
        }

      case LOOPBACK:
      case MGMT:
      default:
        return SwitchportMode.NONE;
    }
  }

  public boolean getSwitchportMonitor() {
    return _switchportMonitor;
  }

  public @Nonnull CiscoNxosInterfaceType getType() {
    return _type;
  }

  public @Nullable Integer getVlan() {
    return _vlan;
  }

  public @Nullable String getVrfMember() {
    return _vrfMember;
  }

  public void setAccessVlan(@Nullable Integer accessVlan) {
    _accessVlan = accessVlan;
  }

  public void setAddress(@Nullable InterfaceAddressWithAttributes address) {
    _address = address;
  }

  public void setAllowedVlans(@Nonnull IntegerSpace allowedVlans) {
    _allowedVlans = allowedVlans;
  }

  public void setAutostate(boolean autostate) {
    _autostate = autostate;
  }

  public void setBandwidth(@Nullable Long bandwidth) {
    _bandwidth = bandwidth;
  }

  public void setChannelGroup(@Nullable String channelGroup) {
    _channelGroup = channelGroup;
  }

  public void setDelayTensOfMicroseconds(@Nullable Integer delayTensOfMicroseconds) {
    _delayTensOfMicroseconds = delayTensOfMicroseconds;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setEigrpBandwidth(@Nullable Long eigrpBandwidth) {
    _eigrpBandwidth = eigrpBandwidth;
  }

  public void setEigrpDelay(@Nullable Integer delay) {
    _eigrpDelay = delay;
  }

  public void setEigrpInboundDistributeList(@Nullable DistributeList eigrpInboundDistributeList) {
    _eigrpInboundDistributeList = eigrpInboundDistributeList;
  }

  public void setEigrpOutboundDistributeList(@Nullable DistributeList eigrpOutboundDistributeList) {
    _eigrpOutboundDistributeList = eigrpOutboundDistributeList;
  }

  public void setEigrpPassive(boolean eigrpPassive) {
    _eigrpPassive = eigrpPassive;
  }

  public void setEncapsulationVlan(@Nullable Integer encapsulationVlan) {
    _encapsulationVlan = encapsulationVlan;
  }

  public void setIpAccessGroupIn(@Nullable String ipAcessGroupIn) {
    _ipAccessGroupIn = ipAcessGroupIn;
  }

  public void setIpAccessGroupOut(@Nullable String ipAcessGroupOut) {
    _ipAccessGroupOut = ipAcessGroupOut;
  }

  public void setIpAddressDhcp(boolean ipAddressDhcp) {
    _ipAddressDhcp = ipAddressDhcp;
  }

  public void setIpForward(@Nullable Boolean ipForward) {
    _ipForward = ipForward;
  }

  public void setIpv6Address(@Nullable InterfaceIpv6AddressWithAttributes ipv6Address) {
    _ipv6Address = ipv6Address;
  }

  public void setIpv6AddressDhcp(boolean ipv6AddressDhcp) {
    _ipv6AddressDhcp = ipv6AddressDhcp;
  }

  public void setMtu(@Nullable Integer mtu) {
    _mtu = mtu;
  }

  public void setNativeVlan(@Nullable Integer nativeVlan) {
    _nativeVlan = nativeVlan;
  }

  public void setOspf(@Nullable OspfInterface ospf) {
    _ospf = ospf;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  public void setSwitchportMode(@Nullable SwitchportMode switchportMode) {
    _switchportMode = switchportMode;
  }

  public void setSwitchportMonitor(boolean switchportMonitor) {
    _switchportMonitor = switchportMonitor;
  }

  public void setVrfMember(@Nullable String vrfMember) {
    _vrfMember = vrfMember;
  }
}
