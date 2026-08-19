package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Vendor-specific representation of an Aruba AOS-CX BGP process. */
public final class AosCxBgpProcess implements Serializable {

  public AosCxBgpProcess(long localAs) {
    _localAs = localAs;
    _neighbors = new HashMap<>();
  }

  public long getLocalAs() {
    return _localAs;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  public @Nonnull Map<Ip, AosCxBgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  public @Nonnull AosCxBgpNeighbor getOrCreateNeighbor(Ip ip) {
    return _neighbors.computeIfAbsent(ip, AosCxBgpNeighbor::new);
  }

  private final long _localAs;
  private @Nullable Ip _routerId;
  private final @Nonnull Map<Ip, AosCxBgpNeighbor> _neighbors;
}
