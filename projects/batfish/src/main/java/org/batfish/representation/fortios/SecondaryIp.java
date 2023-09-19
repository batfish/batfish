package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** FortiOS datamodel component containing secondary IP configuration */
public final class SecondaryIp implements Serializable {
  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable ConcreteInterfaceAddress getIp() {
    return _ip;
  }

  public void setIp(ConcreteInterfaceAddress ip) {
    _ip = ip;
  }

  public SecondaryIp(String name) {
    _name = name;
  }

  private final @Nonnull String _name;
  private @Nullable ConcreteInterfaceAddress _ip;
}
