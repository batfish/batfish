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
  private final boolean _withdraw;
  @Nonnull private final Reason _reason;

  private transient volatile int _hashcode = 0;

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
   * Create a new route advertisement, optionally allowing this to be a withdrawal advertisement
   *
   * @param route route that is advertised
   * @param withdraw whether the route is being withdrawn
   */
  @VisibleForTesting
  RouteAdvertisement(T route, boolean withdraw, Reason reason) {
    _route = route;
    _withdraw = withdraw;
    _reason = reason;
  }

  /**
   * Shortcut to create a new route advertisement
   *
   * @param route route that is advertised
   */
  public RouteAdvertisement(T route) {
    _route = route;
    _withdraw = false;
    _reason = Reason.ADD;
  }

  /** Get the underlying route that's being advertised (or withdrawn) */
  @Nonnull
  public T getRoute() {
    return _route;
  }

  @Nonnull
  public Reason getReason() {
    return _reason;
  }

  /**
   * Check if this route is being withdrawn
   *
   * @return true if the route is being withdrawn
   */
  public boolean isWithdrawn() {
    return _withdraw;
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
    return _withdraw == that._withdraw
        && Objects.equals(_route, that._route)
        && _reason == that._reason;
  }

  @Override
  public int hashCode() {
    if (_hashcode == 0) {
      _hashcode = Objects.hash(_route, _withdraw, _reason);
    }
    return _hashcode;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("route", _route)
        .add("withdraw", _withdraw)
        .add("reason", _reason)
        .toString();
  }

  @Nonnull
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  @Nonnull
  public Builder<T> toBuilder() {
    return RouteAdvertisement.<T>builder()
        .setRoute(_route)
        .setWithdraw(_withdraw)
        .setReason(_reason);
  }

  /** Builder for {@link RouteAdvertisement} */
  public static final class Builder<T> {
    private T _route;
    private boolean _withdraw;
    private Reason _reason = Reason.ADD;

    private Builder() {}

    public Builder<T> setRoute(T route) {
      _route = route;
      return this;
    }

    public Builder<T> setWithdraw(boolean withdraw) {
      _withdraw = withdraw;
      return this;
    }

    public Builder<T> setReason(Reason reason) {
      _reason = reason;
      return this;
    }

    @Nonnull
    public RouteAdvertisement<T> build() {
      checkArgument(_route != null, "Route advertisement missing the route");
      checkArgument(
          !_withdraw || _reason != Reason.ADD,
          "Route advertisement is missing a withdrawal reason");
      checkArgument(
          _withdraw || (_reason != Reason.WITHDRAW && _reason != Reason.REPLACE),
          "Route advertisement has invalid reason %s",
          _reason);
      return new RouteAdvertisement<>(_route, _withdraw, _reason);
    }
  }
}
