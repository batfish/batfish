package org.batfish.datamodel.bgp.community;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigInteger;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.WellKnownCommunity;

/**
 * A standard BGP community, as defined in <a href="https://tools.ietf.org/html/rfc1997">RFC1197</a>
 */
@ParametersAreNonnullByDefault
public final class StandardCommunity extends Community {

  public static final StandardCommunity ACCEPT_OWN =
      StandardCommunity.of(WellKnownCommunity.ACCEPT_OWN);
  public static final StandardCommunity ACCEPT_OWN_NEXTHOP =
      StandardCommunity.of(WellKnownCommunity.ACCEPT_OWN_NEXTHOP);
  public static final StandardCommunity BLACKHOLE =
      StandardCommunity.of(WellKnownCommunity.BLACKHOLE);
  public static final StandardCommunity GRACEFUL_SHUTDOWN =
      StandardCommunity.of(WellKnownCommunity.GRACEFUL_SHUTDOWN);
  public static final StandardCommunity INTERNET =
      StandardCommunity.of(WellKnownCommunity.INTERNET);
  public static final StandardCommunity LLGR_STALE =
      StandardCommunity.of(WellKnownCommunity.LLGR_STALE);
  public static final StandardCommunity NO_ADVERTISE =
      StandardCommunity.of(WellKnownCommunity.NO_ADVERTISE);
  public static final StandardCommunity NO_EXPORT =
      StandardCommunity.of(WellKnownCommunity.NO_EXPORT);
  public static final StandardCommunity NO_EXPORT_SUBCONFED =
      StandardCommunity.of(WellKnownCommunity.NO_EXPORT_SUBCONFED);
  public static final StandardCommunity NO_LLGR = StandardCommunity.of(WellKnownCommunity.NO_LLGR);
  public static final StandardCommunity NO_PEER = StandardCommunity.of(WellKnownCommunity.NO_PEER);
  public static final StandardCommunity ROUTE_FILTER_TRANSLATED_V4 =
      StandardCommunity.of(WellKnownCommunity.ROUTE_FILTER_TRANSLATED_V4);
  public static final StandardCommunity ROUTE_FILTER_TRANSLATED_V6 =
      StandardCommunity.of(WellKnownCommunity.ROUTE_FILTER_TRANSLATED_V6);
  public static final StandardCommunity ROUTE_FILTER_V4 =
      StandardCommunity.of(WellKnownCommunity.ROUTE_FILTER_V4);
  public static final StandardCommunity ROUTE_FILTER_V6 =
      StandardCommunity.of(WellKnownCommunity.ROUTE_FILTER_V6);

  private final long _value;

  // Cached string representation
  private @Nullable transient String _str;

  private StandardCommunity(long value) {
    checkArgument(
        value >= 0 && value <= 0xFFFFFFFFL, "Community value %s is not in the valid range", value);
    _value = value;
  }

  @JsonCreator
  private static StandardCommunity create(@Nullable JsonNode value) {
    // Keep this creator backwards compatible with previous communities implementation
    checkArgument(value != null && !value.isNull(), "Standard community string must not be null");
    if (value.isTextual()) {
      return parse(value.textValue());
    } else if (value.isInt() || value.isLong()) {
      return StandardCommunity.of(value.longValue());
    } else if (value.isArray()
        && value.has(0)
        && value.get(0).textValue().equalsIgnoreCase("standard")) {
      return parse(value.get(1).textValue());
    } else {
      throw new IllegalArgumentException(
          String.format("Invalid standard community value: %s", value));
    }
  }

  public static @Nonnull StandardCommunity parse(String value) {
    String[] parts = value.split(":");
    checkArgument(parts.length <= 2, "Invalid standard community string: %s", value);
    if (parts.length == 2) {
      return of(Integer.parseUnsignedInt(parts[0]), Integer.parseUnsignedInt(parts[1]));
    } else {
      return of(Long.parseUnsignedLong(value));
    }
  }

  public static @Nonnull Optional<StandardCommunity> tryParse(String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static @Nonnull StandardCommunity of(long value) {
    return new StandardCommunity(value);
  }

  public static @Nonnull StandardCommunity of(int high, int low) {
    checkArgument(low >= 0 && low <= 0xFFFF, "Invalid low value: %s", low);
    checkArgument(high >= 0 && high <= 0xFFFF, "Invalid high value: %s", low);
    return new StandardCommunity((long) high << 16 | low);
  }

  /** Return the high 16 bits of the community value as an integer */
  public int high() {
    return (int) (_value >> 16);
  }

  /** Return the lower 16 bits of the community value as an integer */
  public int low() {
    return (int) (_value & 0xFFFF);
  }

  @Override
  public <T> T accept(CommunityVisitor<T> visitor) {
    return visitor.visitStandardCommunity(this);
  }

  @Override
  public boolean isTransitive() {
    // By default standard communities are not transitive
    return false;
  }

  @Override
  public @Nonnull String matchString() {
    return toString();
  }

  /** Return this community's value as a long */
  public long asLong() {
    return _value;
  }

  @Override
  @JsonValue
  public @Nonnull String toString() {
    if (_str == null) {
      _str = (_value >> 16) + ":" + (_value & 0xFFFF);
    }
    return _str;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StandardCommunity)) {
      return false;
    }
    StandardCommunity that = (StandardCommunity) o;
    return _value == that._value;
  }

  @Override
  public int hashCode() {
    // Inline Long hashcode for speed
    return (int) (_value ^ (_value >>> 32));
  }

  @Override
  protected @Nonnull BigInteger asBigIntImpl() {
    return BigInteger.valueOf(_value);
  }
}
