package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** Represents an unsupported "from" line in a {@link PsTerm} */
public final class PsFromUnsupported extends PsFrom {

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    // We warn during conversion, just return False here.
    return BooleanExprs.FALSE;
  }
}
