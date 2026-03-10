package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A route or route builder with readable weight. */
@ParametersAreNonnullByDefault
public interface HasReadableWeight {

  @Nonnull
  int getWeight();
}
