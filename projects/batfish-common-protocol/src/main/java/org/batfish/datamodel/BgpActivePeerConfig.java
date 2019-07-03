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

/** Represent a BGP config which allows peering with a single remote peer. */
public final class BgpActivePeerConfig extends BgpPeerConfig {
  private static final String PROP_PEER_ADDRESS = "peerAddress";

  /** The remote peer's IP address */
  @Nullable private final Ip _peerAddress;

  @JsonCreator
  private static @Nonnull BgpActivePeerConfig create(
      @JsonProperty(PROP_APPLIED_RIB_GROUP) @Nullable RibGroup appliedRibGroup,
      @JsonProperty(PROP_AUTHENTICATION_SETTINGS) @Nullable
          BgpAuthenticationSettings authenticationSettings,
      @JsonProperty(PROP_CLUSTER_ID) @Nullable Long clusterId,
      @JsonProperty(PROP_DEFAULT_METRIC) int defaultMetric,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_EBGP_MULTIHOP) boolean ebgpMultihop,
      @JsonProperty(PROP_ENFORCE_FIRST_AS) boolean enforceFirstAs,
      @JsonProperty(PROP_GENERATED_ROUTES) @Nullable Set<GeneratedRoute> generatedRoutes,
      @JsonProperty(PROP_GROUP) @Nullable String group,
      @JsonProperty(PROP_LOCAL_AS) @Nullable Long localAs,
      @JsonProperty(PROP_LOCAL_IP) @Nullable Ip localIp,
      @JsonProperty(PROP_PEER_ADDRESS) @Nullable Ip peerAddress,
      @JsonProperty(PROP_REMOTE_ASNS) @Nullable LongSpace remoteAsns,
      @JsonProperty(PROP_ROUTE_REFLECTOR) boolean routeReflectorClient,
      @JsonProperty(PROP_IPV4_UNICAST_ADDRESS_FAMILY) @Nullable
          Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @JsonProperty(PROP_EVPN_ADDRESS_FAMILY) @Nullable EvpnAddressFamily evpnAddressFamily) {
    return new BgpActivePeerConfig(
        appliedRibGroup,
        authenticationSettings,
        clusterId,
        defaultMetric,
        description,
        ebgpMultihop,
        enforceFirstAs,
        generatedRoutes,
        group,
        localAs,
        localIp,
        peerAddress,
        firstNonNull(remoteAsns, LongSpace.EMPTY),
        routeReflectorClient,
        ipv4UnicastAddressFamily,
        evpnAddressFamily);
  }

  private BgpActivePeerConfig(
      @Nullable RibGroup appliedRibGroup,
      @Nullable BgpAuthenticationSettings authenticationSettings,
      @Nullable Long clusterId,
      int defaultMetric,
      @Nullable String description,
      boolean ebgpMultihop,
      boolean enforceFirstAs,
      @Nullable Set<GeneratedRoute> generatedRoutes,
      @Nullable String group,
      @Nullable Long localAs,
      @Nullable Ip localIp,
      @Nullable Ip peerAddress,
      @Nullable LongSpace remoteAsns,
      boolean routeReflectorClient,
      Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @Nullable EvpnAddressFamily evpnAddressFamily) {
    super(
        appliedRibGroup,
        authenticationSettings,
        clusterId,
        defaultMetric,
        description,
        ebgpMultihop,
        enforceFirstAs,
        generatedRoutes,
        group,
        localAs,
        localIp,
        remoteAsns,
        routeReflectorClient,
        ipv4UnicastAddressFamily,
        evpnAddressFamily);
    _peerAddress = peerAddress;
  }

  /** The IPV4 address of the remote peer. */
  @Nullable
  @JsonProperty(PROP_PEER_ADDRESS)
  public Ip getPeerAddress() {
    return _peerAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpActivePeerConfig)) {
      return false;
    }
    BgpActivePeerConfig that = (BgpActivePeerConfig) o;
    return Objects.equals(_peerAddress, that._peerAddress) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), _peerAddress);
  }

  public static class Builder extends BgpPeerConfig.Builder<Builder, BgpActivePeerConfig> {
    @Nullable private Ip _peerAddress;

    protected Builder() {
      super();
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    @Nonnull
    public BgpActivePeerConfig build() {
      checkValid();
      BgpActivePeerConfig bgpPeerConfig =
          new BgpActivePeerConfig(
              _appliedRibGroup,
              _authenticationSettings,
              _clusterId,
              _defaultMetric,
              _description,
              _ebgpMultihop,
              _enforceFirstAs,
              _generatedRoutes,
              _group,
              _localAs,
              _localIp,
              _peerAddress,
              _remoteAsns,
              _routeReflectorClient,
              _ipv4UnicastAddressFamily,
              _evpnAddressFamily);
      if (_bgpProcess != null) {
        _bgpProcess
            .getActiveNeighbors()
            .put(
                Prefix.create(Objects.requireNonNull(_peerAddress), Prefix.MAX_PREFIX_LENGTH),
                bgpPeerConfig);
      }
      return bgpPeerConfig;
    }

    public Builder setPeerAddress(@Nullable Ip peerAddress) {
      _peerAddress = peerAddress;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
