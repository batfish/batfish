package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;

/** Loopback adapter */
public class Loopback implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull List<InterfaceAddress> _addresses;
  private @Nullable Ip _clagVxlanAnycastIp;
  private boolean _enabled;

  public Loopback() {
    _addresses = new LinkedList<>();
  }

  public @Nonnull List<InterfaceAddress> getAddresses() {
    return _addresses;
  }

  public @Nullable Ip getClagVxlanAnycastIp() {
    return _clagVxlanAnycastIp;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  public void setClagVxlanAnycastIp(@Nullable Ip clagVxlanAnycastIp) {
    _clagVxlanAnycastIp = clagVxlanAnycastIp;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }
}
