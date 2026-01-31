package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** Configuration for an snmp-server. */
public final class SnmpServer implements Serializable {

  public SnmpServer(String host) {
    _host = host;
  }

  public String getHost() {
    return _host;
  }

  private final @Nonnull String _host;
}
