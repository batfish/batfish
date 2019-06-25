package org.batfish.dataplane.rib;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;

/**
 * An {@link AbstractRib} in which all routes are of type {@link AnnotatedRoute} to preserve
 * provenance information.
 *
 * @param <R> Type of {@link AbstractRoute} contained in the RIB's annotated routes.
 */
@ParametersAreNonnullByDefault
public abstract class AnnotatedRib<R extends AbstractRoute> extends AbstractRib<AnnotatedRoute<R>>
    implements Serializable {

  AnnotatedRib() {
    super();
  }

  /*
  TODO If an AnnotatedRoute is added to a RIB that already has an identical route with a different
   source VRF, the new route should not be installed or added to _backupRoutes. If the routes are
   not identical, the worse one should be relegated to _backupRoutes. Behavior TBD if routes are
   different but have the same preference.
   */

  @Override
  public abstract int comparePreference(AnnotatedRoute<R> lhs, AnnotatedRoute<R> rhs);

  @SuppressWarnings("unchecked") // Since R is a supertype, this cast should be fine
  public boolean containsRoute(AnnotatedRoute<? extends R> route) {
    return super.containsRoute((AnnotatedRoute<R>) route);
  }
}
