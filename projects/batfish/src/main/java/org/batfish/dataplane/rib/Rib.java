package org.batfish.dataplane.rib;

import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.HasAbstractRoute;

/**
 * Represents a general RIB, capable of storing routes across different protocols. Uses
 * administrative cost (a.k.a admin distance) to determine route preference.
 */
@ParametersAreNonnullByDefault
public class Rib extends AnnotatedRib<AbstractRoute> implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Create a new empty RIB. */
  public Rib(String vrfName) {
    super(null, vrfName);
  }

  @Override
  public int comparePreference(
      @Nonnull AnnotatedRoute<AbstractRoute> lhs, @Nonnull AnnotatedRoute<AbstractRoute> rhs) {
    // Flipped rhs & lhs because lower values are preferable.
    return Comparator.comparing(AbstractRoute::getAdministrativeCost)
        .thenComparing(AbstractRoute::getMetric)
        .compare(rhs.getRoute(), lhs.getRoute());
  }

  /** Overrides {@link AnnotatedRib#mergeRouteGetDelta(AbstractRoute)} */
  @Override
  @Nonnull
  public <T extends AbstractRoute> RibDelta<AnnotatedRoute<AbstractRoute>> mergeRouteGetDelta(
      T route) {
    return !route.getNonRouting() ? super.mergeRouteGetDelta(route) : RibDelta.empty();
  }

  /** Overrides {@link AbstractRib#mergeRouteGetDelta(HasAbstractRoute)} */
  @Override
  @Nonnull
  public RibDelta<AnnotatedRoute<AbstractRoute>> mergeRouteGetDelta(
      AnnotatedRoute<AbstractRoute> route) {
    return !route.getRoute().getNonRouting() ? super.mergeRouteGetDelta(route) : RibDelta.empty();
  }
}
