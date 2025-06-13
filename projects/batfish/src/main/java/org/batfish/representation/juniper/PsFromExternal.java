package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchOspfExternalType;

/** Represents a "from external type" line in a {@link PsTerm} */
public final class PsFromExternal extends PsFrom {

  private final @Nonnull OspfMetricType _type;

  public PsFromExternal(@Nonnull OspfMetricType type) {
    _type = type;
  }

  public @Nonnull OspfMetricType getType() {
    return _type;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchOspfExternalType(_type);
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
    return _type.ordinal();
  }
}
