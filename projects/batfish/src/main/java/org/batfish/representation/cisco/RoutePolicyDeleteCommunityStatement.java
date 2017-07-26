package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.RetainCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicyDeleteCommunityStatement extends RoutePolicyDeleteStatement {

  private static final long serialVersionUID = 1L;

  private RoutePolicyCommunitySet _commset;

  private boolean _negated;

  public RoutePolicyDeleteCommunityStatement(boolean negated, RoutePolicyCommunitySet commset) {
    _negated = negated;
    _commset = commset;
  }

  public RoutePolicyCommunitySet getCommSet() {
    return _commset;
  }

  @Override
  public RoutePolicyDeleteType getDeleteType() {
    return RoutePolicyDeleteType.COMMUNITY;
  }

  public boolean getNegated() {
    return _negated;
  }

  @Override
  public Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    if (_negated) {
      return new RetainCommunity(_commset.toCommunitySetExpr(cc, c, w));
    } else {
      return new DeleteCommunity(_commset.toCommunitySetExpr(cc, c, w));
    }
  }
}
