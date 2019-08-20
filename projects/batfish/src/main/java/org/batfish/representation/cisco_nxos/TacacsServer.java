package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** Configuration for a TACACS server. */
public final class TacacsServer implements Serializable {

  public TacacsServer(String host) {
    _host = host;
  }

  public @Nonnull String getHost() {
    return _host;
  }

  private final @Nonnull String _host;
}
