package org.batfish.dataplane.rib;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * Represents a general RIB, capable of storing routes across different protocols. Uses
 * administrative cost (a.k.a admin distance) to determine route preference.
 */
@ParametersAreNonnullByDefault
public class Rib extends AnnotatedRib<AbstractRoute> implements Serializable {

  private final class OwnNextHopManager {

    private final @Nonnull SetMultimap<Ip, AnnotatedRoute<AbstractRoute>> _routesByNextHopIp;

    private OwnNextHopManager() {
      _routesByNextHopIp = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
    }

    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> mergeRouteGetDelta(
        AnnotatedRoute<AbstractRoute> route) {
      if (containsOwnNextHop(route)) {
        // invariant: containsOwnNextHop(route) guarantees route has next hop IP
        _routesByNextHopIp.put(route.getAbstractRoute().getNextHopIp(), route);
        if (!canActivate(route)) {
          // If there is no existing more-specific LPM for the nextHopIp, this route cannot be
          // merged.
          return RibDelta.empty();
        }
      }
      return processSideEffects(Rib.super.mergeRouteGetDelta(route));
    }

    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> removeRouteGetDelta(
        AnnotatedRoute<AbstractRoute> route) {
      if (containsOwnNextHop(route)) {
        // invariant: containsOwnNextHop(route) guarantees route has next hop IP
        _routesByNextHopIp.remove(route.getAbstractRoute().getNextHopIp(), route);
      }
      return processSideEffects(Rib.super.removeRouteGetDelta(route, Reason.WITHDRAW));
    }

    /** Returns true iff a route whose network contains its own next hop IP can be activated. */
    private boolean canActivate(AnnotatedRoute<AbstractRoute> ownNextHopRoute) {
      int prefixLength = ownNextHopRoute.getNetwork().getPrefixLength();
      return longestPrefixMatch(
              ownNextHopRoute.getRoute().getNextHopIp(), ResolutionRestriction.alwaysTrue())
          .stream()
          .anyMatch(lpmRoute -> lpmRoute.getNetwork().getPrefixLength() > prefixLength);
    }

    /**
     * Process the side-effects of applied changes to the RIB. Returns all changes to the RIB,
     * including the initial ones.
     */
    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> processSideEffects(
        RibDelta<AnnotatedRoute<AbstractRoute>> changes) {
      RibDelta<AnnotatedRoute<AbstractRoute>> currentSideEffects = changes;
      RibDelta.Builder<AnnotatedRoute<AbstractRoute>> cumulativeResult =
          RibDelta.<AnnotatedRoute<AbstractRoute>>builder().from(currentSideEffects);
      // loop invariant: currentSideEffects have been applied to the RIB
      while (!currentSideEffects.isEmpty()) {
        currentSideEffects = applyDeltaGetDelta(getSideEffects(currentSideEffects));
        cumulativeResult.from(currentSideEffects);
      }
      return cumulativeResult.build();
    }

    /**
     * Returns the side-effects of an applied delta to the RIB. Affected prefixes are the networks
     * of the routes in the delta. Affected next hop IPs are those contained in any affected prefix.
     * Affected routes are those with an affected next hop IP, and whose network contains their own
     * next hop IP. For each such route, if there is no more specific route that resolves its next
     * hop IP, a withdrawal is returned. Otherwise, an add is returned.
     */
    private @Nonnull RibDelta<AnnotatedRoute<AbstractRoute>> getSideEffects(
        RibDelta<AnnotatedRoute<AbstractRoute>> delta) {
      RibDelta.Builder<AnnotatedRoute<AbstractRoute>> sideEffects = RibDelta.builder();
      delta
          .getPrefixes()
          .forEach(
              affectedPrefix ->
                  _routesByNextHopIp.keySet().stream()
                      .filter(affectedPrefix::containsIp)
                      .forEach(
                          affectedNextHopIp -> {
                            Optional<Integer> maybeLpmPrefixLength =
                                longestPrefixMatch(
                                        affectedNextHopIp, ResolutionRestriction.alwaysTrue())
                                    .stream()
                                    .findFirst()
                                    .map(lpmRoute -> lpmRoute.getNetwork().getPrefixLength());
                            if (!maybeLpmPrefixLength.isPresent()) {
                              _routesByNextHopIp
                                  .get(affectedNextHopIp)
                                  .forEach(
                                      affectedRoute ->
                                          sideEffects.remove(affectedRoute, Reason.WITHDRAW));
                            } else {
                              int lpmPrefixLength = maybeLpmPrefixLength.get();
                              _routesByNextHopIp
                                  .get(affectedNextHopIp)
                                  .forEach(
                                      affectedRoute -> {
                                        if (affectedRoute.getNetwork().getPrefixLength()
                                            < lpmPrefixLength) {
                                          sideEffects.add(affectedRoute);
                                        } else {
                                          sideEffects.remove(affectedRoute, Reason.WITHDRAW);
                                        }
                                      });
                            }
                          }));
      return sideEffects.build();
    }
  }

  private static boolean containsOwnNextHop(AnnotatedRoute<AbstractRoute> route) {
    Prefix network = route.getNetwork();
    NextHop nextHop = route.getAbstractRoute().getNextHop();
    if (!(nextHop instanceof NextHopIp)) {
      // no next-hop resolution, so stop here
      return false;
    }
    Ip nextHopIp = ((NextHopIp) nextHop).getIp();
    return network.containsIp(nextHopIp);
  }

  /** Create a new empty RIB. */
  public Rib() {
    this(false);
  }

  public Rib(boolean noInstallOwnNextHop) {
    _ownNextHopManager = noInstallOwnNextHop ? new OwnNextHopManager() : null;
  }

  @Override
  public int comparePreference(
      @Nonnull AnnotatedRoute<AbstractRoute> lhs, @Nonnull AnnotatedRoute<AbstractRoute> rhs) {
    // Flipped rhs & lhs because lower values are preferable.
    return Comparator.comparing(AbstractRoute::getAdministrativeCost)
        .thenComparing(AbstractRoute::getMetric)
        .compare(rhs.getRoute(), lhs.getRoute());
  }

  @Override
  @Nonnull
  public RibDelta<AnnotatedRoute<AbstractRoute>> mergeRouteGetDelta(
      AnnotatedRoute<AbstractRoute> route) {
    return !route.getRoute().getNonRouting()
        ? _ownNextHopManager != null
            ? _ownNextHopManager.mergeRouteGetDelta(route)
            : super.mergeRouteGetDelta(route)
        : RibDelta.empty();
  }

  @Nonnull
  @Override
  public RibDelta<AnnotatedRoute<AbstractRoute>> removeRouteGetDelta(
      AnnotatedRoute<AbstractRoute> route) {
    return !route.getRoute().getNonRouting()
        ? _ownNextHopManager != null
            ? _ownNextHopManager.removeRouteGetDelta(route)
            : super.removeRouteGetDelta(route)
        : RibDelta.empty();
  }

  private final @Nullable OwnNextHopManager _ownNextHopManager;
}
