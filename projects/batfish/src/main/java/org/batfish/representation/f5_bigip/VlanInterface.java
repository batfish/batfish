package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A member interface of a {@link Vlan}. */
@ParametersAreNonnullByDefault
public final class VlanInterface implements Serializable {

  private final @Nonnull String _name;

  public VlanInterface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
