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

  @Nullable
  public String getComment() {
    return _comment;
  }

  @Nonnull
  public Prefix getDestination() {
    return _destination;
  }

  /**
   * nexthop target -> nexthop configuration
   *
   * <p>Only one nexthop configuration exists per nexthop target.
   */
  @Nonnull
  public Map<NexthopTarget, Nexthop> getNexthops() {
    return _nexthops;
  }

  public void setComment(String comment) {
    _comment = comment;
  }

  @Nullable private String _comment;
  @Nonnull private final Prefix _destination;
  @Nonnull private final Map<NexthopTarget, Nexthop> _nexthops;
}
