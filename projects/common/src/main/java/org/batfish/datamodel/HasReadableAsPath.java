package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A route or route builder with readable as-path. */
@ParametersAreNonnullByDefault
public interface HasReadableAsPath {

  @Nonnull
  AsPath getAsPath();
}
