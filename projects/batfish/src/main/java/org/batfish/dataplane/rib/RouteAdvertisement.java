package org.batfish.dataplane.rib;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A wrapper around a route object representing a route advertisement action (send/withdraw) and the
 * reason for the action
 */
@ParametersAreNonnullByDefault
public final class RouteAdvertisement<T> {
  private final @Nonnull T _route;
  private final @Nonnull Reason _reason;

  private transient int _hashCode = 0;

  /** Reason for the advertisement */
  public enum Reason {
    /** The route was added */
    ADD,
    /** The route was replaced by a better route */
    REPLACE,
    /** The route was removed */
    WITHDRAW
  }

  /**
   * Create a new route advertisement
   *
   * @param route route object that is advertised
   * @param reason The {@link Reason} indicating if the route was added or withdrawn and why
   */
  @VisibleForTesting
  RouteAdvertisement(T route, Reason reason) {
    _route = route;
    _reason = reason;
  }

  /**
   * Shortcut to create a new route advertisement
   *
   * @param route route that is advertised
   */
  public RouteAdvertisement(T route) {
    _route = route;
    _reason = Reason.ADD;
  }

  public static <T> RouteAdvertisement<T> adding(T route) {
    return new RouteAdvertisement<>(route, Reason.ADD);
  }

  public static <T> RouteAdvertisement<T> replacing(T route) {
    return new RouteAdvertisement<>(route, Reason.REPLACE);
  }

  public static <T> RouteAdvertisement<T> withdrawing(T route) {
    return new RouteAdvertisement<>(route, Reason.WITHDRAW);
  }

  /** Get the underlying route that's being advertised (or withdrawn) */
  public @Nonnull T getRoute() {
    return _route;
  }

  public @Nonnull Reason getReason() {
    return _reason;
  }

  /**
   * Check if this route is being withdrawn
   *
   * @return true if the route is being withdrawn
   */
  public boolean isWithdrawn() {
    return switch (_reason) {
      case WITHDRAW, REPLACE -> true;
      case ADD -> false;
    };
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
        && _reason == that._reason;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = 31 * _route.hashCode() + _reason.ordinal();
      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("route", _route).add("reason", _reason).toString();
  }

  public static @Nonnull <T> Builder<T> builder() {
    return new Builder<>();
  }

  public @Nonnull Builder<T> toBuilder() {
    return RouteAdvertisement.<T>builder().setRoute(_route).setReason(_reason);
  }

  /** Builder for {@link RouteAdvertisement} */
  public static final class Builder<T> {
    private T _route;
    private Reason _reason = Reason.ADD;

    private Builder() {}

    public Builder<T> setRoute(T route) {
      _route = route;
      return this;
    }

    public Builder<T> setReason(Reason reason) {
      _reason = reason;
      return this;
    }

    public @Nonnull RouteAdvertisement<T> build() {
      checkArgument(_route != null, "Route advertisement missing the route");
      return new RouteAdvertisement<>(_route, _reason);
    }
  }

  /**
   * Returns a version of this route advertisement with REPLACE changed to WITHDRAW. Replace is a
   * local operation but always looks like withdraw to neighbors.
   */
  public RouteAdvertisement<T> sanitizeForExport() {
    if (_reason != Reason.REPLACE) {
      return this;
    }
    return new RouteAdvertisement<T>(_route, Reason.WITHDRAW);
  }
}
