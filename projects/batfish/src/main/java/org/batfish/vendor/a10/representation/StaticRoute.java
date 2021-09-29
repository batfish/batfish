package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Datamodel class representing configuration of a static route on A10 devices. */
public final class StaticRoute implements Serializable {
  @Nullable
  public String getDescription() {
    return _description;
  }

  @Nullable
  public Integer getDistance() {
    return _distance;
  }

  public Ip getForwardingRouterAddress() {
    return _forwardingRouterAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute that = (StaticRoute) o;
    return Objects.equals(_description, that._description)
        && Objects.equals(_distance, that._distance)
        && _forwardingRouterAddress.equals(that._forwardingRouterAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _description, _forwardingRouterAddress);
  }

  public StaticRoute(
      Ip forwardingRouterAddress, @Nullable String description, @Nullable Integer distance) {
    _description = description;
    _distance = distance;
    _forwardingRouterAddress = forwardingRouterAddress;
  }

  @Nullable private final String _description;
  @Nullable private final Integer _distance;
  @Nonnull private final Ip _forwardingRouterAddress;
}
