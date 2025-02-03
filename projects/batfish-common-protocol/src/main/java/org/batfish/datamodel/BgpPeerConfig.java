package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

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
  static final String PROP_CHECK_LOCAL_IP_ON_ACCEPT = "checkLocalIpOnAccept";
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

  static final String PROP_REPLACE_NON_LOCAL_ASES_ON_EXPORT = "replaceNonLocalAsesOnExport";

  private final @Nullable RibGroup _appliedRibGroup;
  private final @Nullable BgpAuthenticationSettings _authenticationSettings;

  /**
   * Whether this peer will check local IP, if configured, when accepting a connection. Not all
   * devices do.
   */
  private final boolean _checkLocalIpOnAccept;

  /** The cluster id associated with this peer to be used in route reflection */
  private final @Nullable Long _clusterId;

  private final @Nullable Long _confederationAsn;

  /** The default metric associated with routes sent to this peer */
  private final int _defaultMetric;

  protected final String _description;
  private final boolean _ebgpMultihop;
  private final boolean _enforceFirstAs;

  /**
   * The set of generated and/or aggregate routes to be potentially sent to this peer before
   * outbound policies are taken into account
   */
  private final @Nonnull Set<GeneratedRoute> _generatedRoutes;

  /**
   * The group name associated with this peer in the vendor-specific configuration from which the
   * containing configuration is derived. This field is OPTIONAL and should not impact the
   * subsequent data plane computation.
   */
  private final @Nullable String _group;

  /** The autonomous system number of the containing BGP process as reported to this peer */
  private final @Nullable Long _localAs;

  /** The ip address of the containing router as reported to this peer */
  private final @Nullable Ip _localIp;

  protected final @Nonnull LongSpace _remoteAsns;

  // Address families
  private @Nullable Ipv4UnicastAddressFamily _ipv4UnicastAddressFamily;
  private @Nullable EvpnAddressFamily _evpnAddressFamily;

  private final boolean _replaceNonLocalAsesOnExport;

  protected BgpPeerConfig(
      @Nullable RibGroup appliedRibGroup,
      @Nullable BgpAuthenticationSettings authenticationSettings,
      @Nullable Boolean checkLocalIpOnAccept,
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
      @Nullable EvpnAddressFamily evpnAddressFamily,
      boolean replaceNonLocalAsesOnExport) {
    _appliedRibGroup = appliedRibGroup;
    _authenticationSettings = authenticationSettings;
    _checkLocalIpOnAccept = firstNonNull(checkLocalIpOnAccept, true);
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
    _replaceNonLocalAsesOnExport = replaceNonLocalAsesOnExport;
  }

  /** Return the {@link RibGroup} applied to this config */
  public @Nullable RibGroup getAppliedRibGroup() {
    return _appliedRibGroup;
  }

  /** The authentication setting to be used for this neighbor */
  @JsonProperty(PROP_AUTHENTICATION_SETTINGS)
  public @Nullable BgpAuthenticationSettings getAuthenticationSettings() {
    return _authenticationSettings;
  }

  /**
   * Returns true if this device will reject an incoming BGP session where the destination IP is in
   * this VRF, but does not match the configured local IP.
   */
  @JsonProperty(PROP_CHECK_LOCAL_IP_ON_ACCEPT)
  public boolean getCheckLocalIpOnAccept() {
    return _checkLocalIpOnAccept;
  }

  /** Route-reflection cluster-id for this peer */
  @JsonProperty(PROP_CLUSTER_ID)
  public @Nullable Long getClusterId() {
    return _clusterId;
  }

  /** Confederation AS number. Only present if the peer is inside a BGP confederation */
  @JsonProperty(PROP_CONFEDERATION_AS)
  public @Nullable Long getConfederationAsn() {
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
  @JsonProperty(PROP_GENERATED_ROUTES)
  public @Nonnull Set<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  /**
   * Name of a group in the original vendor-specific configuration to which this peer is assigned
   */
  @JsonProperty(PROP_GROUP)
  public @Nullable String getGroup() {
    return _group;
  }

  /** The local autonomous system of this peering */
  @JsonProperty(PROP_LOCAL_AS)
  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  /**
   * Get the IP that this peer will originate BGP sessions from and/or listen for incoming
   * connections on.
   *
   * <p>If {@code null}, the IP address will be chosen dynamically among valid IPs in the vrf,
   * perhaps by vendor's logic (e.g., lo0) or by the dest IP of an incoming BGP connection.
   */
  @JsonProperty(PROP_LOCAL_IP)
  public @Nullable Ip getLocalIp() {
    return _localIp;
  }

  /** Space of acceptable remote AS numbers for session to be established */
  @JsonProperty(PROP_REMOTE_ASNS)
  public @Nonnull LongSpace getRemoteAsns() {
    return _remoteAsns;
  }

  /**
   * Return settings for the IPv4 unicast address family. Presence of this field indicates the peer
   * should participate in the exchange of IPv4 routes
   */
  @JsonProperty(PROP_IPV4_UNICAST_ADDRESS_FAMILY)
  public @Nullable Ipv4UnicastAddressFamily getIpv4UnicastAddressFamily() {
    return _ipv4UnicastAddressFamily;
  }

  /**
   * Return settings for the EVPN address family. Presence of this field indicates the peer should
   * participate in the exchange of EVPN routes
   */
  @JsonProperty(PROP_EVPN_ADDRESS_FAMILY)
  public @Nullable EvpnAddressFamily getEvpnAddressFamily() {
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
  public @Nullable AddressFamily getAddressFamily(AddressFamily.Type type) {
    return switch (type) {
      case IPV4_UNICAST -> _ipv4UnicastAddressFamily;
      case EVPN -> _evpnAddressFamily;
    };
  }

  /**
   * When true, replace every AS-path element with the singleton element of the local AS as the last
   * step post-export. Only applicable to eBGP sessions.
   */
  @JsonProperty(PROP_REPLACE_NON_LOCAL_ASES_ON_EXPORT)
  public boolean getReplaceNonLocalAsesOnExport() {
    return _replaceNonLocalAsesOnExport;
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
    return _checkLocalIpOnAccept == that._checkLocalIpOnAccept
        && _defaultMetric == that._defaultMetric
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
        && Objects.equals(_evpnAddressFamily, that._evpnAddressFamily)
        && _replaceNonLocalAsesOnExport == that._replaceNonLocalAsesOnExport;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _appliedRibGroup,
        _authenticationSettings,
        _checkLocalIpOnAccept,
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
        _evpnAddressFamily,
        _replaceNonLocalAsesOnExport);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_appliedRibGroup", _appliedRibGroup)
        .add("_authenticationSettings", _authenticationSettings)
        .add("_checkLocalIpOnAccept", _checkLocalIpOnAccept)
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
        .add("_replaceNonLocalAsesOnExport", _replaceNonLocalAsesOnExport)
        .toString();
  }

  public abstract static class Builder<S extends Builder<S, T>, T extends BgpPeerConfig> {
    protected @Nullable RibGroup _appliedRibGroup;
    protected @Nullable BgpAuthenticationSettings _authenticationSettings;
    protected @Nullable BgpProcess _bgpProcess;
    protected @Nullable Boolean _checkLocalIpOnAccept;
    protected @Nullable Long _clusterId;
    protected @Nullable Long _confederation;
    protected int _defaultMetric;
    protected String _description;
    protected boolean _ebgpMultihop;
    protected boolean _enforceFirstAs;
    protected @Nullable Set<GeneratedRoute> _generatedRoutes;
    protected @Nullable String _group;
    protected @Nullable Long _localAs;
    protected @Nullable Ip _localIp;
    protected @Nonnull LongSpace _remoteAsns;
    protected @Nullable Ipv4UnicastAddressFamily _ipv4UnicastAddressFamily;
    protected @Nullable EvpnAddressFamily _evpnAddressFamily;

    // Identifying fields
    protected @Nullable String _hostname;

    protected boolean _replaceNonLocalAsesOnExport;

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

    public S setCheckLocalIpOnAccept(@Nullable Boolean checkLocalIpOnAccept) {
      _checkLocalIpOnAccept = checkLocalIpOnAccept;
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

    /**
     * Set the IP that this peer will originate BGP sessions from and/or listen for incoming
     * connections on.
     *
     * <p>If {@code null}, the IP address will be chosen dynamically among valid IPs in the vrf,
     * perhaps by vendor's logic (e.g., lo0) or by the dest IP of an incoming BGP connection.
     */
    public S setLocalIp(@Nullable Ip localIp) {
      assert localIp == null || localIp.valid();
      if (localIp != null && !localIp.valid()) {
        _localIp = null;
      } else {
        _localIp = localIp;
      }
      return getThis();
    }

    /** Sets space of acceptable remote AS numbers to singleton of {@code remoteAs}. */
    public S setRemoteAs(long remoteAs) {
      checkArgument(ALL_AS_NUMBERS.contains(remoteAs), "Invalid remote-as value: %s", remoteAs);
      _remoteAsns = LongSpace.of(remoteAs);
      return getThis();
    }

    public S setRemoteAsns(LongSpace remoteAs) {
      checkArgument(ALL_AS_NUMBERS.contains(remoteAs), "Invalid remote-as space: %s", remoteAs);
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

    public S setReplaceNonLocalAsesOnExport(boolean replaceNonLocalAsesOnExport) {
      _replaceNonLocalAsesOnExport = replaceNonLocalAsesOnExport;
      return getThis();
    }
  }
}
