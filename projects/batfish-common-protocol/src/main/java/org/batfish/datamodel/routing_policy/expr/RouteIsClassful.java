package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that tests whether an IPv4 route is classful, i.e. the distributed prefix is
 * exactly an entire subnet in its IPv4 address class.
 */
public final class RouteIsClassful extends BooleanExpr {

  private static final long serialVersionUID = 1L;

  private static final RouteIsClassful INSTANCE = new RouteIsClassful();

  private RouteIsClassful() {}

  @JsonCreator
  public static RouteIsClassful instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  /**
   * Returns {@code true} iff the IPv4 route is classful, i.e. the distributed prefix is exactly an
   * entire subnet in its IPv4 address class.
   *
   * <p>Returns {@code true} for networks that do not have a subnet size.
   */
  @Override
  public Result evaluate(Environment environment) {
    AbstractRoute route = environment.getOriginalRoute();
    Prefix network = route.getNetwork();
    int classSize = network.getStartIp().getClassNetworkSize();

    Result ret = new Result();
    ret.setBooleanValue(classSize == network.getPrefixLength());
    return ret;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
