package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetCommunity extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  private boolean _additive;

  private RoutePolicyCommunitySet _commSet;

  public RoutePolicySetCommunity(RoutePolicyCommunitySet commSet, boolean additive) {
    _commSet = commSet;
    _additive = additive;
  }

  public boolean getAdditive() {
    return _additive;
  }

  public RoutePolicyCommunitySet getCommunitySet() {
    return _commSet;
  }

  @Override
  public Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    CommunitySetExpr expr = _commSet.toCommunitySetExpr(cc, c, w);
    if (_additive) {
      return new AddCommunity(expr);
    } else {
      return new SetCommunity(expr);
    }
  }
}
