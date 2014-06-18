package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import batfish.representation.SwitchportEncapsulationType;
import batfish.representation.SwitchportMode;
import batfish.util.SubRange;

public class Interface {

   private int _accessVlan;
   private boolean _active;
   private ArrayList<SubRange> _allowedVlans;
   private Integer _area;
   private Double _bandwidth;
   private String _incomingFilter;
   private String _ip;
   private String _name;
   private int _nativeVlan;
   private Integer _ospfCost;
   private int _ospfDeadInterval;
   private int _ospfHelloMultiplier;
   private String _outgoingFilter;
   private Map<String, String> _secondaryIps;
   private String _subnet;
   private SwitchportMode _switchportMode;
   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   public Interface(String name) {
      _name = name;
      _area = null;
      _ip = null;
      _active = true;
      _nativeVlan = 1;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
      _secondaryIps = new HashMap<String, String>();
      _ospfCost = null;
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

   public Integer getArea() {
      return _area;
   }

   public Double getBandwidth() {
      return _bandwidth;
   }

   public String getIncomingFilter() {
      return _incomingFilter;
   }

   public String getIP() {
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

   public int getOspfDeadInterval() {
      return _ospfDeadInterval;
   }

   public int getOspfHelloMultiplier() {
      return _ospfHelloMultiplier;
   }

   public String getOutgoingFilter() {
      return _outgoingFilter;
   }

   public String getSubnetMask() {
      return _subnet;
   }

   public SwitchportMode getSwitchportMode() {
      return _switchportMode;
   }

   public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
      return _switchportTrunkEncapsulation;
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

   public void setIP(String ip) {
      _ip = ip;
   }

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public void setOspfCost(int ospfCost) {
      _ospfCost = ospfCost;
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

   public void setSubnetMask(String subnet) {
      _subnet = subnet;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

   public Map<String, String> getSecondaryIps() {
      return _secondaryIps;
   }

}
