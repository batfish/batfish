package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.Prefix;

public class NatRule implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private NatRuleThen _then;

  public void setDestinationAddress(Prefix prefix) {}

  public void setDestinationAddressName(String name) {}

  public void setDestinationPortFrom(int port) {}

  public void setDestinationPortTo(int toPort) {}

  public void setSourceAddress(Prefix prefix) {}

  public void setSourceAddressName(String name) {}

  public void setSourcePortFrom(int fromPort) {}

  public void setSourcePortTo(int toPort) {}

  public void setThen(NatRuleThen then) {
    _then = then;
  }
}
