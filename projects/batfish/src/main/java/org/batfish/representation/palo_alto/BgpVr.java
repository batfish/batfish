package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Configuration of BGP within a virtual-router. Config at {@code network virtual-router NAME
 * protocol bgp}.
 */
public class BgpVr implements Serializable {

  public BgpVr() {
    _localAs = null;
    _routerId = null;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  // private implementation details

  private @Nullable Long _localAs;
  private @Nullable Ip _routerId;
}
