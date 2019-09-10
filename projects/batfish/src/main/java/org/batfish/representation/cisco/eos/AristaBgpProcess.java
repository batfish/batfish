package org.batfish.representation.cisco.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Global BGP configuration for Arista */
public final class AristaBgpProcess implements Serializable {
  public static final String DEFAULT_VRF = "default";
  private final long _asn;
  @Nonnull private final Map<String, AristaBgpVlanAwareBundle> _vlanAwareBundles;
  @Nonnull private final Map<Integer, AristaBgpVlan> _vlans;
  @Nonnull private final Map<String, AristaBgpVrf> _vrfs;

  public AristaBgpProcess(long asn) {
    _asn = asn;
    _vlanAwareBundles = new HashMap<>(0);
    _vlans = new HashMap<>(0);
    _vrfs = new HashMap<>(0);
  }

  public long getAsn() {
    return _asn;
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

  @Nullable
  public AristaBgpVrf getDefaultVrf() {
    return _vrfs.get(DEFAULT_VRF);
  }
}
