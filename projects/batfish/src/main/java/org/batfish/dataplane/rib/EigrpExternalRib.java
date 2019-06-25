package org.batfish.dataplane.rib;

import java.util.Comparator;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.EigrpExternalRoute;

/** Rib that stores external EIGRP routes */
@ParametersAreNonnullByDefault
public class EigrpExternalRib extends AbstractRib<EigrpExternalRoute> {

  public EigrpExternalRib() {
    super(true);
  }

  @Override
  public int comparePreference(EigrpExternalRoute lhs, EigrpExternalRoute rhs) {
    return Comparator.comparing(EigrpExternalRoute::getCompositeCost)
        .thenComparing(EigrpExternalRoute::getDestinationAsn)
        .compare(rhs, lhs);
  }
}
