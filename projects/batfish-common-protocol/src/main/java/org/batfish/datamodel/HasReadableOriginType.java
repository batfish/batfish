package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A route or route builder with readable origin type. */
@ParametersAreNonnullByDefault
public interface HasReadableOriginType {

  @Nonnull
  OriginType getOriginType();
}
