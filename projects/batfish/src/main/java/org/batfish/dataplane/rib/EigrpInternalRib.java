package org.batfish.dataplane.rib;

import java.util.Comparator;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.EigrpInternalRoute;

/** Rib that stores internal EIGRP routes */
@ParametersAreNonnullByDefault
public class EigrpInternalRib extends AbstractRib<EigrpInternalRoute> {

  public EigrpInternalRib() {
    super();
  }

  @Override
  public int comparePreference(EigrpInternalRoute lhs, EigrpInternalRoute rhs) {
    return Comparator.comparing(EigrpInternalRoute::getCompositeCost)
        // TODO compare MTU
        // https://github.com/batfish/batfish/issues/1946
        .thenComparing(EigrpInternalRoute::getProcessAsn)
        .compare(rhs, lhs);
  }
}
