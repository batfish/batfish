package org.batfish.representation.cisco.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** BGP configuration for a single VRF */
public final class AristaBgpVrf implements Serializable {

  @Nonnull private final Map<Prefix, AristaBgpAggregateNetwork> _v4aggregates;
  @Nullable private Integer _holdTimer;
  @Nullable private Integer _keepAliveTimer;
  @Nullable private Ip _routerId;
  @Nonnull private final String _name;
  @Nullable private Boolean _shutdown;
  @Nonnull private Map<Ip, AristaBgpV4Neighbor> _v4neighbors;

  public AristaBgpVrf(String name) {
    _name = name;
    _v4aggregates = new HashMap<>(0);
    _v4neighbors = new HashMap<>(0);
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

  @Nonnull
  public Map<Prefix, AristaBgpAggregateNetwork> getV4aggregates() {
    return _v4aggregates;
  }

  @Nonnull
  public Map<Ip, AristaBgpV4Neighbor> getV4neighbors() {
    return _v4neighbors;
  }
}
