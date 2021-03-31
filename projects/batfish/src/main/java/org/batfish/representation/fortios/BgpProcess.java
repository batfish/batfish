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
  }

  public @Nullable Long getAs() {
    return _as;
  }

  public long getAsEffective() {
    return firstNonNull(_as, DEFAULT_AS);
  }

  public @Nonnull Map<Ip, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setAs(Long as) {
    _as = as;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  public static long DEFAULT_AS = 0L;

  private @Nullable Long _as;
  private @Nullable Ip _routerId;
  private final Map<Ip, BgpNeighbor> _neighbors;
}
