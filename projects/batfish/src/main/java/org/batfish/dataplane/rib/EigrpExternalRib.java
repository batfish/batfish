package org.batfish.dataplane.rib;

import java.util.Comparator;
import java.util.TreeMap;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.Prefix;

/** Rib that stores external EIGRP routes */
public class EigrpExternalRib extends AbstractRib<EigrpExternalRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpExternalRib() {
    super(new TreeMap<>(), r -> r);
  }

  @Override
  public int comparePreference(EigrpExternalRoute lhs, EigrpExternalRoute rhs) {
    return Comparator.comparing(EigrpExternalRoute::getCompositeCost)
        .thenComparing(EigrpExternalRoute::getDestinationAsn)
        .compare(rhs, lhs);
  }

  @Override
  public Prefix getNetwork(EigrpExternalRoute route) {
    return route.getNetwork();
  }
}
