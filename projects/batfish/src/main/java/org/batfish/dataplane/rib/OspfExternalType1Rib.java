package org.batfish.dataplane.rib;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.OspfExternalType1Route;

@ParametersAreNonnullByDefault
public class OspfExternalType1Rib extends AbstractRib<OspfExternalType1Route> {

  private final String _hostname;

  public OspfExternalType1Rib(String hostname) {
    super(true);
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
}
