package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Decrement HSRP priority when track fails. */
@ParametersAreNonnullByDefault
public final class HsrpDecrementPriority implements HsrpTrackAction {
  public HsrpDecrementPriority(@Nullable Integer decrement) {
    _decrement = decrement;
  }

  public @Nullable Integer getDecrement() {
    return _decrement;
  }

  private final @Nullable Integer _decrement;
}
