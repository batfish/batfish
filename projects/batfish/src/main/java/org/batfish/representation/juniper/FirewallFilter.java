package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** A firewall filter on Juniper */
public abstract class FirewallFilter implements Serializable {
  public final @Nonnull String getName() {
    return _name;
  }

  public abstract Family getFamily();

  public abstract boolean isUsedForFBF();

  // Private implementation details.

  protected FirewallFilter(@Nonnull String name) {
    _name = name;
  }

  private final @Nonnull String _name;
}
