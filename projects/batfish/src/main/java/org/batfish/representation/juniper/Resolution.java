package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Resolution settings for a routing instance. */
@ParametersAreNonnullByDefault
public final class Resolution implements Serializable {

  @Nonnull
  public ResolutionRib getOrReplaceRib(String name) {
    if (_rib == null || !_rib.getName().equals(name)) {
      _rib = new ResolutionRib(name);
    }
    return _rib;
  }

  @Nullable
  public ResolutionRib getRib() {
    return _rib;
  }

  @Nullable private ResolutionRib _rib;
}
