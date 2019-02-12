package org.batfish.dataplane.rib;

import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Prefix;

/**
 * Represents a general RIB, capable of storing routes across different protocols. Uses
 * administrative cost (a.k.a admin distance) to determine route preference.
 */
public class Rib extends AbstractRib<AbstractRoute> implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Create a new empty RIB. */
  public Rib() {
    super(null, r -> r);
  }

  @Override
  public int comparePreference(@Nonnull AbstractRoute lhs, @Nonnull AbstractRoute rhs) {
    // Flipped rhs & lhs because lower values are preferable.
    return Comparator.comparing(AbstractRoute::getAdministrativeCost)
        .thenComparing(AbstractRoute::getMetric)
        .compare(rhs, lhs);
  }

  @Override
  @Nonnull
  public RibDelta<AbstractRoute> mergeRouteGetDelta(AbstractRoute route) {
    if (!route.getNonRouting()) {
      return super.mergeRouteGetDelta(route);
    } else {
      return RibDelta.empty();
    }
  }

  @Override
  public Prefix getNetwork(AbstractRoute route) {
    return route.getNetwork();
  }
}
