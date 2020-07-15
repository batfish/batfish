package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A concrete IPv4 address assigned to an interface */
@ParametersAreNonnullByDefault
public final class ConcreteInterfaceAddress extends InterfaceAddress {

  private static final Comparator<ConcreteInterfaceAddress> COMPARATOR =
      Comparator.comparing(ConcreteInterfaceAddress::getIp)
          .thenComparing(ConcreteInterfaceAddress::getNetworkBits);

  @Nonnull private final Ip _ip;
  private final int _networkBits;

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
        "Invalid network mask %s, must be between 1 and %s",
        networkBits,
        Prefix.MAX_PREFIX_LENGTH);
    return new ConcreteInterfaceAddress(ip, networkBits);
  }

  public static ConcreteInterfaceAddress create(Ip ip, Ip networkMask) {
    return create(ip, networkMask.numSubnetBits());
  }

  public static ConcreteInterfaceAddress parse(String text) {
    String[] parts = text.split("/");
    checkArgument(
        parts.length == 2,
        "Invalid %s string: \"%s\"",
        ConcreteInterfaceAddress.class.getSimpleName(),
        text);
    Ip ip = Ip.parse(parts[0]);
    int networkBits = Integer.parseUnsignedInt(parts[1]);
    return create(ip, networkBits);
  }

  @Override
  public int compareTo(InterfaceAddress rhs) {
    if (rhs instanceof ConcreteInterfaceAddress) {
      return COMPARATOR.compare(this, (ConcreteInterfaceAddress) rhs);
    }
    return 1;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ConcreteInterfaceAddress)) {
      return false;
    }
    ConcreteInterfaceAddress rhs = (ConcreteInterfaceAddress) o;
    return _ip.equals(rhs._ip) && _networkBits == rhs._networkBits;
  }

  @Nonnull
  public Ip getIp() {
    return _ip;
  }

  public int getNetworkBits() {
    return _networkBits;
  }

  @Nonnull
  public Prefix getPrefix() {
    return Prefix.create(_ip, _networkBits);
  }

  @Override
  public int hashCode() {
    // We want a custom quick implementation, so don't call Objects.hash()
    return 31 * Long.hashCode(_ip.asLong()) + _networkBits;
  }

  @Override
  @JsonValue
  public String toString() {
    return _ip + "/" + _networkBits;
  }
}
