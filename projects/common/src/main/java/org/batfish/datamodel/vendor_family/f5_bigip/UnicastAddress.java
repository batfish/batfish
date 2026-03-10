package org.batfish.datamodel.vendor_family.f5_bigip;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A unicast-address structure associated with a {@link Device}. */
public final class UnicastAddress implements Serializable {

  public static final class Builder {

    public @Nonnull UnicastAddress build() {
      return new UnicastAddress(_effectiveIp, _effectivePort, _ip, _port);
    }

    public @Nonnull Builder setEffectiveIp(@Nullable UnicastAddressIp effectiveIp) {
      _effectiveIp = effectiveIp;
      return this;
    }

    public @Nonnull Builder setEffectivePort(int effectivePort) {
      _effectivePort = effectivePort;
      return this;
    }

    public @Nonnull Builder setIp(@Nullable UnicastAddressIp ip) {
      _ip = ip;
      return this;
    }

    public @Nonnull Builder setPort(@Nullable Integer port) {
      _port = port;
      return this;
    }

    private @Nullable UnicastAddressIp _effectiveIp;
    private int _effectivePort;
    private @Nullable UnicastAddressIp _ip;
    private @Nullable Integer _port;

    private Builder() {
      _port = DEFAULT_EFFECTIVE_PORT;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nullable UnicastAddressIp getEffectiveIp() {
    return _effectiveIp;
  }

  public int getEffectivePort() {
    return _effectivePort;
  }

  public @Nullable UnicastAddressIp getIp() {
    return _ip;
  }

  public @Nullable Integer getPort() {
    return _port;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UnicastAddress)) {
      return false;
    }
    UnicastAddress rhs = (UnicastAddress) obj;
    return Objects.equals(_effectiveIp, rhs._effectiveIp)
        && _effectivePort == rhs._effectivePort
        && Objects.equals(_ip, rhs._ip)
        && Objects.equals(_port, rhs._port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_effectiveIp, _effectivePort, _ip, _port);
  }

  private static final int DEFAULT_EFFECTIVE_PORT = 1026;

  private final @Nullable UnicastAddressIp _effectiveIp;
  private final int _effectivePort;
  private final @Nullable UnicastAddressIp _ip;
  private final @Nullable Integer _port;

  private UnicastAddress(
      @Nullable UnicastAddressIp effectiveIp,
      int effectivePort,
      @Nullable UnicastAddressIp ip,
      @Nullable Integer port) {
    _effectiveIp = effectiveIp;
    _effectivePort = effectivePort;
    _ip = ip;
    _port = port;
  }
}
