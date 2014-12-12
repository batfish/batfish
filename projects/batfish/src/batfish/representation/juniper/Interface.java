package batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import batfish.representation.Ip;
import batfish.representation.SwitchportEncapsulationType;
import batfish.representation.SwitchportMode;
import batfish.util.SubRange;

public class Interface implements Serializable {

   private static final long serialVersionUID = 1L;

   private int _accessVlan;
   private boolean _active;
   private ArrayList<SubRange> _allowedVlans;
   private Double _bandwidth;
   private String _incomingFilter;
   private Ip _ip;
   private String _name;
   private int _nativeVlan;
   private Integer _ospfCost;
   private int _ospfDeadInterval;
   private int _ospfHelloMultiplier;
   private String _outgoingFilter;
   private Ip _subnet;
   private SwitchportMode _switchportMode;
   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   private Map<String, Interface> _units;

   public Interface(String name) {
      _name = name;
      _active = true;
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

   public Ip getIp() {
      return _ip;
   }

   public String getName() {
      return _name;
   }

   public int getNativeVlan() {
      return _nativeVlan;
   }

   public Integer getOspfCost() {
      return _ospfCost;
   }

   public int getOSPFDeadInterval() {
      return _ospfDeadInterval;
   }

   public int getOSPFHelloMultiplier() {
      return _ospfHelloMultiplier;
   }

   public String getOutgoingFilter() {
      return _outgoingFilter;
   }

   public Ip getSubnetMask() {
      return _subnet;
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

   public void setIp(Ip ip) {
      _ip = ip;
   }

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public void setOspfCost(int defaultOspfCost) {
      _ospfCost = defaultOspfCost;
   }

   public void setOSPFDeadInterval(int seconds) {
      _ospfDeadInterval = seconds;
   }

   public void setOSPFHelloMultiplier(int multiplier) {
      _ospfHelloMultiplier = multiplier;
   }

   public void setOutgoingFilter(String accessListName) {
      _outgoingFilter = accessListName;
   }

   public void setSubnetMask(Ip mask) {
      _subnet = mask;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }
}
