package org.batfish.dataplane.rib;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A wrapper around a route object representing a route advertisement action (send/withdraw) and the
 * reason for the action
 */
@ParametersAreNonnullByDefault
public final class RouteAdvertisement<T> {
  @Nonnull private final T _route;
  private final boolean _withdrawn;

  private transient int _hashCode = 0;

  /**
   * Create a new route advertisement
   *
   * @param route route object that is advertised
   * @param withdrawn Whether the advertisement withdraws the route (if false, it adds the route)
   */
  @VisibleForTesting
  RouteAdvertisement(T route, boolean withdrawn) {
    _route = route;
    _withdrawn = withdrawn;
  }

  /**
   * Shortcut to create a new route advertisement
   *
   * @param route route that is advertised
   */
  public RouteAdvertisement(T route) {
    _route = route;
    _withdrawn = false;
  }

  public static <T> RouteAdvertisement<T> adding(T route) {
    return new RouteAdvertisement<>(route, false);
  }

  public static <T> RouteAdvertisement<T> withdrawing(T route) {
    return new RouteAdvertisement<>(route, true);
  }

  /** Get the underlying route that's being advertised (or withdrawn) */
  @Nonnull
  public T getRoute() {
    return _route;
  }

  /**
   * Check if this route is being withdrawn
   *
   * @return true if the route is being withdrawn
   */
  public boolean isWithdrawn() {
    return _withdrawn;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RouteAdvertisement)) {
      return false;
    }
    RouteAdvertisement<?> that = (RouteAdvertisement<?>) o;
    return (_hashCode == that._hashCode || _hashCode == 0 || that._hashCode == 0)
        && _route.equals(that._route)
        && _withdrawn == that._withdrawn;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_route, _withdrawn);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("route", _route)
        .add("withdrawn", _withdrawn)
        .toString();
  }

  @Nonnull
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  @Nonnull
  public Builder<T> toBuilder() {
    return RouteAdvertisement.<T>builder().setRoute(_route).setWithdrawn(_withdrawn);
  }

  /** Builder for {@link RouteAdvertisement} */
  public static final class Builder<T> {
    private T _route;
    private boolean _withdrawn = false;

    private Builder() {}

    public Builder<T> setRoute(T route) {
      _route = route;
      return this;
    }

    public Builder<T> setWithdrawn(boolean withdrawn) {
      _withdrawn = withdrawn;
      return this;
    }

    @Nonnull
    public RouteAdvertisement<T> build() {
      checkArgument(_route != null, "Route advertisement missing the route");
      return new RouteAdvertisement<>(_route, _withdrawn);
    }
  }
}
