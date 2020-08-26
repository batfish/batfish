package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.isis.IsisInterfaceMode;

public class Interface implements Serializable {

  private static final double DEFAULT_FAST_ETHERNET_SPEED = 100E6D;

  private static final double DEFAULT_GIGABIT_ETHERNET_SPEED = 1E9D;

  private static final double DEFAULT_INTERFACE_BANDWIDTH = 1E12D;

  private static final double DEFAULT_VLAN_BANDWIDTH = 1E9D;

  private static final int DEFAULT_INTERFACE_MTU = 1500;

  private static final double DEFAULT_ETHERNET_SPEED = 1E7D;

  private static final double DEFAULT_LONG_REACH_ETHERNET_SPEED = 10E6D;

  /** Loopback bandwidth */
  private static final double DEFAULT_LOOPBACK_BANDWIDTH = 8E9D;

  private static final double DEFAULT_TEN_GIGABIT_ETHERNET_SPEED = 10E9D;

  /** Loopback delay in picoseconds for IOS */
  private static final long LOOPBACK_IOS_DELAY = (long) 5e9;

  /**
   * Tunnel bandwidth in bps for IOS (checked in IOS 16.4)
   *
   * <p>See https://bst.cloudapps.cisco_xr.com/bugsearch/bug/CSCse69736
   */
  private static final double TUNNEL_IOS_BANDWIDTH = 100E3D;

  /**
   * Tunnel delay in picoseconds for IOS (checked in IOS 16.4)
   *
   * <p>See https://bst.cloudapps.cisco_xr.com/bugsearch/bug/CSCse69736
   */
  private static final long TUNNEL_IOS_DELAY = (long) 5e10;

  public static @Nullable Double getDefaultBandwidth(
      @Nonnull String name, @Nonnull ConfigurationFormat format) {
    Double defaultSpeed = getDefaultSpeed(name);
    if (defaultSpeed != null) {
      return defaultSpeed;
    } else if (name.startsWith("Bundle-Ether")) {
      // Derived from member interfaces
      return null;
    } else if (name.startsWith("Loopback")) {
      return DEFAULT_LOOPBACK_BANDWIDTH;
    } else if (name.startsWith("Port-Channel")) {
      // Derived from member interfaces
      return null;
    } else if (name.startsWith("Vlan")) {
      return DEFAULT_VLAN_BANDWIDTH;
    } else if (format == ConfigurationFormat.CISCO_IOS && name.startsWith("Tunnel")) {
      return TUNNEL_IOS_BANDWIDTH;
    } else {
      // Use default bandwidth for other interface types that have no speed
      return DEFAULT_INTERFACE_BANDWIDTH;
    }
  }

  public static @Nullable Double getDefaultSpeed(@Nonnull String name) {
    if (name.startsWith("Ethernet")) {
      // TODO: verify https://github.com/batfish/batfish/issues/1548
      return DEFAULT_ETHERNET_SPEED;
    } else if (name.startsWith("FastEthernet")) {
      return DEFAULT_FAST_ETHERNET_SPEED;
    } else if (name.startsWith("GigabitEthernet")) {
      return DEFAULT_GIGABIT_ETHERNET_SPEED;
    } else if (name.startsWith("LongReachEthernet")) {
      return DEFAULT_LONG_REACH_ETHERNET_SPEED;
    } else if (name.startsWith("TenGigE")) {
      return DEFAULT_TEN_GIGABIT_ETHERNET_SPEED;
    } else if (name.startsWith("Wlan-GigabitEthernet")) {
      return DEFAULT_GIGABIT_ETHERNET_SPEED;
    } else {
      // Bundle-Ethernet
      // Loopback
      // Port-Channel
      // Vlan
      // Wlan-ap0 (a management interface)
      // ... others
      return null;
    }
  }

  public static int getDefaultMtu() {
    return DEFAULT_INTERFACE_MTU;
  }

  @Nullable private Integer _accessVlan;

  private boolean _active;

  @Nullable private IntegerSpace _allowedVlans;

  private boolean _autoState;

  @Nullable private Double _bandwidth;

  private @Nullable Integer _bundleId;

  private String _cryptoMap;

  /** Delay value for this interface, if set. In picoseconds */
  @Nullable private Long _delay;

  private String _description;

  private SortedSet<Ip> _dhcpRelayAddresses;

  private boolean _dhcpRelayClient;

  private @Nullable Integer _encapsulationVlan;

  private Map<Integer, HsrpGroup> _hsrpGroups;

  private String _hsrpVersion;

  private String _incomingFilter;

  @Nullable private Long _isisCost;

  @Nullable private IsisInterfaceMode _isisInterfaceMode;

  private int _mtu;

  private final String _name;

  @Nullable private Integer _nativeVlan;

  private Long _ospfArea;

  private Integer _ospfCost;

  @Nullable private Integer _ospfDeadInterval;

  @Nullable private Integer _ospfHelloInterval;

  private int _ospfHelloMultiplier;

  @Nullable private OspfNetworkType _ospfNetworkType;

  @Nullable private Boolean _ospfPassive;

  @Nullable private String _ospfProcess;

  private boolean _ospfShutdown;

  private String _outgoingFilter;

  private ConcreteInterfaceAddress _address;

  private boolean _proxyArp;

  private String _routingPolicy;

  private Set<ConcreteInterfaceAddress> _secondaryAddresses;

  private boolean _spanningTreePortfast;

  private ConcreteInterfaceAddress _standbyAddress;

  private boolean _switchport;

  private boolean _switchportAccessDynamic;

  private SwitchportMode _switchportMode;

  private SwitchportEncapsulationType _switchportTrunkEncapsulation;

  private Tunnel _tunnel;

  private String _vrf;

  private SortedSet<String> _declaredNames;

  private @Nullable Double _speed;

  /** Returns the default interface delay in picoseconds for the given {@code format}. */
  public static long getDefaultDelay(String name, ConfigurationFormat format) {
    if (format == ConfigurationFormat.CISCO_IOS && name.startsWith("Loopback")) {
      // Enhanced Interior Gateway Routing Protocol (EIGRP) Wide Metrics White Paper
      return LOOPBACK_IOS_DELAY;
    }
    if (format == ConfigurationFormat.CISCO_IOS && name.startsWith("Tunnel")) {
      return TUNNEL_IOS_DELAY;
    }

    Double bandwidth = getDefaultBandwidth(name, format);
    if (bandwidth == null || bandwidth == 0D) {
      return 0xFFFFFFFFL;
    }

    /*
     * Delay is only relevant on routers that support EIGRP (CiscoXr).
     *
     * When bandwidth > 1Gb, this formula is used. The interface may report a different value.
     * For bandwidths < 1Gb, the delay may be interface type-specific.
     * See https://tools.ietf.org/html/rfc7868#section-5.6.1.2
     */
    return (long) (1E16 / bandwidth);
  }

  public static final IntegerSpace ALL_VLANS = IntegerSpace.of(new SubRange(1, 4094));

  public Interface(String name, CiscoXrConfiguration c) {
    _active = true;
    _autoState = true;
    _declaredNames = ImmutableSortedSet.of();
    _dhcpRelayAddresses = new TreeSet<>();
    _hsrpGroups = new TreeMap<>();
    _isisInterfaceMode = IsisInterfaceMode.UNSET;
    _name = name;
    _secondaryAddresses = new LinkedHashSet<>();

    // Switchport defaults
    _switchportMode = SwitchportMode.NONE;
    _switchport = false;
    _spanningTreePortfast = c.getSpanningTreePortfastDefault();
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

  @Nullable
  public IntegerSpace getAllowedVlans() {
    return _allowedVlans;
  }

  public Set<ConcreteInterfaceAddress> getAllAddresses() {
    Set<ConcreteInterfaceAddress> allAddresses = new TreeSet<>();
    if (_address != null) {
      allAddresses.add(_address);
    }
    allAddresses.addAll(_secondaryAddresses);
    return allAddresses;
  }

  public boolean getAutoState() {
    return _autoState;
  }

  public Double getBandwidth() {
    return _bandwidth;
  }

  public Integer getBundleId() {
    return _bundleId;
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

  /** Get the time (in seconds) to wait before neighbors are declared dead */
  @Nullable
  public Integer getOspfDeadInterval() {
    return _ospfDeadInterval;
  }

  /** Get the time (in seconds) between sending hello messages to neighbors */
  @Nullable
  public Integer getOspfHelloInterval() {
    return _ospfHelloInterval;
  }

  public int getOspfHelloMultiplier() {
    return _ospfHelloMultiplier;
  }

  public OspfNetworkType getOspfNetworkType() {
    return _ospfNetworkType;
  }

  @Nullable
  public Boolean getOspfPassive() {
    return _ospfPassive;
  }

  public String getOspfProcess() {
    return _ospfProcess;
  }

  public boolean getOspfShutdown() {
    return _ospfShutdown;
  }

  public String getOutgoingFilter() {
    return _outgoingFilter;
  }

  public ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  /** Return the delay value for this interface, in picoseconds */
  @Nullable
  public Long getDelay() {
    return _delay;
  }

  public boolean getProxyArp() {
    return _proxyArp;
  }

  public String getRoutingPolicy() {
    return _routingPolicy;
  }

  public Set<ConcreteInterfaceAddress> getSecondaryAddresses() {
    return _secondaryAddresses;
  }

  public boolean getSpanningTreePortfast() {
    return _spanningTreePortfast;
  }

  public @Nullable Double getSpeed() {
    return _speed;
  }

  public ConcreteInterfaceAddress getStandbyAddress() {
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

  public String getVrf() {
    return _vrf;
  }

  public void setAccessVlan(int vlan) {
    _accessVlan = vlan;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public void setAutoState(boolean autoState) {
    _autoState = autoState;
  }

  public void setBandwidth(@Nullable Double bandwidth) {
    _bandwidth = bandwidth;
  }

  public void setBundleId(@Nullable Integer bundleId) {
    _bundleId = bundleId;
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

  public void setOspfHelloInterval(int seconds) {
    _ospfHelloInterval = seconds;
  }

  public void setOspfHelloMultiplier(int multiplier) {
    _ospfHelloMultiplier = multiplier;
  }

  public void setOspfNetworkType(OspfNetworkType ospfNetworkType) {
    _ospfNetworkType = ospfNetworkType;
  }

  public void setOspfPassive(@Nullable Boolean ospfPassive) {
    _ospfPassive = ospfPassive;
  }

  public void setOspfProcess(@Nullable String processName) {
    _ospfProcess = processName;
  }

  public void setOspfShutdown(boolean ospfShutdown) {
    _ospfShutdown = ospfShutdown;
  }

  public void setOutgoingFilter(String accessListName) {
    _outgoingFilter = accessListName;
  }

  public void setAddress(ConcreteInterfaceAddress address) {
    _address = address;
  }

  /** Set delay for this interface, in picoseconds */
  public void setDelay(@Nullable Long delayPs) {
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

  public void setStandbyAddress(ConcreteInterfaceAddress standbyAddress) {
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

  public void setHsrpVersion(String hsrpVersion) {
    _hsrpVersion = hsrpVersion;
  }
}
