package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** Configuration for a logging-server. */
public final class LoggingServer implements Serializable {

  public LoggingServer(String host) {
    _host = host;
  }

  public String getHost() {
    return _host;
  }

  private final @Nonnull String _host;
}
