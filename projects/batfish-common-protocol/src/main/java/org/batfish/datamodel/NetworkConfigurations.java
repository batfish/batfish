package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ospf.OspfNeighborConfig;
import org.batfish.datamodel.ospf.OspfNeighborConfigId;
import org.batfish.datamodel.ospf.OspfProcess;

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
  public Collection<Configuration> all() {
    return _configurations.values();
  }

  @Nonnull
  public Optional<Configuration> get(String hostname) {
    return Optional.ofNullable(_configurations.get(hostname));
  }

  /**
   * Returns a {@link BgpPeerConfig} matching the given {@code id} if one exists, otherwise {@code
   * null}.
   */
  @Nullable
  public BgpPeerConfig getBgpPeerConfig(BgpPeerConfigId id) {
    switch (id.getType()) {
      case ACTIVE:
        return getBgpPointToPointPeerConfig(id);
      case DYNAMIC:
        return getBgpDynamicPeerConfig(id);
      case UNNUMBERED:
        return getBgpUnnumberedPeerConfig(id);
      default:
        throw new IllegalArgumentException(String.format("Unrecognized peer type: %s", id));
    }
  }

  /**
   * Returns a {@link BgpPassivePeerConfig} matching the given {@code id} if one exists, otherwise
   * {@code null}.
   */
  @Nullable
  public BgpPassivePeerConfig getBgpDynamicPeerConfig(BgpPeerConfigId id) {
    if (id.getRemotePeerPrefix() == null) {
      return null;
    }
    return getVrf(id.getHostname(), id.getVrfName())
        .map(Vrf::getBgpProcess)
        .map(BgpProcess::getPassiveNeighbors)
        .map(m -> m.get(id.getRemotePeerPrefix()))
        .orElse(null);
  }

  /**
   * Returns a {@link BgpActivePeerConfig} matching the given {@code id} if one exists, otherwise
   * {@code null}.
   */
  @Nullable
  public BgpActivePeerConfig getBgpPointToPointPeerConfig(BgpPeerConfigId id) {
    if (id.getRemotePeerPrefix() == null) {
      return null;
    }
    return getVrf(id.getHostname(), id.getVrfName())
        .map(Vrf::getBgpProcess)
        .map(BgpProcess::getActiveNeighbors)
        .map(m -> m.get(id.getRemotePeerPrefix()))
        .orElse(null);
  }

  /**
   * Returns a {@link BgpUnnumberedPeerConfig} matching the given {@code id} if one exists,
   * otherwise {@code null}.
   */
  @Nullable
  public BgpUnnumberedPeerConfig getBgpUnnumberedPeerConfig(BgpPeerConfigId id) {
    if (id.getPeerInterface() == null) {
      return null;
    }
    return getVrf(id.getHostname(), id.getVrfName())
        .map(Vrf::getBgpProcess)
        .map(proc -> proc.getInterfaceNeighbors().get(id.getPeerInterface()))
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

  /** Return the {@link Mlag} configuration identified by a given ID. */
  @Nonnull
  public Optional<Mlag> getMlagConfig(String hostname, String id) {
    return get(hostname).map(Configuration::getMlags).map(m -> m.get(id));
  }

  public Optional<OspfNeighborConfig> getOspfNeighborConfig(OspfNeighborConfigId ospfConfigId) {
    return getVrf(ospfConfigId.getHostname(), ospfConfigId.getVrfName())
        .map(vrf -> vrf.getOspfProcesses().get(ospfConfigId.getProcName()))
        .map(OspfProcess::getOspfNeighborConfigs)
        .map(oc -> oc.get(ospfConfigId.getInterfaceName()));
  }

  /** Return {@link VniSettings} identificated by {@code hostname} and {@code vni} number. */
  @Nonnull
  public Optional<VniSettings> getVniSettings(String hostname, int vni) {
    // implementation assumes a given VNI can be present in at most one VRF.
    return get(hostname)
        .map(Configuration::getVrfs)
        .map(
            vrfs ->
                vrfs.values().stream()
                    .map(Vrf::getVniSettings)
                    .map(vniSettingsMap -> vniSettingsMap.get(vni))
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null));
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
