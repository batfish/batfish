package org.batfish.datamodel.bgp.community;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.primitives.Longs;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a large BGP community, as described in <a
 * href="https://tools.ietf.org/html/rfc8092">RFC8092</a>
 */
@ParametersAreNonnullByDefault
public final class LargeCommunity implements Community {

  private static final long serialVersionUID = -1;

  private final long _globalAdministrator;
  private final long _localData1;
  private final long _localData2;

  // Cached string representation
  @Nullable private String _str;

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
        parts.length == 3 || parts.length == 4, "Invalid large BGP community string %s", value);
    if (parts.length == 3) {
      return of(getLongValue(parts[0]), getLongValue(parts[1]), getLongValue(parts[2]));
    } else if (parts[0].equalsIgnoreCase("large")) {
      return of(getLongValue(parts[1]), getLongValue(parts[2]), getLongValue(parts[3]));
    } else {
      throw new IllegalArgumentException(
          String.format("Invalid large community string: %s", value));
    }
  }

  private static long getLongValue(String s) {
    Long val = Longs.tryParse(s);
    checkArgument(val != null && val >= 0 && val <= 0xFFFFFFFFL, "Invalid identifier: %s", s);
    return val;
  }

  public static LargeCommunity of(long globalAdministrator, long localData1, long localData2) {
    checkArgument(
        globalAdministrator >= 0 && globalAdministrator <= 0xFFFFFFFFL,
        "Invalid global administrator value: %d",
        globalAdministrator);
    checkArgument(
        localData1 >= 0 && localData1 <= 0xFFFFFFFFL,
        "Invalid global administrator value: %d",
        globalAdministrator);
    checkArgument(
        localData2 >= 0 && localData2 <= 0xFFFFFFFFL,
        "Invalid global administrator value: %d",
        globalAdministrator);
    return new LargeCommunity(globalAdministrator, localData1, localData2);
  }

  @Override
  public boolean isTransitive() {
    // False by default
    return false;
  }

  @Override
  public String matchString() {
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
  public String toString() {
    if (_str == null) {
      _str = _globalAdministrator + ":" + _localData1 + ":" + _localData2;
    }
    return _str;
  }
}
