package org.batfish.dataplane.rib;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
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

  private final List<RouteAdvertisement<R>> _actions;

  private static final RibDelta<AbstractRoute> EMPTY = new RibDelta<>(ImmutableList.of());

  private RibDelta(List<RouteAdvertisement<R>> actions) {
    _actions = ImmutableList.copyOf(actions);
  }

  /**
   * Create a new {@link RibDelta} advertising the input route with {@link Reason} {@link
   * Reason#ADD}.
   */
  public static <R extends AbstractRouteDecorator> RibDelta<R> adding(R route) {
    return new RibDelta<>(ImmutableList.of(RouteAdvertisement.adding(route)));
  }

  /**
   * Create a new {@link RibDelta} advertising the input routes with {@link Reason} {@link
   * Reason#ADD}.
   */
  public static <R extends AbstractRouteDecorator> RibDelta<R> adding(Collection<R> routes) {
    ImmutableList.Builder<RouteAdvertisement<R>> builder =
        ImmutableList.builderWithExpectedSize(routes.size());
    for (R route : routes) {
      builder.add(RouteAdvertisement.adding(route));
    }
    return new RibDelta<>(builder.build());
  }

  public static <R extends AbstractRouteDecorator> RibDelta<R> of(RouteAdvertisement<R> action) {
    return new RibDelta<>(ImmutableList.of(action));
  }

  public static <R extends AbstractRouteDecorator> RibDelta<R> of(
      Collection<RouteAdvertisement<R>> actions) {
    return new RibDelta<>(ImmutableList.copyOf(actions));
  }

  public static <R extends AbstractRouteDecorator> RibDelta<R> merge(
      @Nullable RibDelta<R> delta1, RibDelta<R> delta2) {
    if (delta1 == null || delta1.isEmpty()) {
      return delta2;
    }
    return RibDelta.<R>builder().from(delta1.getActions()).from(delta2.getActions()).build();
  }

  /**
   * Return the set of prefixes this delta has modifications for
   *
   * @return a set of {@link Prefix}
   */
  @Nonnull
  public Stream<Prefix> getPrefixes() {
    return _actions.stream().map(ra -> ra.getRoute().getNetwork()).distinct();
  }

  /** Return all the RIB actions that need to be applied (in order). */
  @Nonnull
  public Stream<RouteAdvertisement<R>> getActions() {
    return _actions.stream();
  }

  /** Check whether this delta is empty (has no outstanding actions) */
  public boolean isEmpty() {
    return _actions.isEmpty();
  }

  /**
   * Helper method: retrieves all routes affected by this delta.
   *
   * @return List of routes
   */
  @VisibleForTesting
  @Nonnull
  List<R> getRoutes() {
    return getRoutesStream().collect(ImmutableList.toImmutableList());
  }

  /** Helper method: retrieves all routes affected by this delta. */
  @Nonnull
  public Stream<R> getRoutesStream() {
    return _actions.stream().map(RouteAdvertisement::getRoute);
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
    exporter
        .getActions()
        .forEach(
            ra -> {
              AnnotatedRoute<T> tRoute = new AnnotatedRoute<>(ra.getRoute(), vrfName);
              if (ra.isWithdrawn()) {
                importer.remove(tRoute, ra.getReason());
              } else {
                importer.add(tRoute);
              }
            });
  }

  /** Builder for {@link RibDelta} */
  @ParametersAreNonnullByDefault
  public static final class Builder<R extends AbstractRouteDecorator> {

    @SuppressWarnings("PMD.LooseCoupling") // insertion order matters
    private LinkedHashMap<R, RouteAdvertisement<R>> _actions;

    /** Initialize a new RibDelta builder */
    private Builder() {
      // use a LinkedHashMap to preserve insertion order
      _actions = new LinkedHashMap<>();
    }

    /**
     * Indicate that a route was added to the RIB
     *
     * @param route Route that was added
     */
    public Builder<R> add(R route) {
      RouteAdvertisement<R> old = _actions.put(route, new RouteAdvertisement<>(route));
      if (old != null && old.isWithdrawn()) {
        // In this same delta, we withdrew the route and are now re-advertising. Should be no-op.
        _actions.remove(route);
      }
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
      RouteAdvertisement<R> old =
          _actions.put(
              route, RouteAdvertisement.<R>builder().setRoute(route).setReason(reason).build());
      if (old != null && old.getReason() == Reason.ADD) {
        // In this same delta, we added the route and are now withdrawing. Instead, no-op.
        _actions.remove(route);
      }
      return this;
    }

    /**
     * Indicate that multiple routes were removed from the RIB
     *
     * @param routes that were removed
     */
    public Builder<R> remove(Collection<R> routes, Reason reason) {
      for (R route : routes) {
        remove(route, reason);
      }
      return this;
    }

    public boolean isEmpty() {
      return _actions.isEmpty();
    }

    /**
     * Create a new RIB delta.
     *
     * @return A new {@link RibDelta}
     */
    @Nonnull
    public RibDelta<R> build() {
      if (isEmpty()) {
        return empty();
      }
      return new RibDelta<>(ImmutableList.copyOf(_actions.values()));
    }

    /** Process all added and removed routes from a given delta */
    @Nonnull
    public <T extends R> Builder<R> from(RibDelta<T> delta) {
      return from(delta.getActions());
    }

    /** Process all added and removed routes from a given delta */
    @Nonnull
    public <T extends R> Builder<R> from(Stream<RouteAdvertisement<T>> actions) {
      actions.forEach(this::from);
      return this;
    }

    @Nonnull
    public <T extends R> Builder<R> from(RouteAdvertisement<T> routeAdvertisement) {
      if (routeAdvertisement.isWithdrawn()) {
        remove(routeAdvertisement.getRoute(), routeAdvertisement.getReason());
      } else {
        add(routeAdvertisement.getRoute());
      }
      return this;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("actions", _actions).toString();
    }
  }

  @Nonnull
  public static <T extends AbstractRouteDecorator> Builder<T> builder() {
    return new Builder<>();
  }

  /** Return an empty RIB delta */
  @Nonnull
  @SuppressWarnings("unchecked") // Fully variant implementation, never stores any Ts
  public static <T extends AbstractRouteDecorator> RibDelta<T> empty() {
    return (RibDelta<T>) EMPTY;
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
    delta._actions.forEach(
        (uRouteAdvertisement) -> {
          T tRoute = converter.apply(uRouteAdvertisement.getRoute());
          if (uRouteAdvertisement.isWithdrawn()) {
            builder.from(importingRib.removeRouteGetDelta(tRoute, uRouteAdvertisement.getReason()));
          } else {
            builder.from(importingRib.mergeRouteGetDelta(tRoute));
          }
        });
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
    return Objects.hashCode(_actions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("actions", _actions).toString();
  }
}
