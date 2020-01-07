package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
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
      @JsonProperty(PROP_CONFEDERATION_AS) @Nullable Long confederation,
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
      @JsonProperty(PROP_IPV4_UNICAST_ADDRESS_FAMILY) @Nullable
          Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @JsonProperty(PROP_EVPN_ADDRESS_FAMILY) @Nullable EvpnAddressFamily evpnAddressFamily) {
    return new BgpActivePeerConfig(
        appliedRibGroup,
        authenticationSettings,
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
        peerAddress,
        firstNonNull(remoteAsns, LongSpace.EMPTY),
        ipv4UnicastAddressFamily,
        evpnAddressFamily);
  }

  private BgpActivePeerConfig(
      @Nullable RibGroup appliedRibGroup,
      @Nullable BgpAuthenticationSettings authenticationSettings,
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
      @Nullable Ip peerAddress,
      @Nullable LongSpace remoteAsns,
      Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @Nullable EvpnAddressFamily evpnAddressFamily) {
    super(
        appliedRibGroup,
        authenticationSettings,
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_peerAddress", _peerAddress)
        .add("_description", _description)
        .add("_remoteAsns", _remoteAsns)
        .add("peerconfig", super.toString())
        .toString();
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
      BgpActivePeerConfig bgpPeerConfig =
          new BgpActivePeerConfig(
              _appliedRibGroup,
              _authenticationSettings,
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
              _peerAddress,
              _remoteAsns,
              _ipv4UnicastAddressFamily,
              _evpnAddressFamily);
      if (_bgpProcess != null) {
        _bgpProcess
            .getActiveNeighbors()
            .put(Objects.requireNonNull(_peerAddress).toPrefix(), bgpPeerConfig);
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
