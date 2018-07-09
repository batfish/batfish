package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a set of configurations in a network. Has helper methods to "walk the configuration
 * tree" and extract specific objects based on their keys.
 */
public final class NetworkConfigurations {
  @Nonnull private final Map<String, Configuration> _configurations;

  /** Wrap a configurations map */
  private NetworkConfigurations(@Nonnull Map<String, Configuration> configurations) {
    _configurations = requireNonNull(configurations);
  }

  @Nonnull
  public Map<String, Configuration> getMap() {
    return _configurations;
  }

  @Nullable
  public Configuration get(@Nonnull String hostname) {
    return _configurations.get(hostname);
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

  @Nullable
  public Interface getInterface(@Nonnull String hostname, @Nonnull String interfaceName) {
    Configuration c = get(hostname);
    if (c == null) {
      return null;
    }
    return c.getInterfaces().get(interfaceName);
  }

  @Nullable
  public Interface getInterface(
      @Nonnull String hostname, @Nonnull String vrfName, @Nonnull String interfaceName) {
    Configuration c = get(hostname);
    if (c == null) {
      return null;
    }
    Vrf v = c.getVrfs().get(vrfName);
    if (v == null) {
      return null;
    }
    return v.getInterfaces().get(interfaceName);
  }

  @Nullable
  public Vrf getVrf(@Nonnull String hostname, @Nonnull String vrfName) {
    Configuration c = _configurations.get(hostname);
    if (c == null) {
      return null;
    }
    return c.getVrfs().get(vrfName);
  }

  /** Wrap a configurations map */
  public static NetworkConfigurations of(@Nonnull Map<String, Configuration> configurations) {
    return new NetworkConfigurations(configurations);
  }
}
