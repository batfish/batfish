package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Datamodel class representing configuration of a static route on A10 devices. */
public final class StaticRoute implements Serializable {
  public static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable Integer getDistance() {
    return _distance;
  }

  public @Nonnull Ip getForwardingRouterAddress() {
    return _forwardingRouterAddress;
  }

  public StaticRoute(
      Ip forwardingRouterAddress, @Nullable String description, @Nullable Integer distance) {
    _description = description;
    _distance = distance;
    _forwardingRouterAddress = forwardingRouterAddress;
  }

  private final @Nullable String _description;
  private final @Nullable Integer _distance;
  private final @Nonnull Ip _forwardingRouterAddress;
}
