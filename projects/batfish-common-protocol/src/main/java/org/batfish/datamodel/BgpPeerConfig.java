package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.NetworkFactory.NetworkFactoryBuilder;

/** Represents a configured BGP peering, at the control plane level */
@JsonSchemaDescription("A configured e/iBGP peering relationship")
public final class BgpPeerConfig extends ComparableStructure<Prefix> {

  public static class Builder extends NetworkFactoryBuilder<BgpPeerConfig> {
    private Configuration _owner;
    private Ip _localIp;
    private Ip _peerIpAddress;
    private Long _localAs;
    private Long _remoteAs;
    private BgpProcess _bgpProcess;
    private String _exportPolicy;
    private Boolean _advertiseExternal;
    private Boolean _additionalPathSend;
    private Boolean _additionalPathSelectAll;
    private Boolean _advertiseInactive;
    private Vrf _vrf;
    private Long _clusterId;
    private Boolean _routeReflectorClient;
    private boolean _dynamic = false;

    Builder(NetworkFactory networkFactory) {
      super(networkFactory, BgpPeerConfig.class);
    }

    @Override
    public BgpPeerConfig build() {
      BgpPeerConfig bgpPeerConfig;
      Ip peerIpAddress = _peerIpAddress != null ? _peerIpAddress : new Ip(generateLong());
      bgpPeerConfig = new BgpPeerConfig(peerIpAddress, _owner, _dynamic);
      if (_clusterId != null) {
        bgpPeerConfig.setClusterId(_clusterId);
      }
      if (_localIp != null) {
        bgpPeerConfig.setLocalIp(_localIp);
      }
      if (_localAs != null) {
        bgpPeerConfig.setLocalAs(_localAs);
      }
      if (_remoteAs != null) {
        bgpPeerConfig.setRemoteAs(_remoteAs);
      }
      if (_bgpProcess != null) {
        _bgpProcess.getNeighbors().put(bgpPeerConfig.getPrefix(), bgpPeerConfig);
      }
      if (_exportPolicy != null) {
        bgpPeerConfig.setExportPolicy(_exportPolicy);
      }
      if (_advertiseInactive != null) {
        bgpPeerConfig.setAdvertiseInactive(_advertiseInactive);
      }
      if (_advertiseExternal != null) {
        bgpPeerConfig.setAdvertiseExternal(_advertiseExternal);
      }
      if (_additionalPathSend != null) {
        bgpPeerConfig.setAdditionalPathsSend(_additionalPathSend);
      }
      if (_additionalPathSelectAll != null) {
        bgpPeerConfig.setAdditionalPathsSelectAll(_additionalPathSelectAll);
      }
      if (_routeReflectorClient != null) {
        bgpPeerConfig.setRouteReflectorClient(_routeReflectorClient);
      }
      if (_vrf != null) {
        bgpPeerConfig.setVrf(_vrf.getName());
      }
      return bgpPeerConfig;
    }

    public Builder setOwner(Configuration owner) {
      _owner = owner;
      return this;
    }

    public Builder setBgpProcess(BgpProcess bgpProcess) {
      _bgpProcess = bgpProcess;
      return this;
    }

    public Builder setLocalIp(Ip localIp) {
      _localIp = localIp;
      return this;
    }

    public Builder setPeerAddress(Ip peerIpAddress) {
      _peerIpAddress = peerIpAddress;
      return this;
    }

    public Builder setLocalAs(Long localAs) {
      _localAs = localAs;
      return this;
    }

    public Builder setRemoteAs(Long remoteAs) {
      _remoteAs = remoteAs;
      return this;
    }

    public Builder setExportPolicy(String exportPolicy) {
      _exportPolicy = exportPolicy;
      return this;
    }

    public Builder setAdvertiseExternal(Boolean advertiseExternal) {
      _advertiseExternal = advertiseExternal;
      return this;
    }

    public Builder setAdditionalPathSend(Boolean additionalPathSend) {
      _additionalPathSend = additionalPathSend;
      return this;
    }

    public Builder setAdditionalPathSelectAll(Boolean additionalPathSelectAll) {
      _additionalPathSelectAll = additionalPathSelectAll;
      return this;
    }

    public Builder setAdvertiseInactive(Boolean advertiseInactive) {
      _advertiseInactive = advertiseInactive;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }

    public Builder setClusterId(Long clusterId) {
      _clusterId = clusterId;
      return this;
    }

    public Builder setRouteReflectorClient(Boolean routeReflectorClient) {
      _routeReflectorClient = routeReflectorClient;
      return this;
    }

    public Builder setDynamic(boolean dynamic) {
      _dynamic = dynamic;
      return this;
    }
  }

  private static final String PROP_ADDITIONAL_PATHS_RECEIVE = "additionalPathsReceive";

  private static final String PROP_ADDITIONAL_PATHS_SELECT_ALL = "additionalPathsSelectAll";

  private static final String PROP_ADDITIONAL_PATHS_SEND = "additionalPathsSend";

  private static final String PROP_ADDRESS = "address";

  private static final String PROP_ADVERTISE_EXTERNAL = "advertiseExternal";

  private static final String PROP_ADVERTISE_INACTIVE = "advertiseInactive";

  private static final String PROP_ALLOW_LOCAL_AS_IN = "allowLocalAsIn";

  private static final String PROP_ALLOW_REMOTE_AS_OUT = "allowRemoteAsOut";

  private static final String PROP_AUTHENTICATION_SETTINGS = "authenticationSettings";

  private static final String PROP_CLUSTER_ID = "clusterId";

  private static final String PROP_DEFAULT_METRIC = "defaultMetric";

  private static final String PROP_DESCRIPTION = "description";

  private static final String PROP_DYNAMIC = "dynamic";

  private static final String PROP_EBGP_MULTIHOP = "ebgpMultihop";

  private static final String PROP_ENFORCE_FIRST_AS = "enforceFirstAs";

  private static final String PROP_EXPORT_POLICY = "exportPolicy";

  private static final String PROP_EXPORT_POLICY_SOURCES = "exportPolicySources";

  private static final String PROP_GENERATED_ROUTES = "generatedRoutes";

  private static final String PROP_GROUP = "group";

  private static final String PROP_IMPORT_POLICY = "importPolicy";

  private static final String PROP_IMPORT_POLICY_SOURCES = "importPolicySources";

  private static final String PROP_LOCAL_AS = "localAs";

  private static final String PROP_LOCAL_IP = "localIp";

  private static final String PROP_NAME = "name";

  private static final String PROP_OWNER = "owner";

  private static final String PROP_REMOTE_AS = "remoteAs";

  private static final String PROP_REMOTE_PREFIX = "remotePrefix";

  private static final String PROP_SEND_COMMUNITY = "sendCommunity";

  private static final long serialVersionUID = 1L;

  private boolean _additionalPathsReceive;

  private boolean _additionalPathsSelectAll;

  private boolean _additionalPathsSend;

  private boolean _advertiseExternal;

  private boolean _advertiseInactive;

  private boolean _allowLocalAsIn;

  private boolean _allowRemoteAsOut;

  private BgpAuthenticationSettings _authenticationSettings;

  /** The cluster id associated with this peer to be used in route reflection */
  private Long _clusterId;

  /** The default metric associated with routes sent to this peer */
  private int _defaultMetric;

  private String _description;

  private boolean _ebgpMultihop;

  private boolean _enforceFirstAs;

  private String _exportPolicy;

  private SortedSet<String> _exportPolicySources;

  /**
   * The set of generated and/or aggregate routes to be potentially sent to this peer before
   * outbound policies are taken into account
   */
  private Set<GeneratedRoute> _generatedRoutes;

  /**
   * The group name associated with this peer in the vendor-specific configuration from which the
   * containing configuration is derived. This field is OPTIONAL and should not impact the
   * subsequent data plane computation.
   */
  private String _group;

  private String _importPolicy;

  private SortedSet<String> _importPolicySources;

  /** The autonomous system number of the containing BGP process as reported to this peer */
  private Long _localAs;

  /** The ip address of the containing router as reported to this peer */
  private Ip _localIp;

  private Configuration _owner;

  /** Whether this session is passive/dynamic (e.g., listens for incoming connections only) */
  private boolean _dynamic;

  /** The autonomous system number that the containing BGP process considers this peer to have. */
  private Long _remoteAs;

  /** Flag indicating that this neighbor is a route reflector client */
  private boolean _routeReflectorClient;

  /**
   * Flag governing whether to include community numbers in outgoing route advertisements to this
   * peer
   */
  private boolean _sendCommunity;

  private String _vrf;

  @SuppressWarnings("unused")
  private BgpPeerConfig() {
    this(null);
  }

  /**
   * Constructs a BgpPeerConfig with the given peer ip address for {@code address} and owner for
   * {@code owner}. Allows specifying whether the end of this session is passive with {@code
   * passive}
   *
   * @param address The address of this neighbor
   * @param owner The owner of this neighbor
   * @param dynamic Whether the peering is passive (a.k.a dynamic)
   */
  public BgpPeerConfig(Ip address, Configuration owner, boolean dynamic) {
    this(new Prefix(address, Prefix.MAX_PREFIX_LENGTH), owner, dynamic);
  }

  @JsonCreator
  public BgpPeerConfig(@JsonProperty(PROP_NAME) Prefix prefix) {
    super(prefix);
    _exportPolicySources = new TreeSet<>();
    _generatedRoutes = new LinkedHashSet<>();
    _importPolicySources = new TreeSet<>();
    _dynamic = false;
  }

  /**
   * Constructs a BgpPeerConfig with the given peer dynamic ip range for {@code prefix} and owner
   * for {@code owner}
   *
   * @param prefix The dynamic ip range of this neighbor
   * @param owner The owner of this neighbor
   * @param dynamic Whether the peering is passive (a.k.a dynamic)
   */
  public BgpPeerConfig(Prefix prefix, Configuration owner, boolean dynamic) {
    this(prefix);
    _owner = owner;
    _dynamic = dynamic;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpPeerConfig)) {
      return false;
    }
    BgpPeerConfig other = (BgpPeerConfig) o;
    if (this._advertiseExternal != other._advertiseExternal) {
      return false;
    }
    if (this._advertiseInactive != other._advertiseInactive) {
      return false;
    }
    if (this._allowLocalAsIn != other._allowLocalAsIn) {
      return false;
    }
    if (this._allowRemoteAsOut != other._allowRemoteAsOut) {
      return false;
    }
    if (Objects.equals(this._authenticationSettings, other._authenticationSettings)) {
      return false;
    }
    if (_clusterId == null) {
      if (other._clusterId != null) {
        return false;
      }
    } else if (!this._clusterId.equals(other._clusterId)) {
      return false;
    }
    if (this._defaultMetric != other._defaultMetric) {
      return false;
    }
    // we will skip description
    if (this._ebgpMultihop != other._ebgpMultihop) {
      return false;
    }
    if (_enforceFirstAs != other._enforceFirstAs) {
      return false;
    }
    if (!this._exportPolicy.equals(other._exportPolicy)) {
      return false;
    }
    // we will skip generated routes.
    if (!Objects.equals(this._group, other._group)) {
      return false;
    }
    if (!Objects.equals(this._importPolicy, other._importPolicy)) {
      return false;
    }
    if (!this._localAs.equals(other._localAs)) {
      return false;
    }
    if (!Objects.equals(this._localIp, other._localIp)) {
      return false;
    }
    // we will skip owner.
    return this._remoteAs.equals(other._remoteAs)
        && this._routeReflectorClient == other._routeReflectorClient
        && this._sendCommunity == other._sendCommunity
        && this._dynamic == other._dynamic;
  }

  @JsonProperty(PROP_ADDITIONAL_PATHS_RECEIVE)
  public boolean getAdditionalPathsReceive() {
    return _additionalPathsReceive;
  }

  @JsonProperty(PROP_ADDITIONAL_PATHS_SELECT_ALL)
  public boolean getAdditionalPathsSelectAll() {
    return _additionalPathsSelectAll;
  }

  @JsonProperty(PROP_ADDITIONAL_PATHS_SEND)
  public boolean getAdditionalPathsSend() {
    return _additionalPathsSend;
  }

  @Nullable
  @JsonProperty(PROP_ADDRESS)
  @JsonPropertyDescription("The IPV4 address of the remote peer if not dynamic (passive)")
  public Ip getAddress() {
    if (_key != null && _key.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
      return _key.getStartIp();
    } else {
      return null;
    }
  }

  @JsonProperty(PROP_ADVERTISE_EXTERNAL)
  @JsonPropertyDescription(
      "Whether to advertise the best eBGP route for each network independently of whether it is "
          + "the best BGP route for that network")
  public boolean getAdvertiseExternal() {
    return _advertiseExternal;
  }

  @JsonProperty(PROP_ADVERTISE_INACTIVE)
  @JsonPropertyDescription(
      "Whether to advertise the best BGP route for each network independently of whether it is "
          + "the best overall route for that network")
  public boolean getAdvertiseInactive() {
    return _advertiseInactive;
  }

  @JsonProperty(PROP_ALLOW_LOCAL_AS_IN)
  @JsonPropertyDescription(
      "Whether to allow reception of advertisements containing the local AS number in the AS-path")
  public boolean getAllowLocalAsIn() {
    return _allowLocalAsIn;
  }

  @JsonProperty(PROP_ALLOW_REMOTE_AS_OUT)
  @JsonPropertyDescription(
      "Whether to allow sending of advertisements containing the remote AS number in the AS-path")
  public boolean getAllowRemoteAsOut() {
    return _allowRemoteAsOut;
  }

  @Nullable
  @JsonProperty(PROP_AUTHENTICATION_SETTINGS)
  @JsonPropertyDescription("The authentication setting to be used for this neighbor")
  public BgpAuthenticationSettings getAuthenticationSettings() {
    return _authenticationSettings;
  }

  @JsonProperty(PROP_CLUSTER_ID)
  @JsonPropertyDescription("Route-reflection cluster-id for this peer")
  public Long getClusterId() {
    return _clusterId;
  }

  @JsonProperty(PROP_DEFAULT_METRIC)
  @JsonPropertyDescription("Default MED for routes sent to this neighbor")
  public int getDefaultMetric() {
    return _defaultMetric;
  }

  @JsonProperty(PROP_DESCRIPTION)
  @JsonPropertyDescription("Description of this peer")
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_DYNAMIC)
  @JsonPropertyDescription(
      "Whether this represents a connection to a specific peer (false) or a passive connection to "
          + "a network of peers (true)")
  public boolean getDynamic() {
    return _dynamic;
  }

  @JsonProperty(PROP_EBGP_MULTIHOP)
  @JsonPropertyDescription(
      "Whether to allow establishment of a multihop eBGP connection with this peer")
  public boolean getEbgpMultihop() {
    return _ebgpMultihop;
  }

  @JsonProperty(PROP_ENFORCE_FIRST_AS)
  @JsonPropertyDescription(
      "Whether to discard updates received from an external BGP (eBGP) peers that do not list "
          + "their autonomous system (AS) number as the first AS path segment")
  public boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  @JsonProperty(PROP_EXPORT_POLICY)
  @JsonPropertyDescription("The policy governing all advertisements sent to this peer")
  public String getExportPolicy() {
    return _exportPolicy;
  }

  @JsonProperty(PROP_EXPORT_POLICY_SOURCES)
  public SortedSet<String> getExportPolicySources() {
    return _exportPolicySources;
  }

  @JsonProperty(PROP_GENERATED_ROUTES)
  @JsonPropertyDescription(
      "Generated routes specific to this peer not otherwise imported into any of this node's RIBs")
  public Set<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  @JsonProperty(PROP_GROUP)
  @JsonPropertyDescription(
      "Name of a group in the original vendor-specific configuration to which this peer is "
          + "assigned")
  public String getGroup() {
    return _group;
  }

  @JsonProperty(PROP_IMPORT_POLICY)
  @JsonPropertyDescription("Routing policy governing all advertisements received from this peer")
  public String getImportPolicy() {
    return _importPolicy;
  }

  @JsonProperty(PROP_IMPORT_POLICY_SOURCES)
  public SortedSet<String> getImportPolicySources() {
    return _importPolicySources;
  }

  @JsonProperty(PROP_LOCAL_AS)
  @JsonPropertyDescription("The local autonomous system of this peering")
  @Nullable
  public Long getLocalAs() {
    return _localAs;
  }

  @JsonProperty(PROP_LOCAL_IP)
  @JsonPropertyDescription("The local (source) IPV4 address of this peering")
  public Ip getLocalIp() {
    return _localIp;
  }

  @JsonIgnore
  public Configuration getOwner() {
    return _owner;
  }

  @JsonProperty(PROP_REMOTE_PREFIX)
  @JsonPropertyDescription(
      "The remote (destination) IPV4 address of this peering (when not-dynamic), or the "
          + "network of peers allowed to connect on this peering (otherwise)")
  public Prefix getPrefix() {
    return _key;
  }

  @JsonProperty(PROP_REMOTE_AS)
  @JsonPropertyDescription("The remote autonomous system of this peering")
  @Nullable
  public Long getRemoteAs() {
    return _remoteAs;
  }

  @JsonPropertyDescription("Whether or not this peer is a route-reflector client")
  public boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  @JsonProperty(PROP_SEND_COMMUNITY)
  @JsonPropertyDescription(
      "Whether or not to propagate the community attribute(s) of advertisements to this peer")
  public boolean getSendCommunity() {
    return _sendCommunity;
  }

  @JsonPropertyDescription(
      "The name of the VRF containing the BGP process containing this " + "peering")
  public String getVrf() {
    return _vrf;
  }

  public void resolveReferences(Configuration owner) {
    _owner = owner;
  }

  @JsonProperty(PROP_ADDITIONAL_PATHS_RECEIVE)
  public void setAdditionalPathsReceive(boolean additionalPathsReceive) {
    _additionalPathsReceive = additionalPathsReceive;
  }

  @JsonProperty(PROP_ADDITIONAL_PATHS_SELECT_ALL)
  public void setAdditionalPathsSelectAll(boolean additionalPathsSelectAll) {
    _additionalPathsSelectAll = additionalPathsSelectAll;
  }

  @JsonProperty(PROP_ADDITIONAL_PATHS_SEND)
  public void setAdditionalPathsSend(boolean additionalPathsSend) {
    _additionalPathsSend = additionalPathsSend;
  }

  @JsonProperty(PROP_ADDRESS)
  public void setAddress(Ip address) {
    // Intentionally empty
  }

  @JsonProperty(PROP_ADVERTISE_EXTERNAL)
  public void setAdvertiseExternal(boolean advertiseExternal) {
    _advertiseExternal = advertiseExternal;
  }

  @JsonProperty(PROP_ADVERTISE_INACTIVE)
  public void setAdvertiseInactive(boolean advertiseInactive) {
    _advertiseInactive = advertiseInactive;
  }

  @JsonProperty(PROP_ALLOW_LOCAL_AS_IN)
  public void setAllowLocalAsIn(boolean allowLocalAsIn) {
    _allowLocalAsIn = allowLocalAsIn;
  }

  @JsonProperty(PROP_ALLOW_REMOTE_AS_OUT)
  public void setAllowRemoteAsOut(boolean allowRemoteAsOut) {
    _allowRemoteAsOut = allowRemoteAsOut;
  }

  @JsonProperty(PROP_AUTHENTICATION_SETTINGS)
  public void setAuthenticationSettings(BgpAuthenticationSettings authenticationSettings) {
    _authenticationSettings = authenticationSettings;
  }

  @JsonProperty(PROP_CLUSTER_ID)
  public void setClusterId(Long clusterId) {
    _clusterId = clusterId;
  }

  @JsonProperty(PROP_DEFAULT_METRIC)
  public void setDefaultMetric(int defaultMetric) {
    _defaultMetric = defaultMetric;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(PROP_DYNAMIC)
  public void setDynamic(boolean dynamic) {
    _dynamic = dynamic;
  }

  @JsonProperty(PROP_EBGP_MULTIHOP)
  public void setEbgpMultihop(boolean ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  @JsonProperty(PROP_ENFORCE_FIRST_AS)
  public void setEnforceFirstAs(boolean enforceFistAs) {
    _enforceFirstAs = enforceFistAs;
  }

  @JsonProperty(PROP_EXPORT_POLICY)
  public void setExportPolicy(String originationPolicyName) {
    _exportPolicy = originationPolicyName;
  }

  @JsonProperty(PROP_EXPORT_POLICY_SOURCES)
  public void setExportPolicySources(SortedSet<String> exportPolicySources) {
    _exportPolicySources = exportPolicySources;
  }

  @JsonProperty(PROP_GENERATED_ROUTES)
  public void setGeneratedRoutes(Set<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = generatedRoutes;
  }

  @JsonProperty(PROP_GROUP)
  public void setGroup(String name) {
    _group = name;
  }

  @JsonProperty(PROP_IMPORT_POLICY)
  public void setImportPolicy(String importPolicy) {
    _importPolicy = importPolicy;
  }

  @JsonProperty(PROP_IMPORT_POLICY_SOURCES)
  public void setImportPolicySources(SortedSet<String> importPolicySources) {
    _importPolicySources = importPolicySources;
  }

  @JsonProperty(PROP_LOCAL_AS)
  public void setLocalAs(Long localAs) {
    _localAs = localAs;
  }

  @JsonProperty(PROP_LOCAL_IP)
  public void setLocalIp(Ip localIp) {
    _localIp = localIp;
  }

  @JsonProperty(PROP_OWNER)
  public void setOwner(Configuration owner) {
    _owner = owner;
  }

  @JsonProperty(PROP_REMOTE_AS)
  public void setRemoteAs(Long remoteAs) {
    _remoteAs = remoteAs;
  }

  @JsonProperty(PROP_REMOTE_PREFIX)
  public void setRemotePrefix(Prefix remotePrefix) {
    // Intentionally empty
  }

  public void setRouteReflectorClient(boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  @JsonProperty(PROP_SEND_COMMUNITY)
  public void setSendCommunity(boolean sendCommunity) {
    _sendCommunity = sendCommunity;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }

  @Override
  public String toString() {
    return "BgpPeerConfig<Prefix:" + _key + ", AS:" + _remoteAs + ">";
  }
}
