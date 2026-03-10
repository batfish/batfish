package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that tests whether an IPv4 route is classful, i.e. the matched prefix is not a
 * strict subset of an IPv4 address class.
 *
 * <p>This is typically used for Cisco IOS {@code redistribute} without {@code subnets} keyword.
 */
public final class RouteIsClassful extends BooleanExpr {

  private static final RouteIsClassful INSTANCE = new RouteIsClassful();

  private RouteIsClassful() {}

  @JsonCreator
  public static RouteIsClassful instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitRouteIsClassful(this, arg);
  }

  /**
   * Returns {@code true} iff the IPv4 route is classful, i.e. the matched prefix is not a struct
   * subset of an IPv4 address class.
   *
   * <p>Returns {@code true} for networks that do not have a subnet size.
   */
  @Override
  public Result evaluate(Environment environment) {
    AbstractRoute route = environment.getOriginalRoute();
    Prefix network = route.getNetwork();
    int classSize = network.getStartIp().getClassNetworkSize();
    return new Result(network.getPrefixLength() <= classSize);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof RouteIsClassful;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
