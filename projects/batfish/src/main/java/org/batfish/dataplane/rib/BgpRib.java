package org.batfish.dataplane.rib;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;

/** BGP-specific RIB implementation */
@ParametersAreNonnullByDefault
public final class BgpRib extends BgpRibAbstract<Bgpv4Route> {

  public BgpRib(
      @Nullable Map<Prefix, SortedSet<Bgpv4Route>> backupRoutes,
      @Nullable Rib mainRib,
      BgpTieBreaker tieBreaker,
      @Nullable Integer maxPaths,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode) {
    super(backupRoutes, multipathEquivalentAsPathMatchMode, mainRib, tieBreaker, maxPaths);
    }
}
