package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** A virtual routing and forwarding instance. */
public final class Vrf implements Serializable {

  private final @Nonnull String _name;
  private boolean _shutdown;

  public Vrf(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }
}
