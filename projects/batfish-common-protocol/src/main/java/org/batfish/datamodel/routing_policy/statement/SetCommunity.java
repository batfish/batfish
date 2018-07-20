package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.SortedSet;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

public class SetCommunity extends Statement {

  /** */
  private static final long serialVersionUID = 1L;

  private CommunitySetExpr _expr;

  @JsonCreator
  private SetCommunity() {}

  public SetCommunity(CommunitySetExpr expr) {
    _expr = expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SetCommunity other = (SetCommunity) obj;
    if (_expr == null) {
      if (other._expr != null) {
        return false;
      }
    } else if (!_expr.equals(other._expr)) {
      return false;
    }
    return true;
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    BgpRoute.Builder bgpRoute = (BgpRoute.Builder) environment.getOutputRoute();
    SortedSet<Long> communities = _expr.asLiteralCommunities(environment);
    bgpRoute.getCommunities().clear();
    bgpRoute.getCommunities().addAll(communities);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().getCommunities().clear();
      environment.getIntermediateBgpAttributes().getCommunities().addAll(communities);
    }
    return result;
  }

  public CommunitySetExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expr == null) ? 0 : _expr.hashCode());
    return result;
  }

  public void setExpr(CommunitySetExpr expr) {
    _expr = expr;
  }
}
