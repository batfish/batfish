package org.batfish.dataplane.rib;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;

/** Internal RIB implementation for all types of EVPN routes for a given route distinguisher. */
@ParametersAreNonnullByDefault
final class EvpnRib<R extends EvpnRoute<?, ?>> extends BgpRib<R> {

  public EvpnRib(
      BgpTieBreaker tieBreaker,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean clusterListAsIgpCost,
      LocalOriginationTypeTieBreaker localOriginationTypeTieBreaker) {
    super(
        null,
        tieBreaker,
        1,
        multipathEquivalentAsPathMatchMode,
        true,
        clusterListAsIgpCost,
        localOriginationTypeTieBreaker);
  }
}
