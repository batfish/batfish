package org.batfish.dataplane.rib;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;

/** RIB implementation for all types of EVPN routes */
@ParametersAreNonnullByDefault
public final class EvpnRib extends BgpRib<EvpnRoute> {

  private static final long serialVersionUID = 1L;

  public EvpnRib(
      @Nullable Map<Prefix, SortedSet<EvpnRoute>> backupRoutes,
      @Nullable Rib mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode) {
    super(backupRoutes, mainRib, tieBreaker, maxPaths, multipathEquivalentAsPathMatchMode);
  }
}
