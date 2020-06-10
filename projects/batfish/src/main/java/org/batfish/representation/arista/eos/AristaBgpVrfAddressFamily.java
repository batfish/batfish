package org.batfish.representation.arista.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;

/**
 * Address family settings that are common to all address families and can be set at the VRF level
 */
public abstract class AristaBgpVrfAddressFamily implements Serializable {
  @Nullable protected AristaBgpAdditionalPathsConfig _additionalPaths;
  @Nonnull private final Map<String, AristaBgpNeighborAddressFamily> _peerGroups;
  @Nonnull private final Map<Ip, AristaBgpNeighborAddressFamily> _v4Neighbors;
  @Nonnull private final Map<Prefix, AristaBgpNeighborAddressFamily> _v4DynamicNeighbors;
  @Nonnull private final Map<Ip6, AristaBgpNeighborAddressFamily> _v6Neighbors;
  @Nullable protected Boolean _nextHopUnchanged;

  public AristaBgpVrfAddressFamily() {
    _peerGroups = new HashMap<>();
    _v4DynamicNeighbors = new HashMap<>();
    _v4Neighbors = new HashMap<>();
    _v6Neighbors = new HashMap<>();
  }

  @Nullable
  public AristaBgpAdditionalPathsConfig getAdditionalPaths() {
    return _additionalPaths;
  }

  @Nonnull
  public AristaBgpAdditionalPathsConfig getOrCreateAdditionalPaths() {
    if (_additionalPaths == null) {
      _additionalPaths = new AristaBgpAdditionalPathsConfig();
    }
    return _additionalPaths;
  }

  @Nullable
  public Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  @Nullable
  public AristaBgpNeighborAddressFamily getNeighbor(Ip neighbor) {
    return _v4Neighbors.get(neighbor);
  }

  @Nonnull
  public AristaBgpNeighborAddressFamily getOrCreateNeighbor(Ip neighbor) {
    return _v4Neighbors.computeIfAbsent(neighbor, n -> new AristaBgpNeighborAddressFamily());
  }

  @Nullable
  public AristaBgpNeighborAddressFamily getNeighbor(Prefix neighbor) {
    return _v4DynamicNeighbors.get(neighbor);
  }

  @Nonnull
  public AristaBgpNeighborAddressFamily getOrCreateNeighbor(Prefix neighbor) {
    return _v4DynamicNeighbors.computeIfAbsent(neighbor, n -> new AristaBgpNeighborAddressFamily());
  }

  @Nullable
  public AristaBgpNeighborAddressFamily getNeighbor(Ip6 neighbor) {
    return _v6Neighbors.get(neighbor);
  }

  @Nonnull
  public AristaBgpNeighborAddressFamily getOrCreateNeighbor(Ip6 neighbor) {
    return _v6Neighbors.computeIfAbsent(neighbor, n -> new AristaBgpNeighborAddressFamily());
  }

  @Nullable
  public AristaBgpNeighborAddressFamily getPeerGroup(String peerGroup) {
    return _peerGroups.get(peerGroup);
  }

  @Nonnull
  public AristaBgpNeighborAddressFamily getOrCreatePeerGroup(String peerGroup) {
    return _peerGroups.computeIfAbsent(peerGroup, n -> new AristaBgpNeighborAddressFamily());
  }
}
