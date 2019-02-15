package org.batfish.representation.juniper;

import java.io.Serializable;

public class PatPool implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  // -1 means that no port is specified; so we need to apply a default value
  private int _fromPort;

  // -1 means that no port is specified; so we need to apply a default value
  private int _toPort;

  // 1. true means that we need to apply PAT
  // 2. no means that we should not apply PAT
  private boolean _portTranslation;

  public PatPool() {
    _fromPort = -1;
    _toPort = -1;
    _portTranslation = false;
  }

  public Integer getFromPort() {
    return _fromPort;
  }

  public Integer getToPort() {
    return _toPort;
  }

  public Boolean getPortTranslation() {
    return _portTranslation;
  }

  public void setFromPort(int fromPort) {
    _fromPort = fromPort;
  }

  public void setToPort(int toPort) {
    _toPort = toPort;
  }

  public void setPortTranslation(boolean portTranslation) {
    _portTranslation = portTranslation;
  }
}
