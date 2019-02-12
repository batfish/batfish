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
    super(backupRoutes, r -> r);
    _hostname = hostname;
  }

  @Override
  public int comparePreference(OspfExternalType1Route lhs, OspfExternalType1Route rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }

  @Override
  @Nonnull
  public RibDelta<OspfExternalType1Route> mergeRouteGetDelta(OspfExternalType1Route route) {
    String advertiser = route.getAdvertiser();
    if (!route.getNonRouting() && _hostname.equals(advertiser)) {
      return RibDelta.empty();
    } else {
      return super.mergeRouteGetDelta(route);
    }
  }

  @Override
  public Prefix getNetwork(OspfExternalType1Route route) {
    return route.getNetwork();
  }
}
