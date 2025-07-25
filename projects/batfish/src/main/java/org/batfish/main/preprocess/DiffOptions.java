package org.batfish.main.preprocess;

import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration options for preprocessed configuration diffing. */
@ParametersAreNonnullByDefault
public final class DiffOptions {
  private final int _contextLines;

  private DiffOptions(int contextLines) {
    _contextLines = contextLines;
  }

  /** Number of context lines to show around changes in unified diff format. */
  public int getContextLines() {
    return _contextLines;
  }

  /** Create DiffOptions with default settings (3 context lines). */
  public static DiffOptions defaults() {
    return new DiffOptions(3);
  }

  /** Create DiffOptions with specified context lines. */
  public static DiffOptions withContextLines(int contextLines) {
    return new DiffOptions(contextLines);
  }
}
