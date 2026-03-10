package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.BgpAggregate;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathExpr;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.vendor_family.VendorFamily;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;

/**
 * A Configuration represents an autonomous network device, such as a router, host, switch, or
 * firewall.
 */
public final class Configuration implements Serializable {

  public static Builder builder() {
    return new Builder(null);
  }

  public static Builder builder(@Nonnull Supplier<String> hostnameGenerator) {
    return new Builder(hostnameGenerator);
  }

  public static class Builder {

    private ConfigurationFormat _configurationFormat;
    private @Nullable DeviceModel _deviceModel;
    private String _hostname;
    private String _humanName;
    private Supplier<String> _hostnameGenerator;
    private String _domainName;
    private LineAction _defaultCrossZoneAction;
    private LineAction _defaultInboundAction;
    private boolean _disconnectAdminDownInterfaces;
    private boolean _mainRibEnforceResolvability;

    private Builder(@Nullable Supplier<String> hostnameGenerator) {
      _hostnameGenerator = hostnameGenerator;
      _disconnectAdminDownInterfaces = true;
    }

    public Configuration build() {
      checkArgument(
          _hostname != null || _hostnameGenerator != null,
          "Must set hostname or supply name generator");
      String name = _hostname != null ? _hostname : _hostnameGenerator.get().toLowerCase();
      Configuration configuration = new Configuration(name, _configurationFormat);
      configuration.setHumanName(_humanName);
      if (_defaultCrossZoneAction != null) {
        configuration.setDefaultCrossZoneAction(_defaultCrossZoneAction);
      }
      if (_defaultInboundAction != null) {
        configuration.setDefaultInboundAction(_defaultInboundAction);
      }
      configuration.setDeviceModel(_deviceModel);
      configuration.setDomainName(_domainName);
      configuration.setMainRibEnforceResolvability(_mainRibEnforceResolvability);
      configuration.setDisconnectAdminDownInterfaces(_disconnectAdminDownInterfaces);
      return configuration;
    }

    public Builder setConfigurationFormat(ConfigurationFormat configurationFormat) {
      _configurationFormat = configurationFormat;
      return this;
    }

    public Builder setDeviceModel(@Nullable DeviceModel model) {
      _deviceModel = model;
      return this;
    }

    public Builder setDomainName(String domainName) {
      _domainName = domainName;
      return this;
    }

    public Builder setHostname(@Nullable String hostname) {
      if (hostname != null) {
        _hostname = hostname.toLowerCase();
      } else {
        _hostname = null;
      }
      return this;
    }

    public Builder setHumanName(@Nullable String humanName) {
      _humanName = humanName;
      return this;
    }

    public Builder setDefaultCrossZoneAction(LineAction defaultCrossZoneAction) {
      _defaultCrossZoneAction = defaultCrossZoneAction;
      return this;
    }

    public Builder setDefaultInboundAction(LineAction defaultInboundAction) {
      _defaultInboundAction = defaultInboundAction;
      return this;
    }

    public @Nonnull Builder setDisconnectAdminDownInterfaces(
        boolean disconnectAdminDownInterfaces) {
      _disconnectAdminDownInterfaces = disconnectAdminDownInterfaces;
      return this;
    }

    public @Nonnull Builder setMainRibEnforceResolvability(boolean mainRibEnforceResolvability) {
      _mainRibEnforceResolvability = mainRibEnforceResolvability;
      return this;
    }
  }

  public static final String DEFAULT_VRF_NAME = "default";

  private static final String PROP_AS_PATH_ACCESS_LISTS = "asPathAccessLists";
  private static final String PROP_AS_PATH_EXPRS = "asPathExprs";
  private static final String PROP_AS_PATH_MATCH_EXPRS = "asPathMatchExprs";
  private static final String PROP_AUTHENTICATION_KEY_CHAINS = "authenticationKeyChains";
  private static final String PROP_COMMUNITY_MATCH_EXPRS = "communityMatchExprs";
  private static final String PROP_COMMUNITY_SET_EXPRS = "communitySetExprs";
  private static final String PROP_COMMUNITY_SET_MATCH_EXPRS = "communitySetMatchExprs";
  private static final String PROP_COMMUNITY_SETS = "communitySets";
  private static final String PROP_CONFIGURATION_FORMAT = "configurationFormat";
  private static final String PROP_DEFAULT_CROSS_ZONE_ACTION = "defaultCrossZoneAction";
  private static final String PROP_DEFAULT_INBOUND_ACTION = "defaultInboundAction";
  private static final String PROP_DEVICE_MODEL = "deviceModel";
  private static final String PROP_DEVICE_TYPE = "deviceType";
  private static final String PROP_DISCONNECT_ADMIN_DOWN_INTERFACES =
      "disconnectAdminDownInterfaces";
  private static final String PROP_DNS_SERVERS = "dnsServers";
  private static final String PROP_DNS_SOURCE_INTERFACE = "dnsSourceInterface";
  private static final String PROP_DOMAIN_NAME = "domainName";
  private static final String PROP_EXPORT_BGP_FROM_BGP_RIB = "exportBgpFromBgpRib";
  private static final String PROP_GENERATE_BGP_AGGREGATES_FROM_MAIN_RIB =
      "generateBgpAggregatesFromMainRib";
  private static final String PROP_GENERATED_REFERENCE_BOOKS = "generatedReferenceBooks";
  private static final String PROP_HUMAN_NAME = "humanName";
  private static final String PROP_IKE_PHASE1_KEYS = "ikePhase1Keys";
  private static final String PROP_IKE_PHASE1_POLICIES = "ikePhase1Policies";
  private static final String PROP_IKE_PHASE1_PROPOSALS = "ikePhase1Proposals";
  private static final String PROP_INTERFACES = "interfaces";
  private static final String PROP_IP6_ACCESS_LISTS = "ip6AccessLists";
  private static final String PROP_IP_ACCESS_LISTS = "ipAccessLists";
  private static final String PROP_IP_SPACES = "ipSpaces";
  private static final String PROP_IP_SPACE_METADATA = "ipSpaceMetadata";
  private static final String PROP_IPSEC_PEER_CONFIGS = "ipsecPeerConfigs";
  private static final String PROP_IPSEC_PHASE2_POLICIES = "ipsecPhase2Policies";
  private static final String PROP_IPSEC_PHASE2_PROPOSALS = "ipsecPhase2Proposals";
  private static final String PROP_LOGGING_SERVERS = "loggingServers";
  private static final String PROP_LOGGING_SOURCE_INTERFACE = "loggingSourceInterface";
  private static final String PROP_MAIN_RIB_ENFORCE_RESOLVABILITY = "mainRibEnforceResolvability";
  private static final String PROP_MLAGS = "mlags";
  private static final String PROP_NAME = "name";
  private static final String PROP_NTP_SERVERS = "ntpServers";
  private static final String PROP_NTP_SOURCE_INTERFACE = "ntpSourceInterface";
  private static final String PROP_PACKET_POLICIES = "packetPolicies";
  private static final String PROP_ROUTE6_FILTER_LISTS = "route6FilterLists";
  private static final String PROP_ROUTE_FILTER_LISTS = "routeFilterLists";
  private static final String PROP_ROUTING_POLICIES = "routingPolicies";
  private static final String PROP_SNMP_SOURCE_INTERFACE = "snmpSourceInterface";
  private static final String PROP_SNMP_TRAP_SERVERS = "snmpTrapServers";
  private static final String PROP_TACACS_SERVERS = "tacacsServers";
  private static final String PROP_TACACS_SOURCE_INTERFACE = "tacacsSourceInterface";
  private static final String PROP_TRACKING_GROUPS = "trackingGroups";
  private static final String PROP_VENDOR_FAMILY = "vendorFamily";
  private static final String PROP_VRFS = "vrfs";
  private static final String PROP_ZONES = "zones";

  private static final int VLAN_NORMAL_MAX_DEFAULT = 4094;

  private static final int VLAN_NORMAL_MIN_DEFAULT = 1;

  private Map<String, AsPathAccessList> _asPathAccessLists;

  private Map<String, AuthenticationKeyChain> _authenticationKeyChains;

  private Map<String, AsPathExpr> _asPathExprs;
  private Map<String, AsPathMatchExpr> _asPathMatchExprs;
  private Map<String, CommunityMatchExpr> _communityMatchExprs;
  private Map<String, CommunitySetExpr> _communitySetExprs;
  private Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
  private Map<String, CommunitySet> _communitySets;

  private final ConfigurationFormat _configurationFormat;

  private LineAction _defaultCrossZoneAction;

  private LineAction _defaultInboundAction;

  private DeviceModel _deviceModel;
  private DeviceType _deviceType;

  private boolean _disconnectAdminDownInterfaces;

  private Set<String> _dnsServers;

  private String _dnsSourceInterface;

  private String _domainName;

  /**
   * Whether the BGP export pipeline should start with main RIB routes (Juniper-like behavior) or
   * BGP RIB routes (Cisco-like behavior).
   */
  private boolean _exportBgpFromBgpRib;

  /**
   * Whether {@link BgpAggregate BGP aggregates} should be generated using routes in the main RIB
   * (Arista-like behavior) or routes in the BGP RIB (Cisco-like behavior).
   */
  private boolean _generateBgpAggregatesFromMainRib;

  private Map<String, ReferenceBook> _generatedReferenceBooks;

  private @Nonnull Map<String, IkePhase1Key> _ikePhase1keys;

  private Map<String, IkePhase1Proposal> _ikePhase1Proposals;

  private Map<String, IkePhase1Policy> _ikePhase1Policies;

  private Map<String, Interface> _interfaces;

  private Map<String, IpAccessList> _ipAccessLists;

  private Map<String, IpSpace> _ipSpaces;

  private Map<String, IpSpaceMetadata> _ipSpaceMetadata;

  private Map<String, IpsecPeerConfig> _ipsecPeerConfigs;

  private Map<String, IpsecPhase2Policy> _ipsecPhase2Policies;

  private Map<String, IpsecPhase2Proposal> _ipsecPhase2Proposals;

  private @Nullable Map<Location, LocationInfo> _locationInfo;

  private Set<String> _loggingServers;

  private String _loggingSourceInterface;

  private boolean _mainRibEnforceResolvability;

  private Map<String, Mlag> _mlags;

  private final String _name;
  private @Nullable String _humanName;

  /**
   * Normal =&gt; Excluding extended and reserved vlans that should not be modified or deleted. Also
   * excludes vlans on relevant devices (e.g. NX-OS) that are associated with an L3 VNI.
   */
  private @Nonnull IntegerSpace _normalVlanRange;

  private Set<String> _ntpServers;

  private String _ntpSourceInterface;

  private Map<String, PacketPolicy> _packetPolicies;

  private Map<String, RouteFilterList> _routeFilterLists;

  private Map<String, RoutingPolicy> _routingPolicies;

  private String _snmpSourceInterface;

  private Set<String> _snmpTrapServers;

  private Set<String> _tacacsServers;

  private String _tacacsSourceInterface;

  private Map<String, TrackMethod> _trackingGroups;

  private VendorFamily _vendorFamily;

  private Map<String, Vrf> _vrfs;

  private Map<String, Zone> _zones;

  @JsonCreator
  private static Configuration makeConfiguration(
      @JsonProperty(PROP_NAME) @Nullable String hostname,
      @JsonProperty(PROP_CONFIGURATION_FORMAT) @Nullable ConfigurationFormat configurationFormat,
      @JsonProperty(PROP_EXPORT_BGP_FROM_BGP_RIB) @Nullable Boolean exportBgpFromBgpRib,
      @JsonProperty(PROP_GENERATE_BGP_AGGREGATES_FROM_MAIN_RIB) @Nullable
          Boolean generateBgpAggregatesFromMainRib) {
    checkNotNull(hostname, "%s cannot be null", PROP_NAME);
    checkNotNull(configurationFormat, "%s cannot be null", PROP_CONFIGURATION_FORMAT);
    Configuration c = new Configuration(hostname, configurationFormat);
    c.setExportBgpFromBgpRib(Boolean.TRUE.equals(exportBgpFromBgpRib));
    c.setGenerateBgpAggregatesFromMainRib(Boolean.TRUE.equals(generateBgpAggregatesFromMainRib));
    return c;
  }

  public Configuration(@Nonnull String hostname, @Nonnull ConfigurationFormat configurationFormat) {
    _name = hostname.toLowerCase();
    _asPathAccessLists = new TreeMap<>();
    _asPathExprs = new HashMap<>();
    _asPathMatchExprs = new HashMap<>();
    _authenticationKeyChains = new TreeMap<>();
    _communityMatchExprs = new HashMap<>();
    _communitySetExprs = new HashMap<>();
    _communitySetMatchExprs = new HashMap<>();
    _communitySets = new HashMap<>();
    _configurationFormat = configurationFormat;
    _disconnectAdminDownInterfaces = true;
    _dnsServers = new TreeSet<>();
    _domainName = null;
    _generatedReferenceBooks = new TreeMap<>();
    _ikePhase1keys = new TreeMap<>();
    _ikePhase1Policies = new TreeMap<>();
    _ikePhase1Proposals = new TreeMap<>();
    _interfaces = new TreeMap<>();
    _ipAccessLists = new TreeMap<>();
    _ipSpaces = new HashMap<>();
    _ipSpaceMetadata = new TreeMap<>();
    _ipsecPeerConfigs = new TreeMap<>();
    _ipsecPhase2Policies = new TreeMap<>();
    _ipsecPhase2Proposals = new TreeMap<>();
    _loggingServers = new TreeSet<>();
    _mlags = ImmutableSortedMap.of();
    _normalVlanRange =
        IntegerSpace.of(new SubRange(VLAN_NORMAL_MIN_DEFAULT, VLAN_NORMAL_MAX_DEFAULT));
    _ntpServers = new TreeSet<>();
    _packetPolicies = new TreeMap<>();
    _routeFilterLists = new TreeMap<>();
    _routingPolicies = new TreeMap<>();
    _snmpTrapServers = new TreeSet<>();
    _tacacsServers = new TreeSet<>();
    _trackingGroups = new TreeMap<>();
    _vendorFamily = new VendorFamily();
    _vrfs = new TreeMap<>();
    _zones = new TreeMap<>();
  }

  private void computeRoutingPolicySources(String routingPolicyName, Warnings w) {
    if (routingPolicyName == null) {
      return;
    }
    RoutingPolicy routingPolicy = _routingPolicies.get(routingPolicyName);
    if (routingPolicy == null) {
      return;
    }
    routingPolicy.computeSources(Collections.emptySet(), _routingPolicies, w);
  }

  public void computeRoutingPolicySources(Warnings w) {
    for (String rpName : _routingPolicies.keySet()) {
      computeRoutingPolicySources(rpName, w);
    }
    for (Vrf vrf : _vrfs.values()) {
      BgpProcess bgpProcess = vrf.getBgpProcess();
      if (bgpProcess != null) {
        for (BgpPeerConfig neighbor : bgpProcess.getAllPeerConfigs()) {
          for (AddressFamily af : neighbor.getAllAddressFamilies()) {
            af.setExportPolicySources(getRoutingPolicySources(af.getExportPolicy()));
            af.setImportPolicySources(getRoutingPolicySources(af.getImportPolicy()));
          }
        }
      }
      for (OspfProcess ospfProcess : vrf.getOspfProcesses().values()) {
        ospfProcess.setExportPolicySources(getRoutingPolicySources(ospfProcess.getExportPolicy()));
      }
      for (GeneratedRoute gr : vrf.getGeneratedRoutes()) {
        gr.setAttributePolicySources(getRoutingPolicySources(gr.getAttributePolicy()));
        gr.setGenerationPolicySources(getRoutingPolicySources(gr.getGenerationPolicy()));
      }
    }
  }

  /** Dictionary of all AS-path access-lists for this node. */
  @JsonProperty(PROP_AS_PATH_ACCESS_LISTS)
  public Map<String, AsPathAccessList> getAsPathAccessLists() {
    return _asPathAccessLists;
  }

  /** Dictionary of all authentication key chains for this node. */
  @JsonProperty(PROP_AUTHENTICATION_KEY_CHAINS)
  public Map<String, AuthenticationKeyChain> getAuthenticationKeyChains() {
    return _authenticationKeyChains;
  }

  @Deprecated
  public Set<String> activeInterfaceNames() {
    return activeInterfaces().map(Interface::getName).collect(ImmutableSet.toImmutableSet());
  }

  public @Nonnull Stream<Interface> activeInterfaces() {
    return _interfaces.values().stream().filter(Interface::getActive);
  }

  public @Nonnull Stream<Interface> activeL3Interfaces() {
    return _interfaces.values().stream().filter(Interface::isActiveL3);
  }

  @JsonIgnore
  public @Nonnull Map<String, AsPathExpr> getAsPathExprs() {
    return _asPathExprs;
  }

  @JsonProperty(PROP_AS_PATH_EXPRS)
  private @Nonnull NavigableMap<String, AsPathExpr> getAsPathExprsSorted() {
    return ImmutableSortedMap.copyOf(_asPathExprs, Comparator.naturalOrder());
  }

  @JsonIgnore
  public @Nonnull Map<String, AsPathMatchExpr> getAsPathMatchExprs() {
    return _asPathMatchExprs;
  }

  @JsonProperty(PROP_AS_PATH_MATCH_EXPRS)
  private @Nonnull NavigableMap<String, AsPathMatchExpr> getAsPathMatchExprsSorted() {
    return ImmutableSortedMap.copyOf(_asPathMatchExprs, Comparator.naturalOrder());
  }

  @JsonIgnore
  public @Nonnull Map<String, CommunityMatchExpr> getCommunityMatchExprs() {
    return _communityMatchExprs;
  }

  @JsonProperty(PROP_COMMUNITY_MATCH_EXPRS)
  private @Nonnull NavigableMap<String, CommunityMatchExpr> getCommunityMatchExprsSorted() {
    return ImmutableSortedMap.copyOf(_communityMatchExprs);
  }

  @JsonIgnore
  public @Nonnull Map<String, CommunitySetExpr> getCommunitySetExprs() {
    return _communitySetExprs;
  }

  @JsonProperty(PROP_COMMUNITY_SET_EXPRS)
  private @Nonnull NavigableMap<String, CommunitySetExpr> getCommunitySetExprsSorted() {
    return ImmutableSortedMap.copyOf(_communitySetExprs);
  }

  @JsonIgnore
  public @Nonnull Map<String, CommunitySetMatchExpr> getCommunitySetMatchExprs() {
    return _communitySetMatchExprs;
  }

  @JsonProperty(PROP_COMMUNITY_SET_MATCH_EXPRS)
  private @Nonnull NavigableMap<String, CommunitySetMatchExpr> getCommunitySetMatchExprsSorted() {
    return ImmutableSortedMap.copyOf(_communitySetMatchExprs);
  }

  @JsonIgnore
  public @Nonnull Map<String, CommunitySet> getCommunitySets() {
    return _communitySets;
  }

  @JsonProperty(PROP_COMMUNITY_SETS)
  private @Nonnull NavigableMap<String, CommunitySet> getCommunitySetsSorted() {
    return ImmutableSortedMap.copyOf(_communitySets);
  }

  /**
   * Best guess at vendor configuration format. Used for setting default values, protocol costs,
   * etc.
   */
  @JsonProperty(PROP_CONFIGURATION_FORMAT)
  public ConfigurationFormat getConfigurationFormat() {
    return _configurationFormat;
  }

  /** Default forwarding action to take for traffic that crosses firewall zones. */
  @JsonProperty(PROP_DEFAULT_CROSS_ZONE_ACTION)
  public LineAction getDefaultCrossZoneAction() {
    return _defaultCrossZoneAction;
  }

  /** Default forwarding action to take for traffic destined for this device. */
  @JsonProperty(PROP_DEFAULT_INBOUND_ACTION)
  public LineAction getDefaultInboundAction() {
    return _defaultInboundAction;
  }

  @JsonIgnore
  public Vrf getDefaultVrf() {
    return _vrfs.get(DEFAULT_VRF_NAME);
  }

  @JsonProperty(PROP_DEVICE_MODEL)
  public DeviceModel getDeviceModel() {
    return _deviceModel;
  }

  @JsonProperty(PROP_DEVICE_TYPE)
  public DeviceType getDeviceType() {
    return _deviceType;
  }

  public Set<String> getDnsServers() {
    return _dnsServers;
  }

  @JsonProperty(PROP_DNS_SERVERS)
  private Set<String> getDnsServersJson() {
    return ImmutableSortedSet.copyOf(_dnsServers);
  }

  @JsonProperty(PROP_DNS_SOURCE_INTERFACE)
  public String getDnsSourceInterface() {
    return _dnsSourceInterface;
  }

  /** Domain name of this node. */
  @JsonProperty(PROP_DOMAIN_NAME)
  public String getDomainName() {
    return _domainName;
  }

  /**
   * Whether the BGP export pipeline should start with main RIB routes (Juniper-like behavior) or
   * BGP RIB routes (Cisco-like behavior).
   */
  @JsonProperty(PROP_EXPORT_BGP_FROM_BGP_RIB)
  public boolean getExportBgpFromBgpRib() {
    return _exportBgpFromBgpRib;
  }

  @JsonIgnore
  public void setExportBgpFromBgpRib(boolean exportBgpFromBgpRib) {
    _exportBgpFromBgpRib = exportBgpFromBgpRib;
  }

  /**
   * Whether {@link BgpAggregate BGP aggregates} should be generated using routes in the main RIB
   * (Arista-like behavior) or in the BGP RIB (Cisco-like behavior).
   */
  @JsonProperty(PROP_GENERATE_BGP_AGGREGATES_FROM_MAIN_RIB)
  public boolean getGenerateBgpAggregatesFromMainRib() {
    return _generateBgpAggregatesFromMainRib;
  }

  @JsonIgnore
  public void setGenerateBgpAggregatesFromMainRib(boolean generateBgpAggregatesFromMainRib) {
    _generateBgpAggregatesFromMainRib = generateBgpAggregatesFromMainRib;
  }

  /** Dictionary of Reference Books generated from device configurations (e.g., F5 Pools). */
  @JsonProperty(PROP_GENERATED_REFERENCE_BOOKS)
  public Map<String, ReferenceBook> getGeneratedReferenceBooks() {
    return _generatedReferenceBooks;
  }

  @JsonProperty(PROP_GENERATED_REFERENCE_BOOKS)
  public void setGeneratedReferenceBooks(Map<String, ReferenceBook> generatedReferenceBooks) {
    _generatedReferenceBooks = generatedReferenceBooks;
  }

  /** Hostname of this node. */
  @JsonProperty(PROP_NAME)
  public String getHostname() {
    return _name;
  }

  /** A human-readable name, alternative to {@link #getHostname}, but not guaranteed unique. */
  @JsonProperty(PROP_HUMAN_NAME)
  public @Nullable String getHumanName() {
    return _humanName;
  }

  /** Dictionary of all IKE phase1 keys for this node. */
  @JsonProperty(PROP_IKE_PHASE1_KEYS)
  public Map<String, IkePhase1Key> getIkePhase1Keys() {
    return _ikePhase1keys;
  }

  /** Dictionary of all IKE phase1 policies for this node. */
  @JsonProperty(PROP_IKE_PHASE1_POLICIES)
  public Map<String, IkePhase1Policy> getIkePhase1Policies() {
    return _ikePhase1Policies;
  }

  /** Dictionary of all IKE phase1 proposals for this node. */
  @JsonProperty(PROP_IKE_PHASE1_PROPOSALS)
  public Map<String, IkePhase1Proposal> getIkePhase1Proposals() {
    return _ikePhase1Proposals;
  }

  /** Dictionary of all interfaces across all VRFs for this node. */
  @JsonProperty(PROP_INTERFACES)
  public Map<String, Interface> getAllInterfaces() {
    return _interfaces;
  }

  /**
   * Return all interfaces in a given VRF
   *
   * @param vrf the VRF name
   */
  @JsonIgnore
  public Map<String, Interface> getAllInterfaces(@Nonnull String vrf) {
    return _interfaces.entrySet().stream()
        .filter(e -> e.getValue().getVrfName().equals(vrf))
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Return all active interfaces in a given VRF
   *
   * @param vrf the VRF name
   */
  @JsonIgnore
  public Map<String, Interface> getActiveInterfaces(@Nonnull String vrf) {
    return activeInterfaces()
        .filter(i -> i.getVrfName().equals(vrf))
        .collect(ImmutableMap.toImmutableMap(Interface::getName, i -> i));
  }

  @JsonIgnore
  public Map<String, Interface> getActiveInterfaces() {
    return activeInterfaces().collect(ImmutableMap.toImmutableMap(Interface::getName, i -> i));
  }

  /** Whether administratively disconnected interfaces are always automatically line down. */
  @JsonProperty(PROP_DISCONNECT_ADMIN_DOWN_INTERFACES)
  public boolean getDisconnectAdminDownInterfaces() {
    return _disconnectAdminDownInterfaces;
  }

  /** Dictionary of all IPV4 access-lists for this node. */
  @JsonProperty(PROP_IP_ACCESS_LISTS)
  public Map<String, IpAccessList> getIpAccessLists() {
    return _ipAccessLists;
  }

  /** Dictionary of all IPSec peer configs for this node. */
  @JsonProperty(PROP_IPSEC_PEER_CONFIGS)
  public Map<String, IpsecPeerConfig> getIpsecPeerConfigs() {
    return _ipsecPeerConfigs;
  }

  public Map<String, IpSpace> getIpSpaces() {
    return _ipSpaces;
  }

  @JsonProperty(PROP_IP_SPACES)
  private Map<String, IpSpace> getIpSpacesJson() {
    return ImmutableSortedMap.copyOf(_ipSpaces);
  }

  @JsonProperty(PROP_IP_SPACE_METADATA)
  public Map<String, IpSpaceMetadata> getIpSpaceMetadata() {
    return _ipSpaceMetadata;
  }

  /** Dictionary of all IPSec phase 2 policies for this node. */
  @JsonProperty(PROP_IPSEC_PHASE2_POLICIES)
  public Map<String, IpsecPhase2Policy> getIpsecPhase2Policies() {
    return _ipsecPhase2Policies;
  }

  /** Dictionary of all IPSec phase 2 proposals for this node. */
  @JsonProperty(PROP_IPSEC_PHASE2_PROPOSALS)
  public Map<String, IpsecPhase2Proposal> getIpsecPhase2Proposals() {
    return _ipsecPhase2Proposals;
  }

  @JsonIgnore
  public @Nullable Map<Location, LocationInfo> getLocationInfo() {
    return _locationInfo;
  }

  public Set<String> getLoggingServers() {
    return _loggingServers;
  }

  @JsonProperty(PROP_LOGGING_SERVERS)
  private Set<String> getLoggingServersJson() {
    return ImmutableSortedSet.copyOf(_loggingServers);
  }

  @JsonProperty(PROP_LOGGING_SOURCE_INTERFACE)
  public String getLoggingSourceInterface() {
    return _loggingSourceInterface;
  }

  @JsonProperty(PROP_MAIN_RIB_ENFORCE_RESOLVABILITY)
  public boolean getMainRibEnforceResolvability() {
    return _mainRibEnforceResolvability;
  }

  @JsonProperty(PROP_MLAGS)
  public @Nonnull Map<String, Mlag> getMlags() {
    return _mlags;
  }

  @JsonIgnore
  public @Nonnull IntegerSpace getNormalVlanRange() {
    return _normalVlanRange;
  }

  public Set<String> getNtpServers() {
    return _ntpServers;
  }

  @JsonProperty(PROP_NTP_SERVERS)
  private Set<String> getNtpServersJson() {
    return ImmutableSortedSet.copyOf(_ntpServers);
  }

  @JsonProperty(PROP_NTP_SOURCE_INTERFACE)
  public String getNtpSourceInterface() {
    return _ntpSourceInterface;
  }

  /** Return the defined policies that can be used for policy-based routing */
  @JsonProperty(PROP_PACKET_POLICIES)
  public Map<String, PacketPolicy> getPacketPolicies() {
    return _packetPolicies;
  }

  /** Dictionary of all IPV4 route filter lists for this node. */
  @JsonProperty(PROP_ROUTE_FILTER_LISTS)
  public Map<String, RouteFilterList> getRouteFilterLists() {
    return _routeFilterLists;
  }

  /** Dictionary of all routing policies for this node. */
  @JsonProperty(PROP_ROUTING_POLICIES)
  public Map<String, RoutingPolicy> getRoutingPolicies() {
    return _routingPolicies;
  }

  private SortedSet<String> getRoutingPolicySources(@Nullable String routingPolicyName) {
    if (routingPolicyName == null) {
      return Collections.emptySortedSet();
    }
    RoutingPolicy rp = _routingPolicies.get(routingPolicyName);
    if (rp == null) {
      return Collections.emptySortedSet();
    }
    return rp.getSources().stream()
        .filter(not(RoutingPolicy::isGenerated))
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  @JsonProperty(PROP_SNMP_SOURCE_INTERFACE)
  public String getSnmpSourceInterface() {
    return _snmpSourceInterface;
  }

  public Set<String> getSnmpTrapServers() {
    return _snmpTrapServers;
  }

  @JsonProperty(PROP_SNMP_TRAP_SERVERS)
  private Set<String> getSnmpTrapServersJson() {
    return ImmutableSortedSet.copyOf(_snmpTrapServers);
  }

  public Set<String> getTacacsServers() {
    return _tacacsServers;
  }

  @JsonProperty(PROP_TACACS_SERVERS)
  private Set<String> getTacacsServersJson() {
    return ImmutableSortedSet.copyOf(_tacacsServers);
  }

  @JsonProperty(PROP_TACACS_SOURCE_INTERFACE)
  public String getTacacsSourceInterface() {
    return _tacacsSourceInterface;
  }

  /** Mapping: trackingGroupID -&gt; trackMethod */
  @JsonProperty(PROP_TRACKING_GROUPS)
  public @Nonnull Map<String, TrackMethod> getTrackingGroups() {
    return _trackingGroups;
  }

  /** Object containing vendor-specific information for this node. */
  @JsonProperty(PROP_VENDOR_FAMILY)
  public VendorFamily getVendorFamily() {
    return _vendorFamily;
  }

  /** Dictionary of all VRFs for this node. */
  @JsonProperty(PROP_VRFS)
  public Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  /** Dictionary of all firewall zones for this node. */
  @JsonProperty(PROP_ZONES)
  public Map<String, Zone> getZones() {
    return _zones;
  }

  @JsonProperty(PROP_AS_PATH_ACCESS_LISTS)
  public void setAsPathAccessLists(Map<String, AsPathAccessList> asPathAccessLists) {
    _asPathAccessLists = asPathAccessLists;
  }

  @JsonProperty(PROP_AUTHENTICATION_KEY_CHAINS)
  public void setAuthenticationKeyChains(
      Map<String, AuthenticationKeyChain> authenticationKeyChains) {
    _authenticationKeyChains = authenticationKeyChains;
  }

  @JsonProperty(PROP_AS_PATH_EXPRS)
  public void setAsPathExprs(@Nonnull Map<String, AsPathExpr> asPathExprs) {
    _asPathExprs = asPathExprs;
  }

  @JsonProperty(PROP_AS_PATH_MATCH_EXPRS)
  public void setAsPathMatchExprs(@Nonnull Map<String, AsPathMatchExpr> asPathMatchExprs) {
    _asPathMatchExprs = asPathMatchExprs;
  }

  @JsonProperty(PROP_COMMUNITY_MATCH_EXPRS)
  public void setCommunityMatchExprs(@Nonnull Map<String, CommunityMatchExpr> communityMatchExprs) {
    _communityMatchExprs = communityMatchExprs;
  }

  @JsonProperty(PROP_COMMUNITY_SET_EXPRS)
  public void setCommunitySetExprs(@Nonnull Map<String, CommunitySetExpr> communitySetExprs) {
    _communitySetExprs = communitySetExprs;
  }

  @JsonProperty(PROP_COMMUNITY_SET_MATCH_EXPRS)
  public void setCommunitySetMatchExprs(
      @Nonnull Map<String, CommunitySetMatchExpr> communitySetMatchExprs) {
    _communitySetMatchExprs = communitySetMatchExprs;
  }

  @JsonProperty(PROP_COMMUNITY_SETS)
  public void setCommunitySets(@Nonnull Map<String, CommunitySet> communitySets) {
    _communitySets = communitySets;
  }

  public void setDefaultCrossZoneAction(LineAction defaultCrossZoneAction) {
    _defaultCrossZoneAction = defaultCrossZoneAction;
  }

  public void setDefaultInboundAction(LineAction defaultInboundAction) {
    _defaultInboundAction = defaultInboundAction;
  }

  @JsonProperty(PROP_DEVICE_MODEL)
  public void setDeviceModel(DeviceModel deviceModel) {
    _deviceModel = deviceModel;
  }

  @JsonProperty(PROP_DEVICE_TYPE)
  public void setDeviceType(DeviceType deviceType) {
    _deviceType = deviceType;
  }

  @JsonProperty(PROP_DISCONNECT_ADMIN_DOWN_INTERFACES)
  public void setDisconnectAdminDownInterfaces(boolean disconnectAdminDownInterfaces) {
    _disconnectAdminDownInterfaces = disconnectAdminDownInterfaces;
  }

  @JsonProperty(PROP_DNS_SERVERS)
  public void setDnsServers(Set<String> dnsServers) {
    _dnsServers = dnsServers;
  }

  @JsonProperty(PROP_DNS_SOURCE_INTERFACE)
  public void setDnsSourceInterface(String dnsSourceInterface) {
    _dnsSourceInterface = dnsSourceInterface;
  }

  @JsonProperty(PROP_DOMAIN_NAME)
  public void setDomainName(String domainName) {
    _domainName = domainName;
  }

  @JsonProperty(PROP_HUMAN_NAME)
  public void setHumanName(@Nullable String humanName) {
    _humanName = humanName;
  }

  @JsonProperty(PROP_IKE_PHASE1_KEYS)
  public void setIkePhase1Keys(@Nullable Map<String, IkePhase1Key> ikePhase1Keys) {
    _ikePhase1keys = firstNonNull(ikePhase1Keys, ImmutableMap.of());
  }

  @JsonProperty(PROP_IKE_PHASE1_KEYS)
  public void extendIkePhase1Keys(@Nullable Map<String, IkePhase1Key> ikePhase1Keys) {
    _ikePhase1keys.putAll(firstNonNull(ikePhase1Keys, ImmutableMap.of()));
  }

  @JsonProperty(PROP_IKE_PHASE1_POLICIES)
  public void setIkePhase1Policies(Map<String, IkePhase1Policy> ikePhase1Policies) {
    _ikePhase1Policies = ikePhase1Policies;
  }

  @JsonProperty(PROP_IKE_PHASE1_POLICIES)
  public void extendIkePhase1Policies(Map<String, IkePhase1Policy> ikePhase1Policies) {
    _ikePhase1Policies.putAll(firstNonNull(ikePhase1Policies, ImmutableMap.of()));
  }

  @JsonProperty(PROP_IKE_PHASE1_PROPOSALS)
  public void setIkePhase1Proposals(Map<String, IkePhase1Proposal> ikePhase1Proposals) {
    _ikePhase1Proposals = ikePhase1Proposals;
  }

  @JsonProperty(PROP_IKE_PHASE1_PROPOSALS)
  public void extendIkePhase1Proposls(@Nullable Map<String, IkePhase1Proposal> ikePhase1Proposals) {
    _ikePhase1Proposals.putAll(firstNonNull(ikePhase1Proposals, ImmutableMap.of()));
  }

  @JsonProperty(PROP_INTERFACES)
  public void setInterfaces(Map<String, Interface> interfaces) {
    _interfaces = interfaces;
  }

  @JsonProperty(PROP_IP6_ACCESS_LISTS)
  @Deprecated
  @SuppressWarnings("unused")
  private void setIp6AccessListsDeprecatedForJson(Object unused) {}

  @JsonProperty(PROP_IP_ACCESS_LISTS)
  public void setIpAccessLists(Map<String, IpAccessList> ipAccessLists) {
    _ipAccessLists = ipAccessLists;
  }

  @JsonProperty(PROP_IPSEC_PEER_CONFIGS)
  public void setIpsecPeerConfigs(@Nullable Map<String, IpsecPeerConfig> ipsecPeerConfigs) {
    _ipsecPeerConfigs =
        ipsecPeerConfigs == null ? ImmutableMap.of() : ImmutableMap.copyOf(ipsecPeerConfigs);
  }

  @JsonProperty(PROP_IPSEC_PEER_CONFIGS)
  public void extendIpsecPeerConfigs(@Nullable Map<String, IpsecPeerConfig> ipsecPeerConfigs) {
    _ipsecPeerConfigs.putAll(firstNonNull(ipsecPeerConfigs, ImmutableMap.of()));
  }

  @JsonProperty(PROP_IPSEC_PHASE2_POLICIES)
  public void setIpsecPhase2Policies(@Nullable Map<String, IpsecPhase2Policy> ipsecPhase2Policies) {
    _ipsecPhase2Policies = firstNonNull(ipsecPhase2Policies, ImmutableMap.of());
  }

  @JsonProperty(PROP_IPSEC_PHASE2_POLICIES)
  public void extendIpsecPhase2Policies(
      @Nullable Map<String, IpsecPhase2Policy> ipsecPhase2Policies) {
    _ipsecPhase2Policies.putAll(firstNonNull(ipsecPhase2Policies, ImmutableMap.of()));
  }

  @JsonProperty(PROP_IPSEC_PHASE2_PROPOSALS)
  public void setIpsecPhase2Proposals(
      @Nullable Map<String, IpsecPhase2Proposal> ipsecPhase2Proposals) {
    _ipsecPhase2Proposals = firstNonNull(ipsecPhase2Proposals, ImmutableMap.of());
  }

  @JsonProperty(PROP_IPSEC_PHASE2_PROPOSALS)
  public void extendIpsecPhase2Proposals(
      @Nullable Map<String, IpsecPhase2Proposal> ipsecPhase2Proposals) {
    _ipsecPhase2Proposals.putAll(firstNonNull(ipsecPhase2Proposals, ImmutableMap.of()));
  }

  @JsonProperty(PROP_IP_SPACES)
  public void setIpSpaces(Map<String, IpSpace> ipSpaces) {
    _ipSpaces = ipSpaces;
  }

  @JsonProperty(PROP_IP_SPACE_METADATA)
  public void setIpSpaceMetadata(Map<String, IpSpaceMetadata> ipSpaceMetadata) {
    _ipSpaceMetadata = ipSpaceMetadata;
  }

  /**
   * Set the {@link LocationInfo} for {@link Location locations} on this node. Any missing locations
   * will have their {@link LocationInfo} created automatically. See {@link
   * org.batfish.specifier.LocationInfoUtils}.
   */
  @JsonIgnore
  public void setLocationInfo(Map<Location, LocationInfo> locationInfo) {
    _locationInfo = ImmutableMap.copyOf(locationInfo);
  }

  @JsonProperty(PROP_LOGGING_SERVERS)
  public void setLoggingServers(Set<String> loggingServers) {
    _loggingServers = loggingServers;
  }

  @JsonProperty(PROP_LOGGING_SOURCE_INTERFACE)
  public void setLoggingSourceInterface(String loggingSourceInterface) {
    _loggingSourceInterface = loggingSourceInterface;
  }

  @JsonProperty(PROP_MAIN_RIB_ENFORCE_RESOLVABILITY)
  public void setMainRibEnforceResolvability(boolean mainRibEnforceResolvability) {
    _mainRibEnforceResolvability = mainRibEnforceResolvability;
  }

  @JsonProperty(PROP_MLAGS)
  public void setMlags(Map<String, Mlag> mlags) {
    _mlags = mlags;
  }

  @JsonIgnore
  public void setNormalVlanRange(@Nonnull IntegerSpace normalVlanRange) {
    _normalVlanRange = normalVlanRange;
  }

  @JsonProperty(PROP_NTP_SERVERS)
  public void setNtpServers(Set<String> ntpServers) {
    _ntpServers = ntpServers;
  }

  @JsonProperty(PROP_NTP_SOURCE_INTERFACE)
  public void setNtpSourceInterface(String ntpSourceInterface) {
    _ntpSourceInterface = ntpSourceInterface;
  }

  @JsonProperty(PROP_ROUTE6_FILTER_LISTS)
  @Deprecated
  @SuppressWarnings("unused")
  private void setRoute6FilterListsDeprecatedForJson(Object unused) {}

  @JsonProperty(PROP_ROUTE_FILTER_LISTS)
  public void setRouteFilterLists(Map<String, RouteFilterList> routeFilterLists) {
    _routeFilterLists = routeFilterLists;
  }

  @JsonProperty(PROP_ROUTING_POLICIES)
  public void setRoutingPolicies(Map<String, RoutingPolicy> routingPolicies) {
    _routingPolicies = routingPolicies;
  }

  @JsonProperty(PROP_PACKET_POLICIES)
  public void setPacketPolicies(Map<String, PacketPolicy> packetPolicies) {
    _packetPolicies = packetPolicies;
  }

  @JsonProperty(PROP_SNMP_SOURCE_INTERFACE)
  public void setSnmpSourceInterface(String snmpSourceInterface) {
    _snmpSourceInterface = snmpSourceInterface;
  }

  @JsonProperty(PROP_SNMP_TRAP_SERVERS)
  public void setSnmpTrapServers(Set<String> snmpTrapServers) {
    _snmpTrapServers = snmpTrapServers;
  }

  @JsonProperty(PROP_TACACS_SERVERS)
  public void setTacacsServers(Set<String> tacacsServers) {
    _tacacsServers = tacacsServers;
  }

  @JsonProperty(PROP_TACACS_SOURCE_INTERFACE)
  public void setTacacsSourceInterface(String tacacsSourceInterface) {
    _tacacsSourceInterface = tacacsSourceInterface;
  }

  @JsonProperty(PROP_TRACKING_GROUPS)
  public void setTrackingGroups(@Nonnull Map<String, TrackMethod> trackingGroups) {
    _trackingGroups = trackingGroups;
  }

  @JsonProperty(PROP_VENDOR_FAMILY)
  public void setVendorFamily(VendorFamily vendorFamily) {
    _vendorFamily = vendorFamily;
  }

  @JsonProperty(PROP_VRFS)
  public void setVrfs(Map<String, Vrf> vrfs) {
    _vrfs = vrfs;
  }

  @JsonProperty(PROP_ZONES)
  public void setZones(Map<String, Zone> zones) {
    _zones = zones;
  }

  public void simplifyRoutingPolicies() {
    NavigableMap<String, RoutingPolicy> simpleRoutingPolicies =
        _routingPolicies.entrySet().stream()
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Ordering.natural(), Entry::getKey, e -> e.getValue().simplify()));
    _routingPolicies = simpleRoutingPolicies;
  }

  @Override
  public String toString() {
    return _name;
  }
}
