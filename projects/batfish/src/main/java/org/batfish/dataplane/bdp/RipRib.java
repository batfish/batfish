package org.batfish.dataplane.bdp;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.RipRoute;
import org.batfish.datamodel.RoutingProtocol;

public class RipRib extends AbstractRib<RipRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public RipRib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(RipRoute lhs, RipRoute rhs) {
    int lhsTypeCost = getTypeCost(lhs.getProtocol());
    int rhsTypeCost = getTypeCost(rhs.getProtocol());
    return Integer.compare(rhsTypeCost, lhsTypeCost);
  }

  private static int getTypeCost(RoutingProtocol protocol) {
    switch (protocol) {
      case RIP:
        return 0;
        // $CASES-OMITTED$
      default:
        throw new BatfishException("Invalid rip protocol: '" + protocol + "'");
    }
  }
}
