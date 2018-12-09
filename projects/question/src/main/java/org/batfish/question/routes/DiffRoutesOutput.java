package org.batfish.question.routes;

import java.util.List;
import javax.annotation.Nonnull;

public class DiffRoutesOutput {

  public enum KeyPresence {
    ONLY_IN_SNAPSHOT,
    ONLY_IN_REFERENCE,
    IN_BOTH;
  }

  @Nonnull private RouteRowKey _routeRowKey;

  @Nonnull private List<List<RouteRowAttribute>> _diffInAttributes;

  @Nonnull private KeyPresence _keyPresenceStatus;

  public DiffRoutesOutput(
      @Nonnull RouteRowKey routeRowKey,
      @Nonnull List<List<RouteRowAttribute>> diffInAttributes,
      @Nonnull KeyPresence keyPresenceStatus) {
    _routeRowKey = routeRowKey;
    _diffInAttributes = diffInAttributes;
    _keyPresenceStatus = keyPresenceStatus;
  }

  @Nonnull
  public RouteRowKey getRouteRowKey() {
    return _routeRowKey;
  }

  @Nonnull
  public List<List<RouteRowAttribute>> getDiffInAttributes() {
    return _diffInAttributes;
  }

  @Nonnull
  public KeyPresence getKeyPresenceStatus() {
    return _keyPresenceStatus;
  }
}
