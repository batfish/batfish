package org.batfish.dataplane.rib;

import java.util.Comparator;
import java.util.TreeMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.EigrpExternalRoute;

/** Rib that stores external EIGRP routes */
public class EigrpExternalRib extends AbstractRib<EigrpExternalRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpExternalRib() {
    super(new TreeMap<>());
  }

  @Override
  public int comparePreference(EigrpExternalRoute lhs, EigrpExternalRoute rhs) {
    return Comparator.comparing(EigrpExternalRoute::getCompositeCost)
        .thenComparing(EigrpExternalRoute::getDestinationAsn)
        .compare(rhs, lhs);
  }

  @Override
  public AbstractRoute getAbstractRoute(EigrpExternalRoute route) {
    return route;
  }
}
