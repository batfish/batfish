package org.batfish.dataplane.rib;

import java.util.Comparator;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.Prefix;

/** Rib that stores internal and external EIGRP routes */
public class EigrpRib extends AbstractRib<EigrpRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpRib() {
    super(null, r -> r);
  }

  private static int getTypeCost(EigrpRoute route) {
    switch (route.getProtocol()) {
      case EIGRP:
        return 0;
      case EIGRP_EX:
        return 1;
      default:
        throw new BatfishException("Invalid EIGRP protocol: '" + route.getProtocol() + "'");
    }
  }

  @Override
  public int comparePreference(EigrpRoute lhs, EigrpRoute rhs) {
    return Comparator.comparing(EigrpRib::getTypeCost)
        .thenComparing(EigrpRoute::getCompositeCost)
        /* Flipped rhs & lhs because lower values are more preferred. */
        .compare(rhs, lhs);
  }

  @Override
  public Prefix getNetwork(EigrpRoute route) {
    return route.getNetwork();
  }
}
