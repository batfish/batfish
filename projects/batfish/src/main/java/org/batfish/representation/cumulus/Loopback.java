package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;

/** Loopback adapter */
public class Loopback implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull List<ConcreteInterfaceAddress> _addresses;
  private @Nullable Ip _clagVxlanAnycastIp;
  private boolean _configured;

  public Loopback() {
    _addresses = new LinkedList<>();
  }

  public @Nonnull List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }

  public @Nullable Ip getClagVxlanAnycastIp() {
    return _clagVxlanAnycastIp;
  }

  public boolean getConfigured() {
    return _configured;
  }

  public void setClagVxlanAnycastIp(@Nullable Ip clagVxlanAnycastIp) {
    _clagVxlanAnycastIp = clagVxlanAnycastIp;
  }

  public void setConfigured(boolean configured) {
    _configured = configured;
  }
}
