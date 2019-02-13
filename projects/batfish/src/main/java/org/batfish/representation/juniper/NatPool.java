package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** Represents Juniper nat pools */
@ParametersAreNonnullByDefault
public final class NatPool implements Serializable {

  private static final long serialVersionUID = 1L;

  private Ip _fromAddress;

  private Ip _toAddress;

  // null value means no pat is configured
  @Nullable private PatPool _patPool;

  public NatPool() {
    _fromAddress = Prefix.ZERO.getStartIp();
    _toAddress = Prefix.ZERO.getEndIp();
    _patPool = null;
  }

  @Nonnull
  public Ip getFromAddress() {
    return _fromAddress;
  }

  @Nonnull
  public Ip getToAddress() {
    return _toAddress;
  }

  @Nullable
  public PatPool getPatPool() {
    return _patPool;
  }

  public void setFromAddress(Ip fromAddress) {
    _fromAddress = fromAddress;
  }

  public void setToAddress(Ip toAddress) {
    _toAddress = toAddress;
  }

  public void setPatPool(PatPool patPool) {
    _patPool = patPool;
  }
}
