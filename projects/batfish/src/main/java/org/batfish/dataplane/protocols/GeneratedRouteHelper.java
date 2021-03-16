package org.batfish.dataplane.protocols;

import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/**
 * Contains helper logic for manipulating {@link GeneratedRoute} objects in the context of dataplane
 * computation.
 */
public class GeneratedRouteHelper {

  /**
   * Check if a given generated route should be activated. If yes, return an appropriate {@link
   * GeneratedRoute.Builder}. Otherwise return {@code null}.
   *
   * @param generatedRoute a {@link GeneratedRoute} that should be run though a routing policy
   * @param policy a {@link RoutingPolicy} that imposes additional constraints on route activation.
   *     If {@code null}, then route will be considered ready for activation.
   * @param contributingRoutes A set of routes that can contribute trigger route's activation.
   *     Usually a set of routes from the Main RIB.
   * @return A generated route builder, or {@code null} if the route should not be activated.
   */
  @Nullable
  public static GeneratedRoute.Builder activateGeneratedRoute(
      GeneratedRoute generatedRoute,
      @Nullable RoutingPolicy policy,
      Set<AnnotatedRoute<AbstractRoute>> contributingRoutes) {
    if (policy == null) {
      // no contributor is needed if there is no policy
      return generatedRoute.toBuilder();
    }
    // Find first matching route among candidates
    for (AnnotatedRoute<AbstractRoute> contributingCandidate : contributingRoutes) {
      GeneratedRoute.Builder grb = generatedRoute.toBuilder();
      if (policy.process(contributingCandidate, grb, Direction.OUT)) {
        if (!generatedRoute.getDiscard()) {
          grb.setNextHopIp(contributingCandidate.getAbstractRoute().getNextHopIp());
        }
        return grb;
      }
    }
    return null;
  }
}
