package org.batfish.representation.cisco.eos;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** BGP configuration for a single VRF */
public final class AristaBgpVrf implements Serializable {

  @Nullable private Integer _holdTimer;
  @Nullable private Integer _keepAliveTimer;
  @Nullable private Ip _routerId;
  @Nonnull private final String _name;
  @Nullable private Boolean _shutdown;

  public AristaBgpVrf(String name) {
    _name = name;
  }

  /** Hold timer, in seconds */
  @Nullable
  public Integer getHoldTimer() {
    return _holdTimer;
  }

  /** Set the Hold timer, in seconds */
  public void setHoldTimer(@Nullable Integer holdTimer) {
    _holdTimer = holdTimer;
  }

  /** Keepalive timer, in seconds */
  @Nullable
  public Integer getKeepAliveTimer() {
    return _keepAliveTimer;
  }

  /** Set the Keepalive timer, in seconds */
  public void setKeepAliveTimer(@Nullable Integer keepAliveTimer) {
    _keepAliveTimer = keepAliveTimer;
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }
}
