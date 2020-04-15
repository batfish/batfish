package org.batfish.representation.arista.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Global BGP configuration for Arista */
public final class AristaBgpProcess implements Serializable {
  public static final String DEFAULT_VRF = "default";
  private final long _asn;
  @Nonnull private Map<String, AristaBgpPeerGroupNeighbor> _peerGroups;
  @Nonnull private final Map<String, AristaBgpVlanAwareBundle> _vlanAwareBundles;
  @Nonnull private final Map<Integer, AristaBgpVlan> _vlans;
  @Nonnull private final Map<String, AristaBgpVrf> _vrfs;

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

  @Nonnull
  public Map<String, AristaBgpPeerGroupNeighbor> getPeerGroups() {
    return _peerGroups;
  }

  @Nonnull
  public AristaBgpPeerGroupNeighbor getOrCreatePeerGroup(String name) {
    return _peerGroups.computeIfAbsent(name, AristaBgpPeerGroupNeighbor::new);
  }

  @Nullable
  public AristaBgpPeerGroupNeighbor getPeerGroup(String name) {
    return _peerGroups.get(name);
  }

  @Nonnull
  public Map<String, AristaBgpVlanAwareBundle> getVlanAwareBundles() {
    return _vlanAwareBundles;
  }

  @Nonnull
  public Map<Integer, AristaBgpVlan> getVlans() {
    return _vlans;
  }

  @Nonnull
  public Map<String, AristaBgpVrf> getVrfs() {
    return _vrfs;
  }

  @Nonnull
  public AristaBgpVrf getDefaultVrf() {
    assert _vrfs.containsKey(DEFAULT_VRF); // populated in constructor
    return _vrfs.get(DEFAULT_VRF);
  }
}
