package org.batfish.datamodel;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A route or route builder with readable source protocol. */
@ParametersAreNonnullByDefault
public interface HasReadableSourceProtocol {

  @Nullable
  RoutingProtocol getSrcProtocol();
}
