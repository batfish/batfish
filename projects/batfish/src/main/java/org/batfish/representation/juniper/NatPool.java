package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** Represents Juniper nat pools */
@ParametersAreNonnullByDefault
public final class NatPool implements Serializable {

  private static final long serialVersionUID = 1L;

  private Ip _fromAddress;

  private Ip _toAddress;

  private Integer _fromPort;

  private Integer _toPort;

  private boolean _portTranslation;

  public NatPool() {
    _fromAddress = Prefix.ZERO.getStartIp();
    _toAddress = Prefix.ZERO.getEndIp();
    _fromPort = null;
    _toPort = null;
    _portTranslation = false;
  }

  @Nonnull
  public Ip getFromAddress() {
    return _fromAddress;
  }

  @Nonnull
  public Ip getToAddress() {
    return _toAddress;
  }

  public Integer getFromPort() {
    return _fromPort;
  }

  public Integer getToPort() {
    return _toPort;
  }

  public boolean getPortTranslation() {
    return _portTranslation;
  }

  public void setFromAddress(Ip fromAddress) {
    _fromAddress = fromAddress;
  }

  public void setToAddress(Ip toAddress) {
    _toAddress = toAddress;
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
