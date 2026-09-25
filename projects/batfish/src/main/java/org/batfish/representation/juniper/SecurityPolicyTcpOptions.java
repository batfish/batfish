package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/** TCP session-handling options configured on a security policy. */
public final class SecurityPolicyTcpOptions implements Serializable {

  public @Nullable Integer getInitialTcpMss() {
    return _initialTcpMss;
  }

  public @Nullable Integer getReverseTcpMss() {
    return _reverseTcpMss;
  }

  public boolean getSequenceCheckRequired() {
    return _sequenceCheckRequired;
  }

  public boolean getSynCheckRequired() {
    return _synCheckRequired;
  }

  public boolean getWindowScale() {
    return _windowScale;
  }

  public void setInitialTcpMss(int initialTcpMss) {
    _initialTcpMss = initialTcpMss;
  }

  public void setReverseTcpMss(int reverseTcpMss) {
    _reverseTcpMss = reverseTcpMss;
  }

  public void setSequenceCheckRequired() {
    _sequenceCheckRequired = true;
  }

  public void setSynCheckRequired() {
    _synCheckRequired = true;
  }

  public void setWindowScale() {
    _windowScale = true;
  }

  private @Nullable Integer _initialTcpMss;
  private @Nullable Integer _reverseTcpMss;
  private boolean _sequenceCheckRequired;
  private boolean _synCheckRequired;
  private boolean _windowScale;
}
