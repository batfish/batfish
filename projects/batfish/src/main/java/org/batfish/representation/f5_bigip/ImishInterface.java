package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** IMISH-specific configuration for an interface of any type. */
public final class ImishInterface implements Serializable {

  public ImishInterface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable OspfInterface getOspf() {
    return _ospf;
  }

  public @Nonnull OspfInterface getOrCreateOspf() {
    if (_ospf == null) {
      _ospf = new OspfInterface();
    }
    return _ospf;
  }

  private final @Nonnull String _name;
  private @Nullable OspfInterface _ospf;
}
