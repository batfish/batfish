package org.batfish.dataplane.rib;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.Prefix;

public class OspfExternalType1Rib extends AbstractRib<OspfExternalType1Route> {

  private static final long serialVersionUID = 1L;

  private final String _hostname;

  public OspfExternalType1Rib(
      @Nonnull String hostname,
      @Nullable Map<Prefix, SortedSet<OspfExternalType1Route>> backupRoutes) {
    super(backupRoutes);
    _hostname = hostname;
  }

  @Override
  public int comparePreference(OspfExternalType1Route lhs, OspfExternalType1Route rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }

  @Override
  @Nullable
  public RibDelta<OspfExternalType1Route> mergeRouteGetDelta(OspfExternalType1Route route) {
    String advertiser = route.getAdvertiser();
    if (!route.getNonRouting() && _hostname.equals(advertiser)) {
      return null;
    } else {
      return super.mergeRouteGetDelta(route);
    }
  }
}
