package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Datamodel class representing configuration of a static route on A10 devices. */
public final class StaticRoute implements Serializable {
  public static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;

  @Nullable
  public String getDescription() {
    return _description;
  }

  @Nullable
  public Integer getDistance() {
    return _distance;
  }

  @Nonnull
  public Ip getForwardingRouterAddress() {
    return _forwardingRouterAddress;
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
