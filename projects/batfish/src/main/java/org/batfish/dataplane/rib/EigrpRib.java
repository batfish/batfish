package org.batfish.dataplane.rib;

import java.util.Comparator;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.RoutingProtocol;

// TOOD javadoc
public class EigrpRib extends AbstractRib<EigrpRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpRib() {
    super(null);
  }

  @Override
  public int comparePreference(EigrpRoute lhs, EigrpRoute rhs) {
    // Flipped rhs & lhs because lower values are more preferred.
    return Comparator.comparing(EigrpRoute::getAdministrativeCost)
        .thenComparing(EigrpRib::getTypeCost)
        .thenComparing(EigrpRoute::getMetric)
        .thenComparing(EigrpRoute::getAsNumber)
        .compare(rhs, lhs);
  }

  private static int getTypeCost(EigrpRoute route) {
    RoutingProtocol protocol = route.getProtocol();
    switch (protocol) {
      case EIGRP:
        return 0;
      case EIGRP_EX:
        return 1;
      default:
        throw new BatfishException("Invalid EIGRP protocol: '" + protocol + "'");
    }
  }
}
