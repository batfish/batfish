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

  private final @Nonnull List<ConcreteInterfaceAddress> _addresses;

  // TODO: push this to VI model after concatenated conversion is in master
  private @Nullable String _alias;

  private @Nullable Ip _clagVxlanAnycastIp;
  private boolean _configured;
  private @Nullable Double _bandwidth;

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

  public void setBandwidth(@Nullable Double bw) {
    _bandwidth = bw;
  }

  @Nullable
  public Double getBandwidth() {
    return _bandwidth;
  }

  @Nullable
  public String getAlias() {
    return _alias;
  }

  public void setAlias(@Nullable String alias) {
    _alias = alias;
  }
}
