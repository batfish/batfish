package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

/** Represents a "from route-filter" line in a {@link PsTerm} */
public final class PsFromRouteType extends PsFrom {

  public enum Type {
    INTERNAL,
    EXTERNAL,
  };

  private final @Nonnull Type _type;

  public PsFromRouteType(@Nonnull Type type) {
    _type = type;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    // Looks like this is only for BGP:
    // https://www.juniper.net/documentation/us/en/software/junos/routing-policy/topics/concept/policy-configuring-match-conditions-in-routing-policy-terms.html
    if (_type == Type.EXTERNAL) {
      return new MatchProtocol(RoutingProtocol.BGP);
    }
    assert _type == Type.INTERNAL;
    return new MatchProtocol(RoutingProtocol.IBGP);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PsFromRouteType that)) {
      return false;
    }
    return _type == that._type;
  }

  @Override
  public int hashCode() {
    return _type.ordinal();
  }
}
