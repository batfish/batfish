package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.SubRange;

public class Interface implements Serializable {

  /**
   * A vendor-specific type for an interface.
   *
   * <p>NB: if you update this, update functions like {@link #isPhysicalLike}, {@link #isUnit}, etc.
   */
  public enum InterfaceType {
    /** An aggregated interface, such as ae0. */
    AGGREGATED,
    /** A unit on an aggregated interface, such as ae0.7. */
    AGGREGATED_UNIT,
    /** One domain in the integrated routing and bridging such as irb.7. */
    IRB_UNIT,
    /** Loopback, such as lo0. */
    LOOPBACK,
    /** A unit on a loopback, such as lo0.7. */
    LOOPBACK_UNIT,
    /** Management, such as em0. */
    MANAGEMENT,
    /** A unit on a management interface, such as em0.0. */
    MANAGEMENT_UNIT,
    /** A physical port, such as xe-0/0/0. */
    PHYSICAL,
    /** A unit on a physical port, such as xe-0/0/0.7. */
    PHYSICAL_UNIT,
    /** A redundant ethernet interface, such as reth0. */
    REDUNDANT,
    /** A unit on a redundant ethernet interface, such as reth0.7. */
    REDUNDANT_UNIT,

    //// Batfish-internal types below.

    /** The Global Master Interface, used for inheriting. */
    MASTER,
    /** An unknown interface type; mostly unsupported. */
    UNKNOWN,
  }

  /**
   * Returns true if this interface is configured like a physical interface. The main use of this
   * function is for features that can be configured on a group of bundled interfaces.
   */
  public boolean isPhysicalLike() {
    return _type == InterfaceType.PHYSICAL
        || _type == InterfaceType.MANAGEMENT
        || _type == InterfaceType.AGGREGATED
        || _type == InterfaceType.REDUNDANT;
  }

  /** Returns true if this interface is a unit. */
  public boolean isUnit() {
    return _type == InterfaceType.PHYSICAL_UNIT
        || _type == InterfaceType.IRB_UNIT
        || _type == InterfaceType.LOOPBACK_UNIT
        || _type == InterfaceType.MANAGEMENT_UNIT
        || _type == InterfaceType.AGGREGATED_UNIT
        || _type == InterfaceType.REDUNDANT_UNIT;
  }

  public enum VlanTaggingMode {
    /** By default, physical interfaces do not handle tagged frames. */
    NONE,
    /**
     * support dot1q encapsulation.
     * https://www.juniper.net/documentation/us/en/software/junos/multicast-l2/topics/ref/statement/vlan-tagging-edit-interfaces.html
     */
    VLAN_TAGGING,
    /**
     * support dot1q encapsulation and q-in-q encapsulation.
     * https://www.juniper.net/documentation/us/en/software/junos/multicast-l2/topics/ref/statement/flexible-vlan-tagging-edit-interfaces.html
     */
    FLEXIBLE_VLAN_TAGGING,
  }

  public static double getDefaultBandwidthByName(String name) {
    if (name.startsWith("xe")) {
      return 1E10;
    } else if (name.startsWith("ge")) {
      return 1E9;
    } else if (name.startsWith("fe")) {
      return 1E8;
    } else if (name.startsWith("irb")) {
      return 1E9;
    } else if (name.startsWith("et")) {
      return 1E11;
    } else if (name.startsWith("em")) {
      // In show data, management interfaces have bandwidth of 1 Gbps.
      return 1E9;
    } else {
      return 1E12;
    }
  }

  public static InterfaceType getInterfaceTypeByName(String name) {
    if (name.startsWith("et")
        || name.startsWith("fe")
        || name.startsWith("ge")
        || name.startsWith("xe")) {
      return name.contains(".") ? InterfaceType.PHYSICAL_UNIT : InterfaceType.PHYSICAL;
    } else if (name.startsWith("irb.")) {
      return InterfaceType.IRB_UNIT;
    } else if (name.startsWith("lo")) {
      return name.contains(".") ? InterfaceType.LOOPBACK_UNIT : InterfaceType.LOOPBACK;
    } else if (name.startsWith("em") || name.startsWith("fxp")) {
      return name.contains(".") ? InterfaceType.MANAGEMENT_UNIT : InterfaceType.MANAGEMENT;
    } else if (name.startsWith("ae")) {
      return name.contains(".") ? InterfaceType.AGGREGATED_UNIT : InterfaceType.AGGREGATED;
    } else if (name.startsWith("reth")) {
      return name.contains(".") ? InterfaceType.REDUNDANT_UNIT : InterfaceType.REDUNDANT;
    } else if (name.equals(RoutingInstance.MASTER_INTERFACE_NAME)) {
      return InterfaceType.MASTER;
    }
    return InterfaceType.UNKNOWN;
  }

  public @Nullable EthernetSwitching getEthernetSwitching() {
    return _ethernetSwitching;
  }

  public void initEthernetSwitching() {
    if (_ethernetSwitching == null) {
      _ethernetSwitching = new EthernetSwitching();
    }
  }

  private boolean _active;
  private Set<Ip> _additionalArpIps;
  private final Set<ConcreteInterfaceAddress> _allAddresses;
  private final Set<ConcreteInterfaceAddress6> _allAddresses6;
  // Dumb name to appease checkstyle
  private String _agg8023adInterface;
  private final Set<Ip> _allAddressIps;
  private final List<SubRange> _allowedVlans;
  private final List<String> _allowedVlanNames;
  private double _bandwidth;
  private String _description;
  private boolean _defined;
  private @Nullable EthernetSwitching _ethernetSwitching;
  private @Nullable String _incomingFilter;
  private @Nullable List<String> _incomingFilterList;
  private transient boolean _inherited;
  private @Nullable IsisInterfaceSettings _isisSettings;
  private IsoAddress _isoAddress;
  private Integer _mtu;
  private final String _name;
  private @Nullable Integer _nativeVlan;
  private @Nullable OspfInterfaceSettings _ospfSettings;
  private @Nullable String _outgoingFilter;
  private @Nullable List<String> _outgoingFilterList;
  private Interface _parent;
  private InterfaceAddress _preferredAddress;
  private @Nullable ConcreteInterfaceAddress6 _preferredAddress6;
  private ConcreteInterfaceAddress _primaryAddress;
  private @Nullable ConcreteInterfaceAddress6 _primaryAddress6;
  private boolean _primary;
  private @Nullable String _redundantParentInterface;
  private RoutingInstance _routingInstance;
  private final @Nonnull InterfaceType _type;
  private final SortedMap<String, Interface> _units;
  private final SortedMap<Integer, VrrpGroup> _vrrpGroups;
  private @Nullable Integer _vlanId;
  private @Nonnull VlanTaggingMode _vlanTagging;
  private Integer _tcpMss;

  public Interface(String name) {
    _active = true;
    _additionalArpIps = ImmutableSet.of();
    _allAddresses = new LinkedHashSet<>();
    _allAddresses6 = new LinkedHashSet<>();
    _allAddressIps = new LinkedHashSet<>();
    _bandwidth = getDefaultBandwidthByName(name);
    _defined = false;
    _name = name;
    _allowedVlans = new LinkedList<>();
    _allowedVlanNames = new LinkedList<>();
    _type = getInterfaceTypeByName(name);
    _units = new TreeMap<>();
    _vlanTagging = VlanTaggingMode.NONE;
    _vrrpGroups = new TreeMap<>();
  }

  public String get8023adInterface() {
    return _agg8023adInterface;
  }

  public boolean getActive() {
    return _active;
  }

  public Set<Ip> getAdditionalArpIps() {
    return _additionalArpIps;
  }

  public Set<ConcreteInterfaceAddress> getAllAddresses() {
    return _allAddresses;
  }

  public Set<ConcreteInterfaceAddress6> getAllAddresses6() {
    return _allAddresses6;
  }

  public Set<Ip> getAllAddressIps() {
    return _allAddressIps;
  }

  public List<SubRange> getAllowedVlans() {
    return _allowedVlans;
  }

  public List<String> getAllowedVlanNames() {
    return _allowedVlanNames;
  }

  public double getBandwidth() {
    return _bandwidth;
  }

  public String getDescription() {
    return _description;
  }

  public @Nullable String getIncomingFilter() {
    return _incomingFilter;
  }

  public @Nullable List<String> getIncomingFilterList() {
    return _incomingFilterList;
  }

  public @Nullable IsisInterfaceSettings getIsisSettings() {
    return _isisSettings;
  }

  /** Initializes {@link IsisInterfaceSettings} for this interface if not already initialized */
  public IsisInterfaceSettings getOrInitIsisSettings() {
    if (_isisSettings == null) {
      _isisSettings = new IsisInterfaceSettings();
    }
    return _isisSettings;
  }

  public IsoAddress getIsoAddress() {
    return _isoAddress;
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  public String getName() {
    return _name;
  }

  /**
   * The vlan assigned to untagged packets. These packets are sent to the logical L3 subinterface
   * with the same vlan-id. This is distinct from the concept of trunk native-vlan.
   */
  public @Nullable Integer getNativeVlan() {
    return _nativeVlan;
  }

  public OspfInterfaceSettings getOspfSettings() {
    return _ospfSettings;
  }

  public @Nullable String getOutgoingFilter() {
    return _outgoingFilter;
  }

  public @Nullable List<String> getOutgoingFilterList() {
    return _outgoingFilterList;
  }

  public Interface getParent() {
    return _parent;
  }

  public InterfaceAddress getPreferredAddress() {
    return _preferredAddress;
  }

  public @Nullable ConcreteInterfaceAddress6 getPreferredAddress6() {
    return _preferredAddress6;
  }

  public ConcreteInterfaceAddress getPrimaryAddress() {
    return _primaryAddress;
  }

  public @Nullable ConcreteInterfaceAddress6 getPrimaryAddress6() {
    return _primaryAddress6;
  }

  /**
   * Whether this interface is explicitly configured as primary.
   *
   * <p>Primary interfaces are used to decide IP addresses for outgoing packets. If no interface is
   * an explicit primary, Junos has rules for inferring primary.
   */
  public boolean getPrimary() {
    return _primary;
  }

  public @Nullable String getRedundantParentInterface() {
    return _redundantParentInterface;
  }

  public RoutingInstance getRoutingInstance() {
    return _routingInstance;
  }

  public @Nonnull InterfaceType getType() {
    return _type;
  }

  public Map<String, Interface> getUnits() {
    return _units;
  }

  public @Nullable Integer getVlanId() {
    return _vlanId;
  }

  public void setVlanId(@Nullable Integer vlanId) {
    _vlanId = vlanId;
  }

  public @Nonnull VlanTaggingMode getVlanTagging() {
    return _vlanTagging;
  }

  public void setVlanTagging(@Nonnull VlanTaggingMode vlanTagging) {
    _vlanTagging = vlanTagging;
  }

  public SortedMap<Integer, VrrpGroup> getVrrpGroups() {
    return _vrrpGroups;
  }

  /**
   * Returns effective ISIS settings, that is, those are directly configured for the interface or
   * those inherited via "interface all" in the routing instance.
   */
  // This behavior wasn't lab tested but assumed to be identical to (lab tested) OSPF
  public @Nullable IsisInterfaceSettings getEffectiveIsisSettings() {
    return _isisSettings != null ? _isisSettings : _routingInstance.getInterfaceAllIsisSettings();
  }

  /**
   * Returns effective OSPF settings, that is, those are directly configured for the interface or
   * those inherited via "interface all" in the routing instance.
   */
  // This behavior was tested in lab. See unit test testOspfInterfaceAll in FlatJuniperGrammarTest
  public @Nullable OspfInterfaceSettings getEffectiveOspfSettings() {
    return _ospfSettings != null ? _ospfSettings : _routingInstance.getInterfaceAllOspfSettings();
  }

  public void inheritUnsetFields() {
    if (_parent == null || _inherited) {
      return;
    }
    _inherited = true;
    _parent.inheritUnsetFields();
    if (_description == null) {
      _description = _parent._description;
    }
    if (_mtu == null) {
      _mtu = _parent._mtu;
    }
  }

  /**
   * Copies the values of fields associated with physical interfaces from {@code bestower} to this
   * interface.
   *
   * <p>TODO: This list is incomplete. We don't have a clean separation of which properties are
   * physical only
   */
  public void inheritUnsetPhysicalFields(Interface bestower) {
    if (_agg8023adInterface == null) {
      _agg8023adInterface = bestower._agg8023adInterface;
    }
    if (_description == null) {
      _description = bestower._description;
    }
    if (_mtu == null) {
      _mtu = bestower._mtu;
    }
    if (_redundantParentInterface == null) {
      _redundantParentInterface = bestower._redundantParentInterface;
    }
  }

  /**
   * Returns true if the interface was defined in the config. Needed to check if interface was
   * referred but not defined
   *
   * @return true if interface was defined.
   */
  public boolean isDefined() {
    return _defined;
  }

  public void set8023adInterface(String interfaceName) {
    _agg8023adInterface = interfaceName;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public void setAdditionalArpIps(Iterable<Ip> additionalArpIps) {
    _additionalArpIps = ImmutableSet.copyOf(additionalArpIps);
  }

  public void setBandwidth(double bandwidth) {
    _bandwidth = bandwidth;
  }

  public void setDefined(boolean defined) {
    _defined = defined;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIncomingFilter(@Nullable String accessListName) {
    _incomingFilter = accessListName;
    _incomingFilterList = null;
  }

  public void addIncomingFilterList(@Nonnull String accessListName) {
    _incomingFilter = null;
    if (_incomingFilterList == null) {
      _incomingFilterList = new LinkedList<>();
    }
    _incomingFilterList.add(accessListName);
  }

  public void setIsoAddress(IsoAddress address) {
    _isoAddress = address;
  }

  public void setMtu(Integer mtu) {
    _mtu = mtu;
  }

  public void setNativeVlan(Integer vlan) {
    _nativeVlan = vlan;
  }

  public void setOspfSettings(OspfInterfaceSettings ospfSettings) {
    _ospfSettings = ospfSettings;
  }

  public void setOutgoingFilter(@Nullable String accessListName) {
    _outgoingFilter = accessListName;
    _outgoingFilterList = null;
  }

  public void addOutgoingFilterList(@Nonnull String accessListName) {
    _outgoingFilter = null;
    if (_outgoingFilterList == null) {
      _outgoingFilterList = new LinkedList<>();
    }
    _outgoingFilterList.add(accessListName);
  }

  public void setParent(Interface parent) {
    _parent = parent;
  }

  public void setPreferredAddress(InterfaceAddress address) {
    _preferredAddress = address;
  }

  public void setPreferredAddress6(@Nullable ConcreteInterfaceAddress6 address) {
    _preferredAddress6 = address;
  }

  public void setPrimaryAddress(ConcreteInterfaceAddress address) {
    _primaryAddress = address;
  }

  public void setPrimaryAddress6(@Nullable ConcreteInterfaceAddress6 address) {
    _primaryAddress6 = address;
  }

  public void setPrimary(boolean primary) {
    _primary = primary;
  }

  public void setRedundantParentInterface(@Nullable String redundantParentInterface) {
    _redundantParentInterface = redundantParentInterface;
  }

  public void setRoutingInstance(RoutingInstance routingInstance) {
    _routingInstance = routingInstance;
  }

  public void setTcpMss(@Nullable Integer tcpMss) {
    _tcpMss = tcpMss;
  }

  public @Nullable Integer getTcpMss() {
    return _tcpMss;
  }

  @Override
  public String toString() {
    return _name + " parent=" + _parent;
  }
}
