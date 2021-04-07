package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** FortiOS datamodel component containing BGP configuration */
public final class BgpProcess implements Serializable {
  public BgpProcess() {
    _neighbors = new HashMap<>();
    _networks = new HashMap<>();
  }

  public @Nullable Long getAs() {
    return _as;
  }

  public long getAsEffective() {
    return firstNonNull(_as, DEFAULT_AS);
  }

  public @Nullable Boolean getEbgpMultipath() {
    return _ebgpMultipath;
  }

  public boolean getEbgpMultipathEffective() {
    return firstNonNull(_ebgpMultipath, DEFAULT_EBGP_MULTIPATH);
  }

  public @Nullable Boolean getIbgpMultipath() {
    return _ibgpMultipath;
  }

  public boolean getIbgpMultipathEffective() {
    return firstNonNull(_ibgpMultipath, DEFAULT_IBGP_MULTIPATH);
  }

  public @Nonnull Map<Ip, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  public @Nonnull Map<String, BgpNetwork> getNetworks() {
    return _networks;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setAs(Long as) {
    _as = as;
  }

  public void setEbgpMultipath(boolean ebgpMultipath) {
    _ebgpMultipath = ebgpMultipath;
  }

  public void setIbgpMultipath(boolean ibgpMultipath) {
    _ibgpMultipath = ibgpMultipath;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  public static long DEFAULT_AS = 0L;
  public static boolean DEFAULT_EBGP_MULTIPATH = false;
  public static boolean DEFAULT_IBGP_MULTIPATH = false;

  private @Nullable Long _as;
  private @Nullable Boolean _ebgpMultipath;
  private @Nullable Boolean _ibgpMultipath;
  private @Nullable Ip _routerId;
  private @Nonnull final Map<Ip, BgpNeighbor> _neighbors;
  private @Nonnull final Map<String, BgpNetwork> _networks;
}
