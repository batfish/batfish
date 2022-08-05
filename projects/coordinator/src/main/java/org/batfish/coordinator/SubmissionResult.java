package org.batfish.coordinator;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** The result of attempting to submit work. */
@ParametersAreNonnullByDefault
public final class SubmissionResult {

  public enum Type {
    /**
     * The submission failed because of an unrecoverable error. The submission should not be
     * reattempted.
     */
    ERROR,
    /** The submission succeeded. The result contains a task handle to track the submission. */
    SUCCESS,
    /**
     * The submission failed because the executor is currently busy. The submission should be
     * reattempted at a later time.
     */
    BUSY,
  }

  public static @Nonnull SubmissionResult error(String message) {
    return new SubmissionResult(Type.ERROR, null, message);
  }

  public static @Nonnull SubmissionResult busy() {
    return BUSY;
  }

  public static @Nonnull SubmissionResult success(TaskHandle handle) {
    return new SubmissionResult(Type.SUCCESS, handle, null);
  }

  public @Nonnull Type getType() {
    return _type;
  }

  /**
   * Task handle for a result of type {@link Type#SUCCESS}.
   *
   * @throws IllegalStateException if result is of any other type.
   */
  public @Nonnull TaskHandle getTaskHandle() {
    checkState(
        _type == Type.SUCCESS,
        "Only a submission of type SUCCESS has a task handle, but type is %s",
        _type);
    assert _taskHandle != null;
    return _taskHandle;
  }

  /**
   * Message for a result of type {@link Type#ERROR}.
   *
   * @throws IllegalStateException if result is of any other type.
   */
  public @Nonnull String getMessage() {
    checkState(
        _type == Type.ERROR,
        "Only a submission of type ERROR has a message, but type is %s",
        _type);
    assert _message != null;
    return _message;
  }

  private SubmissionResult(Type type, @Nullable TaskHandle taskHandle, @Nullable String message) {
    _type = type;
    _taskHandle = taskHandle;
    _message = message;
  }

  private static final SubmissionResult BUSY = new SubmissionResult(Type.BUSY, null, null);

  private final @Nonnull Type _type;
  private final @Nullable String _message;
  private final @Nullable TaskHandle _taskHandle;
}
