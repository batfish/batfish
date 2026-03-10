package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

/** Represents a "from external [type]" line in a {@link PsTerm} */
public final class PsFromExternal extends PsFrom {

  private final @Nullable OspfMetricType _type;

  public PsFromExternal(@Nullable OspfMetricType type) {
    _type = type;
  }

  public @Nullable OspfMetricType getType() {
    return _type;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    if (_type == null) {
      // Match any OSPF external route (both E1 and E2)
      return new MatchProtocol(RoutingProtocol.OSPF_E1, RoutingProtocol.OSPF_E2);
    } else if (_type == OspfMetricType.E1) {
      return new MatchProtocol(RoutingProtocol.OSPF_E1);
    } else {
      return new MatchProtocol(RoutingProtocol.OSPF_E2);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PsFromExternal that)) {
      return false;
    }
    return _type == that._type;
  }

  @Override
  public int hashCode() {
    return _type == null ? -1 : _type.ordinal();
  }
}
