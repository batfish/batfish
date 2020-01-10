package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/** The VS structure for things that are ACL-like. */
public abstract class FirewallFilter implements Serializable {
  public final @Nonnull String getName() {
    return _name;
  }

  public abstract Family getFamily();

  public abstract boolean isUsedForFBF();

  public abstract Stream<String> getFromZones();

  // Private implementation details.

  protected FirewallFilter(@Nonnull String name) {
    _name = name;
  }

  private final @Nonnull String _name;
}
