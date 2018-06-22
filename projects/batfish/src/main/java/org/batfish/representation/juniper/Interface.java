package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.VrrpGroup;

public class Interface extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  public static double getDefaultBandwidthByName(String name) {
    if (name.startsWith("xe")) {
      return 1E10;
    } else if (name.startsWith("ge")) {
      return 1E9;
    } else if (name.startsWith("fe")) {
      return 1E8;
    } else {
      return 1E12;
    }
  }

  private String _accessVlan;

  private boolean _active;

  private Set<Ip> _additionalArpIps;

  private final Set<InterfaceAddress> _allAddresses;

  private final Set<Ip> _allAddressIps;

  private final List<SubRange> _allowedVlans;

  private double _bandwidth;

  private final int _definitionLine;

  private String _description;

  private String _incomingFilter;

  private transient boolean _inherited;

  private final IsisInterfaceSettings _isisSettings;

  private IsoAddress _isoAddress;

  private Integer _mtu;

  private int _nativeVlan;

  private Ip _ospfArea;

  private Integer _ospfCost;

  private int _ospfDeadInterval;

  private int _ospfHelloMultiplier;

  private boolean _ospfPassive;

  private String _outgoingFilter;

  private Interface _parent;

  private InterfaceAddress _preferredAddress;

  private InterfaceAddress _primaryAddress;

  private String _routingInstance;

  private SwitchportMode _switchportMode;

  private SwitchportEncapsulationType _switchportTrunkEncapsulation;

  private final SortedMap<String, Interface> _units;

  private final SortedMap<Integer, VrrpGroup> _vrrpGroups;

  @SuppressWarnings("unused")
  private Interface() {
    this("", -1);
  }

  public Interface(String name, int definitionLine) {
    super(name);
    _active = true;
    _additionalArpIps = ImmutableSet.of();
    _allAddresses = new LinkedHashSet<>();
    _allAddressIps = new LinkedHashSet<>();
    _bandwidth = getDefaultBandwidthByName(name);
    _definitionLine = definitionLine;
    _isisSettings = new IsisInterfaceSettings();
    _nativeVlan = 1;
    _switchportMode = SwitchportMode.NONE;
    _switchportTrunkEncapsulation = SwitchportEncapsulationType.DOT1Q;
    _allowedVlans = new ArrayList<>();
    _ospfCost = null;
    _units = new TreeMap<>();
    _vrrpGroups = new TreeMap<>();
  }

  public void addAllowedRanges(List<SubRange> ranges) {
    _allowedVlans.addAll(ranges);
  }

  public String getAccessVlan() {
    return _accessVlan;
  }

  public boolean getActive() {
    return _active;
  }

  public Set<Ip> getAdditionalArpIps() {
    return _additionalArpIps;
  }

  public Set<InterfaceAddress> getAllAddresses() {
    return _allAddresses;
  }

  public Set<Ip> getAllAddressIps() {
    return _allAddressIps;
  }

  public List<SubRange> getAllowedVlans() {
    return _allowedVlans;
  }

  public double getBandwidth() {
    return _bandwidth;
  }

  public int getDefinitionLine() {
    return _definitionLine;
  }

  public String getDescription() {
    return _description;
  }

  public String getIncomingFilter() {
    return _incomingFilter;
  }

  public IsisInterfaceSettings getIsisSettings() {
    return _isisSettings;
  }

  public IsoAddress getIsoAddress() {
    return _isoAddress;
  }

  public Integer getMtu() {
    return _mtu;
  }

  public int getNativeVlan() {
    return _nativeVlan;
  }

  public Ip getOspfArea() {
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

  public boolean getOspfPassive() {
    return _ospfPassive;
  }

  public String getOutgoingFilter() {
    return _outgoingFilter;
  }

  public Interface getParent() {
    return _parent;
  }

  public InterfaceAddress getPreferredAddress() {
    return _preferredAddress;
  }

  public InterfaceAddress getPrimaryAddress() {
    return _primaryAddress;
  }

  public String getRoutingInstance() {
    return _routingInstance;
  }

  public SwitchportMode getSwitchportMode() {
    return _switchportMode;
  }

  public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
    return _switchportTrunkEncapsulation;
  }

  public Map<String, Interface> getUnits() {
    return _units;
  }

  public SortedMap<Integer, VrrpGroup> getVrrpGroups() {
    return _vrrpGroups;
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
    if (_ospfCost == null) {
      _ospfCost = _parent._ospfCost;
    }
    if (_ospfArea == null) {
      _ospfArea = _parent._ospfArea;
    }
  }

  public void setAccessVlan(String vlan) {
    _accessVlan = vlan;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public void setAdditionalArpIps(Iterable<Ip> additionalArpIps) {
    _additionalArpIps = ImmutableSet.copyOf(additionalArpIps);
  }

  public void setBandwidth(Double bandwidth) {
    _bandwidth = bandwidth;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIncomingFilter(String accessListName) {
    _incomingFilter = accessListName;
  }

  public void setIsoAddress(IsoAddress address) {
    _isoAddress = address;
  }

  public void setMtu(Integer mtu) {
    _mtu = mtu;
  }

  public void setNativeVlan(int vlan) {
    _nativeVlan = vlan;
  }

  public void setOspfArea(Ip ospfArea) {
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

  public void setOspfPassive(boolean ospfPassive) {
    _ospfPassive = true;
  }

  public void setOutgoingFilter(String accessListName) {
    _outgoingFilter = accessListName;
  }

  public void setParent(Interface parent) {
    _parent = parent;
  }

  public void setPreferredAddress(InterfaceAddress address) {
    _preferredAddress = address;
  }

  public void setPrimaryAddress(InterfaceAddress address) {
    _primaryAddress = address;
  }

  public void setRoutingInstance(String routingInstance) {
    _routingInstance = routingInstance;
  }

  public void setSwitchportMode(SwitchportMode switchportMode) {
    _switchportMode = switchportMode;
  }

  public void setSwitchportTrunkEncapsulation(SwitchportEncapsulationType encapsulation) {
    _switchportTrunkEncapsulation = encapsulation;
  }
}
