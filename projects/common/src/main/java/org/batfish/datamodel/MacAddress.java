package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.io.Serializable;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A 48-bit MAC address */
@ParametersAreNonnullByDefault
public class MacAddress implements Comparable<MacAddress>, Serializable {

  private static final Pattern PATTERN =
      Pattern.compile(
          "^[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]$");

  @VisibleForTesting
  static @Nonnull String asMacAddressString(long longVal) {
    return String.join(":", Splitter.fixedLength(2).split(String.format("%012x", longVal)));
  }

  @JsonCreator
  private static @Nonnull MacAddress create(@Nullable String macAddressStr) {
    checkArgument(macAddressStr != null, "MAC address string cannot be null");
    return parse(macAddressStr);
  }

  /**
   * Creates a MAC address from a 48-bit number represented as a long.
   *
   * @throws IllegalArgumentException if {@code longVal} does not represent a valid MAC address
   */
  public static @Nonnull MacAddress of(long longVal) {
    checkArgument(
        longVal == (longVal & 0xFFFFFFFFFFFFL),
        "Cannot create MAC address from invalid long value: %s",
        longVal);
    return new MacAddress(longVal);
  }

  /**
   * Parses a MAC address from its string representation of the form 00:11:22:Aa:bB:CC
   *
   * @throws IllegalArgumentException if {@code macAddressStr} does not represent a valid MAC
   *     address
   */
  public static @Nonnull MacAddress parse(String macAddressStr) {
    checkArgument(PATTERN.matcher(macAddressStr).matches(), "Not a MAC address: %s", macAddressStr);
    String hexDigits = macAddressStr.replace(":", "");
    return new MacAddress(Long.parseLong(hexDigits, 16));
  }

  private final long _longVal;

  private MacAddress(long longVal) {
    _longVal = longVal;
  }

  public long asLong() {
    return _longVal;
  }

  @Override
  public int compareTo(MacAddress o) {
    return Long.compare(_longVal, o._longVal);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MacAddress)) {
      return false;
    }
    return _longVal == ((MacAddress) obj)._longVal;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_longVal);
  }

  @JsonValue
  @Override
  public @Nonnull String toString() {
    return asMacAddressString(_longVal);
  }
}
