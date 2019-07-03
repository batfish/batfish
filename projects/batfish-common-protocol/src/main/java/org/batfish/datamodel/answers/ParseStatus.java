package org.batfish.datamodel.answers;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.batfish.common.BatfishException;

public enum ParseStatus {
  /** The file was empty, we have nothing to do */
  EMPTY,
  /** Batfish has encountered an unrecoverable error during parsing */
  FAILED,
  /** File was explicitly ignored by the user */
  IGNORED,
  /** File is part of an unused overlay configuration */
  ORPHANED,
  /** Some syntax was unrecognized, but Batfish processed the file */
  PARTIALLY_UNRECOGNIZED,
  /** File was fully parsed */
  PASSED,
  /** Batfish could not detect the file format */
  UNKNOWN,
  /** Batfish does not support the format/vendor config in the file */
  UNSUPPORTED,
  /** Configuration would be rejected by a device (failed to commit) because it is invalid */
  WILL_NOT_COMMIT;

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

  /** Get explanation for parse statuses. */
  public static String explanation(ParseStatus status) {
    switch (status) {
      case EMPTY:
        return "File is empty";
      case FAILED:
        return "File failed to parse";
      case IGNORED:
        return "File explicitly ignored by user";
      case ORPHANED:
        return "File is an orphaned overlay configuration";
      case PARTIALLY_UNRECOGNIZED:
        return "File contained at least one unrecognized line";
      case PASSED:
        return "File parsed successfully";
      case UNKNOWN:
        return "File format is unknown";
      case UNSUPPORTED:
        return "File format is known but unsupported";
      case WILL_NOT_COMMIT:
        return "File contains configuration that will be rejected by a device";
      default:
        throw new BatfishException(
            String.format("Unhandled parse status explanation for status: %s", status));
    }
  }
}
