package org.batfish.vendor.a10.representation;

import java.io.Serializable;

/** Datamodel class representing an interface's LLDP configuration. */
public class InterfaceLldp implements Serializable {

  public boolean getEnableRx() {
    return _enableRx;
  }

  public void setEnableRx(boolean enableRx) {
    _enableRx = enableRx;
  }

  public boolean getEnableTx() {
    return _enableTx;
  }

  public void setEnableTx(boolean enableTx) {
    _enableTx = enableTx;
  }

  private boolean _enableRx;
  private boolean _enableTx;
}
