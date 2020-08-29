package org.batfish.representation.arista;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.isis.IsisInterfaceMode;

public class Interface implements Serializable {

  private static final double DEFAULT_ARISTA_ETHERNET_SPEED = 1E9D;

  private static final double DEFAULT_INTERFACE_BANDWIDTH = 1E12D;

  private static final double DEFAULT_VLAN_BANDWIDTH = 1E9D;

  private static final int DEFAULT_INTERFACE_MTU = 1500;

  /** Loopback bandwidth */
  private static final double DEFAULT_LOOPBACK_BANDWIDTH = 8E9D;

  public static @Nullable Double getDefaultBandwidth(@Nonnull String name) {
    Double defaultSpeed = getDefaultSpeed(name);
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
    } else if (name.startsWith("Vlan")) {
      return DEFAULT_VLAN_BANDWIDTH;
    } else {
      // Use default bandwidth for other interface types that have no speed
      return DEFAULT_INTERFACE_BANDWIDTH;
    }
  }

  public static @Nullable Double getDefaultSpeed(@Nonnull String name) {
    if (name.startsWith("Ethernet")) {
      return DEFAULT_ARISTA_ETHERNET_SPEED;
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

  private boolean _shutdown;

  @Nullable private IntegerSpace _allowedVlans;

  private List<AristaDynamicSourceNat> _aristaNats;

  private boolean _autoState;

  @Nullable private Double _bandwidth;

  private String _channelGroup;

  private String _cryptoMap;

  private String _description;

  private SortedSet<Ip> _dhcpRelayAddresses;

  private boolean _dhcpRelayClient;

  private @Nullable Integer _encapsulationVlan;

  private String _incomingFilter;

  @Nullable private Long _isisCost;

  @Nullable private IsisInterfaceMode _isisInterfaceMode;

  private @Nullable Boolean _localProxyArp;

  private final @Nonnull Set<String> _memberInterfaces;

  @Nullable private Integer _mlagId;

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

  private boolean _switchport;

  private boolean _switchportAccessDynamic;

  private SwitchportMode _switchportMode;

  private SwitchportEncapsulationType _switchportTrunkEncapsulation;

  private Tunnel _tunnel;

  @Nonnull private Set<String> _vlanTrunkGroups;

  private String _vrf;

  private SortedSet<String> _declaredNames;

  private @Nullable Double _speed;

  public static final IntegerSpace ALL_VLANS = IntegerSpace.of(new SubRange(1, 4094));

  /**
   * Returns the default {@link SwitchportMode} to be used when a switchport is explicitly enabled,
   * ignoring any override of the default mode.
   */
  public static SwitchportMode getUndeclaredDefaultSwitchportMode() {
    return SwitchportMode.ACCESS;
  }

  public Interface(String name, AristaConfiguration c) {
    _shutdown = false;
    _autoState = true;
    _declaredNames = ImmutableSortedSet.of();
    _dhcpRelayAddresses = new TreeSet<>();
    _isisInterfaceMode = IsisInterfaceMode.UNSET;
    _memberInterfaces = new HashSet<>();
    _name = name;
    _secondaryAddresses = new LinkedHashSet<>();

    // Switchport defaults
    if (name.startsWith("Ethernet") || name.startsWith("Port-Channel")) {
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

  public boolean getShutdown() {
    return _shutdown;
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

  public String getIncomingFilter() {
    return _incomingFilter;
  }

  public Long getIsisCost() {
    return _isisCost;
  }

  public IsisInterfaceMode getIsisInterfaceMode() {
    return _isisInterfaceMode;
  }

  public @Nullable Boolean getLocalProxyArp() {
    return _localProxyArp;
  }

  public void setLocalProxyArp(@Nullable Boolean localProxyArp) {
    _localProxyArp = localProxyArp;
  }

  public @Nonnull Set<String> getMemberInterfaces() {
    return _memberInterfaces;
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

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
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
}
