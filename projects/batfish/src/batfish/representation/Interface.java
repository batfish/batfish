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
   private IpAccessList _incomingFilter;
   private Ip _ip;
   private int _nativeVlan;
   private Integer _ospfArea;
   private Integer _ospfCost;
   private int _ospfDeadInterval;
   private int _ospfHelloMultiplier;
   private IpAccessList _outgoingFilter;
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

   public boolean sameParseTree(Interface face, Configuration lhsC,
         Configuration rhsC, String prefix) {
      boolean res = _name.equals(face._name);
      boolean finalRes = res;

      res = (_accessVlan == face._accessVlan);
      if (res == false) {
         System.out.println("Interface:" + _name + ":accessVlan " + prefix);
         finalRes = res;
      }

      res = (_active == face._active);
      if (res == false) {
         System.out.println("Interface:" + _name + ":Active " + prefix);
         finalRes = res;
      }

      if (_allowedVlans.size() != face._allowedVlans.size()) {
         System.out.println("Interface:" + _name + ":allowedVlans:Size "
               + prefix);
         finalRes = false;
      }
      else {
         for (int i = 0; i < _allowedVlans.size(); i++) {
            res = (_allowedVlans.get(i).toString().equals(face._allowedVlans
                  .get(i).toString()));
            if (res == false) {
               System.out.println("Interface:" + _name + ":allowedVlans "
                     + prefix);
               finalRes = res;
            }
         }
      }

      if ((_bandwidth != null) && (face._bandwidth != null)) {
         res = (_bandwidth.equals(face._bandwidth));
      }
      else {
         res = (_bandwidth == null) && (face._bandwidth == null);
      }
      if (res == false) {
         System.out.println("Interface:" + _name + ":Bandwidth " + prefix);
         finalRes = res;
      }

      if ((_incomingFilter != null) && (face._incomingFilter != null)) {
         // res = res && (_incomingFilter.equals(face._incomingFilter));
         res = (_incomingFilter.sameParseTree(face._incomingFilter,
               "Interface:" + _name + ":IncomingFilter " + prefix, true));
      }
      else {
         res = (_incomingFilter == null) && (face._incomingFilter == null);
         if (res == false) {
            System.out.println("Interface:" + _name + ":IncomingFilter "
                  + prefix);
         }
      }
      if (res == false) {
         finalRes = res;
      }

      if ((_ip != null) && (face._ip != null)) {
         res = (_ip.equals(face._ip));
      }
      else {
         res = (_ip == null) && (face._ip == null);
      }
      if (res == false) {
         System.out.println("Interface:" + _name + ":Ip " + prefix);
         finalRes = res;
      }

      res = res && (_nativeVlan == face._nativeVlan);
      if (res == false) {
         System.out.println("Interface:" + _name + ":NativeVlan " + prefix);
         finalRes = res;
      }

      if ((_ospfArea != null) && (face._ospfArea != null)) {
         res = (_ospfArea.equals(face._ospfArea));
      }
      else {
         res = (_ospfArea == null) && (face._ospfArea == null);
      }
      if (res == false) {
         System.out.println("Interface:" + _name + ":OspfArea " + prefix);
         finalRes = res;
      }

      if ((_ospfCost != null) && (face._ospfCost != null)) {
         res = (_ospfCost.equals(face._ospfCost));
      }
      else {
         Integer lhs = _ospfCost;
         if (lhs == null) {
            lhs = Math
                  .max((int) (lhsC.getOspfProcess().getReferenceBandwidth() / _bandwidth),
                        1);
         }
         Integer rhs = face._ospfCost;
         if (rhs == null) {
            rhs = Math
                  .max((int) (rhsC.getOspfProcess().getReferenceBandwidth() / face._bandwidth),
                        1);
         }
         res = (lhs.equals(rhs));
      }
      if (res == false) {
         System.out.println("Interface:" + _name + ":OspfCost " + prefix);
         finalRes = res;
      }

      if ((_outgoingFilter != null) && (face._outgoingFilter != null)) {
         // res = res && (_outgoingFilter.equals(face._outgoingFilter));
         res = (_outgoingFilter.sameParseTree(face._outgoingFilter,
               "Interface:" + _name + ":OutgoingFilter " + prefix, true));
      }
      else {
         res = (_outgoingFilter == null) && (face._outgoingFilter == null);
         if (res == false) {
            System.out.println("Interface:" + _name + ":OutgoingFilter "
                  + prefix);

         }
      }
      if (res == false) {
         finalRes = res;
      }

      res = (_ospfDeadInterval == face._ospfDeadInterval)
            && (_ospfHelloMultiplier == face._ospfHelloMultiplier);
      if (res == false) {
         System.out.println("Interface:" + _name + " " + prefix);
         finalRes = res;
      }

      if ((_subnet != null) && (face._subnet != null)) {
         res = (_subnet.equals(face._subnet));
      }
      else {
         res = (_subnet == null) && (face._subnet == null);
      }
      if (res == false) {
         System.out.println("Interface:" + _name + ":Subnet " + prefix);
         finalRes = res;
      }

      if (_secondaryIps.size() != face._secondaryIps.size()) {
         System.out.println("Interface:" + _name + ":secondIp:Size " + prefix);
         finalRes = false;
      }
      else {
         for (Ip ip : _secondaryIps.keySet()) {
            Ip lhs = _secondaryIps.get(ip);
            Ip rhs = face._secondaryIps.get(ip);
            if (rhs == null) {
               System.out.println("Interface:" + _name + ":secondIp:NullRhs "
                     + prefix);
               finalRes = false;
            }
            else {
               res = lhs.equals(rhs);
               if (res == false) {
                  System.out.println("Interface:" + _name + ":secondIp "
                        + prefix);
                  finalRes = res;
               }
            }
         }
      }

      res = (_switchportMode == face._switchportMode)
            && (_switchportTrunkEncapsulation == face._switchportTrunkEncapsulation);
      if (res == false) {
         System.out.println("Interface:" + _name + " " + prefix);
         finalRes = res;
      }

      return finalRes;
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
