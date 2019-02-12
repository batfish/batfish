package org.batfish.dataplane.rib;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.OspfRoute;
import org.batfish.datamodel.RoutingProtocol;

public class OspfRib extends AbstractRib<OspfRoute> {

  private static final long serialVersionUID = 1L;

  public OspfRib() {
    super(null);
  }

  @Override
  public int comparePreference(OspfRoute lhs, OspfRoute rhs) {
    int lhsTypeCost = getTypeCost(lhs.getProtocol());
    int rhsTypeCost = getTypeCost(rhs.getProtocol());
    return Integer.compare(rhsTypeCost, lhsTypeCost);
  }

  private static int getTypeCost(RoutingProtocol protocol) {
    switch (protocol) {
      case OSPF:
        return 0;
      case OSPF_E1:
        return 2;
      case OSPF_E2:
        return 3;
      case OSPF_IA:
        return 1;
        // $CASES-OMITTED$
      default:
        throw new BatfishException("Invalid OSPF protocol: '" + protocol + "'");
    }
  }

  @Override
  public AbstractRoute getAbstractRoute(OspfRoute route) {
    return route;
  }
}
