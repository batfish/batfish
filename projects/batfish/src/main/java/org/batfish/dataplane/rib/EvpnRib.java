package org.batfish.dataplane.rib;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;

/** RIB implementation for all types of EVPN routes */
@ParametersAreNonnullByDefault
public final class EvpnRib<R extends EvpnRoute<?, ?>> extends BgpRib<R> {

  public EvpnRib(
      @Nullable Rib mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean clusterListAsIgpCost) {
    super(
        mainRib,
        tieBreaker,
        maxPaths,
        multipathEquivalentAsPathMatchMode,
        true,
        clusterListAsIgpCost);
  }
}
