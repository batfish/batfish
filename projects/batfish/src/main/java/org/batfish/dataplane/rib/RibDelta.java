package org.batfish.dataplane.rib;

import com.google.common.annotations.VisibleForTesting;
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
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * Represents a change in RIB state
 *
 * @param <R> route type
 */
@ParametersAreNonnullByDefault
public final class RibDelta<R> {

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

  /** Builder for {@link RibDelta} */
  @ParametersAreNonnullByDefault
  public static final class Builder<R> {

    private Map<Prefix, LinkedHashMap<R, RouteAdvertisement<R>>> _actions;

    /** Initialize a new RibDelta builder */
    private Builder() {
      _actions = new LinkedHashMap<>();
    }

    /**
     * Indicate that a route was added to the RIB
     *
     * @param prefix {@link Prefix} representing destination network of route to add
     * @param route Route to add
     */
    public Builder<R> add(Prefix prefix, R route) {
      LinkedHashMap<R, RouteAdvertisement<R>> l =
          _actions.computeIfAbsent(prefix, p -> new LinkedHashMap<>(10, 1, true));
      l.put(route, new RouteAdvertisement<>(route));
      return this;
    }

    /**
     * Indicate that a route was removed from the RIB
     *
     * @param prefix {@link Prefix} representing destination network of route to remove
     * @param route that was removed
     */
    public Builder<R> remove(Prefix prefix, R route, Reason reason) {
      LinkedHashMap<R, RouteAdvertisement<R>> l =
          _actions.computeIfAbsent(prefix, p -> new LinkedHashMap<>(10, 1, true));
      l.put(route, RouteAdvertisement.<R>builder().setRoute(route).setReason(reason).build());
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

    /**
     * Process all added and removed routes from a collection of deltas
     *
     * @param prefixExtractor Function to extract destination network from routes of type {@link R}
     */
    public Builder<R> from(Collection<RibDelta<R>> deltas, Function<R, Prefix> prefixExtractor) {
      deltas.forEach(d -> from(d, prefixExtractor));
      return this;
    }

    /**
     * Process all added and removed routes from a given delta
     *
     * @param prefixExtractor Function to extract destination network from routes of type {@link R}
     */
    @Nonnull
    public <T extends R> Builder<R> from(RibDelta<T> delta, Function<R, Prefix> prefixExtractor) {
      for (RouteAdvertisement<T> a : delta.getActions()) {
        LinkedHashMap<R, RouteAdvertisement<R>> l =
            _actions.computeIfAbsent(
                prefixExtractor.apply(a.getRoute()), p -> new LinkedHashMap<>(10, 1, true));
        l.put(
            a.getRoute(),
            RouteAdvertisement.<R>builder()
                .setRoute(a.getRoute())
                .setReason(a.getReason())
                .build());
      }
      return this;
    }
  }

  @Nonnull
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /** Return an empty RIB delta */
  @Nonnull
  public static <T> RibDelta<T> empty() {
    return RibDelta.<T>builder().build();
  }

  /**
   * Apply a {@link RibDelta} to a given RIB
   *
   * @param importingRib the RIB to apply the delta to
   * @param delta the delta to apply
   * @param <U> Type of route in the RIB
   * @param <T> type of route in the delta (must be more specific than {@link U}
   * @return the {@link RibDelta} that results from modifying {@code importingRib}
   */
  @VisibleForTesting
  @Nonnull
  public static <U, T extends U> RibDelta<U> importRibDelta(
      @Nonnull AbstractRib<U> importingRib, @Nonnull RibDelta<T> delta) {
    if (delta.isEmpty()) {
      return empty();
    }
    Builder<U> builder = RibDelta.builder();
    delta
        .getActionMap()
        .forEach(
            (prefix, actions) ->
                actions.stream()
                    // TODO: uncomment after all route types properly implement equality
                    // .distinct()
                    .forEachOrdered(
                        tRouteAdvertisement -> {
                          if (tRouteAdvertisement.isWithdrawn()) {
                            builder.from(
                                importingRib.removeRouteGetDelta(
                                    tRouteAdvertisement.getRoute(),
                                    tRouteAdvertisement.getReason()),
                                importingRib::getNetwork);
                          } else {
                            builder.from(
                                importingRib.mergeRouteGetDelta(tRouteAdvertisement.getRoute()),
                                importingRib::getNetwork);
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
