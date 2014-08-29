package batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
   private String _ip;
   private String _name;
   private int _nativeVlan;
   private Integer _ospfCost;
   private int _ospfDeadInterval;
   private int _ospfHelloMultiplier;
   private String _outgoingFilter;
   private String _subnet;
   private SwitchportMode _switchportMode;
   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   public Interface(String name) {
      _name = name;
      _ip = "";
      _active = true;
      _nativeVlan = 1;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
      _ospfCost = null;
   }

   public void setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
   }

   public double getBandwidth() {
      return _bandwidth;
   }

   public String getName() {
      return _name;
   }

   public boolean getActive() {
      return _active;
   }

   public void setActive(boolean active) {
      _active = active;
   }

   public String getIP() {
      return _ip;
   }

   public int getAccessVlan() {
      return _accessVlan;
   }

   public int getNativeVlan() {
      return _nativeVlan;
   }

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public int getOSPFDeadInterval() {
      return _ospfDeadInterval;
   }

   public void setOSPFDeadInterval(int seconds) {
      _ospfDeadInterval = seconds;
   }

   public int getOSPFHelloMultiplier() {
      return _ospfHelloMultiplier;
   }

   public void setOSPFHelloMultiplier(int multiplier) {
      _ospfHelloMultiplier = multiplier;
   }

   public void setIP(String ip) {
      _ip = ip;
   }

   public String getSubnetMask() {
      return _subnet;
   }

   public void setSubnetMask(String subnet) {
      _subnet = subnet;
   }

   public void setAccessVlan(int vlan) {
      _accessVlan = vlan;
   }

   public SwitchportMode getSwitchportMode() {
      return _switchportMode;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public List<SubRange> getAllowedVlans() {
      return _allowedVlans;
   }

   public void addAllowedRanges(List<SubRange> ranges) {
      _allowedVlans.addAll(ranges);
   }

   public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
      return _switchportTrunkEncapsulation;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

   public void setOspfCost(int defaultOspfCost) {
      _ospfCost = defaultOspfCost;
   }

   public Integer getOspfCost() {
      return _ospfCost;
   }

   public String getOutgoingFilter() {
      return _outgoingFilter;
   }
   
   public void setOutgoingFilter(String accessListName) {
      _outgoingFilter = accessListName;
   }
   
   public void setIncomingFilter(String accessListName) {
      _incomingFilter = accessListName;
   }
   
   public String getIncomingFilter() {
      return _incomingFilter;
   }
}
