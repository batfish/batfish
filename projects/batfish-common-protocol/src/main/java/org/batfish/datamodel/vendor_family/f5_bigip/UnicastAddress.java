package org.batfish.datamodel.vendor_family.f5_bigip;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
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

  @JsonProperty(PROP_EFFECTIVE_IP)
  public @Nullable UnicastAddressIp getEffectiveIp() {
    return _effectiveIp;
  }

  @JsonProperty(PROP_EFFECTIVE_PORT)
  public int getEffectivePort() {
    return _effectivePort;
  }

  @JsonProperty(PROP_IP)
  public @Nullable UnicastAddressIp getIp() {
    return _ip;
  }

  @JsonProperty(PROP_PORT)
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
  private static final String PROP_EFFECTIVE_IP = "effectiveIp";
  private static final String PROP_EFFECTIVE_PORT = "effectivePort";
  private static final String PROP_IP = "ip";
  private static final String PROP_PORT = "port";

  @JsonCreator
  private static @Nonnull UnicastAddress create(
      @JsonProperty(PROP_EFFECTIVE_IP) @Nullable UnicastAddressIp effectiveIp,
      @JsonProperty(PROP_EFFECTIVE_PORT) @Nullable Integer effectivePort,
      @JsonProperty(PROP_IP) @Nullable UnicastAddressIp ip,
      @JsonProperty(PROP_PORT) @Nullable Integer port) {
    Builder builder = builder().setEffectiveIp(effectiveIp);
    Optional.ofNullable(effectivePort).ifPresent(builder::setEffectivePort);
    return builder.setIp(ip).setPort(port).build();
  }

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
