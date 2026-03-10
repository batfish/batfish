package org.batfish.common;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Result of attempting to launch a Batfish workitem on a worker. */
@ParametersAreNonnullByDefault
public final class LaunchResult {

  public enum Type {
    /** Worker was busy, client may attempt to launch task again later. */
    BUSY,
    /** Launch resulted in an unrecoverable error. Client should not relaunch this task. */
    ERROR,
    /** Task was launched successfully. */
    LAUNCHED,
  }

  public static @Nonnull LaunchResult launched() {
    return LAUNCHED;
  }

  public static @Nonnull LaunchResult busy() {
    return BUSY;
  }

  public static @Nonnull LaunchResult error(String msg) {
    return new LaunchResult(Type.ERROR, msg);
  }

  private static final LaunchResult LAUNCHED = new LaunchResult(Type.LAUNCHED, null);
  private static final LaunchResult BUSY = new LaunchResult(Type.BUSY, null);

  private LaunchResult(Type type, @Nullable String message) {
    _type = type;
    _message = message;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  /**
   * Error message in case of error.
   *
   * @throws IllegalStateException if this is not an error result.
   */
  public @Nonnull String getMessage() {
    checkState(_type == Type.ERROR && _message != null, "Can only get message for error type");
    return _message;
  }

  private final @Nonnull Type _type;
  private final @Nullable String _message;
}
