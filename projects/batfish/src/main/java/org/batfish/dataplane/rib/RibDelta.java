package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * Represents a change in RIB state
 *
 * @param <R> route type
 */
@ParametersAreNonnullByDefault
public final class RibDelta<R extends AbstractRouteDecorator> {

  /** Sorted for deterministic iteration order */
  private SortedMap<Prefix, List<RouteAdvertisement<R>>> _actions;

  private RibDelta(Map<Prefix, List<RouteAdvertisement<R>>> actions) {
    _actions = ImmutableSortedMap.copyOf(actions);
  }

  /**
   * Return all the RIB actions that need to be applied (in order)
   *
   * @param p a particular {@link Prefix} to retrieve the actions for. If {@code null}, all actions
   *     are returned
   * @return a list of {@link RouteAdvertisement}
   */
  @Nonnull
  private List<RouteAdvertisement<R>> getActions(@Nullable Prefix p) {
    if (p == null) {
      return _actions.values().stream()
          .flatMap(List::stream)
          .collect(ImmutableList.toImmutableList());
    }
    return _actions.getOrDefault(p, ImmutableList.of());
  }

  /**
   * Return the set of prefixes this delta has modifications for
   *
   * @return a set of {@link Prefix}
   */
  @Nonnull
  public Set<Prefix> getPrefixes() {
    return _actions.keySet();
  }

  /**
   * Return all the RIB actions that need to be applied (in order)
   *
   * @return a list of {@link RouteAdvertisement}
   */
  @Nonnull
  public List<RouteAdvertisement<R>> getActions() {
    return getActions(null);
  }

  private Map<Prefix, List<RouteAdvertisement<R>>> getActionMap() {
    return _actions;
  }

  /** Check whether this delta is empty (has no outstanding actions) */
  public boolean isEmpty() {
    return _actions.values().stream().allMatch(List::isEmpty);
  }

  /**
   * Helper method: retrieves all routes affected by this delta.
   *
   * @return List of routes
   */
  @Nonnull
  public List<R> getRoutes() {
    return _actions.values().stream()
        .flatMap(List::stream)
        .map(RouteAdvertisement::getRoute)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Copy annotated versions of all routes advertisements in {@code exporter} to {@code importer}
   *
   * @param importer {@link RibDelta.Builder} to which to copy {@code exporter} route adverts
   * @param exporter {@link RibDelta} from which to copy route adverts
   * @param vrfName Name of VRF with which to annotate copied routes
   */
  public static <T extends AbstractRoute, U extends T> void importDeltaToBuilder(
      RibDelta.Builder<AnnotatedRoute<T>> importer, RibDelta<U> exporter, String vrfName) {
    for (RouteAdvertisement<U> ra : exporter.getActions()) {
      AnnotatedRoute<T> tRoute = new AnnotatedRoute<>(ra.getRoute(), vrfName);
      if (ra.isWithdrawn()) {
        importer.remove(tRoute, ra.getReason());
      } else {
        importer.add(tRoute);
      }
    }
  }

  /** Builder for {@link RibDelta} */
  @ParametersAreNonnullByDefault
  public static final class Builder<R extends AbstractRouteDecorator> {

    private Map<Prefix, LinkedHashMap<R, RouteAdvertisement<R>>> _actions;

    /** Initialize a new RibDelta builder */
    private Builder() {
      _actions = new LinkedHashMap<>();
    }

    /**
     * Indicate that a route was added to the RIB
     *
     * @param route Route that was added
     */
    public Builder<R> add(R route) {
      LinkedHashMap<R, RouteAdvertisement<R>> l =
          _actions.computeIfAbsent(route.getNetwork(), p -> new LinkedHashMap<>(10, 1, true));
      l.put(route, new RouteAdvertisement<>(route));
      return this;
    }

    /**
     * Indicate that multiple routes have been added to the RIB
     *
     * @param routes a collection of routes
     */
    public Builder<R> add(Collection<? extends R> routes) {
      routes.forEach(this::add);
      return this;
    }

    /**
     * Indicate that a route was removed from the RIB
     *
     * @param route that was removed
     */
    public Builder<R> remove(R route, Reason reason) {
      LinkedHashMap<R, RouteAdvertisement<R>> l =
          _actions.computeIfAbsent(route.getNetwork(), p -> new LinkedHashMap<>(10, 1, true));
      l.put(route, RouteAdvertisement.<R>builder().setRoute(route).setReason(reason).build());
      return this;
    }

    /**
     * Indicate that multiple routes were removed from the RIB
     *
     * @param routes that were removed
     */
    public Builder<R> remove(Collection<R> routes, Reason reason) {
      for (R route : routes) {
        LinkedHashMap<R, RouteAdvertisement<R>> l =
            _actions.computeIfAbsent(route.getNetwork(), p -> new LinkedHashMap<>(10, 1, true));
        l.put(route, RouteAdvertisement.<R>builder().setRoute(route).setReason(reason).build());
      }
      return this;
    }

    /**
     * Create a new RIB delta.
     *
     * @return A new {@link RibDelta}
     */
    @Nonnull
    public RibDelta<R> build() {
      return new RibDelta<>(
          _actions.entrySet().stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      Entry::getKey,
                      e ->
                          e.getValue().values().stream()
                              // TODO: uncomment after all route types properly implement equality
                              // .distinct()
                              .collect(ImmutableList.toImmutableList()))));
    }

    /** Process all added and removed routes from a given delta */
    @Nonnull
    public <T extends R> Builder<R> from(RibDelta<T> delta) {
      for (RouteAdvertisement<T> ra : delta.getActions()) {
        if (ra.isWithdrawn()) {
          remove(ra.getRoute(), ra.getReason());
        } else {
          add(ra.getRoute());
        }
      }
      return this;
    }
  }

  @Nonnull
  public static <T extends AbstractRouteDecorator> Builder<T> builder() {
    return new Builder<>();
  }

  /** Return an empty RIB delta */
  @Nonnull
  public static <T extends AbstractRouteDecorator> RibDelta<T> empty() {
    return RibDelta.<T>builder().build();
  }

  /**
   * Apply an annotated {@link RibDelta} to a given annotated RIB
   *
   * @param importingRib the {@link AnnotatedRib} to apply the delta to
   * @param delta the delta to apply
   * @param <T> Type of {@link AbstractRoute} in the RIB
   * @param <U> type of {@link AbstractRoute} in the delta; must extend {@code T}
   * @return the {@link RibDelta} that results from modifying {@code importingRib}
   */
  @Nonnull
  public static <T extends AbstractRoute, U extends T> RibDelta<AnnotatedRoute<T>> importRibDelta(
      AnnotatedRib<T> importingRib, RibDelta<AnnotatedRoute<U>> delta) {
    return importRibDelta(
        importingRib, delta, ar -> new AnnotatedRoute<>(ar.getRoute(), ar.getSourceVrf()));
  }

  /**
   * Apply an unannotated {@link RibDelta} to a given unannotated RIB
   *
   * @param importingRib the {@link AbstractRoute} to apply the delta to
   * @param delta the delta to apply
   * @param <T> Type of {@link AbstractRoute} in the RIB
   * @param <U> type of {@link AbstractRoute} in the delta; must extend {@code T}
   * @return the {@link RibDelta} that results from modifying {@code importingRib}
   */
  @Nonnull
  public static <T extends AbstractRoute, U extends T> RibDelta<T> importRibDelta(
      AbstractRib<T> importingRib, RibDelta<U> delta) {
    return importRibDelta(importingRib, delta, Function.identity());
  }

  /**
   * Apply a {@link RibDelta} with unannotated routes to a given annotated RIB
   *
   * @param importingRib the {@link AnnotatedRib} to apply the delta to
   * @param delta the delta to apply
   * @param vrfName name of source VRF to put in {@code importingRib} route annotations
   * @param <T> Type of {@link AbstractRoute} in the RIB
   * @param <U> type of {@link AbstractRoute} in the delta; must extend {@code T}
   * @return the {@link RibDelta} that results from modifying {@code importingRib}
   */
  @Nonnull
  public static <T extends AbstractRoute, U extends T> RibDelta<AnnotatedRoute<T>> importRibDelta(
      AnnotatedRib<T> importingRib, RibDelta<U> delta, String vrfName) {
    return importRibDelta(importingRib, delta, r -> new AnnotatedRoute<>(r, vrfName));
  }

  /**
   * Apply a {@link RibDelta} to a given RIB
   *
   * @param importingRib the {@link AnnotatedRib} to apply the delta to
   * @param delta the delta to apply
   * @param converter Function to convert RibDelta type {@code U} to importer route type {@code T}
   * @param <T> Type of {@link AbstractRoute} in the RIB
   * @param <U> type of {@link AbstractRoute} in the delta; does not need to extend {@code T}
   * @param <RibT> Type of the RIB
   * @return the {@link RibDelta} that results from modifying {@code importingRib}
   */
  @Nonnull
  private static <
          T extends AbstractRouteDecorator,
          U extends AbstractRouteDecorator,
          RibT extends AbstractRib<T>>
      RibDelta<T> importRibDelta(
          RibT importingRib, RibDelta<U> delta, Function<? super U, ? extends T> converter) {
    if (delta.isEmpty()) {
      return empty();
    }
    Builder<T> builder = RibDelta.builder();
    delta
        .getActionMap()
        .forEach(
            (prefix, actions) ->
                actions.stream()
                    // TODO: uncomment after all route types properly implement equality
                    // .distinct()
                    .forEachOrdered(
                        uRouteAdvertisement -> {
                          T tRoute = converter.apply(uRouteAdvertisement.getRoute());
                          if (uRouteAdvertisement.isWithdrawn()) {
                            builder.from(
                                importingRib.removeRouteGetDelta(
                                    tRoute, uRouteAdvertisement.getReason()));
                          } else {
                            builder.from(importingRib.mergeRouteGetDelta(tRoute));
                          }
                        }));
    return builder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RibDelta<?>)) {
      return false;
    }
    RibDelta<?> ribDelta = (RibDelta<?>) o;
    return Objects.equals(_actions, ribDelta._actions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_actions);
  }
}
