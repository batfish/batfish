package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public final class ConcreteInterfaceAddress
    implements Comparable<ConcreteInterfaceAddress>, Serializable, InterfaceAddress {

  private static final long serialVersionUID = 1L;

  private Ip _ip;

  private int _networkBits;

  private ConcreteInterfaceAddress(Ip ip, int networkBits) {
    _ip = ip;
    _networkBits = networkBits;
  }

  @JsonCreator
  private static ConcreteInterfaceAddress jsonCreator(@Nullable String text) {
    checkArgument(text != null, "Cannot create ConcreteInterfaceAddress from null string");
    return parse(text);
  }

  public static ConcreteInterfaceAddress create(Ip ip, int networkBits) {
    checkArgument(
        networkBits > 0 && networkBits <= Prefix.MAX_PREFIX_LENGTH,
        "Invalid network mask %d, must be between 1 and %d",
        networkBits,
        Prefix.MAX_PREFIX_LENGTH);
    return new ConcreteInterfaceAddress(ip, networkBits);
  }

  public static ConcreteInterfaceAddress create(Ip ip, Ip networkMask) {
    return create(ip, networkMask.numSubnetBits());
  }

  public static ConcreteInterfaceAddress parse(String text) {
    String[] parts = text.split("/");
    if (parts.length != 2) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid %s string: \"%s\"", ConcreteInterfaceAddress.class.getSimpleName(), text));
    }
    Ip ip = Ip.parse(parts[0]);
    int networkBits = Integer.parseUnsignedInt(parts[1]);
    return create(ip, networkBits);
  }

  @Override
  public int compareTo(ConcreteInterfaceAddress rhs) {
    int ret = _ip.compareTo(rhs._ip);
    if (ret != 0) {
      return ret;
    }
    return Integer.compare(_networkBits, rhs._networkBits);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ConcreteInterfaceAddress)) {
      return false;
    }
    ConcreteInterfaceAddress rhs = (ConcreteInterfaceAddress) o;
    return _ip.equals(rhs._ip) && _networkBits == rhs._networkBits;
  }

  public Ip getIp() {
    return _ip;
  }

  public int getNetworkBits() {
    return _networkBits;
  }

  /** Return the canonical {@link Prefix} for this interface address */
  @Nonnull
  public Prefix getPrefix() {
    return Prefix.create(_ip, _networkBits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _networkBits);
  }

  @Override
  @JsonValue
  public String toString() {
    return _ip + "/" + _networkBits;
  }
}
