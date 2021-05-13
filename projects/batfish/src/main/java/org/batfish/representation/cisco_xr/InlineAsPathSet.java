package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An expression for an as-path-set containing a literal definition of an as-path-set. */
@ParametersAreNonnullByDefault
public class InlineAsPathSet implements AsPathSetExpr {

  public InlineAsPathSet(AsPathSet asPathSet) {
    _asPathSet = asPathSet;
  }

  public @Nonnull AsPathSet getAsPathSet() {
    return _asPathSet;
  }

  private final @Nonnull AsPathSet _asPathSet;
}
