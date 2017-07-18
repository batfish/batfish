package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public final class Interface extends ComparableStructure<String> {

   private static final String ACCESS_VLAN_VAR = "accessVlan";

   private static final String ACTIVE_VAR = "active";

   private static final String ALL_PREFIXES_VAR = "allPrefixes";

   private static final String ALLOWED_VLANS_VAR = "allowedVlans";

   private static final String AUTOSTATE_VAR = "autostate";

   private static final String BANDWIDTH_VAR = "bandwidth";

   private static final int DEFAULT_MTU = 1500;

   private static final String DESCRIPTION_VAR = "description";

   private static final String DHCP_RELAY_ADDRESSES_VAR = "dhcpRelayAddresses";

   public static final String FLOW_SINK_TERMINATION_NAME = "flow_sink_termination";

   private static final String INBOUND_FILTER_VAR = "inboundFilter";

   private static final String INCOMING_FILTER_VAR = "incomingFilter";

   private static final String INTERFACE_TYPE_VAR = "type";

   private static final String ISIS_COST_VAR = "isisCost";

   private static final String ISIS_L1_INTERFACE_MODE_VAR = "isisL1InterfaceMode";

   private static final String ISIS_L2_INTERFACE_MODE_VAR = "isisL2InterfaceMode";

   private static final String MTU_VAR = "mtu";

   private static final String NATIVE_VLAN_VAR = "nativeVlan";

   public static final String NULL_INTERFACE_NAME = "null_interface";

   private static final String OSPF_AREA_VAR = "ospfArea";

   private static final String OSPF_COST_VAR = "ospfCost";

   private static final String OSPF_DEAD_INTERVAL_VAR = "ospfDeadInterval";

   private static final String OSPF_ENABLED_VAR = "ospfEnabled";

   private static final String OSPF_HELLO_MULTIPLIER_VAR = "ospfHelloMultiplier";

   private static final String OSPF_PASSIVE_VAR = "ospfPassive";

   private static final String OUTGOING_FILTER_VAR = "outgoingFilter";

   private static final String PREFIX_VAR = "prefix";

   private static final String ROUTING_POLICY_VAR = "routingPolicy";

   private static final long serialVersionUID = 1L;

   private static final String SOURCE_NAT_VAR = "sourceNat";

   private static final String SPANNING_TREE_PORTFAST_VAR = "spanningTreePortfast";

   private static final String SWITCHPORT_MODE_VAR = "switchportMode";

   private static final String SWITCHPORT_TRUNK_ENCAPSULATION_VAR = "switchportTrunkEncapsulation";

   private static final String SWITCHPORT_VAR = "switchport";

   private static final String VRF_VAR = "vrf";

   private static final String VRRP_GROUPS_VAR = "vrrpGroups";

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
      else if (name.startsWith("Bundle-Ethernet")) {
         return InterfaceType.AGGREGATED;
      }
      else if (name.startsWith("cmp-mgmt")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Crypto-Engine")) {
         return InterfaceType.VPN; // IPSec VPN
      }
      else if (name.startsWith("Dialer")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Dot11Radio")) {
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
      else if (name.startsWith("FortyGigabitEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("GigabitEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("GMPLS")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("HundredGigabitEthernet")) {
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
         return InterfaceType.NULL;
      }
      else if (name.startsWith("Port-Channel")) {
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
         return InterfaceType.TUNNEL;
      }
      else if (name.startsWith("tunnel-ip")) {
         return InterfaceType.TUNNEL;
      }
      else if (name.startsWith("tunnel-te")) {
         return InterfaceType.TUNNEL;
      }
      else if (name.startsWith("Vlan")) {
         return InterfaceType.VLAN;
      }
      else if (name.startsWith("Vxlan")) {
         return InterfaceType.TUNNEL;
      }
      else {
         return InterfaceType.UNKNOWN;
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

   public static InterfaceType computeInterfaceType(
         String name,
         ConfigurationFormat format) {
      switch (format) {
      case ALCATEL_AOS:
         return computeAosInteraceType(name);

      case AWS_VPC:
         return computeAwsInterfaceType(name);

      case ARISTA:
      case CISCO_IOS:
      case CISCO_IOS_XR:
      case CISCO_NX:
      case FOUNDRY:
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
         // $CASES-OMITTED$
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

   private boolean _autoState;

   private Double _bandwidth;

   private transient boolean _blacklisted;

   private String _description;

   private SortedSet<Ip> _dhcpRelayAddresses;

   private IpAccessList _inboundFilter;

   private transient String _inboundFilterName;

   private IpAccessList _incomingFilter;

   private transient String _incomingFilterName;

   private InterfaceType _interfaceType;

   private Integer _isisCost;

   private IsisInterfaceMode _isisL1InterfaceMode;

   private IsisInterfaceMode _isisL2InterfaceMode;

   private int _mtu;

   private int _nativeVlan;

   private OspfArea _ospfArea;

   private transient Long _ospfAreaName;

   private Integer _ospfCost;

   private int _ospfDeadInterval;

   private boolean _ospfEnabled;

   private int _ospfHelloMultiplier;

   private boolean _ospfPassive;

   private IpAccessList _outgoingFilter;

   private transient String _outgoingFilterName;

   private Configuration _owner;

   private Prefix _prefix;

   private Boolean _proxyArp;

   private RoutingPolicy _routingPolicy;

   private transient String _routingPolicyName;

   private SourceNat _sourceNat;

   private boolean _spanningTreePortfast;

   private Boolean _switchport;

   private SwitchportMode _switchportMode;

   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   private Vrf _vrf;

   private transient String _vrfName;

   private SortedMap<Integer, VrrpGroup> _vrrpGroups;

   private Zone _zone;

   private transient String _zoneName;

   @SuppressWarnings("unused")
   private Interface() {
      this(null, null);
   }

   @JsonCreator
   public Interface(@JsonProperty(NAME_VAR) String name) {
      this(name, null);
   }

   public Interface(String name, Configuration owner) {
      super(name);
      _active = true;
      _autoState = true;
      _allowedVlans = new ArrayList<>();
      _allPrefixes = new TreeSet<>();
      _interfaceType = InterfaceType.UNKNOWN;
      _mtu = DEFAULT_MTU;
      _nativeVlan = 1;
      _owner = owner;
      _switchportMode = SwitchportMode.NONE;
      _switchportTrunkEncapsulation = SwitchportEncapsulationType.DOT1Q;
      _isisL1InterfaceMode = IsisInterfaceMode.UNSET;
      _isisL2InterfaceMode = IsisInterfaceMode.UNSET;
      _vrfName = Configuration.DEFAULT_VRF_NAME;
      _vrrpGroups = new TreeMap<>();

      computeInterfaceType();
   }

   public void addAllowedRanges(List<SubRange> ranges) {
      _allowedVlans.addAll(ranges);
   }

   private void computeInterfaceType() {
      if ((_key != null) && (_owner != null)) {
         _interfaceType = computeInterfaceType(
               _key,
               _owner.getConfigurationFormat());
      }
   }

   @Override
   public boolean equals(Object object) {

      if (this == object) {
         return true;
      }
      Interface other = (Interface) object;
      if (this._accessVlan != other._accessVlan) {
         return false;
      }
      if (this._active != other._active) {
         return false;
      }
      if (!this._allowedVlans.equals(other._allowedVlans)) {
         return false;
      }
      if (!this._allPrefixes.equals(other._allPrefixes)) {
         return false;
      }
      if (this._autoState != other._autoState) {
         return false;
      }
      if (this._bandwidth.compareTo(other._bandwidth) != 0) {
         return false;
      }
      // we check ACLs for name match only -- full ACL diff can be done
      // elsewhere.
      if (!IpAccessList.bothNullOrSameName(
            this.getInboundFilter(),
            other.getInboundFilter())) {
         return false;
      }

      if (!IpAccessList.bothNullOrSameName(
            this.getIncomingFilter(),
            other.getIncomingFilter())) {
         return false;
      }

      if (this._interfaceType != other._interfaceType) {
         return false;
      }

      // TODO: check ISIS settings for equality.
      if (this._mtu != other._mtu) {
         return false;
      }
      if (this._nativeVlan != other._nativeVlan) {
         return false;
      }
      // TODO: check OSPF settings for equality.

      if (!IpAccessList.bothNullOrSameName(
            this._outgoingFilter,
            other._outgoingFilter)) {
         return false;
      }

      if (!CommonUtil.bothNullOrEqual(this._prefix, other._prefix)) {
         return false;
      }

      if (!CommonUtil.bothNullOrEqual(
            this._routingPolicy,
            other._routingPolicy)) {
         return false;
      }

      if (!CommonUtil.bothNullOrEqual(
            this._switchportMode,
            other._switchportMode)) {
         return false;
      }

      if (!CommonUtil.bothNullOrEqual(this._zone, other._zone)) {
         return false;
      }
      return true;
   }

   @JsonProperty(ACCESS_VLAN_VAR)
   @JsonPropertyDescription("Number of access VLAN when switchport mode is ACCESS")
   public int getAccessVlan() {
      return _accessVlan;
   }

   @JsonProperty(ACTIVE_VAR)
   @JsonPropertyDescription("Whether this interface is administratively active (true) or disabled (false)")
   public boolean getActive() {
      return _active;
   }

   @JsonProperty(ALLOWED_VLANS_VAR)
   @JsonPropertyDescription("Ranges of allowed VLANs when switchport mode is TRUNK")
   public List<SubRange> getAllowedVlans() {
      return _allowedVlans;
   }

   @JsonProperty(ALL_PREFIXES_VAR)
   @JsonPropertyDescription("All IPV4 address/network assignments on this interface")
   public Set<Prefix> getAllPrefixes() {
      return _allPrefixes;
   }

   @JsonProperty(AUTOSTATE_VAR)
   @JsonPropertyDescription("Whether this VLAN interface's operational status is dependent on corresponding member switchports")
   public boolean getAutoState() {
      return _autoState;
   }

   @JsonProperty(BANDWIDTH_VAR)
   @JsonPropertyDescription("The nominal bandwidth of this interface in bits/sec for use in protocol cost calculations")
   public Double getBandwidth() {
      return _bandwidth;
   }

   @JsonIgnore
   public boolean getBlacklisted() {
      return _blacklisted;
   }

   @JsonProperty(DESCRIPTION_VAR)
   @JsonPropertyDescription("Description of this interface")
   public String getDescription() {
      return _description;
   }

   @JsonProperty(DHCP_RELAY_ADDRESSES_VAR)
   public SortedSet<Ip> getDhcpRelayAddresses() {
      return _dhcpRelayAddresses;
   }

   @JsonIgnore
   public IpAccessList getInboundFilter() {
      return _inboundFilter;
   }

   @JsonProperty(INBOUND_FILTER_VAR)
   @JsonPropertyDescription("The IPV4 access-list used to filter traffic destined for this device on this interface.")
   public String getInboundFilterName() {
      if (_inboundFilter != null) {
         return _inboundFilter.getName();
      }
      else {
         return _inboundFilterName;
      }
   }

   @JsonIgnore
   public IpAccessList getIncomingFilter() {
      return _incomingFilter;
   }

   @JsonProperty(INCOMING_FILTER_VAR)
   @JsonPropertyDescription("The IPV4 access-list used to filter traffic that arrives on this interface.")
   public String getIncomingFilterName() {
      if (_incomingFilter != null) {
         return _incomingFilter.getName();
      }
      else {
         return _incomingFilterName;
      }
   }

   @JsonProperty(INTERFACE_TYPE_VAR)
   @JsonPropertyDescription("The type of this interface")
   public InterfaceType getInterfaceType() {
      return _interfaceType;
   }

   @JsonProperty(ISIS_COST_VAR)
   @JsonPropertyDescription("The IS-IS cost of this interface")
   public Integer getIsisCost() {
      return _isisCost;
   }

   @JsonProperty(ISIS_L1_INTERFACE_MODE_VAR)
   @JsonPropertyDescription("Specifies whether this interface is active, passive, or unconfigured with respect to IS-IS level 1")
   public IsisInterfaceMode getIsisL1InterfaceMode() {
      return _isisL1InterfaceMode;
   }

   @JsonProperty(ISIS_L2_INTERFACE_MODE_VAR)
   @JsonPropertyDescription("Specifies whether this interface is active, passive, or unconfigured with respect to IS-IS level 2")
   public IsisInterfaceMode getIsisL2InterfaceMode() {
      return _isisL2InterfaceMode;
   }

   @JsonProperty(MTU_VAR)
   @JsonPropertyDescription("The maximum transmission unit (MTU) of this interface in bytes")
   public int getMtu() {
      return _mtu;
   }

   @JsonProperty(NATIVE_VLAN_VAR)
   @JsonPropertyDescription("The native VLAN of this interface when switchport mode is TRUNK")
   public int getNativeVlan() {
      return _nativeVlan;
   }

   @JsonIgnore
   public OspfArea getOspfArea() {
      return _ospfArea;
   }

   @JsonProperty(OSPF_AREA_VAR)
   @JsonPropertyDescription("The OSPF area to which this interface belongs.")
   public Long getOspfAreaName() {
      if (_ospfArea != null) {
         return _ospfArea.getName();
      }
      else {
         return _ospfAreaName;
      }
   }

   @JsonProperty(OSPF_COST_VAR)
   @JsonPropertyDescription("The explicit OSPF cost of this interface. If unset, the cost is automatically calculated.")
   public Integer getOspfCost() {
      return _ospfCost;
   }

   @JsonProperty(OSPF_DEAD_INTERVAL_VAR)
   @JsonPropertyDescription("Dead-interval in seconds for OSPF updates")
   public int getOspfDeadInterval() {
      return _ospfDeadInterval;
   }

   @JsonProperty(OSPF_ENABLED_VAR)
   @JsonPropertyDescription("Whether or not OSPF is enabled at all on this interface (either actively or passively)")
   public boolean getOspfEnabled() {
      return _ospfEnabled;
   }

   @JsonProperty(OSPF_HELLO_MULTIPLIER_VAR)
   @JsonPropertyDescription("Number of OSPF packets to send out during dead-interval period for fast OSPF updates")
   public int getOspfHelloMultiplier() {
      return _ospfHelloMultiplier;
   }

   @JsonProperty(OSPF_PASSIVE_VAR)
   @JsonPropertyDescription("Whether or not OSPF is enabled passively on this interface. If passive, this interface is included in the OSPF RIB, but no OSPF packets are sent from it.")
   public boolean getOspfPassive() {
      return _ospfPassive;
   }

   @JsonIgnore
   public IpAccessList getOutgoingFilter() {
      return _outgoingFilter;
   }

   @JsonProperty(OUTGOING_FILTER_VAR)
   @JsonPropertyDescription("The IPV4 access-list used to filter traffic that is sent out this interface. Stored as @id")
   public String getOutgoingFilterName() {
      if (_outgoingFilter != null) {
         return _outgoingFilter.getName();
      }
      else {
         return _outgoingFilterName;
      }
   }

   @JsonIgnore
   public Configuration getOwner() {
      return _owner;
   }

   @JsonProperty(PREFIX_VAR)
   @JsonPropertyDescription("The primary IPV4 address/network of this interface")
   public Prefix getPrefix() {
      return _prefix;
   }

   @JsonPropertyDescription("Whether or not proxy-ARP is enabled on this interface.")
   public Boolean getProxyArp() {
      return _proxyArp;
   }

   @JsonIgnore
   public RoutingPolicy getRoutingPolicy() {
      return _routingPolicy;
   }

   @JsonProperty(ROUTING_POLICY_VAR)
   @JsonPropertyDescription("The routing policy used on this interface for policy-routing (as opposed to destination-routing). Stored as @id")
   public String getRoutingPolicyName() {
      if (_routingPolicy != null) {
         return _routingPolicy.getName();
      }
      else {
         return _routingPolicyName;
      }
   }

   @JsonProperty(SOURCE_NAT_VAR)
   public SourceNat getSourceNat() {
      return _sourceNat;
   }

   @JsonProperty(SPANNING_TREE_PORTFAST_VAR)
   @JsonPropertyDescription("Whether or not spanning-tree portfast feature is enabled")
   public boolean getSpanningTreePortfast() {
      return _spanningTreePortfast;
   }

   @JsonProperty(SWITCHPORT_VAR)
   @JsonPropertyDescription("Whether this interface is explicitly set as a switchport. Nothing may be inferred from absence of this field.")
   public Boolean getSwitchport() {
      return _switchport;
   }

   @JsonProperty(SWITCHPORT_MODE_VAR)
   @JsonPropertyDescription("The switchport mode (if any) of this interface")
   public SwitchportMode getSwitchportMode() {
      return _switchportMode;
   }

   @JsonProperty(SWITCHPORT_TRUNK_ENCAPSULATION_VAR)
   @JsonPropertyDescription("The switchport trunk encapsulation type of this interface. Only relevant when switchport mode is TRUNK")
   public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
      return _switchportTrunkEncapsulation;
   }

   @JsonIgnore
   public Vrf getVrf() {
      return _vrf;
   }

   @JsonProperty(VRF_VAR)
   @JsonPropertyDescription("The name of the VRF to which this interface belongs")
   public String getVrfName() {
      if (_vrf != null) {
         return _vrf.getName();
      }
      else {
         return _vrfName;
      }
   }

   @JsonProperty(VRRP_GROUPS_VAR)
   public SortedMap<Integer, VrrpGroup> getVrrpGroups() {
      return _vrrpGroups;
   }

   @JsonIgnore
   public Zone getZone() {
      return _zone;
   }

   @JsonProperty(ZONE_VAR)
   @JsonPropertyDescription("The firewall zone to which this interface belongs.")
   public String getZoneName() {
      if (_zone != null) {
         return _zone.getName();
      }
      else {
         return _zoneName;
      }
   }

   public boolean isLoopback(ConfigurationFormat vendor) {
      String name = _key.toLowerCase();
      if (vendor == ConfigurationFormat.JUNIPER
            || vendor == ConfigurationFormat.FLAT_JUNIPER) {
         if (!name.contains(".")) {
            return false;
         }
      }
      else if (name.contains("longreach")) {
         return false;
      }
      return name.startsWith("lo");
   }

   public void resolveReferences(Configuration owner) {
      _owner = owner;
      _vrf = owner.getVrfs().get(_vrfName);
      if (_inboundFilterName != null) {
         _inboundFilter = owner.getIpAccessLists().get(_inboundFilterName);
      }
      if (_incomingFilterName != null) {
         _incomingFilter = owner.getIpAccessLists().get(_incomingFilterName);
      }
      if (_outgoingFilterName != null) {
         _outgoingFilter = owner.getIpAccessLists().get(_outgoingFilterName);
      }
      if (_ospfAreaName != null) {
         OspfProcess ospfProc = _vrf.getOspfProcess();
         if (ospfProc != null) {
            _ospfArea = ospfProc.getAreas().get(_ospfAreaName);
         }
      }
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

   @JsonProperty(AUTOSTATE_VAR)
   public void setAutoState(boolean autoState) {
      _autoState = autoState;
   }

   @JsonProperty(BANDWIDTH_VAR)
   public void setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
   }

   @JsonIgnore
   public void setBlacklisted(boolean blacklisted) {
      _blacklisted = blacklisted;
   }

   @JsonProperty(DESCRIPTION_VAR)
   public void setDescription(String description) {
      _description = description;
   }

   @JsonProperty(DHCP_RELAY_ADDRESSES_VAR)
   public void setDhcpRelayAddresses(SortedSet<Ip> dhcpRelayAddresses) {
      _dhcpRelayAddresses = dhcpRelayAddresses;
   }

   @JsonIgnore
   public void setInboundFilter(IpAccessList inboundFilter) {
      _inboundFilter = inboundFilter;
   }

   @JsonProperty(INBOUND_FILTER_VAR)
   public void setInboundFilterName(String inboundFilterName) {
      _inboundFilterName = inboundFilterName;
   }

   @JsonIgnore
   public void setIncomingFilter(IpAccessList incomingFilter) {
      _incomingFilter = incomingFilter;
   }

   @JsonProperty(INCOMING_FILTER_VAR)
   public void setIncomingFilterName(String incomingFilterName) {
      _incomingFilterName = incomingFilterName;
   }

   @JsonProperty(INTERFACE_TYPE_VAR)
   public void setInterfaceType(InterfaceType it) {
      _interfaceType = it;
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

   @JsonProperty(MTU_VAR)
   public void setMtu(int mtu) {
      _mtu = mtu;
   }

   @JsonProperty(NATIVE_VLAN_VAR)
   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   @JsonIgnore
   public void setOspfArea(OspfArea ospfArea) {
      _ospfArea = ospfArea;
   }

   @JsonProperty(OSPF_AREA_VAR)
   public void setOspfAreaName(Long ospfAreaName) {
      _ospfAreaName = ospfAreaName;
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

   @JsonIgnore
   public void setOutgoingFilter(IpAccessList outgoingFilter) {
      _outgoingFilter = outgoingFilter;
   }

   @JsonProperty(OUTGOING_FILTER_VAR)
   public void setOutgoingFilter(String outgoingFilterName) {
      _outgoingFilterName = outgoingFilterName;
   }

   @JsonIgnore
   public void setOwner(Configuration owner) {
      _owner = owner;
   }

   @JsonProperty(PREFIX_VAR)
   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   public void setProxyArp(Boolean proxyArp) {
      _proxyArp = proxyArp;
   }

   @JsonIgnore
   public void setRoutingPolicy(RoutingPolicy routingPolicy) {
      _routingPolicy = routingPolicy;
   }

   @JsonProperty(ROUTING_POLICY_VAR)
   public void setRoutingPolicy(String routingPolicyName) {
      _routingPolicyName = routingPolicyName;
   }

   @JsonProperty(SOURCE_NAT_VAR)
   public void setSourceNat(SourceNat sourceNat) {
      _sourceNat = sourceNat;
   }

   @JsonProperty(SPANNING_TREE_PORTFAST_VAR)
   public void setSpanningTreePortfast(boolean spanningTreePortfast) {
      _spanningTreePortfast = spanningTreePortfast;
   }

   @JsonProperty(SWITCHPORT_VAR)
   public void setSwitchport(Boolean switchport) {
      _switchport = switchport;
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

   @JsonIgnore
   public void setVrf(Vrf vrf) {
      _vrf = vrf;
      if (vrf != null) {
         _vrfName = vrf.getName();
      }
   }

   @JsonProperty(VRF_VAR)
   public void setVrfName(String vrfName) {
      _vrfName = vrfName;
   }

   @JsonProperty(VRRP_GROUPS_VAR)
   public void setVrrpGroups(SortedMap<Integer, VrrpGroup> vrrpGroups) {
      _vrrpGroups = vrrpGroups;
   }

   @JsonIgnore
   public void setZone(Zone zone) {
      _zone = zone;
   }

   @JsonProperty(ZONE_VAR)
   public void setZoneName(String zoneName) {
      _zoneName = zoneName;
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject iface = new JSONObject();
      iface.put("node", _owner.getName());
      iface.put("name", _key);
      iface.put(PREFIX_VAR, _prefix.toString());
      iface.put(INTERFACE_TYPE_VAR, _interfaceType.toString());
      return iface;
   }

}
