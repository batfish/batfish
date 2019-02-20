package org.batfish.dataplane.rib;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Prefix;

/**
 * An {@link AbstractRib} in which all routes are of type {@link AnnotatedRoute} to preserve
 * provenance information.
 *
 * @param <R> Type of {@link AbstractRoute} contained in the RIB's annotated routes.
 */
@ParametersAreNonnullByDefault
public abstract class AnnotatedRib<R extends AbstractRoute> extends AbstractRib<AnnotatedRoute<R>>
    implements Serializable {

  private static final long serialVersionUID = 1L;

  AnnotatedRib(@Nullable Map<Prefix, SortedSet<AnnotatedRoute<R>>> backupRoutes) {
    super(backupRoutes);
  }

  /*
  TODO If an AnnotatedRoute is added to a RIB that already has an identical route with a different
   source VRF, the new route should not be installed or added to _backupRoutes. If the routes are
   not identical, the worse one should be relegated to _backupRoutes. Behavior TBD if routes are
   different but have the same preference.
   */

  @Override
  public abstract int comparePreference(AnnotatedRoute<R> lhs, AnnotatedRoute<R> rhs);
}
