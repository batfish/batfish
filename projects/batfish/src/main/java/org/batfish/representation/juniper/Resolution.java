package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Resolution settings for a routing instance. */
@ParametersAreNonnullByDefault
public final class Resolution implements Serializable {

  public @Nonnull ResolutionRib getOrReplaceRib(String name) {
    if (_rib == null || !_rib.getName().equals(name)) {
      _rib = new ResolutionRib(name);
    }
    return _rib;
  }

  public @Nullable ResolutionRib getRib() {
    return _rib;
  }

  private @Nullable ResolutionRib _rib;
}
