package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents an NX-OS NVE (Network Virtual Interface) [sic] - Logical interface where the
 * encapsulation and de-encapsulation occur.
 * https://www.cisco.com/c/en/us/support/docs/switches/nexus-9000-series-switches/118978-config-vxlan-00.html#anc4
 */
@ParametersAreNonnullByDefault
public final class Nve implements Serializable {
  public Nve(int id) {
    _id = id;
    _shutdown = true;
  }

  public int getId() {
    return _id;
  }

  public boolean isShutdown() {
    return _shutdown;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  public void setSourceInterface(@Nullable String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private boolean _shutdown;
  private @Nullable String _sourceInterface;
  private final int _id;
}
