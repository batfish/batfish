package org.batfish.datamodel.bgp.community;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A standard BGP community, as defined in <a href="https://tools.ietf.org/html/rfc1997">RFC1197</a>
 */
@ParametersAreNonnullByDefault
public final class StandardCommunity implements Community {
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
  private static StandardCommunity create(@Nullable String value) {
    checkArgument(value != null, "Standard community string must not be null");
    return parse(value);
  }

  @Nonnull
  public static StandardCommunity parse(String value) {
    String[] parts = value.split(":");
    checkArgument(parts.length == 2, "Invalid standard community string: %s", value);
    return of(Integer.parseUnsignedInt(parts[0]), Integer.parseUnsignedInt(parts[1]));
  }

  @Nonnull
  public static StandardCommunity of(long value) {
    return new StandardCommunity(value);
  }

  @Nonnull
  public static StandardCommunity of(int high, int low) {
    checkArgument(low >= 0 && low <= 0xFFFF, "Invalid low value: %d", low);
    checkArgument(high >= 0 && high <= 0xFFFF, "Invalid high value: %d", low);
    return new StandardCommunity(high << 16 | low);
  }

  @Override
  public boolean isTransitive() {
    // By default standard communities are not transitive
    return false;
  }

  @Override
  public String matchString() {
    return toString();
  }

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
}
