package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nullable;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that matches if a route is an OSPF external route of a specific type (E1 or
 * E2). If type is null, matches any OSPF external route.
 */
public final class MatchOspfExternalType extends BooleanExpr {

  private final @Nullable OspfMetricType _type;

  public MatchOspfExternalType(@Nullable OspfMetricType type) {
    _type = type;
  }

  @Override
  public Result evaluate(Environment environment) {
    if (environment.getOriginalRoute() == null) {
      return new Result(false);
    }

    // Check if the route is an OSPF external route
    RoutingProtocol protocol = environment.getOriginalRoute().getProtocol();
    boolean isOspfExternal =
        protocol == RoutingProtocol.OSPF_E1 || protocol == RoutingProtocol.OSPF_E2;

    if (!isOspfExternal) {
      return new Result(false);
    }

    // If no specific type is specified, match any external route
    if (_type == null) {
      return new Result(true);
    }

    // Check if the route's OSPF metric type matches the specified type
    boolean matchesType =
        (_type == OspfMetricType.E1 && protocol == RoutingProtocol.OSPF_E1)
            || (_type == OspfMetricType.E2 && protocol == RoutingProtocol.OSPF_E2);

    return new Result(matchesType);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchOspfExternalType(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchOspfExternalType)) {
      return false;
    }
    MatchOspfExternalType other = (MatchOspfExternalType) obj;
    return _type == other._type;
  }

  @Override
  public int hashCode() {
    return _type == null ? 0 : _type.ordinal();
  }

  public @Nullable OspfMetricType getType() {
    return _type;
  }

  @Override
  public String toString() {
    return "MatchOspfExternalType{type=" + _type + "}";
  }
}
