package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.batfish.representation.Ip;
import org.batfish.representation.IsoAddress;
import org.batfish.representation.Prefix;
import org.batfish.representation.SwitchportEncapsulationType;
import org.batfish.representation.SwitchportMode;
import org.batfish.util.SubRange;

public class Interface implements Serializable {

   private static final long serialVersionUID = 1L;

   public static double getDefaultBandwidthByName(String name) {
      if (name.startsWith("xe")) {
         return 1E10;
      }
      else if (name.startsWith("ge")) {
         return 1E9;
      }
      else if (name.startsWith("fe")) {
         return 1E8;
      }
      else {
         return 1E12;
      }
   }

   private int _accessVlan;

   private boolean _active;

   private final ArrayList<SubRange> _allowedVlans;

   private final Set<Prefix> _allPrefixes;

   private final Set<Ip> _allPrefixIps;

   private double _bandwidth;

   private String _incomingFilter;

   private final IsisInterfaceSettings _isisSettings;

   private IsoAddress _isoAddress;

   private String _name;

   private int _nativeVlan;

   private Ip _ospfActiveArea;

   private Integer _ospfCost;

   private int _ospfDeadInterval;

   private int _ospfHelloMultiplier;

   private final Set<Ip> _ospfPassiveAreas;

   private String _outgoingFilter;

   private Prefix _preferredPrefix;

   private Prefix _primaryPrefix;

   private SwitchportMode _switchportMode;

   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   private final Map<String, Interface> _units;

   public Interface(String name) {
      _name = name;
      _active = true;
      _allPrefixes = new LinkedHashSet<Prefix>();
      _allPrefixIps = new LinkedHashSet<Ip>();
      _bandwidth = getDefaultBandwidthByName(name);
      _isisSettings = new IsisInterfaceSettings();
      _nativeVlan = 1;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
      _ospfCost = null;
      _ospfPassiveAreas = new HashSet<Ip>();
      _units = new TreeMap<String, Interface>();
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

   public Set<Ip> getAllPrefixIps() {
      return _allPrefixIps;
   }

   public double getBandwidth() {
      return _bandwidth;
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

   public String getName() {
      return _name;
   }

   public int getNativeVlan() {
      return _nativeVlan;
   }

   public Ip getOspfActiveArea() {
      return _ospfActiveArea;
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

   public Set<Ip> getOspfPassiveAreas() {
      return _ospfPassiveAreas;
   }

   public String getOutgoingFilter() {
      return _outgoingFilter;
   }

   public Prefix getPreferredPrefix() {
      return _preferredPrefix;
   }

   public Prefix getPrimaryPrefix() {
      return _primaryPrefix;
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

   public void setAccessVlan(int vlan) {
      _accessVlan = vlan;
   }

   public void setActive(boolean active) {
      _active = active;
   }

   public void setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
   }

   public void setIncomingFilter(String accessListName) {
      _incomingFilter = accessListName;
   }

   public void setIsoAddress(IsoAddress address) {
      _isoAddress = address;
   }

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public void setOspfActiveArea(Ip ospfActiveArea) {
      _ospfActiveArea = ospfActiveArea;
   }

   public void setOspfCost(int defaultOspfCost) {
      _ospfCost = defaultOspfCost;
   }

   public void setOspfDeadInterval(int seconds) {
      _ospfDeadInterval = seconds;
   }

   public void setOspfHelloMultiplier(int multiplier) {
      _ospfHelloMultiplier = multiplier;
   }

   public void setOutgoingFilter(String accessListName) {
      _outgoingFilter = accessListName;
   }

   public void setPreferredPrefix(Prefix prefix) {
      _preferredPrefix = prefix;
   }

   public void setPrimaryPrefix(Prefix prefix) {
      _primaryPrefix = prefix;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

}
