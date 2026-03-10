package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A concrete IPv4 address assigned to an interface */
@ParametersAreNonnullByDefault
public final class ConcreteInterfaceAddress extends InterfaceAddress {

  private static final Comparator<ConcreteInterfaceAddress> COMPARATOR =
      Comparator.comparing(ConcreteInterfaceAddress::getIp)
          .thenComparing(ConcreteInterfaceAddress::getNetworkBits);

  private final @Nonnull Ip _ip;
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
    assert Prefix.create(ip, networkBits).toHostIpSpace().containsIp(ip, ImmutableMap.of())
        : String.format(
            "ConcreteInterfaceAddress: IP is not a host IP. ip=%s, networkBits=%d",
            ip, networkBits);
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

  /**
   * Return an {@link Optional} {@link ConcreteInterfaceAddress} from a string, or {@link
   * Optional#empty} if the string does not represent a {@link ConcreteInterfaceAddress}.
   */
  public static @Nonnull Optional<ConcreteInterfaceAddress> tryParse(String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
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

  public @Nonnull Ip getIp() {
    return _ip;
  }

  public int getNetworkBits() {
    return _networkBits;
  }

  public @Nonnull Prefix getPrefix() {
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
