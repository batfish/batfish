package org.batfish.coordinator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** The result of attempting to submit work. */
@ParametersAreNonnullByDefault
public final class SubmissionResult {

  enum Type {
    ERROR,
    SUCCESS,
    FAILURE,
    TERMINATED
  }

  public static @Nonnull SubmissionResult error() {
    return ERROR;
  }

  public static @Nonnull SubmissionResult failure() {
    return FAILURE;
  }

  public static @Nonnull SubmissionResult success(TaskHandle handle) {
    return new SubmissionResult(handle, Type.SUCCESS);
  }

  public static @Nonnull SubmissionResult terminated() {
    return TERMINATED;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  public @Nullable TaskHandle getTaskHandle() {
    return _taskHandle;
  }

  private SubmissionResult(@Nullable TaskHandle taskHandle, Type type) {
    _taskHandle = taskHandle;
    _type = type;
  }

  private static final SubmissionResult ERROR = new SubmissionResult(null, Type.ERROR);
  private static final SubmissionResult FAILURE = new SubmissionResult(null, Type.FAILURE);
  private static final SubmissionResult TERMINATED = new SubmissionResult(null, Type.TERMINATED);

  private final @Nonnull Type _type;

  private final @Nullable TaskHandle _taskHandle;
}
