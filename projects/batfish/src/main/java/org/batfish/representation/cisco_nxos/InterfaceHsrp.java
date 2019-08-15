package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** HSRP settings for an {@link Interface}. */
public final class InterfaceHsrp implements Serializable {

  public @Nullable Integer getDelayReloadSeconds() {
    return _delayReloadSeconds;
  }

  public void setDelayReloadSeconds(@Nullable Integer delayReloadSeconds) {
    _delayReloadSeconds = delayReloadSeconds;
  }

  public @Nonnull Map<Integer, HsrpGroupIpv4> getIpv4Groups() {
    return _ipv4Groups;
  }

  public @Nullable Integer getVersion() {
    return _version;
  }

  public void setVersion(@Nullable Integer version) {
    _version = version;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  InterfaceHsrp() {
    _ipv4Groups = new HashMap<>();
  }

  private @Nullable Integer _delayReloadSeconds;
  private final @Nonnull Map<Integer, HsrpGroupIpv4> _ipv4Groups;
  private @Nullable Integer _version;
}
