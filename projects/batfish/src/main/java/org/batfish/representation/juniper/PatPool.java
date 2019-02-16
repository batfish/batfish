package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Objects;

public class PatPool implements PortAddressTranslation, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private int _fromPort;

  private int _toPort;

  public PatPool(int fromPort, int toPort) {
    _fromPort = fromPort;
    _toPort = toPort;
  }

  public Integer getFromPort() {
    return _fromPort;
  }

  public Integer getToPort() {
    return _toPort;
  }

  public void setFromPort(int fromPort) {
    _fromPort = fromPort;
  }

  public void setToPort(int toPort) {
    _toPort = toPort;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof PatPool)) {
      return false;
    }
    PatPool other = (PatPool) o;
    return _fromPort == other.getFromPort() && _toPort == other.getToPort();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fromPort, _toPort);
  }
}
