package org.batfish.vendor.nec_ix.representation;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** The {@code router bgp} process. */
public final class NecIxBgpProcess implements Serializable {

  /** Default of {@code default-local-preference}. */
  public static final long DEFAULT_LOCAL_PREFERENCE = 100L;

  private final long _asn;
  private @Nullable Ip _routerId;
  private long _defaultLocalPreference;
  private final @Nonnull Map<String, NecIxBgpVrf> _vrfs;

  public NecIxBgpProcess(long asn) {
    _asn = asn;
    _defaultLocalPreference = DEFAULT_LOCAL_PREFERENCE;
    _vrfs = new LinkedHashMap<>();
    _vrfs.put(DEFAULT_VRF_NAME, new NecIxBgpVrf());
  }

  public long getAsn() {
    return _asn;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public long getDefaultLocalPreference() {
    return _defaultLocalPreference;
  }

  public void setDefaultLocalPreference(long defaultLocalPreference) {
    _defaultLocalPreference = defaultLocalPreference;
  }

  /** Per-VRF configuration keyed by VRF name; the global BGP mode is the default VRF. */
  public @Nonnull Map<String, NecIxBgpVrf> getVrfs() {
    return _vrfs;
  }

  public @Nonnull NecIxBgpVrf getOrCreateVrf(String vrf) {
    return _vrfs.computeIfAbsent(vrf, v -> new NecIxBgpVrf());
  }
}
