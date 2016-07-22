package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.ConfigurationFormat;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Interface extends ComparableStructure<String> {

   private static final String ACCESS_VLAN_VAR = "accessVlan";

   private static final String ACTIVE_VAR = "active";

   private static final String ALL_PREFIXES_VAR = "allPrefixes";

   private static final String ALLOWED_VLANS_VAR = "allowedVlans";

   private static final String BANDWIDTH_VAR = "bandwidth";

   private static final String DESCRIPTION_VAR = "description";

   public static final String FLOW_SINK_TERMINATION_NAME = "flow_sink_termination";

   private static final String INBOUND_FILTER_VAR = "inboundFilter";

   private static final String INCOMING_FILTER_VAR = "incomingFilter";

   private static final String ISIS_COST_VAR = "isisCost";

   private static final String ISIS_L1_INTERFACE_MODE_VAR = "isisL1InterfaceMode";

   private static final String ISIS_L2_INTERFACE_MODE_VAR = "isisL2InterfaceMode";

   private static final String NATIVE_VLAN_VAR = "nativeVlan";

   private static final String OSPF_AREA_VAR = "ospfArea";

   private static final String OSPF_COST_VAR = "ospfCost";

   private static final String OSPF_DEAD_INTERVAL_VAR = "ospfDeadInterval";

   private static final String OSPF_ENABLED_VAR = "ospfEnabled";

   private static final String OSPF_HELLO_MULTIPLIER_VAR = "ospfHelloMultiplier";

   private static final String OSPF_PASSIVE_VAR = "ospfPassive";

   private static final String OUTGOING_FILTER_VAR = "outgoingFilter";

   private static final String OWNER_VAR = "owner";

   private static final String PREFIX_VAR = "prefix";

   private static final String ROUTING_POLICY_VAR = "routingPolicy";

   private static final long serialVersionUID = 1L;

   private static final String SWITCHPORT_MODE_VAR = "switchportMode";

   private static final String SWITCHPORT_TRUNK_ENCAPSULATION_VAR = "switchportTrunkEncapsulation";

   private static final String ZONE_VAR = "zone";

   private static InterfaceType computeAosInteraceType(String name) {
      if (name.startsWith("vlan")) {
         return InterfaceType.VLAN;
      }
      else if (name.startsWith("loopback")) {
         return InterfaceType.LOOPBACK;
      }
      else {
         return InterfaceType.PHYSICAL;
      }
   }

   private static InterfaceType computeAwsInterfaceType(String name) {
      if (name.startsWith("v")) {
         return InterfaceType.VPN;
      }
      else {
         return InterfaceType.PHYSICAL;
      }
   }

   private static InterfaceType computeCiscoInterfaceType(String name) {
      if (name.startsWith("Async")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("ATM")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Bundle-Ether")) {
         return InterfaceType.AGGREGATED;
      }
      else if (name.startsWith("cmp-mgmt")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Embedded-Service-Engine")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Ethernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("FastEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("GigabitEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("GMPLS")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("HundredGigE")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Group-Async")) {
         return InterfaceType.AGGREGATED;
      }
      else if (name.startsWith("Loopback")) {
         return InterfaceType.LOOPBACK;
      }
      else if (name.startsWith("Management")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("mgmt")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("MgmtEth")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Null")) {
         throw new BatfishException("Don't know what to do with this");
      }
      else if (name.startsWith("Port-channel")) {
         return InterfaceType.AGGREGATED;
      }
      else if (name.startsWith("POS")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Serial")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("TenGigabitEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Tunnel")) {
         return InterfaceType.VPN;
      }
      else if (name.startsWith("tunnel-te")) {
         return InterfaceType.VPN;
      }
      else if (name.startsWith("Vlan")) {
         return InterfaceType.VLAN;
      }
      else {
         throw new BatfishException(
               "Missing mapping to interface type for name: \"" + name + "\"");
      }
   }

   private static InterfaceType computeHostInterfaceType(String name) {
      if (name.startsWith("lo")) {
         return InterfaceType.LOOPBACK;
      }
      else {
         return InterfaceType.PHYSICAL;
      }
   }

   public static InterfaceType computeInterfaceType(String name,
         ConfigurationFormat format) {
      switch (format) {
      case ALCATEL_AOS:
         return computeAosInteraceType(name);
      case AWS_VPC:
         return computeAwsInterfaceType(name);
      case ARISTA:
      case CISCO:
      case CISCO_IOS_XR:
         return computeCiscoInterfaceType(name);

      case FLAT_JUNIPER:
      case JUNIPER:
      case JUNIPER_SWITCH:
         return computeJuniperInterfaceType(name);

      case VYOS:
      case FLAT_VYOS:
         return computeVyosInterfaceType(name);

      case HOST:
         return computeHostInterfaceType(name);

      case MRV:
         // TODO: find out if other interface types are possible
         return InterfaceType.PHYSICAL;

      case EMPTY:
      case MSS:
      case IPTABLES:
      case UNKNOWN:
      case VXWORKS:
      default:
         throw new BatfishException(
               "Cannot compute interface type for unsupported configuration format: "
                     + format.toString());
      }
   }

   private static InterfaceType computeJuniperInterfaceType(String name) {
      if (name.startsWith("st")) {
         return InterfaceType.VPN;
      }
      else if (name.startsWith("reth")) {
         return InterfaceType.REDUNDANT;
      }
      else if (name.startsWith("ae")) {
         return InterfaceType.AGGREGATED;
      }
      else {
         return InterfaceType.PHYSICAL;
      }
   }

   private static InterfaceType computeVyosInterfaceType(String name) {
      if (name.startsWith("vti")) {
         return InterfaceType.VPN;
      }
      else if (name.startsWith("lo")) {
         return InterfaceType.LOOPBACK;
      }
      else {
         return InterfaceType.PHYSICAL;
      }
   }

   private int _accessVlan;

   private boolean _active;

   private List<SubRange> _allowedVlans;

   private Set<Prefix> _allPrefixes;

   private Double _bandwidth;

   private String _description;

   private IpAccessList _inboundFilter;

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

   private Configuration _owner;

   private Prefix _prefix;

   private PolicyMap _routingPolicy;

   private SwitchportMode _switchportMode;

   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   private Zone _zone;

   @JsonCreator
   public Interface(@JsonProperty(NAME_VAR) String name) {
      super(name);
   }

   public Interface(String name, Configuration owner) {
      super(name);
      _active = true;
      _allPrefixes = new TreeSet<Prefix>();
      _nativeVlan = 1;
      _owner = owner;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
   }

   public void addAllowedRanges(List<SubRange> ranges) {
      _allowedVlans.addAll(ranges);
   }

   private InterfaceType computeInterfaceType() {
      return computeInterfaceType(_key, _owner.getConfigurationFormat());
   }

   @JsonProperty(ACCESS_VLAN_VAR)
   public int getAccessVlan() {
      return _accessVlan;
   }

   @JsonProperty(ACTIVE_VAR)
   public boolean getActive() {
      return _active;
   }

   @JsonProperty(ALLOWED_VLANS_VAR)
   public List<SubRange> getAllowedVlans() {
      return _allowedVlans;
   }

   @JsonProperty(ALL_PREFIXES_VAR)
   public Set<Prefix> getAllPrefixes() {
      return _allPrefixes;
   }

   @JsonProperty(BANDWIDTH_VAR)
   public Double getBandwidth() {
      return _bandwidth;
   }

   @JsonProperty(DESCRIPTION_VAR)
   public String getDescription() {
      return _description;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(INBOUND_FILTER_VAR)
   public IpAccessList getInboundFilter() {
      return _inboundFilter;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(INCOMING_FILTER_VAR)
   public IpAccessList getIncomingFilter() {
      return _incomingFilter;
   }

   @JsonProperty(ISIS_COST_VAR)
   public Integer getIsisCost() {
      return _isisCost;
   }

   @JsonProperty(ISIS_L1_INTERFACE_MODE_VAR)
   public IsisInterfaceMode getIsisL1InterfaceMode() {
      return _isisL1InterfaceMode;
   }

   @JsonProperty(ISIS_L2_INTERFACE_MODE_VAR)
   public IsisInterfaceMode getIsisL2InterfaceMode() {
      return _isisL2InterfaceMode;
   }

   @JsonProperty(NATIVE_VLAN_VAR)
   public int getNativeVlan() {
      return _nativeVlan;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(OSPF_AREA_VAR)
   public OspfArea getOspfArea() {
      return _ospfArea;
   }

   @JsonProperty(OSPF_COST_VAR)
   public Integer getOspfCost() {
      return _ospfCost;
   }

   @JsonProperty(OSPF_DEAD_INTERVAL_VAR)
   public int getOspfDeadInterval() {
      return _ospfDeadInterval;
   }

   @JsonProperty(OSPF_ENABLED_VAR)
   public boolean getOspfEnabled() {
      return _ospfEnabled;
   }

   @JsonProperty(OSPF_HELLO_MULTIPLIER_VAR)
   public int getOspfHelloMultiplier() {
      return _ospfHelloMultiplier;
   }

   @JsonProperty(OSPF_PASSIVE_VAR)
   public boolean getOspfPassive() {
      return _ospfPassive;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(OUTGOING_FILTER_VAR)
   public IpAccessList getOutgoingFilter() {
      return _outgoingFilter;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(OWNER_VAR)
   public Configuration getOwner() {
      return _owner;
   }

   @JsonProperty(PREFIX_VAR)
   public Prefix getPrefix() {
      return _prefix;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(ROUTING_POLICY_VAR)
   public PolicyMap getRoutingPolicy() {
      return _routingPolicy;
   }

   @JsonProperty(SWITCHPORT_MODE_VAR)
   public SwitchportMode getSwitchportMode() {
      return _switchportMode;
   }

   @JsonProperty(SWITCHPORT_TRUNK_ENCAPSULATION_VAR)
   public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
      return _switchportTrunkEncapsulation;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(ZONE_VAR)
   public Zone getZone() {
      return _zone;
   }

   public boolean isLoopback(ConfigurationFormat vendor) {
      if (vendor == ConfigurationFormat.JUNIPER
            || vendor == ConfigurationFormat.FLAT_JUNIPER) {
         if (!_key.contains(".")) {
            return false;
         }
      }
      return _key.toLowerCase().startsWith("lo");
   }

   @JsonProperty(ACCESS_VLAN_VAR)
   public void setAccessVlan(int vlan) {
      _accessVlan = vlan;
   }

   @JsonProperty(ACTIVE_VAR)
   public void setActive(boolean active) {
      _active = active;
   }

   @JsonProperty(ALLOWED_VLANS_VAR)
   public void setAllowedVlans(List<SubRange> allowedVlans) {
      _allowedVlans = allowedVlans;
   }

   @JsonProperty(ALL_PREFIXES_VAR)
   public void setAllPrefixes(Set<Prefix> allPrefixes) {
      _allPrefixes = allPrefixes;
   }

   @JsonProperty(BANDWIDTH_VAR)
   public void setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
   }

   @JsonProperty(DESCRIPTION_VAR)
   public void setDescription(String description) {
      _description = description;
   }

   @JsonProperty(INBOUND_FILTER_VAR)
   public void setInboundFilter(IpAccessList inboundFilter) {
      _inboundFilter = inboundFilter;
   }

   @JsonProperty(INCOMING_FILTER_VAR)
   public void setIncomingFilter(IpAccessList filter) {
      _incomingFilter = filter;
   }

   @JsonProperty(ISIS_COST_VAR)
   public void setIsisCost(Integer isisCost) {
      _isisCost = isisCost;
   }

   @JsonProperty(ISIS_L1_INTERFACE_MODE_VAR)
   public void setIsisL1InterfaceMode(IsisInterfaceMode mode) {
      _isisL1InterfaceMode = mode;
   }

   @JsonProperty(ISIS_L2_INTERFACE_MODE_VAR)
   public void setIsisL2InterfaceMode(IsisInterfaceMode mode) {
      _isisL2InterfaceMode = mode;
   }

   @JsonProperty(NATIVE_VLAN_VAR)
   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   @JsonProperty(OSPF_AREA_VAR)
   public void setOspfArea(OspfArea ospfArea) {
      _ospfArea = ospfArea;
   }

   @JsonProperty(OSPF_COST_VAR)
   public void setOspfCost(Integer ospfCost) {
      _ospfCost = ospfCost;
   }

   @JsonProperty(OSPF_DEAD_INTERVAL_VAR)
   public void setOspfDeadInterval(int seconds) {
      _ospfDeadInterval = seconds;
   }

   @JsonProperty(OSPF_ENABLED_VAR)
   public void setOspfEnabled(boolean b) {
      _ospfEnabled = b;
   }

   @JsonProperty(OSPF_HELLO_MULTIPLIER_VAR)
   public void setOspfHelloMultiplier(int multiplier) {
      _ospfHelloMultiplier = multiplier;
   }

   @JsonProperty(OSPF_PASSIVE_VAR)
   public void setOspfPassive(boolean passive) {
      _ospfPassive = passive;
   }

   @JsonProperty(OUTGOING_FILTER_VAR)
   public void setOutgoingFilter(IpAccessList filter) {
      _outgoingFilter = filter;
   }

   @JsonProperty(OWNER_VAR)
   public void setOwner(Configuration owner) {
      _owner = owner;
   }

   @JsonProperty(PREFIX_VAR)
   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   @JsonProperty(ROUTING_POLICY_VAR)
   public void setRoutingPolicy(PolicyMap policy) {
      _routingPolicy = policy;
   }

   @JsonProperty(SWITCHPORT_MODE_VAR)
   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   @JsonProperty(SWITCHPORT_TRUNK_ENCAPSULATION_VAR)
   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

   @JsonProperty(ZONE_VAR)
   public void setZone(Zone zone) {
      _zone = zone;
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject iface = new JSONObject();
      iface.put("node", _owner.getName());
      iface.put("name", _key);
      iface.put("prefix", _prefix.toString());
      InterfaceType interfaceType = computeInterfaceType();
      iface.put("type", interfaceType.toString());
      return iface;
   }

}
