package org.batfish.datamodel.bgp.community;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a large BGP community, as described in <a
 * href="https://tools.ietf.org/html/rfc8092">RFC8092</a>
 */
@ParametersAreNonnullByDefault
public final class LargeCommunity extends Community {

  private final long _globalAdministrator;
  private final long _localData1;
  private final long _localData2;

  // Cached string representation
  private @Nullable String _str;

  private LargeCommunity(long globalAdministrator, long localData1, long localData2) {
    _globalAdministrator = globalAdministrator;
    _localData1 = localData1;
    _localData2 = localData2;
  }

  @JsonCreator
  private static LargeCommunity create(@Nullable String value) {
    checkArgument(value != null, "Large BGP community value cannot be null");
    return parse(value);
  }

  public static LargeCommunity parse(String value) {
    String[] parts = value.split(":");
    checkArgument(
        parts.length == 4 && parts[0].equalsIgnoreCase("large"),
        "Invalid large BGP community string %s",
        value);
    return of(
        Long.parseUnsignedLong(parts[1]),
        Long.parseUnsignedLong(parts[2]),
        Long.parseUnsignedLong(parts[3]));
  }

  public static @Nonnull Optional<LargeCommunity> tryParse(String text) {
    try {
      return Optional.of(parse(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static LargeCommunity of(long globalAdministrator, long localData1, long localData2) {
    checkArgument(
        globalAdministrator >= 0 && globalAdministrator <= 0xFFFFFFFFL,
        "Invalid global administrator value: %s",
        globalAdministrator);
    checkArgument(
        localData1 >= 0 && localData1 <= 0xFFFFFFFFL,
        "Invalid local administrator value: %s",
        localData1);
    checkArgument(
        localData2 >= 0 && localData2 <= 0xFFFFFFFFL,
        "Invalid local administrator value: %s",
        localData2);
    return new LargeCommunity(globalAdministrator, localData1, localData2);
  }

  @Override
  public <T> T accept(CommunityVisitor<T> visitor) {
    return visitor.visitLargeCommunity(this);
  }

  public long getGlobalAdministrator() {
    return _globalAdministrator;
  }

  public long getLocalData1() {
    return _localData1;
  }

  public long getLocalData2() {
    return _localData2;
  }

  @Override
  public boolean isTransitive() {
    // False by default
    return false;
  }

  @Override
  public @Nonnull String matchString() {
    return toString();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LargeCommunity that = (LargeCommunity) o;
    return _globalAdministrator == that._globalAdministrator
        && _localData1 == that._localData1
        && _localData2 == that._localData2;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_globalAdministrator, _localData1, _localData2);
  }

  @Override
  public @Nonnull String toString() {
    if (_str == null) {
      _str = "large:" + _globalAdministrator + ":" + _localData1 + ":" + _localData2;
    }
    return _str;
  }

  @Override
  protected @Nonnull BigInteger asBigIntImpl() {
    return BigInteger.valueOf(_globalAdministrator)
        .shiftLeft(64)
        .or(BigInteger.valueOf(_localData1).shiftLeft(32))
        .or(BigInteger.valueOf(_localData2));
  }
}
