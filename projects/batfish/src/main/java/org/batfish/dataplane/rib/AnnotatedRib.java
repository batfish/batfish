package org.batfish.dataplane.rib;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.ibdp.VirtualRouter;

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

  private final String _vrfName;

  AnnotatedRib(@Nullable Map<Prefix, SortedSet<AnnotatedRoute<R>>> backupRoutes, String vrfName) {
    super(backupRoutes);
    _vrfName = vrfName;
  }

  /** Name of {@link VirtualRouter} that owns this RIB */
  @Nonnull
  public String getVrfName() {
    return _vrfName;
  }

  /**
   * Add a new (unannotated) {@link AbstractRoute} to this (annotated) RIB.
   *
   * @param route the route to add
   * @return {@link RibDelta} with the route if it was added, or empty if the route already existed
   *     or was discarded due to preference comparisons.
   */
  @Nonnull
  public <T extends R> RibDelta<AnnotatedRoute<R>> mergeRouteGetDelta(T route) {
    /*
    TODO If an AnnotatedRoute is added to a RIB that already has an identical route with a different
     source VRF, the new route should not be installed or added to _backupRoutes. If the routes are
     not identical, the worse one should be relegated to _backupRoutes. Behavior TBD if routes are
     different but have the same preference.
     */
    return mergeRouteGetDelta(new AnnotatedRoute<>(route, _vrfName));
  }

  /**
   * Add a new (unannotated) {@link AbstractRoute} to this (annotated) RIB.
   *
   * @param route the route to add
   * @return true if the route was added. False if the route already existed or was discarded due to
   *     preference comparisons.
   */
  public <T extends R> boolean mergeRoute(T route) {
    return !mergeRouteGetDelta(route).isEmpty();
  }

  @Override
  public abstract int comparePreference(AnnotatedRoute<R> lhs, AnnotatedRoute<R> rhs);
}
