package org.batfish.vendor.arista.representation.eos;

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
  protected @Nullable AristaBgpAdditionalPathsConfig _additionalPaths;
  private final @Nonnull Map<String, AristaBgpNeighborAddressFamily> _peerGroups;
  private final @Nonnull Map<Ip, AristaBgpNeighborAddressFamily> _v4Neighbors;
  private final @Nonnull Map<Prefix, AristaBgpNeighborAddressFamily> _v4DynamicNeighbors;
  private final @Nonnull Map<Ip6, AristaBgpNeighborAddressFamily> _v6Neighbors;
  protected @Nullable Boolean _nextHopUnchanged;

  public AristaBgpVrfAddressFamily() {
    _peerGroups = new HashMap<>();
    _v4DynamicNeighbors = new HashMap<>();
    _v4Neighbors = new HashMap<>();
    _v6Neighbors = new HashMap<>();
  }

  public @Nullable AristaBgpAdditionalPathsConfig getAdditionalPaths() {
    return _additionalPaths;
  }

  public @Nonnull AristaBgpAdditionalPathsConfig getOrCreateAdditionalPaths() {
    if (_additionalPaths == null) {
      _additionalPaths = new AristaBgpAdditionalPathsConfig();
    }
    return _additionalPaths;
  }

  public @Nullable Boolean getNextHopUnchanged() {
    return _nextHopUnchanged;
  }

  public void setNextHopUnchanged(@Nullable Boolean nextHopUnchanged) {
    _nextHopUnchanged = nextHopUnchanged;
  }

  public @Nullable AristaBgpNeighborAddressFamily getNeighbor(Ip neighbor) {
    return _v4Neighbors.get(neighbor);
  }

  public @Nonnull AristaBgpNeighborAddressFamily getOrCreateNeighbor(Ip neighbor) {
    return _v4Neighbors.computeIfAbsent(neighbor, n -> new AristaBgpNeighborAddressFamily());
  }

  public @Nullable AristaBgpNeighborAddressFamily getNeighbor(Prefix neighbor) {
    return _v4DynamicNeighbors.get(neighbor);
  }

  public @Nonnull AristaBgpNeighborAddressFamily getOrCreateNeighbor(Prefix neighbor) {
    return _v4DynamicNeighbors.computeIfAbsent(neighbor, n -> new AristaBgpNeighborAddressFamily());
  }

  public @Nullable AristaBgpNeighborAddressFamily getNeighbor(Ip6 neighbor) {
    return _v6Neighbors.get(neighbor);
  }

  public @Nonnull AristaBgpNeighborAddressFamily getOrCreateNeighbor(Ip6 neighbor) {
    return _v6Neighbors.computeIfAbsent(neighbor, n -> new AristaBgpNeighborAddressFamily());
  }

  public @Nullable AristaBgpNeighborAddressFamily getPeerGroup(String peerGroup) {
    return _peerGroups.get(peerGroup);
  }

  public @Nonnull AristaBgpNeighborAddressFamily getOrCreatePeerGroup(String peerGroup) {
    return _peerGroups.computeIfAbsent(peerGroup, n -> new AristaBgpNeighborAddressFamily());
  }
}
