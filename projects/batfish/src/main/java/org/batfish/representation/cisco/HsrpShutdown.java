package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Stop participating in HSRP when track fails. */
@ParametersAreNonnullByDefault
public final class HsrpShutdown implements HsrpTrackAction {

  public static @Nonnull HsrpShutdown instance() {
    return INSTANCE;
  }

  private static final HsrpShutdown INSTANCE = new HsrpShutdown();

  private HsrpShutdown() {}
}
