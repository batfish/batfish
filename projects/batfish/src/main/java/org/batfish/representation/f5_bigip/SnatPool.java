package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a pool of nodes. */
@ParametersAreNonnullByDefault
public final class SnatPool implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull String _name;

  public SnatPool(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
