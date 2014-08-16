package batfish.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import batfish.util.NamedStructure;
import batfish.util.SubRange;
import batfish.util.Util;

public class Interface extends NamedStructure {

   private static final long serialVersionUID = 1L;

   private int _accessVlan;
   private boolean _active;
   private ArrayList<SubRange> _allowedVlans;
   private Double _bandwidth;
   private String _description;
   private IpAccessList _incomingFilter;
   private Ip _ip;
   private int _nativeVlan;
   private Integer _ospfArea;
   private Integer _ospfCost;
   private int _ospfDeadInterval;
   private int _ospfHelloMultiplier;
   private IpAccessList _outgoingFilter;
   private PolicyMap _routingPolicy;
   private Map<Ip, Ip> _secondaryIps;
   private Ip _subnet;
   private SwitchportMode _switchportMode;

   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   public Interface(String name) {
      super(name);
      _ip = null;
      _active = true;
      _nativeVlan = 1;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
      _ospfCost = null;
      _ospfArea = null;
      _incomingFilter = null;
      _outgoingFilter = null;
      _secondaryIps = new HashMap<Ip, Ip>();
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
      return _ospfArea;
   }

   public double getBandwidth() {
      return _bandwidth;
   }

   public String getDescription() {
      return _description;
   }

   public String getIFString(int indentLevel) {
      String retString = Util.getIndentString(indentLevel) + "Interface "
            + getName();

      retString += String.format("\n%sActive %s",
            Util.getIndentString(indentLevel + 1), _active);
      retString += String.format("\n%sIp %s",
            Util.getIndentString(indentLevel + 1), _ip);
      retString += String.format("\n%sSubnet %s",
            Util.getIndentString(indentLevel + 1), _subnet);
      retString += String.format("\n%sBandwidth %s",
            Util.getIndentString(indentLevel + 1), _bandwidth);

      retString += String.format("\n%sSwitchportMode %s",
            Util.getIndentString(indentLevel + 1), _switchportMode);
      retString += String.format("\n%sSwitchportEncapsulationType %s",
            Util.getIndentString(indentLevel + 1),
            _switchportTrunkEncapsulation);

      retString += String.format("\n%sOspfArea %s",
            Util.getIndentString(indentLevel + 1), _ospfArea);
      retString += String.format("\n%sOspfCost %s",
            Util.getIndentString(indentLevel + 1), _ospfCost);
      retString += String.format("\n%sOspfDeadInterval %s",
            Util.getIndentString(indentLevel + 1), _ospfDeadInterval);
      retString += String.format("\n%sOspfHelloMultiplier %s",
            Util.getIndentString(indentLevel + 1), _ospfHelloMultiplier);

      retString += String.format("\n%sAccessVlan %s",
            Util.getIndentString(indentLevel + 1), _accessVlan);
      retString += String.format("\n%sNativeVlan %s",
            Util.getIndentString(indentLevel + 1), _nativeVlan);

      if (_allowedVlans.size() > 0) {
         retString += "\n" + Util.getIndentString(indentLevel + 1)
               + "AllowedVlans";

         for (SubRange sr : _allowedVlans) {
            retString += " " + sr.toString();
         }
      }

      if (_incomingFilter != null) {
         retString += "\n" + Util.getIndentString(indentLevel + 1)
               + "IncomingFilter " + _incomingFilter.getName();
      }

      if (_outgoingFilter != null) {
         retString += "\n" + Util.getIndentString(indentLevel + 1)
               + "OutgoingFilter " + _outgoingFilter.getName();
      }

      if (_secondaryIps.size() > 0) {
         for (Map.Entry<Ip, Ip> entry : _secondaryIps.entrySet()) {
            retString += "\n"
                  + Util.getIndentString(indentLevel + 1)
                  + String.format("SecondaryIp %s %s", entry.getKey(),
                        entry.getValue());
         }
      }

      return retString;
   }

   public IpAccessList getIncomingFilter() {
      return _incomingFilter;
   }

   public Ip getIP() {
      return _ip;
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

   public IpAccessList getOutgoingFilter() {
      return _outgoingFilter;
   }

   public PolicyMap getRoutingPolicy() {
      return _routingPolicy;
   }

   public Map<Ip, Ip> getSecondaryIps() {
      return _secondaryIps;
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

   public void setAccessVlan(int vlan) {
      _accessVlan = vlan;
   }

   public void setActive(boolean active) {
      _active = active;
   }

   public void setArea(Integer area) {
      _ospfArea = area;
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

   public void setIP(Ip ip) {
      _ip = ip;
   }

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public void setOspfCost(Integer ospfCost) {
      _ospfCost = ospfCost;
   }

   public void setOspfDeadInterval(int seconds) {
      _ospfDeadInterval = seconds;
   }

   public void setOspfHelloMultiplier(int multiplier) {
      _ospfHelloMultiplier = multiplier;
   }

   public void setOutgoingFilter(IpAccessList filter) {
      _outgoingFilter = filter;
   }

   public void setRoutingPolicy(PolicyMap policy) {
      _routingPolicy = policy;
   }

   public void setSubnetMask(Ip subnet) {
      _subnet = subnet;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

}
