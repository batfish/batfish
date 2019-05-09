package org.batfish.dataplane.rib;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;

/** RIB implementation for all types of EVPN routes */
@ParametersAreNonnullByDefault
public final class EvpnRib<R extends EvpnRoute> extends BgpRib<R> {

  private static final long serialVersionUID = 1L;

  public EvpnRib(
      @Nullable Rib mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode) {
    super(mainRib, tieBreaker, maxPaths, multipathEquivalentAsPathMatchMode, true);
  }
}
