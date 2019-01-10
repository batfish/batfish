package org.batfish.datamodel.isis;

import javax.annotation.Nullable;

public enum IsisLevel {
  LEVEL_1,
  LEVEL_1_2,
  LEVEL_2;

  public boolean includes(@Nullable IsisLevel other) {
    return this.equals(union(this, other));
  }

  @Nullable
  public static IsisLevel intersection(IsisLevel... levels) {
    if (levels.length == 0) {
      return null;
    }
    IsisLevel overlap = LEVEL_1_2;
    for (IsisLevel level : levels) {
      overlap = intersection(overlap, level);
    }
    return overlap;
  }

  @Nullable
  private static IsisLevel intersection(@Nullable IsisLevel first, @Nullable IsisLevel second) {
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

  @Nullable
  public static IsisLevel union(@Nullable IsisLevel level1, @Nullable IsisLevel level2) {
    if (level1 == level2) {
      return level1;
    }
    if (level1 == LEVEL_1_2 || level2 == LEVEL_1_2) {
      return LEVEL_1_2;
    }
    if (level1 == LEVEL_1 || level2 == LEVEL_1) {
      // other is null or LEVEL_2
      if (level1 == LEVEL_2 || level2 == LEVEL_2) {
        return LEVEL_1_2;
      }
      // other is null
      return LEVEL_1;
    }
    // one is null and one is LEVEL_2
    return LEVEL_2;
  }
}
