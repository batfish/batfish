package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.dataplane.rib.RibGroup;

/**
 * Represents a BGP unnumbered config, which allows peering over a layer-3-capable interface without
 * IP configuration.
 */
@JsonIgnoreProperties(BgpPeerConfig.PROP_CHECK_LOCAL_IP_ON_ACCEPT) // not relevant
public final class BgpUnnumberedPeerConfig extends BgpPeerConfig {

  public static class Builder extends BgpPeerConfig.Builder<Builder, BgpUnnumberedPeerConfig> {
    private @Nullable String _peerInterface;

    protected Builder() {
      super();
    }

    @Override
    public @Nonnull BgpUnnumberedPeerConfig build() {
      checkArgument(_peerInterface != null, "Missing %s", PROP_PEER_INTERFACE);
      checkArgument(_checkLocalIpOnAccept == null, "Unsupported %s", PROP_CHECK_LOCAL_IP_ON_ACCEPT);
      BgpUnnumberedPeerConfig bgpPeerConfig =
          new BgpUnnumberedPeerConfig(
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
              _peerInterface,
              _remoteAsns,
              _ipv4UnicastAddressFamily,
              _evpnAddressFamily,
              _replaceNonLocalAsesOnExport);
      if (_bgpProcess != null) {
        _bgpProcess.getInterfaceNeighbors().put(_peerInterface, bgpPeerConfig);
      }
      return bgpPeerConfig;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setPeerInterface(String peerInterface) {
      _peerInterface = peerInterface;
      return this;
    }
  }

  private static final String PROP_PEER_INTERFACE = "peerInterface";

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull BgpUnnumberedPeerConfig create(
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
      @JsonProperty(PROP_PEER_INTERFACE) @Nullable String peerInterface,
      @JsonProperty(PROP_REMOTE_ASNS) @Nullable LongSpace remoteAsns,
      @JsonProperty(PROP_IPV4_UNICAST_ADDRESS_FAMILY) @Nullable
          Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @JsonProperty(PROP_EVPN_ADDRESS_FAMILY) @Nullable EvpnAddressFamily evpnAddressFamily,
      @JsonProperty(PROP_REPLACE_NON_LOCAL_ASES_ON_EXPORT) boolean replaceNonLocalAsesOnExport) {
    checkArgument(peerInterface != null, "Missing %s", PROP_PEER_INTERFACE);
    return new BgpUnnumberedPeerConfig(
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
        peerInterface,
        firstNonNull(remoteAsns, LongSpace.EMPTY),
        ipv4UnicastAddressFamily,
        evpnAddressFamily,
        replaceNonLocalAsesOnExport);
  }

  private final @Nonnull String _peerInterface;

  private BgpUnnumberedPeerConfig(
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
      @Nonnull String peerInterface,
      @Nullable LongSpace remoteAsns,
      @Nullable Ipv4UnicastAddressFamily ipv4UnicastAddressFamily,
      @Nullable EvpnAddressFamily evpnAddressFamily,
      boolean replaceNonLocalAsesOnExport) {
    super(
        appliedRibGroup,
        authenticationSettings,
        false,
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
    _peerInterface = peerInterface;
  }

  /** Returns the interface on which a peering may occur. */
  @JsonProperty(PROP_PEER_INTERFACE)
  public @Nonnull String getPeerInterface() {
    return _peerInterface;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpUnnumberedPeerConfig)) {
      return false;
    }
    BgpUnnumberedPeerConfig that = (BgpUnnumberedPeerConfig) o;
    return _peerInterface.equals(that._peerInterface) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), _peerInterface);
  }
}
