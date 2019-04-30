package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.dataplane.rib.RibGroup;

/**
 * Represents a passive BGP config, which allows peering with multiple remote peers, but cannot
 * initiate connections.
 */
public final class BgpPassivePeerConfig extends BgpPeerConfig {
  private static final String PROP_PEER_PREFIX = "peerPrefix";

  static final long serialVersionUID = 1L;

  /** The prefix from which remote peers can connect. */
  @Nullable private Prefix _peerPrefix;

  @JsonCreator
  private static @Nonnull BgpPassivePeerConfig create(
      @JsonProperty(PROP_ADDITIONAL_PATHS_RECEIVE) boolean additionalPathsReceive,
      @JsonProperty(PROP_ADDITIONAL_PATHS_SELECT_ALL) boolean additionalPathsSelectAll,
      @JsonProperty(PROP_ADDITIONAL_PATHS_SEND) boolean additionalPathsSend,
      @JsonProperty(PROP_ADVERTISE_EXTERNAL) boolean advertiseExternal,
      @JsonProperty(PROP_ADVERTISE_INACTIVE) boolean advertiseInactive,
      @JsonProperty(PROP_ALLOW_LOCAL_AS_IN) boolean allowLocalAsIn,
      @JsonProperty(PROP_ALLOW_REMOTE_AS_OUT) boolean allowRemoteAsOut,
      @JsonProperty(PROP_APPLIED_RIB_GROUP) @Nullable RibGroup appliedRibGroup,
      @JsonProperty(PROP_AUTHENTICATION_SETTINGS) @Nullable
          BgpAuthenticationSettings authenticationSettings,
      @JsonProperty(PROP_CLUSTER_ID) @Nullable Long clusterId,
      @JsonProperty(PROP_DEFAULT_METRIC) int defaultMetric,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_EBGP_MULTIHOP) boolean ebgpMultihop,
      @JsonProperty(PROP_ENFORCE_FIRST_AS) boolean enforceFirstAs,
      @JsonProperty(PROP_EXPORT_POLICY) @Nullable String exportPolicy,
      @JsonProperty(PROP_EXPORT_POLICY_SOURCES) @Nullable SortedSet<String> exportPolicySources,
      @JsonProperty(PROP_GENERATED_ROUTES) @Nullable Set<GeneratedRoute> generatedRoutes,
      @JsonProperty(PROP_GROUP) @Nullable String group,
      @JsonProperty(PROP_IMPORT_POLICY) @Nullable String importPolicy,
      @JsonProperty(PROP_IMPORT_POLICY_SOURCES) @Nullable SortedSet<String> importPolicySources,
      @JsonProperty(PROP_LOCAL_AS) @Nullable Long localAs,
      @JsonProperty(PROP_LOCAL_IP) @Nullable Ip localIp,
      @JsonProperty(PROP_PEER_PREFIX) @Nullable Prefix peerPrefix,
      @JsonProperty(PROP_REMOTE_ASNS) @Nullable LongSpace remoteAsns,
      @JsonProperty(PROP_ROUTE_REFLECTOR) boolean routeReflectorClient,
      @JsonProperty(PROP_SEND_COMMUNITY) boolean sendCommunity,
      @JsonProperty(PROP_IPV4_UNICAST_ADDRESS_FAMILY) @Nullable
          Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @JsonProperty(PROP_EVPN_ADDRESS_FAMILY) @Nullable EvpnAddressFamily evpnAddressFamily) {
    return new BgpPassivePeerConfig(
        additionalPathsReceive,
        additionalPathsSelectAll,
        additionalPathsSend,
        advertiseExternal,
        advertiseInactive,
        allowLocalAsIn,
        allowRemoteAsOut,
        appliedRibGroup,
        authenticationSettings,
        clusterId,
        defaultMetric,
        description,
        ebgpMultihop,
        enforceFirstAs,
        exportPolicy,
        exportPolicySources,
        generatedRoutes,
        group,
        importPolicy,
        importPolicySources,
        localAs,
        localIp,
        peerPrefix,
        firstNonNull(remoteAsns, LongSpace.EMPTY),
        routeReflectorClient,
        sendCommunity,
        ipv4UnicastAddressFamily,
        evpnAddressFamily);
  }

  private BgpPassivePeerConfig(
      boolean additionalPathsReceive,
      boolean additionalPathsSelectAll,
      boolean additionalPathsSend,
      boolean advertiseExternal,
      boolean advertiseInactive,
      boolean allowLocalAsIn,
      boolean allowRemoteAsOut,
      @Nullable RibGroup appliedRibGroup,
      @Nullable BgpAuthenticationSettings authenticationSettings,
      @Nullable Long clusterId,
      int defaultMetric,
      @Nullable String description,
      boolean ebgpMultihop,
      boolean enforceFirstAs,
      @Nullable String exportPolicy,
      @Nullable SortedSet<String> exportPolicySources,
      @Nullable Set<GeneratedRoute> generatedRoutes,
      @Nullable String group,
      @Nullable String importPolicy,
      @Nullable SortedSet<String> importPolicySources,
      @Nullable Long localAs,
      @Nullable Ip localIp,
      @Nullable Prefix peerPrefix,
      @Nullable LongSpace remoteAsns,
      boolean routeReflectorClient,
      boolean sendCommunity,
      @Nullable Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @Nullable EvpnAddressFamily evpnAddressFamily) {
    super(
        additionalPathsReceive,
        additionalPathsSelectAll,
        additionalPathsSend,
        advertiseExternal,
        advertiseInactive,
        allowLocalAsIn,
        allowRemoteAsOut,
        appliedRibGroup,
        authenticationSettings,
        clusterId,
        defaultMetric,
        description,
        ebgpMultihop,
        enforceFirstAs,
        exportPolicy,
        exportPolicySources,
        generatedRoutes,
        group,
        importPolicy,
        importPolicySources,
        localAs,
        localIp,
        remoteAsns,
        routeReflectorClient,
        sendCommunity,
        ipv4UnicastAddressFamily,
        evpnAddressFamily);
    _peerPrefix = peerPrefix;
  }

  /** Check whether this peer's remote prefix contains the given IP. */
  public boolean hasCompatibleRemotePrefix(@Nonnull Ip address) {
    return _peerPrefix != null && _peerPrefix.containsIp(address);
  }

  /** The IPV4 prefix of the remote peer. */
  @Nullable
  @JsonProperty(PROP_PEER_PREFIX)
  public Prefix getPeerPrefix() {
    return _peerPrefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpPassivePeerConfig)) {
      return false;
    }
    BgpPassivePeerConfig that = (BgpPassivePeerConfig) o;
    return Objects.equals(_peerPrefix, that._peerPrefix) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), _peerPrefix);
  }

  public static class Builder extends BgpPeerConfig.Builder<Builder, BgpPassivePeerConfig> {
    @Nullable private Prefix _peerPrefix;

    protected Builder() {
      super();
    }

    @Override
    @Nonnull
    public BgpPassivePeerConfig build() {
      BgpPassivePeerConfig bgpPeerConfig =
          new BgpPassivePeerConfig(
              _additionalPathsReceive,
              _additionalPathsSelectAll,
              _additionalPathsSend,
              _advertiseExternal,
              _advertiseInactive,
              _allowLocalAsIn,
              _allowRemoteAsOut,
              _appliedRibGroup,
              _authenticationSettings,
              _clusterId,
              _defaultMetric,
              _description,
              _ebgpMultihop,
              _enforceFirstAs,
              _exportPolicy,
              _exportPolicySources,
              _generatedRoutes,
              _group,
              _importPolicy,
              _importPolicySources,
              _localAs,
              _localIp,
              _peerPrefix,
              _remoteAsns,
              _routeReflectorClient,
              _sendCommunity,
              _ipv4UnicastAddressFamily,
              _evpnAddressFamily);
      if (_bgpProcess != null) {
        _bgpProcess.getPassiveNeighbors().put(_peerPrefix, bgpPeerConfig);
      }
      return bgpPeerConfig;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setPeerPrefix(@Nullable Prefix peerPrefix) {
      _peerPrefix = peerPrefix;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
