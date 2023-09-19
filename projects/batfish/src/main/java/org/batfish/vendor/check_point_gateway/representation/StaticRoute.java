package org.batfish.vendor.check_point_gateway.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** Data model class containing configuration of a Check Point gateway static-route. */
public class StaticRoute implements Serializable {

  public StaticRoute(Prefix destination) {
    _destination = destination;
    _nexthops = new HashMap<>();
  }

  public @Nullable String getComment() {
    return _comment;
  }

  public @Nonnull Prefix getDestination() {
    return _destination;
  }

  /**
   * nexthop target -> nexthop configuration
   *
   * <p>Only one nexthop configuration exists per nexthop target.
   */
  public @Nonnull Map<NexthopTarget, Nexthop> getNexthops() {
    return _nexthops;
  }

  public void setComment(String comment) {
    _comment = comment;
  }

  private @Nullable String _comment;
  private final @Nonnull Prefix _destination;
  private final @Nonnull Map<NexthopTarget, Nexthop> _nexthops;
}
