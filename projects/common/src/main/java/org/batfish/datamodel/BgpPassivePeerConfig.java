package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
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

  /** The prefix from which remote peers can connect. */
  private @Nullable Prefix _peerPrefix;

  @JsonCreator
  private static @Nonnull BgpPassivePeerConfig create(
      @JsonProperty(PROP_APPLIED_RIB_GROUP) @Nullable RibGroup appliedRibGroup,
      @JsonProperty(PROP_AUTHENTICATION_SETTINGS) @Nullable
          BgpAuthenticationSettings authenticationSettings,
      @JsonProperty(PROP_CHECK_LOCAL_IP_ON_ACCEPT) @Nullable Boolean checkLocalIpOnAccept,
      @JsonProperty(PROP_CLUSTER_ID) @Nullable Long clusterId,
      @JsonProperty(PROP_CONFEDERATION_AS) @Nullable Long confederation,
      @JsonProperty(PROP_DEFAULT_METRIC) int defaultMetric,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_EBGP_MULTIHOP) boolean ebgpMultihop,
      @JsonProperty(PROP_ENFORCE_FIRST_AS) boolean enforceFirstAs,
      @JsonProperty(PROP_GENERATED_ROUTES) @Nullable Set<GeneratedRoute> generatedRoutes,
      @JsonProperty(PROP_GROUP) @Nullable String group,
      @JsonProperty(PROP_LOCAL_AS) @Nullable Long localAs,
      @JsonProperty(PROP_LOCAL_IP) @Nullable Ip localIp,
      @JsonProperty(PROP_PEER_PREFIX) @Nullable Prefix peerPrefix,
      @JsonProperty(PROP_REMOTE_ASNS) @Nullable LongSpace remoteAsns,
      @JsonProperty(PROP_IPV4_UNICAST_ADDRESS_FAMILY) @Nullable
          Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @JsonProperty(PROP_EVPN_ADDRESS_FAMILY) @Nullable EvpnAddressFamily evpnAddressFamily,
      @JsonProperty(PROP_REPLACE_NON_LOCAL_ASES_ON_EXPORT) boolean replaceNonLocalAsesOnExport) {
    return new BgpPassivePeerConfig(
        appliedRibGroup,
        authenticationSettings,
        checkLocalIpOnAccept,
        clusterId,
        confederation,
        defaultMetric,
        description,
        ebgpMultihop,
        enforceFirstAs,
        generatedRoutes,
        group,
        localAs,
        localIp,
        peerPrefix,
        firstNonNull(remoteAsns, LongSpace.EMPTY),
        ipv4UnicastAddressFamily,
        evpnAddressFamily,
        replaceNonLocalAsesOnExport);
  }

  private BgpPassivePeerConfig(
      @Nullable RibGroup appliedRibGroup,
      @Nullable BgpAuthenticationSettings authenticationSettings,
      @Nullable Boolean checkLocalIpOnAccept,
      @Nullable Long clusterId,
      @Nullable Long confederation,
      int defaultMetric,
      @Nullable String description,
      boolean ebgpMultihop,
      boolean enforceFirstAs,
      @Nullable Set<GeneratedRoute> generatedRoutes,
      @Nullable String group,
      @Nullable Long localAs,
      @Nullable Ip localIp,
      @Nullable Prefix peerPrefix,
      @Nullable LongSpace remoteAsns,
      @Nullable Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @Nullable EvpnAddressFamily evpnAddressFamily,
      boolean replaceNonLocalAsesOnExport) {
    super(
        appliedRibGroup,
        authenticationSettings,
        checkLocalIpOnAccept,
        clusterId,
        confederation,
        defaultMetric,
        description,
        ebgpMultihop,
        enforceFirstAs,
        generatedRoutes,
        group,
        localAs,
        localIp,
        remoteAsns,
        ipv4UnicastAddressFamily,
        evpnAddressFamily,
        replaceNonLocalAsesOnExport);
    _peerPrefix = peerPrefix;
  }

  /** Check whether this peer's remote prefix contains the given IP. */
  public boolean hasCompatibleRemotePrefix(@Nonnull Ip address) {
    return _peerPrefix != null && _peerPrefix.containsIp(address);
  }

  /** The IPV4 prefix of the remote peer. */
  @JsonProperty(PROP_PEER_PREFIX)
  public @Nullable Prefix getPeerPrefix() {
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
    private @Nullable Prefix _peerPrefix;

    protected Builder() {
      super();
    }

    @Override
    public @Nonnull BgpPassivePeerConfig build() {
      BgpPassivePeerConfig bgpPeerConfig =
          new BgpPassivePeerConfig(
              _appliedRibGroup,
              _authenticationSettings,
              _checkLocalIpOnAccept,
              _clusterId,
              _confederation,
              _defaultMetric,
              _description,
              _ebgpMultihop,
              _enforceFirstAs,
              _generatedRoutes,
              _group,
              _localAs,
              _localIp,
              _peerPrefix,
              _remoteAsns,
              _ipv4UnicastAddressFamily,
              _evpnAddressFamily,
              _replaceNonLocalAsesOnExport);
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
