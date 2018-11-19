package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class NatPool implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _fromAddress;

  private Ip _toAddress;

  public Ip getFromAddress() {
    return _fromAddress;
  }

  public @Nullable Ip getToAddress() {
    return _toAddress;
  }

  public void setFromAddress(Ip fromAddress) {
    _fromAddress = fromAddress;
  }

  public void setToAddress(Ip toAddress) {
    _toAddress = toAddress;
  }
}
