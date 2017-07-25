package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchIpv6;

public class PsFromFamilyInet6 extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchIpv6();
  }
}
