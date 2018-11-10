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
    BgpProcess p =
        _configurations.get(id.getHostname()).getVrfs().get(id.getVrfName()).getBgpProcess();
    if (p == null) {
      return null;
    }
    return p.getPassiveNeighbors().get(id.getRemotePeerPrefix());
  }

  @Nullable
  public BgpActivePeerConfig getBgpPointToPointPeerConfig(BgpPeerConfigId id) {
    BgpProcess p =
        _configurations.get(id.getHostname()).getVrfs().get(id.getVrfName()).getBgpProcess();
    if (p == null) {
      return null;
    }
    return p.getActiveNeighbors().get(id.getRemotePeerPrefix());
  }

  /** Return an interface identified by hostname and interface name */
  @Nonnull
  public Optional<Interface> getInterface(String hostname, String interfaceName) {
    return get(hostname).map(Configuration::getAllInterfaces).map(m -> m.get(interfaceName));
  }

  @Nullable
  public IpsecPeerConfig getIpsecPeerConfig(IpsecPeerConfigId ipsecPeerConfigId) {
    Configuration c = get(ipsecPeerConfigId.getHostName()).orElse(null);
    return c == null
        ? null
        : c.getIpsecPeerconfigs().get(ipsecPeerConfigId.getIpsecPeerConfigName());
  }

  /** Return a VRF identified by hostname and VRF name */
  public Optional<Vrf> getVrf(String hostname, String vrfName) {
    Configuration c = _configurations.get(hostname);
    if (c == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(c.getVrfs().get(vrfName));
  }

  /** Wrap a configurations map */
  public static NetworkConfigurations of(@Nonnull Map<String, Configuration> configurations) {
    return new NetworkConfigurations(configurations);
  }
}
