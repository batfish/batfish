package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.representation.SwitchportEncapsulationType;
import org.batfish.representation.SwitchportMode;
import org.batfish.util.SubRange;

public class Interface implements Serializable {

   private static final long serialVersionUID = 1L;

   private static double getDefaultBandwidthByName(String name) {
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

   private ArrayList<SubRange> _allowedVlans;

   private double _bandwidth;

   private String _incomingFilter;

   private String _name;

   private int _nativeVlan;

   private Ip _ospfArea;

   private Integer _ospfCost;

   private int _ospfDeadInterval;

   private int _ospfHelloMultiplier;

   private boolean _ospfPassive;

   private String _outgoingFilter;

   private Prefix _prefix;

   private SwitchportMode _switchportMode;

   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   private Map<String, Interface> _units;

   public Interface(String name) {
      _name = name;
      _active = true;
      _bandwidth = getDefaultBandwidthByName(name);
      _nativeVlan = 1;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
      _ospfCost = null;
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

   public double getBandwidth() {
      return _bandwidth;
   }

   public String getIncomingFilter() {
      return _incomingFilter;
   }

   public String getName() {
      return _name;
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

   public Prefix getPrefix() {
      return _prefix;
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

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public void setOspfArea(Ip ospfArea) {
      _ospfArea = ospfArea;
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

   public void setOspfPassive(boolean ospfPassive) {
      _ospfPassive = ospfPassive;
   }

   public void setOutgoingFilter(String accessListName) {
      _outgoingFilter = accessListName;
   }

   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

}
