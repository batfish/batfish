package org.batfish.main.preprocess;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Result of a preprocessed configuration diff operation. */
@ParametersAreNonnullByDefault
public final class DiffResult {
  private final String _unifiedDiff;
  private final boolean _successful;
  private final String _errorMessage;

  private DiffResult(String unifiedDiff, boolean successful, String errorMessage) {
    _unifiedDiff = unifiedDiff;
    _successful = successful;
    _errorMessage = errorMessage;
  }

  /** The unified diff output. Empty string if files are identical or if operation failed. */
  public @Nonnull String getUnifiedDiff() {
    return _unifiedDiff;
  }

  /** True if the files are identical after preprocessing (no differences found). */
  public boolean isEmpty() {
    return _successful && _unifiedDiff.isEmpty();
  }

  /**
   * True if the diff operation completed successfully (regardless of whether differences were
   * found).
   */
  public boolean wasSuccessful() {
    return _successful;
  }

  /** Error message if the operation failed. */
  public Optional<String> getErrorMessage() {
    return _errorMessage != null ? Optional.of(_errorMessage) : Optional.empty();
  }

  /** Create a successful result with the given unified diff output. */
  static DiffResult success(String unifiedDiff) {
    return new DiffResult(unifiedDiff, true, null);
  }

  /** Create a failed result with the given error message. */
  static DiffResult failure(String errorMessage) {
    return new DiffResult("", false, errorMessage);
  }
}
