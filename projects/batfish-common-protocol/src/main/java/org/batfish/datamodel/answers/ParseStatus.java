package org.batfish.datamodel.answers;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum ParseStatus {
  EMPTY,
  FAILED,
  IGNORED,
  ORPHANED,
  PARTIALLY_UNRECOGNIZED,
  PASSED,
  UNKNOWN,
  UNSUPPORTED;

  private static final List<ParseStatus> ORDERED =
      ImmutableList.of(
          FAILED, UNSUPPORTED, ORPHANED, UNKNOWN, IGNORED, PARTIALLY_UNRECOGNIZED, EMPTY, PASSED);

  /**
   * A helper function to determine a "canonical" {@link ParseStatus} for an object with multiple
   * status components, e.g, a host with multiple files contributing status to it.
   */
  public static ParseStatus resolve(ParseStatus... statuses) {
    return Arrays.stream(statuses).min(Comparator.comparingInt(ORDERED::indexOf)).orElse(UNKNOWN);
  }
}
