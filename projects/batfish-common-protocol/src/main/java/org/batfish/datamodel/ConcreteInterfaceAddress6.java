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

/** A concrete IPv6 address assigned to an interface */
@ParametersAreNonnullByDefault
public final class ConcreteInterfaceAddress6 extends InterfaceAddress {

  private static final Comparator<ConcreteInterfaceAddress6> COMPARATOR =
      Comparator.comparing(ConcreteInterfaceAddress6::getIp)
          .thenComparing(ConcreteInterfaceAddress6::getNetworkBits);

  private final @Nonnull Ip6 _ip;
  private final int _networkBits;

  private ConcreteInterfaceAddress6(Ip6 ip, int networkBits) {
    _ip = ip;
    _networkBits = networkBits;
  }

  @JsonCreator
  private static ConcreteInterfaceAddress6 jsonCreator(@Nullable String text) {
    checkArgument(text != null, "Cannot create ConcreteInterfaceAddress6 from null string");
    return parse(text);
  }

  public static ConcreteInterfaceAddress6 create(Ip6 ip, int networkBits) {
    checkArgument(
        networkBits > 0 && networkBits <= Prefix6.MAX_PREFIX_LENGTH,
        "Invalid network mask %s, must be between 1 and %s",
        networkBits,
        Prefix6.MAX_PREFIX_LENGTH);
    assert Prefix6.create(ip, networkBits).toIp6Space().containsIp(ip, ImmutableMap.of())
        : String.format(
            "ConcreteInterfaceAddress6: IP is not a host IP. ip=%s, networkBits=%d",
            ip, networkBits);
    return new ConcreteInterfaceAddress6(ip, networkBits);
  }

  public static ConcreteInterfaceAddress6 parse(String text) {
    String[] parts = text.split("/");
    checkArgument(
        parts.length == 2,
        "Invalid %s string: \"%s\"",
        ConcreteInterfaceAddress6.class.getSimpleName(),
        text);
    Ip6 ip = Ip6.parse(parts[0]);
    int networkBits = Integer.parseUnsignedInt(parts[1]);
    return create(ip, networkBits);
  }

  /**
   * Return an {@link Optional} {@link ConcreteInterfaceAddress6} from a string, or {@link
   * Optional#empty} if the string does not represent a {@link ConcreteInterfaceAddress6}.
   */
  public static @Nonnull Optional<ConcreteInterfaceAddress6> tryParse(String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  @Override
  public int compareTo(InterfaceAddress rhs) {
    if (rhs instanceof ConcreteInterfaceAddress6) {
      return COMPARATOR.compare(this, (ConcreteInterfaceAddress6) rhs);
    }
    return 1;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ConcreteInterfaceAddress6)) {
      return false;
    }
    ConcreteInterfaceAddress6 rhs = (ConcreteInterfaceAddress6) o;
    return _ip.equals(rhs._ip) && _networkBits == rhs._networkBits;
  }

  public @Nonnull Ip6 getIp() {
    return _ip;
  }

  public int getNetworkBits() {
    return _networkBits;
  }

  public @Nonnull Prefix6 getPrefix() {
    return Prefix6.create(_ip, _networkBits);
  }

  @Override
  public int hashCode() {
    return 31 * _ip.hashCode() + _networkBits;
  }

  @Override
  @JsonValue
  public String toString() {
    return _ip + "/" + _networkBits;
  }
}
