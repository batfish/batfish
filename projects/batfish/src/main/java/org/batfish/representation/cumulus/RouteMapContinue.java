package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Construct for continue action in FRR route map */
public class RouteMapContinue implements Serializable {
  // null means continue to the next entry; a nonnull value means go to that entry
  private final @Nullable Integer _next;

  public RouteMapContinue(@Nullable Integer next) {
    _next = next;
  }

  public @Nullable Integer getNext() {
    return _next;
  }
}
