package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.isis.IsisInterfaceMode;

public class Interface implements Serializable {

  private static final double DEFAULT_ARISTA_ETHERNET_SPEED = 1E9D;

  private static final double DEFAULT_FAST_ETHERNET_SPEED = 100E6D;

  private static final double DEFAULT_GIGABIT_ETHERNET_SPEED = 1E9D;

  private static final double DEFAULT_INTERFACE_BANDWIDTH = 1E12D;

  private static final int DEFAULT_INTERFACE_MTU = 1500;

  private static final double DEFAULT_IOS_ETHERNET_SPEED = 1E7D;

  private static final double DEFAULT_LONG_REACH_ETHERNET_SPEED = 10E6D;

  /** Loopback bandwidth */
  private static final double DEFAULT_LOOPBACK_BANDWIDTH = 8E9D;

  /** NX-OS Ethernet 802.3z - may not apply for non-NX-OS */
  private static final double DEFAULT_NXOS_ETHERNET_SPEED = 1E9D;

  private static final double DEFAULT_TEN_GIGABIT_ETHERNET_SPEED = 10E9D;

  /** Loopback delay in picoseconds for IOS */
  private static final double LOOPBACK_IOS_DELAY = 5E9D;

  /**
   * Tunnel bandwidth in bps for IOS (checked in IOS 16.4)
   *
   * <p>See https://bst.cloudapps.cisco.com/bugsearch/bug/CSCse69736
   */
  private static final double TUNNEL_IOS_BANDWIDTH = 100E3D;

  /**
   * Tunnel delay in picoseconds for IOS (checked in IOS 16.4)
   *
   * <p>See https://bst.cloudapps.cisco.com/bugsearch/bug/CSCse69736
   */
  private static final double TUNNEL_IOS_DELAY = 50E9D;

  private static final long serialVersionUID = 1L;

  public static @Nullable Double getDefaultBandwidth(
      @Nonnull String name, @Nonnull ConfigurationFormat format) {
    Double defaultSpeed = getDefaultSpeed(name, format);
    if (defaultSpeed != null) {
      return defaultSpeed;
    } else if (name.startsWith("Bundle-Ethernet")) {
      // Derived from member interfaces
      return null;
    } else if (name.startsWith("Loopback")) {
      return DEFAULT_LOOPBACK_BANDWIDTH;
    } else if (name.startsWith("Port-Channel")) {
      // Derived from member interfaces
      return null;
    } else if (format == ConfigurationFormat.CISCO_IOS && name.startsWith("Tunnel")) {
      return TUNNEL_IOS_BANDWIDTH;
    } else {
      // Use default bandwidth for other interface types that have no speed
      return DEFAULT_INTERFACE_BANDWIDTH;
    }
  }

  public static @Nullable Double getDefaultSpeed(
      @Nonnull String name, @Nonnull ConfigurationFormat format) {
    if (name.startsWith("Ethernet")) {
      switch (format) {
        case ARISTA:
          return DEFAULT_ARISTA_ETHERNET_SPEED;

        case ALCATEL_AOS:
        case ARUBAOS: // TODO: verify https://github.com/batfish/batfish/issues/1548
        case CADANT:
        case CISCO_ASA:
        case CISCO_IOS:
        case CISCO_IOS_XR:
        case FORCE10:
        case FOUNDRY:
          return DEFAULT_IOS_ETHERNET_SPEED;

        case CISCO_NX:
          return DEFAULT_NXOS_ETHERNET_SPEED;

        case AWS:
        case BLADENETWORK:
        case EMPTY:
        case F5:
        case FLAT_JUNIPER:
        case FLAT_VYOS:
        case HOST:
        case IGNORED:
        case IPTABLES:
        case JUNIPER:
        case JUNIPER_SWITCH:
        case MRV:
        case MRV_COMMANDS:
        case MSS:
        case UNKNOWN:
        case VXWORKS:
        case VYOS:
        default:
          throw new BatfishException("Unuspported format: " + format);
      }
    } else if (name.startsWith("FastEthernet")) {
      return DEFAULT_FAST_ETHERNET_SPEED;
    } else if (name.startsWith("GigabitEthernet")) {
      return DEFAULT_GIGABIT_ETHERNET_SPEED;
    } else if (name.startsWith("LongReachEthernet")) {
      return DEFAULT_LONG_REACH_ETHERNET_SPEED;
    } else if (name.startsWith("TenGigabitEthernet")) {
      return DEFAULT_TEN_GIGABIT_ETHERNET_SPEED;
    } else {
      // Bundle-Ethernet
      // Loopback
      // Port-Channel
      // Vlan
      // ... others
      return null;
    }
  }

  public static int getDefaultMtu() {
    return DEFAULT_INTERFACE_MTU;
  }

  @Nullable private Integer _accessVlan;

  private boolean _active;

  private String _alias;

  @Nullable private IntegerSpace _allowedVlans;

  private List<AristaDynamicSourceNat> _aristaNats;

  private boolean _autoState;

  @Nullable private Double _bandwidth;

  private String _channelGroup;

  private String _cryptoMap;

  @Nullable private Double _delay;

  private String _description;

  private SortedSet<Ip> _dhcpRelayAddresses;

  private boolean _dhcpRelayClient;

  private @Nullable Integer _encapsulationVlan;

  private Map<Integer, HsrpGroup> _hsrpGroups;

  private String _hsrpVersion;

  private String _incomingFilter;

  @Nullable private Long _isisCost;

  @Nullable private IsisInterfaceMode _isisInterfaceMode;

  @Nullable private Integer _mlagId;

  private int _mtu;

  private final String _name;

  @Nullable private Integer _nativeVlan;

  private Long _ospfArea;

  private Integer _ospfCost;

  private int _ospfDeadInterval;

  private int _ospfHelloMultiplier;

  @Nullable private Boolean _ospfPassive;

  private boolean _ospfPointToPoint;

  @Nullable private String _ospfProcess;

  private boolean _ospfShutdown;

  private String _outgoingFilter;

  private InterfaceAddress _address;

  private boolean _proxyArp;

  private String _routingPolicy;

  private Set<InterfaceAddress> _secondaryAddresses;

  private boolean _spanningTreePortfast;

  private InterfaceAddress _standbyAddress;

  private boolean _switchport;

  private boolean _switchportAccessDynamic;

  private SwitchportMode _switchportMode;

  private SwitchportEncapsulationType _switchportTrunkEncapsulation;

  private Tunnel _tunnel;

  @Nonnull private Set<String> _vlanTrunkGroups;

  private String _vrf;

  private SortedSet<String> _declaredNames;

  private String _securityZone;

  @Nullable private Integer _securityLevel;

  private @Nullable Double _speed;

  /** Returns the default interface delay in picoseconds for the given {@code format}. */
  @Nullable
  public static Double getDefaultDelay(String name, ConfigurationFormat format) {
    if (format == ConfigurationFormat.CISCO_IOS && name.startsWith("Loopback")) {
      // TODO Cisco NX whitepaper says to use the formula, not this value. Confirm?
      // Enhanced Interior Gateway Routing Protocol (EIGRP) Wide Metrics White Paper
      return LOOPBACK_IOS_DELAY;
    }
    if (format == ConfigurationFormat.CISCO_IOS && name.startsWith("Tunnel")) {
      return TUNNEL_IOS_DELAY;
    }

    Double bandwidth = getDefaultBandwidth(name, format);
    if (bandwidth == null || bandwidth == 0D) {
      return null;
    }

    /*
     * Delay is only relevant on routers that support EIGRP (Cisco).
     *
     * When bandwidth > 1Gb, this formula is used. The interface may report a different value.
     * For bandwidths < 1Gb, the delay may be interface type-specific.
     * See https://tools.ietf.org/html/rfc7868#section-5.6.1.2
     */
    return 1E16 / bandwidth;
  }

  public static final IntegerSpace ALL_VLANS = IntegerSpace.of(new SubRange(1, 4094));

  public String getSecurityZone() {
    return _securityZone;
  }

  /**
   * Returns the default {@link SwitchportMode} for the given {@code vendor} to be used when a
   * switchport is explicitly enabled, ignoring any override of the default mode.
   */
  public static SwitchportMode getUndeclaredDefaultSwitchportMode(ConfigurationFormat vendor) {
    switch (vendor) {
      case ARISTA:
        return SwitchportMode.ACCESS;
      case CISCO_IOS:
        return SwitchportMode.DYNAMIC_AUTO;
      case CISCO_NX:
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/sw/5_x/nx-os/interfaces/configuration/guide/if_cli/if_access_trunk.html#pgfId-1525675
        return SwitchportMode.ACCESS;
      default:
        return SwitchportMode.ACCESS;
    }
  }

  public Interface(String name, CiscoConfiguration c) {
    _active = true;
    _autoState = true;
    _declaredNames = ImmutableSortedSet.of();
    _dhcpRelayAddresses = new TreeSet<>();
    _hsrpGroups = new TreeMap<>();
    _isisInterfaceMode = IsisInterfaceMode.UNSET;
    _name = name;
    _secondaryAddresses = new LinkedHashSet<>();
    ConfigurationFormat vendor = c.getVendor();

    // Proxy-ARP defaults
    switch (vendor) {
      case CISCO_ASA:
      case CISCO_IOS:
        setProxyArp(true);
        break;

        // $CASES-OMITTED$
      default:
        break;
    }

    // Switchport defaults
    if (vendor == ConfigurationFormat.ARISTA
        && (name.startsWith("Ethernet") || name.startsWith("Port-Channel"))) {
      SwitchportMode defaultSwitchportMode = c.getCf().getDefaultSwitchportMode();
      if (defaultSwitchportMode == null) {
        // Arista Ethernet and Port-channel default switchport mode is ACCESS
        _switchportMode = SwitchportMode.ACCESS;
      } else {
        // Arista use alternate default switchport mode if declared
        _switchportMode = defaultSwitchportMode;
      }
    } else {
      // Default switchport mode for non-Arista and Arista non-Ethernet/Port-Channel is NONE
      _switchportMode = SwitchportMode.NONE;
    }
    _switchport = _switchportMode != SwitchportMode.NONE;
    if (_switchportMode == SwitchportMode.TRUNK) {
      _allowedVlans = ALL_VLANS;
    } else if (_switchportMode == SwitchportMode.ACCESS) {
      _allowedVlans = null;
    }
    _vlanTrunkGroups = ImmutableSet.of();
    _spanningTreePortfast = c.getSpanningTreePortfastDefault();
  }

  public void addVlanTrunkGroup(@Nonnull String groupName) {
    _vlanTrunkGroups =
        ImmutableSet.<String>builder().addAll(_vlanTrunkGroups).add(groupName).build();
  }

  public void setAllowedVlans(@Nullable IntegerSpace allowedVlans) {
    _allowedVlans = allowedVlans;
  }

  @Nullable
  public Integer getAccessVlan() {
    return _accessVlan;
  }

  public boolean getActive() {
    return _active;
  }

  public String getAlias() {
    return _alias;
  }

  @Nullable
  public IntegerSpace getAllowedVlans() {
    return _allowedVlans;
  }

  public Set<InterfaceAddress> getAllAddresses() {
    Set<InterfaceAddress> allAddresses = new TreeSet<>();
    if (_address != null) {
      allAddresses.add(_address);
    }
    allAddresses.addAll(_secondaryAddresses);
    return allAddresses;
  }

  public List<AristaDynamicSourceNat> getAristaNats() {
    return _aristaNats;
  }

  public boolean getAutoState() {
    return _autoState;
  }

  public Double getBandwidth() {
    return _bandwidth;
  }

  public String getChannelGroup() {
    return _channelGroup;
  }

  public String getCryptoMap() {
    return _cryptoMap;
  }

  public String getDescription() {
    return _description;
  }

  public SortedSet<Ip> getDhcpRelayAddresses() {
    return _dhcpRelayAddresses;
  }

  public boolean getDhcpRelayClient() {
    return _dhcpRelayClient;
  }

  public @Nullable Integer getEncapsulationVlan() {
    return _encapsulationVlan;
  }

  public Map<Integer, HsrpGroup> getHsrpGroups() {
    return _hsrpGroups;
  }

  public String getHsrpVersion() {
    return _hsrpVersion;
  }

  public String getIncomingFilter() {
    return _incomingFilter;
  }

  public Long getIsisCost() {
    return _isisCost;
  }

  public IsisInterfaceMode getIsisInterfaceMode() {
    return _isisInterfaceMode;
  }

  @Nullable
  public Integer getMlagId() {
    return _mlagId;
  }

  public int getMtu() {
    return _mtu;
  }

  public String getName() {
    return _name;
  }

  @Nullable
  public Integer getNativeVlan() {
    return _nativeVlan;
  }

  public Long getOspfArea() {
    return _ospfArea;
  }

  public Integer getOspfCost() {
    return _ospfCost;
  }

  public int getOspfDeadInterval() {
    return _ospfDeadInterval;
  }

  public int getOspfHelloMultiplier() {
    return _ospfHelloMultiplier;
  }

  @Nullable
  public Boolean getOspfPassive() {
    return _ospfPassive;
  }

  public String getOspfProcess() {
    return _ospfProcess;
  }

  public boolean getOspfPointToPoint() {
    return _ospfPointToPoint;
  }

  public boolean getOspfShutdown() {
    return _ospfShutdown;
  }

  public String getOutgoingFilter() {
    return _outgoingFilter;
  }

  public InterfaceAddress getAddress() {
    return _address;
  }

  @Nullable
  public Double getDelay() {
    return _delay;
  }

  public boolean getProxyArp() {
    return _proxyArp;
  }

  public String getRoutingPolicy() {
    return _routingPolicy;
  }

  public Set<InterfaceAddress> getSecondaryAddresses() {
    return _secondaryAddresses;
  }

  public boolean getSpanningTreePortfast() {
    return _spanningTreePortfast;
  }

  public @Nullable Double getSpeed() {
    return _speed;
  }

  public InterfaceAddress getStandbyAddress() {
    return _standbyAddress;
  }

  public Boolean getSwitchport() {
    return _switchport;
  }

  public boolean getSwitchportAccessDynamic() {
    return _switchportAccessDynamic;
  }

  public SwitchportMode getSwitchportMode() {
    return _switchportMode;
  }

  public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
    return _switchportTrunkEncapsulation;
  }

  public Tunnel getTunnel() {
    return _tunnel;
  }

  public Tunnel getTunnelInitIfNull() {
    if (_tunnel == null) {
      _tunnel = new Tunnel();
    }
    return _tunnel;
  }

  @Nullable
  public Integer getSecurityLevel() {
    return _securityLevel;
  }

  /**
   * Retun the (immutable) set of VLAN trunk groups that this interface belongs to. To add trunk
   * groups, see {@link #addVlanTrunkGroup(String)}
   */
  @Nonnull
  public Set<String> getVlanTrunkGroups() {
    return _vlanTrunkGroups;
  }

  public String getVrf() {
    return _vrf;
  }

  public void setAccessVlan(int vlan) {
    _accessVlan = vlan;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public void setAlias(String alias) {
    _alias = alias;
  }

  public void setAristaNats(List<AristaDynamicSourceNat> aristaNats) {
    _aristaNats = aristaNats;
  }

  public void setAutoState(boolean autoState) {
    _autoState = autoState;
  }

  public void setBandwidth(@Nullable Double bandwidth) {
    _bandwidth = bandwidth;
  }

  public void setChannelGroup(String channelGroup) {
    _channelGroup = channelGroup;
  }

  public void setCryptoMap(String cryptoMap) {
    _cryptoMap = cryptoMap;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setDhcpRelayAddress(SortedSet<Ip> dhcpRelayAddress) {
    _dhcpRelayAddresses = dhcpRelayAddress;
  }

  public void setDhcpRelayClient(boolean dhcpRelayClient) {
    _dhcpRelayClient = dhcpRelayClient;
  }

  public void setEncapsulationVlan(@Nullable Integer encapsulationVlan) {
    _encapsulationVlan = encapsulationVlan;
  }

  public void setIncomingFilter(String accessListName) {
    _incomingFilter = accessListName;
  }

  public void setIsisCost(Long isisCost) {
    _isisCost = isisCost;
  }

  public void setIsisInterfaceMode(IsisInterfaceMode mode) {
    _isisInterfaceMode = mode;
  }

  public void setMlagId(Integer mlagId) {
    _mlagId = mlagId;
  }

  public void setMtu(int mtu) {
    _mtu = mtu;
  }

  public void setNativeVlan(Integer vlan) {
    _nativeVlan = vlan;
  }

  public void setOspfArea(Long ospfArea) {
    _ospfArea = ospfArea;
  }

  public void setOspfCost(int ospfCost) {
    _ospfCost = ospfCost;
  }

  public void setOspfDeadInterval(int seconds) {
    _ospfDeadInterval = seconds;
  }

  public void setOspfHelloMultiplier(int multiplier) {
    _ospfHelloMultiplier = multiplier;
  }

  public void setOspfPassive(@Nullable Boolean ospfPassive) {
    _ospfPassive = ospfPassive;
  }

  public void setOspfProcess(@Nullable String processName) {
    _ospfProcess = processName;
  }

  public void setOspfPointToPoint(boolean ospfPointToPoint) {
    _ospfPointToPoint = ospfPointToPoint;
  }

  public void setOspfShutdown(boolean ospfShutdown) {
    _ospfShutdown = ospfShutdown;
  }

  public void setOutgoingFilter(String accessListName) {
    _outgoingFilter = accessListName;
  }

  public void setAddress(InterfaceAddress address) {
    _address = address;
  }

  public void setDelay(@Nullable Double delayPs) {
    _delay = delayPs;
  }

  public void setProxyArp(boolean proxyArp) {
    _proxyArp = proxyArp;
  }

  public void setRoutingPolicy(String routingPolicy) {
    _routingPolicy = routingPolicy;
  }

  public void setSpanningTreePortfast(boolean spanningTreePortfast) {
    _spanningTreePortfast = spanningTreePortfast;
  }

  public void setSpeed(@Nullable Double speed) {
    _speed = speed;
  }

  public void setStandbyAddress(InterfaceAddress standbyAddress) {
    _standbyAddress = standbyAddress;
  }

  public void setSwitchport(boolean switchport) {
    _switchport = switchport;
  }

  public void setSwitchportAccessDynamic(boolean switchportAccessDynamic) {
    _switchportAccessDynamic = switchportAccessDynamic;
  }

  public void setSwitchportMode(SwitchportMode switchportMode) {
    _switchportMode = switchportMode;
  }

  public void setSwitchportTrunkEncapsulation(SwitchportEncapsulationType encapsulation) {
    _switchportTrunkEncapsulation = encapsulation;
  }

  public void setTunnel(Tunnel tunnel) {
    _tunnel = tunnel;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }

  public SortedSet<String> getDeclaredNames() {
    return _declaredNames;
  }

  public void setDeclaredNames(SortedSet<String> declaredNames) {
    _declaredNames = ImmutableSortedSet.copyOf(declaredNames);
  }

  public void setSecurityZone(String securityZone) {
    _securityZone = securityZone;
  }

  public void setHsrpVersion(String hsrpVersion) {
    _hsrpVersion = hsrpVersion;
  }

  public void setSecurityLevel(@Nullable Integer level) {
    _securityLevel = level;
  }
}
