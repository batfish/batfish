package org.batfish.dataplane.rib;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.OspfExternalType2Route;

@ParametersAreNonnullByDefault
public class OspfExternalType2Rib extends AbstractRib<OspfExternalType2Route> {

  private final String _hostname;

  public OspfExternalType2Rib(String hostname) {
    super(true);
    _hostname = hostname;
  }

  @Override
  public int comparePreference(OspfExternalType2Route lhs, OspfExternalType2Route rhs) {
    // reversed on purpose
    int costComparison = Long.compare(rhs.getMetric(), lhs.getMetric());
    if (costComparison != 0) {
      return costComparison;
    }
    return Long.compare(rhs.getCostToAdvertiser(), lhs.getCostToAdvertiser());
  }

  @Override
  @Nonnull
  public RibDelta<OspfExternalType2Route> mergeRouteGetDelta(OspfExternalType2Route route) {
    String advertiser = route.getAdvertiser();
    if (route.getCostToAdvertiser() != 0 && _hostname.equals(advertiser)) {
      return RibDelta.empty();
    } else {
      return super.mergeRouteGetDelta(route);
    }
  }
}
