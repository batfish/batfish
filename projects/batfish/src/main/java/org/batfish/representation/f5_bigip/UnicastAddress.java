package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nullable;

/** A unicast-address structure associated with a {@link Device}. */
public final class UnicastAddress implements Serializable {

  public UnicastAddress() {
    _effectivePort = DEFAULT_EFFECTIVE_PORT;
  }

  public @Nullable UnicastAddressIp getEffectiveIp() {
    return _effectiveIp;
  }

  public void setEffectiveIp(@Nullable UnicastAddressIp effectiveIp) {
    _effectiveIp = effectiveIp;
  }

  public int getEffectivePort() {
    return _effectivePort;
  }

  public void setEffectivePort(int effectivePort) {
    _effectivePort = effectivePort;
  }

  public @Nullable UnicastAddressIp getIp() {
    return _ip;
  }

  public void setIp(@Nullable UnicastAddressIp ip) {
    _ip = ip;
  }

  public @Nullable Integer getPort() {
    return _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }

  private @Nullable UnicastAddressIp _effectiveIp;
  private int _effectivePort;
  private @Nullable UnicastAddressIp _ip;
  private @Nullable Integer _port;

  private static final int DEFAULT_EFFECTIVE_PORT = 1026;
}
