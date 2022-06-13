package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Juniper BGP add-path settings for a particular address-family and bgp hierarchy. Unset settings
 * for the same family are inherited from more general hierarchy levels.
 */
public final class AddPath implements Serializable {

  public boolean getReceive() {
    return _receive;
  }

  public void setReceive(boolean receive) {
    _receive = receive;
  }

  public @Nullable AddPathSend getSend() {
    return _send;
  }

  public @Nonnull AddPathSend getOrInitSend() {
    if (_send == null) {
      _send = new AddPathSend();
    }
    return _send;
  }

  private boolean _receive;
  private @Nullable AddPathSend _send;
}
