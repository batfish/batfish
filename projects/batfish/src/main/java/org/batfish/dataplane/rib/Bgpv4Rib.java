package org.batfish.dataplane.rib;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;

/** BGPv4-specific RIB implementation */
@ParametersAreNonnullByDefault
public final class Bgpv4Rib extends BgpRib<Bgpv4Route> {

  private static final long serialVersionUID = 1L;

  public Bgpv4Rib(
      @Nullable Rib mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean withBackups) {
    super(mainRib, tieBreaker, maxPaths, multipathEquivalentAsPathMatchMode, withBackups);
  }
}
