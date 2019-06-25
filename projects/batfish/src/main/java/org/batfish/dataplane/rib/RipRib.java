package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.RipRoute;
import org.batfish.datamodel.RoutingProtocol;

/**
 * A {@link Rib} for storing RIP (both internal and external) routes.
 *
 * <p>Note: external RIP routes not supported at this time
 */
@ParametersAreNonnullByDefault
public class RipRib extends AbstractRib<RipRoute> {

  public RipRib() {
    super();
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
        // Only RIP internal supported, and all routes have equal cost
        return 0;
      default:
        throw new BatfishException("Invalid RIP protocol: '" + protocol + "'");
    }
  }
}
