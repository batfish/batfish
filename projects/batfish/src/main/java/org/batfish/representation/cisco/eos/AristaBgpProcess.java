package org.batfish.representation.cisco.eos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Global BGP configuration for Arista */
public final class AristaBgpProcess implements Serializable {
  private final long _asn;
  @Nullable private Integer _holdTimer;
  @Nullable private Integer _keepAliveTimer;
  @Nullable private Ip _routerId;
  @Nullable private Boolean _shutdown;
  @Nonnull private final Map<String, AristaBgpVlanAwareBundle> _vlanAwareBundles;
  @Nonnull private final Map<Integer, AristaBgpVlan> _vlans;

  public AristaBgpProcess(long asn) {
    _asn = asn;
    _vlanAwareBundles = new HashMap<>(0);
    _vlans = new HashMap<>(0);
  }

  public long getAsn() {
    return _asn;
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

  @Nullable
  public Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  @Nonnull
  public Map<String, AristaBgpVlanAwareBundle> getVlanAwareBundles() {
    return _vlanAwareBundles;
  }

  @Nonnull
  public Map<Integer, AristaBgpVlan> getVlans() {
    return _vlans;
  }
}
