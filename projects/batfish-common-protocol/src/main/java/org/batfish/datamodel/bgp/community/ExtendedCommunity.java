package org.batfish.datamodel.bgp.community;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/**
 * Represents an extended BGP community, as described in <a
 * href="https://tools.ietf.org/html/rfc4360">RFC4360</a>
 */
@ParametersAreNonnullByDefault
public final class ExtendedCommunity extends Community {
  /** The max value that can be stored in 6 bytes. */
  private static final long VALUE_MAX = 0xFFFF_FFFF_FFFFL;

  private static final Set<Integer> VALID_TYPES =
      ImmutableSet.of(0x00, 0x01, 0x02, 0x03, 0x40, 0x41, 0x43);

  /** See: https://datatracker.ietf.org/doc/html/rfc8097.html#section-2 */
  public static final ExtendedCommunity ORIGIN_VALIDATION_STATE_VALID =
      ExtendedCommunity.opaque(false, 0, 0);

  /** See: https://datatracker.ietf.org/doc/html/rfc8097.html#section-2 */
  public static final ExtendedCommunity ORIGIN_VALIDATION_STATE_NOT_FOUND =
      ExtendedCommunity.opaque(false, 0, 1);

  /** See: https://datatracker.ietf.org/doc/html/rfc8097.html#section-2 */
  public static final ExtendedCommunity ORIGIN_VALIDATION_STATE_INVALID =
      ExtendedCommunity.opaque(false, 0, 2);

  /** 1 byte. */
  private final int _type;

  /** 1 byte. */
  private final int _subType;

  /** 6 bytes. */
  private final long _value;

  // Cached string representations
  private @Nullable transient String _str;
  private @Nullable transient String _regexStr;

  private ExtendedCommunity(int type, int subType, long value) {
    checkArgument(VALID_TYPES.contains(type), "Not a valid BGP extended community type: %s", type);
    checkArgument(
        subType >= 0 && type <= 0xFF, "Subtype not within accepted 0-0xFF range: %s", type);
    checkArgument(
        value >= 0 && value <= VALUE_MAX, "Value %s is not within the accepted range", value);

    _type = type;
    _subType = subType;
    _value = value;
  }

  @JsonCreator
  private static ExtendedCommunity create(@Nullable String value) {
    checkArgument(value != null, "Extended community string cannot be null");
    return parse(value);
  }

  public static @Nonnull ExtendedCommunity parse(String communityStr) {
    // TODO: move vendor-specific parsing into vendor-specific context, remove redundant validation.

    String[] parts = communityStr.trim().toLowerCase().split(":");
    checkArgument(parts.length == 3, "Invalid extended community string: %s", communityStr);
    if (parts[0].startsWith("0x") && parts[1].startsWith("0x") && parts[2].startsWith("0x")) {
      // Not custom printed like the kind of extcomms that have gaLong.
      @Nullable Integer type = Ints.tryParse(parts[0].substring(2), 16);
      checkArgument(
          type != null && type >= 0, "Invalid extended community string: %s", communityStr);
      @Nullable Integer subtype = Ints.tryParse(parts[1].substring(2), 16);
      checkArgument(
          subtype != null && subtype >= 0, "Invalid extended community string: %s", communityStr);
      @Nullable Long value = Longs.tryParse(parts[2].substring(2), 16);
      checkArgument(
          value != null && value >= 0, "Invalid extended community string: %s", communityStr);
      return new ExtendedCommunity(type, subtype, value);
    }
    long gaLong;
    long laLong;
    String subType = parts[0];
    String globalAdministrator = parts[1];
    String localAdministrator = parts[2];

    // Try to figure out type/subtype first
    Byte typeByte = null;
    byte subTypeByte;
    // Special values
    if (subType.equals("target")) {
      subTypeByte = 0x02;
    } else if (subType.equals("origin")) {
      subTypeByte = 0x03;
    } else {
      // They type/subtype is a literal integer
      Integer intVal = Ints.tryParse(subType);
      if (intVal != null && intVal <= 0xFFFF && intVal >= 0) {
        subTypeByte = (byte) (intVal & 0xFF);
        typeByte = (byte) ((intVal & 0xFF00) >> 8);
      } else {
        throw new IllegalArgumentException(
            String.format("Invalid extended community type: %s", communityStr));
      }
    }
    // Local administrator, can only be a number
    laLong = Long.parseUnsignedLong(localAdministrator);
    // Global administrator, is complicated. Try a bunch of combinations
    String[] gaParts = globalAdministrator.split("\\.");
    if (gaParts.length == 4) { // Dotted IP address notation
      // type 0x01, 1-byte subtype, 4-byte ip address, 2-byte number la
      typeByte = firstNonNull(typeByte, (byte) 0x01);
      // Ip.parse() ensures IP is valid
      gaLong = Ip.parse(globalAdministrator).asLong();
      checkArgument(laLong <= 0xFFFFL, "Invalid local administrator value %s", localAdministrator);
    } else if (gaParts.length == 2) { // Dotted AS notation
      // type 0x02, 1-byte subtype, 2-byte.2-byte dotted as, 2-byte number la
      typeByte = firstNonNull(typeByte, (byte) 0x02);
      int hi = Integer.parseUnsignedInt(gaParts[0]);
      int lo = Integer.parseUnsignedInt(gaParts[1]);
      checkArgument(
          hi <= 0xFFFF && lo <= 0xFFFF,
          "Invalid global administrator value %s",
          globalAdministrator);
      gaLong = ((long) hi) << 16 | lo;
      checkArgument(laLong <= 0xFFFFL, "Invalid local administrator value %s", localAdministrator);
    } else { // Regular numbers, almost
      if (globalAdministrator.endsWith("l")) { // e.g., 123L, a shorthand for forcing 4-byte GA.
        // type 0x02, 1-byte subtype, 4-byte number ga, 2-byte number la
        typeByte = firstNonNull(typeByte, (byte) 0x02);
        // Strip the "L" and convert to long
        gaLong =
            Long.parseUnsignedLong(
                globalAdministrator.substring(0, globalAdministrator.length() - 1));
        checkArgument(
            gaLong <= 0xFFFFFFFFL, "Invalid global administrator value %s", globalAdministrator);
        checkArgument(
            laLong <= 0xFFFFL, "Invalid local administrator value %s", localAdministrator);
      } else { // No type hint, try both variants with 2-byte GA preferred
        // Try for 2-byte ga, unless the number is too big, in which case it is a 4-byte ga.
        gaLong = Long.parseUnsignedLong(globalAdministrator);
        if (gaLong <= 0xFFFFL) {
          checkArgument(
              laLong <= 0xFFFFFFFFL, "Invalid local administrator value %s", localAdministrator);
          // type 0x00, 1-byte subtype, 2-byte number ga, 4-byte number la
          typeByte = firstNonNull(typeByte, (byte) 0x00);
        } else {
          // type 0x02, 1-byte subtype, 4-byte number ga, 2-byte number la
          checkArgument(
              gaLong <= 0xFFFFFFFFL, "Invalid global administrator value %s", globalAdministrator);
          checkArgument(
              laLong <= 0xFFFFL, "Invalid local administrator value %s", localAdministrator);
          typeByte = firstNonNull(typeByte, (byte) 0x02);
        }
      }
    }
    return of((int) typeByte << 8 | (int) subTypeByte, gaLong, laLong);
  }

  public static @Nonnull Optional<ExtendedCommunity> tryParse(String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static ExtendedCommunity of(int type, long globalAdministrator, long localAdministrator) {
    checkArgument(
        type >= 0 && type <= 0xFFFF,
        "Extended community type %s is not within the allowed range",
        type);
    byte typeByte = (byte) (type >> 8);
    checkArgument(
        globalAdministrator >= 0 && localAdministrator >= 0,
        "Administrator values must be positive");
    int gaOffset;
    if (typeByte == 0x00 || typeByte == 0x40) {
      checkArgument(
          globalAdministrator <= 0xFFFFL && localAdministrator <= 0xFFFFFFFFL,
          "Extended community administrator values are not within the allowed range");
      gaOffset = 32;
    } else {
      checkArgument(
          globalAdministrator <= 0xFFFFFFFFL && localAdministrator <= 0xFFFFL,
          "Extended community administrator values are not within the allowed range");
      gaOffset = 16;
    }
    long value = (globalAdministrator << gaOffset) | localAdministrator;
    return new ExtendedCommunity(typeByte, type & 0xFF, value);
  }

  public static ExtendedCommunity of(int type, Ip globalAdministrator, long localAdministrator) {
    byte typeByte = (byte) (type >> 8);
    checkArgument(
        typeByte == 0x01 || typeByte == 0x41,
        "Invalid type value for IPv4 address specific community");
    return of(type, globalAdministrator.asLong(), localAdministrator);
  }

  /** Return a route target extended community */
  public static ExtendedCommunity target(long asn, long value) {
    int type = asn <= 0xFFFFL ? 0x00 : 0x02;
    return of(type << 8 | 0x02, asn, value);
  }

  /** Return an opaque extended community. See https://tools.ietf.org/html/rfc4360 section 3.3 */
  public static ExtendedCommunity opaque(boolean transitive, int subtype, long value) {
    int type = transitive ? 0x03 : 0x43;
    return new ExtendedCommunity(type, subtype, value);
  }

  @Override
  public <T> T accept(CommunityVisitor<T> visitor) {
    return visitor.visitExtendedCommunity(this);
  }

  @Override
  public boolean isTransitive() {
    // Second most significant bit is set
    return (_type & (byte) 0x40) == 0;
  }

  /** Check whether this community is of type route-origin / site-of-origin */
  public boolean isRouteOrigin() {
    // https://tools.ietf.org/html/rfc4360
    // https://tools.ietf.org/html/rfc4364
    return (_type == 0x00 || _type == 0x01 || _type == 0x02) && _subType == 0x03;
  }

  /** Check whether this community represents an BGP VPN/MPLS route target */
  public boolean isRouteTarget() {
    // https://tools.ietf.org/html/rfc4360
    return (_type == 0x00 || _type == 0x01 || _type == 0x02) && _subType == 0x02;
  }

  /** Check whether this community is of type Cisco VPN-Distinguisher */
  public boolean isVpnDistinguisher() {
    // https://tools.ietf.org/html/rfc7153
    return (_type == 0x00 || _type == 0x01) && _subType == 0x10;
  }

  /** Check whether this community is opaque */
  public boolean isOpaque() {
    // https://tools.ietf.org/html/rfc4360
    return _type == 0x03 || _type == 0x43;
  }

  /**
   * Returns the global administrator value, if applicable to this type of extended community. May
   * be either 2 or 4 bytes depending on type/subtype.
   *
   * @throws UnsupportedOperationException if this extended community is not of a type with a global
   *     administrator
   */
  public long getGlobalAdministrator() {
    if (!hasGlobalAndLocalAdministrator()) {
      throw new UnsupportedOperationException(
          String.format("Extended community does not have a global administrator: %s", this));
    }
    return _value >> localAdministratorBits();
  }

  /**
   * Returns the local administrator value, if applicable to this type of extended community. May be
   * either 2 or 4 bytes depending on type/subtype.
   *
   * @throws UnsupportedOperationException if this extended community is not of a type with a local
   *     administrator
   */
  public long getLocalAdministrator() {
    if (!hasGlobalAndLocalAdministrator()) {
      throw new UnsupportedOperationException(
          String.format("Extended community does not have a local administrator: %s", this));
    }
    return _value & (VALUE_MAX >> globalAdministratorBits());
  }

  /** Return true if the value field contains a subtype */
  private boolean hasSubtype() {
    return true;
  }

  /**
   * Returns the subtype value, if applicable to this type of extended community.
   *
   * @throws UnsupportedOperationException if this extended community does not have a subtype.
   */
  public int getSubtype() {
    if (!hasSubtype()) {
      throw new UnsupportedOperationException(
          String.format("Extended community does not have a subtype: %s", this));
    }
    return _subType;
  }

  /** Return the 6 byte value */
  public long getValue() {
    return _value;
  }

  /** Return true if the value field contains a global and local administrator */
  private @Nonnull boolean hasGlobalAndLocalAdministrator() {
    return !(_type == 0x03 || _type == 0x43);
  }

  private @Nonnull int globalAdministratorBits() {
    return _type == 0x00 || _type == 0x40 ? 16 : 32;
  }

  private @Nonnull int localAdministratorBits() {
    return 48 - globalAdministratorBits();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExtendedCommunity)) {
      return false;
    }
    ExtendedCommunity that = (ExtendedCommunity) o;
    return _type == that._type && _subType == that._subType && _value == that._value;
  }

  @Override
  public int hashCode() {
    int valueHash = (int) (_value ^ (_value >>> 32));
    return 31 * 31 * valueHash + 31 * _type + _subType;
  }

  @Override
  public @Nonnull String matchString() {
    if (!hasGlobalAndLocalAdministrator()) {
      return toString();
    } else {
      if (_regexStr == null) {
        // Type is skipped for regex matching
        _regexStr = getGlobalAdministrator() + ":" + getLocalAdministrator();
      }
      return _regexStr;
    }
  }

  @Override
  @JsonValue
  public @Nonnull String toString() {
    if (_str == null) {
      if (!hasGlobalAndLocalAdministrator()) {
        _str = String.format("0x%x:0x%x:0x%x", _type, _subType, _value);
      } else {
        // To differentiate 4 vs 2-byte global admin values
        String gaSuffix = _type == 0x00 || _type == 0x40 ? "" : "L";
        _str =
            ((_type << 8) | _subType)
                + ":"
                + getGlobalAdministrator()
                + gaSuffix
                + ":"
                + getLocalAdministrator();
      }
    }
    return _str;
  }

  @Override
  protected @Nonnull BigInteger asBigIntImpl() {
    return BigInteger.valueOf(_type)
        .shiftLeft(56)
        .or(BigInteger.valueOf(_subType).shiftLeft(48))
        .or(BigInteger.valueOf(_value));
  }
}
