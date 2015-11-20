package org.batfish.representation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.main.ConfigurationFormat;
import org.batfish.util.NamedStructure;
import org.batfish.util.SubRange;

public class Interface extends NamedStructure {

   private static final long serialVersionUID = 1L;

   private int _accessVlan;

   private boolean _active;

   private ArrayList<SubRange> _allowedVlans;

   private final Set<Prefix> _allPrefixes;

   private Double _bandwidth;

   private String _description;

   private IpAccessList _incomingFilter;

   private Integer _isisCost;

   private IsisInterfaceMode _isisL1InterfaceMode;

   private IsisInterfaceMode _isisL2InterfaceMode;

   private int _nativeVlan;

   private OspfArea _ospfArea;

   private Integer _ospfCost;

   private int _ospfDeadInterval;

   private boolean _ospfEnabled;

   private int _ospfHelloMultiplier;

   private boolean _ospfPassive;

   private IpAccessList _outgoingFilter;

   private Prefix _prefix;

   private PolicyMap _routingPolicy;

   private final Set<Prefix> _secondaryPrefixes;

   private SwitchportMode _switchportMode;

   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   public Interface(String name) {
      super(name);
      _active = true;
      _allPrefixes = new TreeSet<Prefix>();
      _nativeVlan = 1;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
      _secondaryPrefixes = new TreeSet<Prefix>();
   }

   public void addAllowedRanges(List<SubRange> ranges) {
      _allowedVlans.addAll(ranges);
   }

   public int getAccessVlan() {
      return _accessVlan;
   }

   public boolean getActive() {
      return _active;
   }

   public List<SubRange> getAllowedVlans() {
      return _allowedVlans;
   }

   public Set<Prefix> getAllPrefixes() {
      return _allPrefixes;
   }

   public double getBandwidth() {
      return _bandwidth;
   }

   public String getDescription() {
      return _description;
   }

   public IpAccessList getIncomingFilter() {
      return _incomingFilter;
   }

   public Integer getIsisCost() {
      return _isisCost;
   }

   public IsisInterfaceMode getIsisL1InterfaceMode() {
      return _isisL1InterfaceMode;
   }

   public IsisInterfaceMode getIsisL2InterfaceMode() {
      return _isisL2InterfaceMode;
   }

   public int getNativeVlan() {
      return _nativeVlan;
   }

   public OspfArea getOspfArea() {
      return _ospfArea;
   }

   public Integer getOspfCost() {
      return _ospfCost;
   }

   public int getOspfDeadInterval() {
      return _ospfDeadInterval;
   }

   public boolean getOspfEnabled() {
      return _ospfEnabled;
   }

   public int getOspfHelloMultiplier() {
      return _ospfHelloMultiplier;
   }

   public boolean getOspfPassive() {
      return _ospfPassive;
   }

   public IpAccessList getOutgoingFilter() {
      return _outgoingFilter;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public PolicyMap getRoutingPolicy() {
      return _routingPolicy;
   }

   public Set<Prefix> getSecondaryPrefixes() {
      return _secondaryPrefixes;
   }

   public SwitchportMode getSwitchportMode() {
      return _switchportMode;
   }

   public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
      return _switchportTrunkEncapsulation;
   }

   public boolean isLoopback(ConfigurationFormat vendor) {
      if (vendor == ConfigurationFormat.JUNIPER
            || vendor == ConfigurationFormat.FLAT_JUNIPER) {
         if (!_name.contains(".")) {
            return false;
         }
      }
      return _name.toLowerCase().startsWith("lo");
   }

   public void setAccessVlan(int vlan) {
      _accessVlan = vlan;
   }

   public void setActive(boolean active) {
      _active = active;
   }

   public void setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
   }

   public void setDescription(String description) {
      _description = description;
   }

   public void setIncomingFilter(IpAccessList filter) {
      _incomingFilter = filter;
   }

   public void setIsisCost(Integer isisCost) {
      _isisCost = isisCost;
   }

   public void setIsisL1InterfaceMode(IsisInterfaceMode mode) {
      _isisL1InterfaceMode = mode;
   }

   public void setIsisL2InterfaceMode(IsisInterfaceMode mode) {
      _isisL2InterfaceMode = mode;
   }

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public void setOspfArea(OspfArea ospfArea) {
      _ospfArea = ospfArea;
   }

   public void setOspfCost(Integer ospfCost) {
      _ospfCost = ospfCost;
   }

   public void setOspfDeadInterval(int seconds) {
      _ospfDeadInterval = seconds;
   }

   public void setOspfEnabled(boolean b) {
      _ospfEnabled = b;
   }

   public void setOspfHelloMultiplier(int multiplier) {
      _ospfHelloMultiplier = multiplier;
   }

   public void setOspfPassive(boolean passive) {
      _ospfPassive = passive;
   }

   public void setOutgoingFilter(IpAccessList filter) {
      _outgoingFilter = filter;
   }

   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   public void setRoutingPolicy(PolicyMap policy) {
      _routingPolicy = policy;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

}
