package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.dataplane.rib.RibGroup;

/** Represents a configured BGP peering, at the control plane level */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class BgpPeerConfig implements Serializable {
  /** A range expressing entire range of valid AS numbers */
  public static final LongSpace ALL_AS_NUMBERS = LongSpace.of(Range.closed(1L, 0xFFFFFFFFL));

  static final String PROP_APPLIED_RIB_GROUP = "appliedRibGroup";
  static final String PROP_AUTHENTICATION_SETTINGS = "authenticationSettings";
  static final String PROP_CLUSTER_ID = "clusterId";
  static final String PROP_CONFEDERATION_AS = "confederationAs";
  static final String PROP_DEFAULT_METRIC = "defaultMetric";
  static final String PROP_DESCRIPTION = "description";
  static final String PROP_EBGP_MULTIHOP = "ebgpMultihop";
  static final String PROP_ENFORCE_FIRST_AS = "enforceFirstAs";
  static final String PROP_GENERATED_ROUTES = "generatedRoutes";
  static final String PROP_GROUP = "group";
  static final String PROP_LOCAL_AS = "localAs";
  static final String PROP_LOCAL_IP = "localIp";
  static final String PROP_REMOTE_ASNS = "remoteAsns";
  static final String PROP_IPV4_UNICAST_ADDRESS_FAMILY = "ipv4UnicastAddressFamily";
  static final String PROP_EVPN_ADDRESS_FAMILY = "evpnAddressFamily";

  @Nullable private final RibGroup _appliedRibGroup;
  @Nullable private final BgpAuthenticationSettings _authenticationSettings;
  /** The cluster id associated with this peer to be used in route reflection */
  @Nullable private final Long _clusterId;

  @Nullable private final Long _confederationAsn;
  /** The default metric associated with routes sent to this peer */
  private final int _defaultMetric;

  protected final String _description;
  private final boolean _ebgpMultihop;
  private final boolean _enforceFirstAs;
  /**
   * The set of generated and/or aggregate routes to be potentially sent to this peer before
   * outbound policies are taken into account
   */
  @Nonnull private final Set<GeneratedRoute> _generatedRoutes;
  /**
   * The group name associated with this peer in the vendor-specific configuration from which the
   * containing configuration is derived. This field is OPTIONAL and should not impact the
   * subsequent data plane computation.
   */
  @Nullable private final String _group;

  /** The autonomous system number of the containing BGP process as reported to this peer */
  @Nullable private final Long _localAs;
  /** The ip address of the containing router as reported to this peer */
  @Nullable private final Ip _localIp;

  @Nonnull protected final LongSpace _remoteAsns;

  // Address families
  @Nullable private Ipv4UnicastAddressFamily _ipv4UnicastAddressFamily;
  @Nullable private EvpnAddressFamily _evpnAddressFamily;

  protected BgpPeerConfig(
      @Nullable RibGroup appliedRibGroup,
      @Nullable BgpAuthenticationSettings authenticationSettings,
      @Nullable Long clusterId,
      @Nullable Long confederationAsn,
      int defaultMetric,
      @Nullable String description,
      boolean ebgpMultihop,
      boolean enforceFirstAs,
      @Nullable Set<GeneratedRoute> generatedRoutes,
      @Nullable String group,
      @Nullable Long localAs,
      @Nullable Ip localIp,
      @Nullable LongSpace remoteAsns,
      @Nullable Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @Nullable EvpnAddressFamily evpnAddressFamily) {
    _appliedRibGroup = appliedRibGroup;
    _authenticationSettings = authenticationSettings;
    _clusterId = clusterId;
    _confederationAsn = confederationAsn;
    _defaultMetric = defaultMetric;
    _description = description;
    _ebgpMultihop = ebgpMultihop;
    _enforceFirstAs = enforceFirstAs;
    _generatedRoutes = firstNonNull(generatedRoutes, ImmutableSet.of());
    _group = group;
    _localAs = localAs;
    _localIp = localIp;
    _remoteAsns = firstNonNull(remoteAsns, ALL_AS_NUMBERS);
    _ipv4UnicastAddressFamily = ipv4UnicastAddressFamily;
    _evpnAddressFamily = evpnAddressFamily;
  }

  /** Return the {@link RibGroup} applied to this config */
  @Nullable
  public RibGroup getAppliedRibGroup() {
    return _appliedRibGroup;
  }

  /** The authentication setting to be used for this neighbor */
  @Nullable
  @JsonProperty(PROP_AUTHENTICATION_SETTINGS)
  public BgpAuthenticationSettings getAuthenticationSettings() {
    return _authenticationSettings;
  }

  /** Route-reflection cluster-id for this peer */
  @JsonProperty(PROP_CLUSTER_ID)
  @Nullable
  public Long getClusterId() {
    return _clusterId;
  }

  /** Confederation AS number. Only present if the peer is inside a BGP confederation */
  @Nullable
  @JsonProperty(PROP_CONFEDERATION_AS)
  public Long getConfederationAsn() {
    return _confederationAsn;
  }

  /** Default MED for routes sent to this neighbor */
  @JsonProperty(PROP_DEFAULT_METRIC)
  public int getDefaultMetric() {
    return _defaultMetric;
  }

  /** Description of this peer */
  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  /** Whether to allow establishment of a multihop eBGP connection with this peer */
  @JsonProperty(PROP_EBGP_MULTIHOP)
  public boolean getEbgpMultihop() {
    return _ebgpMultihop;
  }

  /**
   * Whether to discard updates received from an external BGP (eBGP) peers that do not list their
   * autonomous system (AS) number as the first AS path segment
   */
  @JsonProperty(PROP_ENFORCE_FIRST_AS)
  public boolean getEnforceFirstAs() {
    return _enforceFirstAs;
  }

  /** Generated routes specific to this peer not otherwise imported into any of this node's RIBs */
  @Nonnull
  @JsonProperty(PROP_GENERATED_ROUTES)
  public Set<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  /**
   * Name of a group in the original vendor-specific configuration to which this peer is assigned
   */
  @Nullable
  @JsonProperty(PROP_GROUP)
  public String getGroup() {
    return _group;
  }

  /** The local autonomous system of this peering */
  @Nullable
  @JsonProperty(PROP_LOCAL_AS)
  public Long getLocalAs() {
    return _localAs;
  }

  /** The local (source) IPV4 address of this peering */
  @JsonProperty(PROP_LOCAL_IP)
  @Nullable
  public Ip getLocalIp() {
    return _localIp;
  }

  /** Space of acceptable remote AS numbers for session to be established */
  @JsonProperty(PROP_REMOTE_ASNS)
  @Nonnull
  public LongSpace getRemoteAsns() {
    return _remoteAsns;
  }

  /**
   * Return settings for the IPv4 unicast address family. Presence of this field indicates the peer
   * should participate in the exchange of IPv4 routes
   */
  @JsonProperty(PROP_IPV4_UNICAST_ADDRESS_FAMILY)
  @Nullable
  public Ipv4UnicastAddressFamily getIpv4UnicastAddressFamily() {
    return _ipv4UnicastAddressFamily;
  }

  /**
   * Return settings for the EVPN address family. Presence of this field indicates the peer should
   * participate in the exchange of EVPN routes
   */
  @JsonProperty(PROP_EVPN_ADDRESS_FAMILY)
  @Nullable
  public EvpnAddressFamily getEvpnAddressFamily() {
    return _evpnAddressFamily;
  }

  /** Return a collection of all non-null {@link AddressFamily} configs at this peer */
  @JsonIgnore
  public Collection<AddressFamily> getAllAddressFamilies() {
    HashSet<AddressFamily> collection = new HashSet<>();
    if (getIpv4UnicastAddressFamily() != null) {
      collection.add(getIpv4UnicastAddressFamily());
    }
    if (getEvpnAddressFamily() != null) {
      collection.add(getEvpnAddressFamily());
    }
    return ImmutableSet.copyOf(collection);
  }

  /**
   * Return an {@link AddressFamily} for a given address family type.
   *
   * @throws IllegalArgumentException if the family type is unrecognized.
   */
  @JsonIgnore
  @Nullable
  public AddressFamily getAddressFamily(AddressFamily.Type type) {
    switch (type) {
      case IPV4_UNICAST:
        return _ipv4UnicastAddressFamily;
      case EVPN:
        return _evpnAddressFamily;
      default:
        throw new IllegalArgumentException(String.format("Unknown address family type: %s", type));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BgpPeerConfig that = (BgpPeerConfig) o;
    return _defaultMetric == that._defaultMetric
        && _ebgpMultihop == that._ebgpMultihop
        && _enforceFirstAs == that._enforceFirstAs
        && Objects.equals(_appliedRibGroup, that._appliedRibGroup)
        && Objects.equals(_authenticationSettings, that._authenticationSettings)
        && Objects.equals(_clusterId, that._clusterId)
        && Objects.equals(_confederationAsn, that._confederationAsn)
        && Objects.equals(_description, that._description)
        && Objects.equals(_generatedRoutes, that._generatedRoutes)
        && Objects.equals(_group, that._group)
        && Objects.equals(_localAs, that._localAs)
        && Objects.equals(_localIp, that._localIp)
        && _remoteAsns.equals(that._remoteAsns)
        && Objects.equals(_ipv4UnicastAddressFamily, that._ipv4UnicastAddressFamily)
        && Objects.equals(_evpnAddressFamily, that._evpnAddressFamily);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _appliedRibGroup,
        _authenticationSettings,
        _clusterId,
        _confederationAsn,
        _defaultMetric,
        _description,
        _ebgpMultihop,
        _enforceFirstAs,
        _generatedRoutes,
        _group,
        _localAs,
        _localIp,
        _remoteAsns,
        _ipv4UnicastAddressFamily,
        _evpnAddressFamily);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_appliedRibGroup", _appliedRibGroup)
        .add("_authenticationSettings", _authenticationSettings)
        .add("_clusterId", _clusterId)
        .add("_confederationAsn", _confederationAsn)
        .add("_defaultMetric", _defaultMetric)
        .add("_description", _description)
        .add("_ebgpMultihop", _ebgpMultihop)
        .add("_enforceFirstAs", _enforceFirstAs)
        .add("_generatedRoutes", _generatedRoutes)
        .add("_group", _group)
        .add("_localAs", _localAs)
        .add("_localIp", _localIp)
        .add("_remoteAsns", _remoteAsns)
        .add("_ipv4UnicastAddressFamily", _ipv4UnicastAddressFamily)
        .add("_evpnAddressFamily", _evpnAddressFamily)
        .toString();
  }

  public abstract static class Builder<S extends Builder<S, T>, T extends BgpPeerConfig> {
    @Nullable protected RibGroup _appliedRibGroup;
    @Nullable protected BgpAuthenticationSettings _authenticationSettings;
    @Nullable protected BgpProcess _bgpProcess;
    @Nullable protected Long _clusterId;
    @Nullable protected Long _confederation;
    protected int _defaultMetric;
    protected String _description;
    protected boolean _ebgpMultihop;
    protected boolean _enforceFirstAs;
    protected String _exportPolicy;
    @Nullable protected Set<GeneratedRoute> _generatedRoutes;
    @Nullable protected String _group;
    @Nullable protected Long _localAs;
    @Nullable protected Ip _localIp;
    @Nonnull protected LongSpace _remoteAsns;
    @Nullable protected Ipv4UnicastAddressFamily _ipv4UnicastAddressFamily;
    @Nullable protected EvpnAddressFamily _evpnAddressFamily;

    // Identifying fields
    @Nullable protected String _hostname;

    protected Builder() {
      _remoteAsns = LongSpace.EMPTY;
    }

    public abstract T build();

    protected abstract S getThis();

    public S setAppliedRibGroup(@Nullable RibGroup ribGroup) {
      _appliedRibGroup = ribGroup;
      return getThis();
    }

    public S setAuthenticationSettings(@Nullable BgpAuthenticationSettings authenticationSettings) {
      _authenticationSettings = authenticationSettings;
      return getThis();
    }

    /**
     * If specified, upon calling {@link #build()}, the neighbor will be added to the appropriate
     * list of neighbors in the given {@link BgpProcess}.
     */
    public S setBgpProcess(@Nonnull BgpProcess bgpProcess) {
      _bgpProcess = bgpProcess;
      return getThis();
    }

    public S setClusterId(@Nullable Long clusterId) {
      _clusterId = clusterId;
      return getThis();
    }

    public S setConfederation(@Nullable Long confederation) {
      _confederation = confederation;
      return getThis();
    }

    public S setDefaultMetric(int defaultMetric) {
      _defaultMetric = defaultMetric;
      return getThis();
    }

    public S setDescription(String description) {
      _description = description;
      return getThis();
    }

    public S setEbgpMultihop(boolean ebgpMultihop) {
      _ebgpMultihop = ebgpMultihop;
      return getThis();
    }

    public S setEnforceFirstAs(boolean enforceFirstAs) {
      _enforceFirstAs = enforceFirstAs;
      return getThis();
    }

    public S setGeneratedRoutes(@Nullable Set<GeneratedRoute> generatedRoutes) {
      _generatedRoutes = generatedRoutes;
      return getThis();
    }

    public S setGroup(@Nullable String group) {
      _group = group;
      return getThis();
    }

    public S setLocalAs(@Nullable Long localAs) {
      _localAs = localAs;
      return getThis();
    }

    public S setLocalIp(@Nullable Ip localIp) {
      _localIp = localIp;
      return getThis();
    }

    /**
     * Sets space of acceptable remote AS numbers to singleton of {@code remoteAs} if non-null, or
     * else {@link BgpPeerConfig#ALL_AS_NUMBERS}.
     */
    public S setRemoteAs(@Nullable Long remoteAs) {
      _remoteAsns = remoteAs != null ? LongSpace.of(remoteAs) : ALL_AS_NUMBERS;
      return getThis();
    }

    public S setRemoteAsns(LongSpace remoteAs) {
      _remoteAsns = remoteAs;
      return getThis();
    }

    public S setIpv4UnicastAddressFamily(
        @Nullable Ipv4UnicastAddressFamily ipv4UnicastAddressFamily) {
      _ipv4UnicastAddressFamily = ipv4UnicastAddressFamily;
      return getThis();
    }

    public S setEvpnAddressFamily(@Nullable EvpnAddressFamily evpnAddressFamily) {
      _evpnAddressFamily = evpnAddressFamily;
      return getThis();
    }
  }
}
