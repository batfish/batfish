package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

/**
 * Represents a peering with a single router (by ip address) acting as a bgp peer to the router
 * whose configuration's BGP process contains this object
 */
@JsonSchemaDescription("A configured e/iBGP peering relationship")
public final class BgpNeighbor extends ComparableStructure<Prefix> {

  public static final class BgpNeighborSummary extends ComparableStructure<String> {

    private static final String PROP_DESCRIPTION = "description";

    private static final String PROP_GROUP = "group";

    private static final String PROP_LOCAL_AS = "localAs";

    private static final String PROP_LOCAL_IP = "localIp";

    private static final String PROP_REMOTE_AS = "remoteAs";

    private static final String PROP_REMOTE_IP = "remoteIp";

    private static final String PROP_REMOTE_PREFIX = "dynamicRemotePrefix";

    /** */
    private static final long serialVersionUID = 1L;

    private static final String PROP_VRF = "vrf";

    private final String _description;

    private final String _group;

    private final int _localAs;

    private final Ip _localIp;

    private final int _remoteAs;

    private final Ip _remoteIp;

    private final Prefix _remotePrefix;

    private final String _vrf;

    public BgpNeighborSummary(BgpNeighbor bgpNeighbor) {
      super(
          bgpNeighbor.getOwner().getName()
              + ":"
              + (bgpNeighbor.getDynamic()
                  ? bgpNeighbor.getPrefix().toString()
                  : bgpNeighbor.getAddress().toString()));
      _description = bgpNeighbor._description;
      _group = bgpNeighbor._group;
      _localAs = bgpNeighbor._localAs;
      _localIp = bgpNeighbor._localIp;
      _remoteAs = bgpNeighbor._remoteAs;
      _remoteIp = bgpNeighbor.getAddress();
      _remotePrefix = bgpNeighbor._key;
      _vrf = bgpNeighbor._vrf;
    }

    @JsonCreator
    public BgpNeighborSummary(
        @JsonProperty(PROP_NAME) String name,
        @JsonProperty(PROP_DESCRIPTION) String description,
        @JsonProperty(PROP_GROUP) String group,
        @JsonProperty(PROP_LOCAL_AS) int localAs,
        @JsonProperty(PROP_LOCAL_IP) Ip localIp,
        @JsonProperty(PROP_REMOTE_AS) int remoteAs,
        @JsonProperty(PROP_REMOTE_IP) Ip remoteIp,
        @JsonProperty(PROP_REMOTE_PREFIX) Prefix remotePrefix,
        @JsonProperty(PROP_VRF) String vrf) {
      super(name);
      _description = description;
      _group = group;
      _localAs = localAs;
      _localIp = localIp;
      _remoteAs = remoteAs;
      _remoteIp = remoteIp;
      _remotePrefix = remotePrefix;
      _vrf = vrf;
    }

    @JsonProperty(PROP_DESCRIPTION)
    public String getDescription() {
      return _description;
    }

    @JsonProperty(PROP_GROUP)
    public String getGroup() {
      return _group;
    }

    @JsonProperty(PROP_LOCAL_AS)
    public int getLocalAs() {
      return _localAs;
    }

    @JsonProperty(PROP_LOCAL_IP)
    public Ip getLocalIp() {
      return _localIp;
    }

    @JsonIgnore
    public Prefix getPrefix() {
      if (_remotePrefix == null) {
        return new Prefix(_remoteIp, 32);
      } else {
        return _remotePrefix;
      }
    }

    @JsonProperty(PROP_REMOTE_AS)
    public int getRemoteAs() {
      return _remoteAs;
    }

    @JsonProperty(PROP_REMOTE_IP)
    public Ip getRemoteIp() {
      return _remoteIp;
    }

    @JsonProperty(PROP_REMOTE_PREFIX)
    public Prefix getRemotePrefix() {
      return _remotePrefix;
    }

    @JsonProperty(PROP_VRF)
    public String getVrf() {
      return _vrf;
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

  private static final String PROP_CLUSTER_ID = "clusterId";

  private static final String PROP_DEFAULT_METRIC = "defaultMetric";

  private static final String PROP_DESCRIPTION = "description";

  private static final String PROP_DYNAMIC = "dynamic";

  private static final String PROP_EBGP_MULTIHOP = "ebgpMultihop";

  private static final String PROP_GENERATED_ROUTES = "generatedRoutes";

  private static final String PROP_GROUP = "group";

  private static final String PROP_LOCAL_AS = "localAs";

  private static final String PROP_LOCAL_IP = "localIp";

  private static final String PROP_NAME = "name";

  private static final String PROP_OWNER = "owner";

  private static final String PROP_REMOTE_AS = "remoteAs";

  private static final String PROP_REMOTE_PREFIX = "remotePrefix";

  private static final String PROP_SEND_COMMUNITY = "sendCommunity";

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _additionalPathsReceive;

  private boolean _additionalPathsSelectAll;

  private boolean _additionalPathsSend;

  private boolean _advertiseExternal;

  private boolean _advertiseInactive;

  private boolean _allowLocalAsIn;

  private boolean _allowRemoteAsOut;

  private transient Set<BgpNeighbor> _candidateRemoteBgpNeighbors;

  /** The cluster id associated with this peer to be used in route reflection */
  private Long _clusterId;

  /** The default metric associated with routes sent to this peer */
  private int _defaultMetric;

  private String _description;

  private boolean _ebgpMultihop;

  private String _exportPolicy;

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

  /** The autonomous system number of the containing BGP process as reported to this peer */
  private Integer _localAs;

  /** The ip address of the containing router as reported to this peer */
  private Ip _localIp;

  private Configuration _owner;

  /** The autonomous system number that the containing BGP process considers this peer to have. */
  private Integer _remoteAs;

  private transient BgpNeighbor _remoteBgpNeighbor;

  private boolean _routeReflectorClient;

  /**
   * Flag governing whether to include community numbers in outgoing route advertisements to this
   * peer
   */
  private boolean _sendCommunity;

  private String _vrf;

  @SuppressWarnings("unused")
  private BgpNeighbor() {
    this(null);
  }

  /**
   * Constructs a BgpNeighbor with the given peer ip address for {@link #_address} and owner for
   * {@link #_owner}
   *
   * @param address The address of this neighbor
   * @param owner The owner of this neighbor
   */
  public BgpNeighbor(Ip address, Configuration owner) {
    this(new Prefix(address, 32), owner);
  }

  @JsonCreator
  public BgpNeighbor(@JsonProperty(PROP_NAME) Prefix prefix) {
    super(prefix);
    _generatedRoutes = new LinkedHashSet<>();
  }

  /**
   * Constructs a BgpNeighbor with the given peer dynamic ip range for {@link #_network} and owner
   * for {@link #_owner}
   *
   * @param prefix The dynamic ip range of this neighbor
   * @param owner The owner of this neighbor
   */
  public BgpNeighbor(Prefix prefix, Configuration owner) {
    this(prefix);
    _owner = owner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    BgpNeighbor other = (BgpNeighbor) o;
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
    if (!this._remoteAs.equals(other._remoteAs)) {
      return false;
    }
    if (this._routeReflectorClient != other._routeReflectorClient) {
      return false;
    }
    if (this._sendCommunity != other._sendCommunity) {
      return false;
    }
    return true;
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
    if (_key != null && _key.getPrefixLength() == 32) {
      return _key.getAddress();
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

  @JsonIgnore
  public Set<BgpNeighbor> getCandidateRemoteBgpNeighbors() {
    return _candidateRemoteBgpNeighbors;
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
    return _key != null && _key.getPrefixLength() < 32;
  }

  @JsonProperty(PROP_EBGP_MULTIHOP)
  @JsonPropertyDescription(
      "Whether to allow establishment of a multihop eBGP connection with this peer")
  public boolean getEbgpMultihop() {
    return _ebgpMultihop;
  }

  @JsonPropertyDescription("The policy governing all advertisements sent to this peer")
  public String getExportPolicy() {
    return _exportPolicy;
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

  @JsonPropertyDescription("Routing policy governing all advertisements received from this peer")
  public String getImportPolicy() {
    return _importPolicy;
  }

  @JsonProperty(PROP_LOCAL_AS)
  @JsonPropertyDescription("The local autonomous sysem of this peering")
  public Integer getLocalAs() {
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
      "The remote (destination) IPV4 address of this peering (when prefix-length is 32), or the "
          + "network of peers allowed to connect on this peering (otherwise)")
  public Prefix getPrefix() {
    return _key;
  }

  @JsonProperty(PROP_REMOTE_AS)
  @JsonPropertyDescription("The remote autonomous sysem of this peering")
  public Integer getRemoteAs() {
    return _remoteAs;
  }

  @JsonIgnore
  public BgpNeighbor getRemoteBgpNeighbor() {
    return _remoteBgpNeighbor;
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

  public void initCandidateRemoteBgpNeighbors() {
    _candidateRemoteBgpNeighbors = new LinkedHashSet<>();
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
    // Intentionally empty
  }

  @JsonProperty(PROP_EBGP_MULTIHOP)
  public void setEbgpMultihop(boolean ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  public void setExportPolicy(String originationPolicyName) {
    _exportPolicy = originationPolicyName;
  }

  @JsonProperty(PROP_GENERATED_ROUTES)
  public void setGeneratedRoutes(Set<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = generatedRoutes;
  }

  @JsonProperty(PROP_GROUP)
  public void setGroup(String name) {
    _group = name;
  }

  public void setImportPolicy(String importPolicy) {
    _importPolicy = importPolicy;
  }

  @JsonProperty(PROP_LOCAL_AS)
  public void setLocalAs(Integer localAs) {
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
  public void setRemoteAs(Integer remoteAs) {
    _remoteAs = remoteAs;
  }

  public void setRemoteBgpNeighbor(BgpNeighbor remoteBgpNeighbor) {
    _remoteBgpNeighbor = remoteBgpNeighbor;
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
    return "BgpNeighbor<Prefix:" + _key + ", AS:" + _remoteAs + ">";
  }
}
