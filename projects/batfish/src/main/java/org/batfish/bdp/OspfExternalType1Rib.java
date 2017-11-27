package org.batfish.bdp;

import org.batfish.datamodel.OspfExternalType1Route;

public class OspfExternalType1Rib extends AbstractRib<OspfExternalType1Route> {

  /** */
  private static final long serialVersionUID = 1L;

  public OspfExternalType1Rib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(OspfExternalType1Route lhs, OspfExternalType1Route rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }

  @Override
  public boolean mergeRoute(OspfExternalType1Route route) {
    String advertiser = route.getAdvertiser();
    if (!route.getNonRouting() && _owner._c.getHostname().equals(advertiser)) {
      return false;
    } else {
      return super.mergeRoute(route);
    }
  }
}
