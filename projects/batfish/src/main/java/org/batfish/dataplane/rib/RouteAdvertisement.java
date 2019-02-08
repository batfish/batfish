package org.batfish.dataplane.rib;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

/**
 * A wrapper around a route object representing a route advertisement action (send/withdraw) and the
 * reason for the action
 */
@ParametersAreNonnullByDefault
public final class RouteAdvertisement<T> {
  @Nonnull private final T _route;
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
    switch (_reason) {
      case WITHDRAW:
      case REPLACE:
        return true;
      case ADD:
        return false;
      default:
        throw new BatfishException(
            String.format("Unrecognized RouteAdvertisement reason: %s", _reason));
    }
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
    return Objects.equals(_route, that._route) && _reason == that._reason;
  }

  @Override
  public int hashCode() {
    if (_hashcode == 0) {
      _hashcode = Objects.hash(_route, _reason);
    }
    return _hashcode;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("route", _route).add("reason", _reason).toString();
  }

  @Nonnull
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  @Nonnull
  public Builder<T> toBuilder() {
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

    @Nonnull
    public RouteAdvertisement<T> build() {
      checkArgument(_route != null, "Route advertisement missing the route");
      return new RouteAdvertisement<>(_route, _reason);
    }
  }
}
