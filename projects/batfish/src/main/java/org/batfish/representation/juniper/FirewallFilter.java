package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nonnull;

/** The VS structure for things that are ACL-like. */
public abstract class FirewallFilter implements Serializable {
  public final @Nonnull String getName() {
    return _name;
  }

  public abstract Family getFamily();

  public abstract boolean isUsedForFBF();

  public abstract Optional<String> getFromZone();

  // Private implementation details.

  protected FirewallFilter(@Nonnull String name) {
    _name = name;
  }

  private final @Nonnull String _name;
}
