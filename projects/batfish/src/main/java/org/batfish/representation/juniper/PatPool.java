package org.batfish.representation.juniper;

import java.io.Serializable;

public class PatPool implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  // null value means that no port is specified
  private Integer _fromPort;

  // null value means that no port is specified
  private Integer _toPort;

  // 1. null value means no PAT related configuration is given. when the value is null, source nat
  // will apply PAT and dest nat will not apply PAT by default
  // 2. true means that a specific PAT command is found in the configuration that requires PAT
  // 3. no means that a specific PAT command is found in the configuration that requires no PAT
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
