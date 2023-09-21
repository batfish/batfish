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

  private Ip _fromAddress;

  private Ip _toAddress;

  private @Nullable String _owner;

  // null value means no pat is configured; source nat will apply pat and dest nat will not apply
  // pat by default
  private @Nullable PortAddressTranslation _pat;

  public NatPool() {
    _fromAddress = Prefix.ZERO.getStartIp();
    _toAddress = Prefix.ZERO.getEndIp();
    _pat = null;
    _owner = null;
  }

  public @Nonnull Ip getFromAddress() {
    return _fromAddress;
  }

  public @Nonnull Ip getToAddress() {
    return _toAddress;
  }

  public @Nullable String getOwner() {
    return _owner;
  }

  public @Nullable PortAddressTranslation getPortAddressTranslation() {
    return _pat;
  }

  public void setFromAddress(Ip fromAddress) {
    _fromAddress = fromAddress;
  }

  public void setToAddress(Ip toAddress) {
    _toAddress = toAddress;
  }

  public void setPortAddressTranslation(PortAddressTranslation pat) {
    _pat = pat;
  }

  public void setOwner(String routingInstance) {
    _owner = routingInstance;
  }
}
