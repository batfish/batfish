package org.batfish.datamodel.isis;

import javax.annotation.Nullable;

public enum IsisLevel {
  LEVEL_1,
  LEVEL_1_2,
  LEVEL_2;

  public boolean includes(@Nullable IsisLevel other) {
    return this == union(this, other);
  }

  public static @Nullable IsisLevel intersection(IsisLevel... levels) {
    if (levels.length == 0) {
      return null;
    }
    IsisLevel overlap = LEVEL_1_2;
    for (IsisLevel level : levels) {
      overlap = intersection(overlap, level);
    }
    return overlap;
  }

  private static @Nullable IsisLevel intersection(
      @Nullable IsisLevel first, @Nullable IsisLevel second) {
    if (first == second) {
      return first;
    }
    if (first == LEVEL_1_2) {
      return second;
    }
    if (second == LEVEL_1_2) {
      return first;
    }
    return null;
  }

  public static @Nullable IsisLevel union(@Nullable IsisLevel first, @Nullable IsisLevel second) {
    if (first == second || second == null) {
      return first;
    } else if (first == null) {
      return second;
    }
    // Both nonnull, and different from each other: must include both levels
    return LEVEL_1_2;
  }
}
