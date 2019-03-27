package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

/** A 24-bit MAC address */
@ParametersAreNonnullByDefault
public class MacAddress implements Comparable<MacAddress>, Serializable {

  private static final Pattern PATTERN =
      Pattern.compile(
          "^[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]$");
  private static final long serialVersionUID = 1L;

  @VisibleForTesting
  static @Nonnull String asMacAddressString(long longVal) {
    long remainder = longVal;
    String[] pieces = new String[6];
    for (int i = 0; i < 6; i++) {
      long currentByte = (long) remainder & 0xFFL;
      pieces[5 - i] = String.format("%02x", currentByte);
      remainder >>= 8;
    }
    return StringUtils.join(pieces, ":");
  }

  @JsonCreator
  private static @Nonnull MacAddress create(@Nullable String macAddressStr) {
    checkArgument(macAddressStr != null, "MAC address string cannot be null");
    return parse(macAddressStr);
  }

  /** Creates a MAC address from a 48-bit number represented as a long */
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
