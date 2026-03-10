package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Global BGP configuration for Arista */
public final class AristaBgpProcess implements Serializable {
  public static final String DEFAULT_VRF = "default";
  private final long _asn;
  private @Nonnull Map<String, AristaBgpPeerGroupNeighbor> _peerGroups;
  private final @Nonnull Map<String, AristaBgpVlanAwareBundle> _vlanAwareBundles;
  private final @Nonnull Map<Integer, AristaBgpVlan> _vlans;
  private final @Nonnull Map<String, AristaBgpVrf> _vrfs;

  public AristaBgpProcess(long asn) {
    _asn = asn;
    _peerGroups = new HashMap<>(0);
    _vlanAwareBundles = new HashMap<>(0);
    _vlans = new HashMap<>(0);
    _vrfs = new HashMap<>(1);
    // Create the default VRF automatically.
    _vrfs.put(DEFAULT_VRF, new AristaBgpVrf(DEFAULT_VRF));
  }

  public long getAsn() {
    return _asn;
  }

  public @Nonnull Map<String, AristaBgpPeerGroupNeighbor> getPeerGroups() {
    return _peerGroups;
  }

  public @Nonnull AristaBgpPeerGroupNeighbor getOrCreatePeerGroup(String name) {
    return _peerGroups.computeIfAbsent(name, AristaBgpPeerGroupNeighbor::new);
  }

  public @Nullable AristaBgpPeerGroupNeighbor deletePeerGroup(String name) {
    return _peerGroups.remove(name);
  }

  public @Nullable AristaBgpPeerGroupNeighbor getPeerGroup(String name) {
    return _peerGroups.get(name);
  }

  public @Nonnull Map<String, AristaBgpVlanAwareBundle> getVlanAwareBundles() {
    return _vlanAwareBundles;
  }

  public @Nonnull Map<Integer, AristaBgpVlan> getVlans() {
    return _vlans;
  }

  public @Nonnull Map<String, AristaBgpVrf> getVrfs() {
    return _vrfs;
  }

  public @Nonnull AristaBgpVrf getDefaultVrf() {
    assert _vrfs.containsKey(DEFAULT_VRF); // populated in constructor
    return _vrfs.get(DEFAULT_VRF);
  }
}
