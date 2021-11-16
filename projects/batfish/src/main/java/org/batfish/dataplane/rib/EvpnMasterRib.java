package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.bgp.RouteDistinguisher;

/**
 * RIB implementation for all types of EVPN routes. Chooses best paths independently for each pair
 * of prefix and route distinguisher.
 */
@ParametersAreNonnullByDefault
public final class EvpnMasterRib<R extends EvpnRoute<?, ?>> {

  public EvpnMasterRib(
      BgpTieBreaker tieBreaker,
      @Nullable MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean clusterListAsIgpCost) {
    _tieBreaker = tieBreaker;
    _multipathEquivalentAsPathMatchMode = multipathEquivalentAsPathMatchMode;
    _clusterListAsIgpCost = clusterListAsIgpCost;
    _ribsByRd = new HashMap<>();
  }

  public @Nonnull RibDelta<R> mergeRouteGetDelta(R route) {
    return getOrCreateRib(route.getRouteDistinguisher()).mergeRouteGetDelta(route);
  }

  public @Nonnull Set<R> getTypedRoutes() {
    return _ribsByRd.values().stream()
        .flatMap(rib -> rib.getTypedRoutes().stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  public @Nonnull RibDelta<R> removeRouteGetDelta(R route) {
    return getOrCreateRib(route.getRouteDistinguisher()).removeRouteGetDelta(route);
  }

  public @Nonnull Set<R> getTypedBackupRoutes() {
    return _ribsByRd.values().stream()
        .flatMap(rib -> rib.getTypedBackupRoutes().stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  private final boolean _clusterListAsIgpCost;
  private final @Nullable MultipathEquivalentAsPathMatchMode _multipathEquivalentAsPathMatchMode;
  private final @Nonnull Map<RouteDistinguisher, EvpnRib<R>> _ribsByRd;
  private final @Nonnull BgpTieBreaker _tieBreaker;

  private @Nonnull EvpnRib<R> getOrCreateRib(RouteDistinguisher rd) {
    return _ribsByRd.computeIfAbsent(
        rd,
        unused ->
            new EvpnRib<>(_tieBreaker, _multipathEquivalentAsPathMatchMode, _clusterListAsIgpCost));
  }
}
