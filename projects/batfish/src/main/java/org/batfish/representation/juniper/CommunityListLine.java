package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.WellKnownCommunity;

@ParametersAreNonnullByDefault
public final class CommunityListLine implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private static boolean isNonDigit(int c) {
    return c < '0' || '9' < c;
  }

  public static @Nullable Long literalCommunityValue(String str) {
    String[] parts = str.trim().split(":", -1);
    if (parts.length != 2) {
      return null;
    }
    Long p0 = shortValue(parts[0]);
    if (p0 == null) {
      return null;
    }
    Long p1 = shortValue(parts[1]);
    if (p1 == null) {
      return null;
    }
    return (p0 << 16) | p1;
  }

  private static @Nullable Long shortValue(String str) {
    if (str.chars().anyMatch(CommunityListLine::isNonDigit)) {
      return null;
    }
    if (str.length() > 5) {
      return null;
    }
    long val = Long.parseLong(str);
    if (val < 0 || 65535L < val) {
      return null;
    }
    return val;
  }

  private String _regex;

  public CommunityListLine(String regex) {
    _regex = regex;
  }

  public String getRegex() {
    return _regex;
  }

  /**
   * If {@code _regex} represents a literal community, returns the long value of that community.
   * Else, returns {@code null}.
   */
  public @Nullable Long juniperLiteralCommunityValue() {
    switch (_regex) {
      case "no-advertise":
        return WellKnownCommunity.NO_ADVERTISE;
      case "no-export":
        return WellKnownCommunity.NO_EXPORT;
      case "no-export-subconfed":
        return WellKnownCommunity.NO_EXPORT_SUBCONFED;
      default:
        return literalCommunityValue(_regex);
    }
  }
}
