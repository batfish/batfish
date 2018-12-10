package org.batfish.datamodel;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class StandardCommunity {

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
    if (str.chars().anyMatch(StandardCommunity::isNonDigit)) {
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
}
