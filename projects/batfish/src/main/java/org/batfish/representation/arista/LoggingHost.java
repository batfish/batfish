package org.batfish.representation.arista;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class LoggingHost implements Serializable {
  private final @Nonnull String _host;

  public LoggingHost(String host) {
    _host = host;
  }

  public @Nonnull String getHost() {
    return _host;
  }
}
