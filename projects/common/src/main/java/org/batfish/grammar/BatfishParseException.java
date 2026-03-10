package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.ErrorDetails;

@ParametersAreNonnullByDefault
public class BatfishParseException extends BatfishException {

  private final @Nonnull ErrorDetails _errorDetails;

  public BatfishParseException(String msg, @Nullable Throwable cause, ErrorDetails errorDetails) {
    super(msg, cause);
    _errorDetails = errorDetails;
  }

  public @Nonnull ErrorDetails getErrorDetails() {
    return _errorDetails;
  }
}
