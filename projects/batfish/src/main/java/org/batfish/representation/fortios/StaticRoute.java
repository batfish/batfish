package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class StaticRoute implements Serializable {
  public enum Status {
    ENABLE,
    DISABLE
  }

  private @Nullable String _device;
  private @Nullable Integer _distance;
  private @Nullable Prefix _dst;
  private @Nullable Ip _gateway;
  private @Nullable Boolean _sdwanEnabled;
  private final @Nonnull String _seqNum;
  private @Nullable Status _status;
  private @Nullable Boolean _bfd;

  public static final int DEFAULT_DISTANCE = 10; // for non-SD-WAN routes only
  public static final int DEFAULT_DISTANCE_SDWAN = 1;
  public static final Prefix DEFAULT_DST = Prefix.ZERO;
  public static final boolean DEFAULT_SDWAN_ENABLED = false;
  public static final Status DEFAULT_STATUS = Status.ENABLE;
  public static final boolean DEFAULT_BFD = false;

  public StaticRoute(String seqNum) {
    _seqNum = seqNum;
  }

  public @Nullable String getDevice() {
    return _device;
  }

  public @Nullable Integer getDistance() {
    return _distance;
  }

  /** Effective administrative distance, even if not explicitly configured. */
  public int getDistanceEffective() {
    return firstNonNull(
        _distance, getSdwanEnabledEffective() ? DEFAULT_DISTANCE_SDWAN : DEFAULT_DISTANCE);
  }

  public @Nullable Prefix getDst() {
    return _dst;
  }

  public @Nonnull Prefix getDstEffective() {
    return firstNonNull(_dst, DEFAULT_DST);
  }

  public @Nullable Ip getGateway() {
    return _gateway;
  }

  public Boolean getSdwanEnabled() {
    return _sdwanEnabled;
  }

  public boolean getSdwanEnabledEffective() {
    return firstNonNull(_sdwanEnabled, DEFAULT_SDWAN_ENABLED);
  }

  public @Nonnull String getSeqNum() {
    return _seqNum;
  }

  public @Nullable Status getStatus() {
    return _status;
  }

  /** Effective status: {@code true} if route is enabled. */
  public boolean getStatusEffective() {
    return firstNonNull(_status, DEFAULT_STATUS) == Status.ENABLE;
  }

  public @Nullable Boolean getBfd() {
    return _bfd;
  }

  /** Effective BFD setting: {@code true} if BFD is enabled. */
  public boolean getBfdEffective() {
    return firstNonNull(_bfd, DEFAULT_BFD);
  }

  public void setDevice(String device) {
    _device = device;
  }

  public void setDistance(Integer distance) {
    _distance = distance;
  }

  public void setDst(Prefix dst) {
    _dst = dst;
  }

  public void setGateway(Ip gateway) {
    _gateway = gateway;
  }

  public void setSdwanEnabled(Boolean sdwanEnabled) {
    _sdwanEnabled = sdwanEnabled;
  }

  public void setStatus(Status status) {
    _status = status;
  }

  public void setBfd(Boolean bfd) {
    _bfd = bfd;
  }
}
