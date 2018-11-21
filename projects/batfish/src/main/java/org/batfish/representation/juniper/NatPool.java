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

  public NatPool() {
    _fromAddress = Prefix.ZERO.getStartIp();
    _toAddress = Prefix.ZERO.getEndIp();
  }

  @Nonnull
  public Ip getFromAddress() {
    return _fromAddress;
  }

  @Nonnull
  public Ip getToAddress() {
    return _toAddress;
  }

  public void setFromAddress(Ip fromAddress) {
    _fromAddress = fromAddress;
  }

  public void setToAddress(Ip toAddress) {
    _toAddress = toAddress;
  }
}
