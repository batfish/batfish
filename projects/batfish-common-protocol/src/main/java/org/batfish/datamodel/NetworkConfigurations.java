package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a set of configurations in a network. Has helper methods to "walk the configuration
 * tree" and extract specific objects based on their keys.
 */
@ParametersAreNonnullByDefault
public final class NetworkConfigurations {
  @Nonnull private final Map<String, Configuration> _configurations;

  /** Wrap a configurations map */
  private NetworkConfigurations(Map<String, Configuration> configurations) {
    _configurations = ImmutableMap.copyOf(requireNonNull(configurations));
  }

  @Nonnull
  public Map<String, Configuration> getMap() {
    return _configurations;
  }

  @Nonnull
  public Optional<Configuration> get(String hostname) {
    return Optional.ofNullable(_configurations.get(hostname));
  }

  @Nullable
  public BgpPeerConfig getBgpPeerConfig(BgpPeerConfigId id) {
    BgpPeerConfig c = getBgpPointToPointPeerConfig(id);
    return c == null ? getBgpDynamicPeerConfig(id) : c;
  }

  @Nullable
  public BgpPassivePeerConfig getBgpDynamicPeerConfig(BgpPeerConfigId id) {
    return getVrf(id.getHostname(), id.getVrfName())
        .map(Vrf::getBgpProcess)
        .map(BgpProcess::getPassiveNeighbors)
        .map(m -> m.get(id.getRemotePeerPrefix()))
        .orElse(null);
  }

  @Nullable
  public BgpActivePeerConfig getBgpPointToPointPeerConfig(BgpPeerConfigId id) {
    return getVrf(id.getHostname(), id.getVrfName())
        .map(Vrf::getBgpProcess)
        .map(BgpProcess::getActiveNeighbors)
        .map(m -> m.get(id.getRemotePeerPrefix()))
        .orElse(null);
  }

  /** Return an interface identified by hostname and interface name */
  @Nonnull
  public Optional<Interface> getInterface(String hostname, String interfaceName) {
    return get(hostname).map(Configuration::getAllInterfaces).map(m -> m.get(interfaceName));
  }

  @Nullable
  public IpsecPeerConfig getIpsecPeerConfig(IpsecPeerConfigId ipsecPeerConfigId) {
    return get(ipsecPeerConfigId.getHostName())
        .map(Configuration::getIpsecPeerConfigs)
        .map(m -> m.get(ipsecPeerConfigId.getIpsecPeerConfigName()))
        .orElse(null);
  }

  /** Return a VRF identified by hostname and VRF name */
  public Optional<Vrf> getVrf(String hostname, String vrfName) {
    return get(hostname).map(Configuration::getVrfs).map(m -> m.get(vrfName));
  }

  /** Wrap a configurations map */
  public static NetworkConfigurations of(Map<String, Configuration> configurations) {
    return new NetworkConfigurations(configurations);
  }
}
