package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;

public final class PsFromPrefixList extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public PsFromPrefixList(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(_name));
  }
}
