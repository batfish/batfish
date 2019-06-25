package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a pool of snat-translations. */
@ParametersAreNonnullByDefault
public final class SnatPool implements Serializable {

  private final @Nonnull Set<String> _members;

  private final @Nonnull String _name;

  public SnatPool(String name) {
    _name = name;
    _members = new HashSet<>();
  }

  public @Nonnull Set<String> getMembers() {
    return _members;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
