package org.batfish.representation.juniper;

import java.io.Serializable;

public class PatPool implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  // null value means that no port is specified
  private Integer _fromPort;

  // null value means that no port is specified
  private Integer _toPort;

  // null value means that no port translation is specified
  private Boolean _portTranslation;

  public PatPool() {
    _fromPort = null;
    _toPort = null;
    _portTranslation = null;
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
