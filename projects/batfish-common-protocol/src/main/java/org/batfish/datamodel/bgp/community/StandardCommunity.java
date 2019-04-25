package org.batfish.datamodel.bgp.community;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A standard BGP community, as defined in <a href="https://tools.ietf.org/html/rfc1997">RFC1197</a>
 */
@ParametersAreNonnullByDefault
public final class StandardCommunity extends Community {
  private static final long serialVersionUID = 1L;

  private long _value;

  // Cached string representation
  @Nullable private String _str;

  private StandardCommunity(long value) {
    checkArgument(
        value >= 0 && value <= 0xFFFFFFFFL, "Community value %d is not in the valid range", value);
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

  @Nonnull
  public static StandardCommunity parse(String value) {
    String[] parts = value.split(":");
    checkArgument(parts.length <= 2, "Invalid standard community string: %s", value);
    if (parts.length == 2) {
      return of(Integer.parseUnsignedInt(parts[0]), Integer.parseUnsignedInt(parts[1]));
    } else {
      return of(Long.parseUnsignedLong(value));
    }
  }

  @Nonnull
  public static StandardCommunity of(long value) {
    return new StandardCommunity(value);
  }

  @Nonnull
  public static StandardCommunity of(int high, int low) {
    checkArgument(low >= 0 && low <= 0xFFFF, "Invalid low value: %d", low);
    checkArgument(high >= 0 && high <= 0xFFFF, "Invalid high value: %d", low);
    return new StandardCommunity((long) high << 16 | low);
  }

  /** Return the lower 16 bits of the community value as an integer */
  public int low() {
    return (int) (_value & 0xFFFF);
  }

  /** Return the lower 16 bits of the community value as an integer */
  public int high() {
    return (int) (_value >> 16);
  }

  @Override
  public boolean isTransitive() {
    // By default standard communities are not transitive
    return false;
  }

  @Nonnull
  @Override
  public String matchString() {
    return toString();
  }

  /** Return this community's value as a long */
  public long asLong() {
    return _value;
  }

  @Nonnull
  @Override
  @JsonValue
  public String toString() {
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

  @Nonnull
  @Override
  public BigInteger asBigInt() {
    return BigInteger.valueOf(_value);
  }
}
