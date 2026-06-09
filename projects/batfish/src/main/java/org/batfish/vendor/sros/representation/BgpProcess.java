package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * SR-OS BGP configuration under a {@link Router} (e.g. {@code router "Base" bgp}). Holds the
 * router-id plus the peer {@link BgpGroup}s and {@link BgpNeighbor}s.
 */
public final class BgpProcess implements Serializable {

  public BgpProcess() {
    _groups = new HashMap<>();
    _neighbors = new HashMap<>();
  }

  /** The {@code router-id}, or {@code null} if unset (derived elsewhere in that case). */
  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  /** Peer groups (templates), keyed by group-name. */
  public @Nonnull Map<String, BgpGroup> getGroups() {
    return _groups;
  }

  /** Neighbors, keyed by peer IP string. */
  public @Nonnull Map<String, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  private @Nullable Ip _routerId;
  private final @Nonnull Map<String, BgpGroup> _groups;
  private final @Nonnull Map<String, BgpNeighbor> _neighbors;
}
