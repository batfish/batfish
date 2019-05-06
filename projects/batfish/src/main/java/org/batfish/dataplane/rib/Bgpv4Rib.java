package org.batfish.dataplane.rib;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;

/** BGPv4-specific RIB implementation */
@ParametersAreNonnullByDefault
public final class Bgpv4Rib extends BgpRib<Bgpv4Route> {

  private static final long serialVersionUID = 1L;

  public Bgpv4Rib(
      @Nullable Map<Prefix, SortedSet<Bgpv4Route>> backupRoutes,
      @Nullable Rib mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode) {
    super(backupRoutes, mainRib, tieBreaker, maxPaths, multipathEquivalentAsPathMatchMode);
  }
}
