package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchCommunitySet extends BooleanExpr {

  private static final String PROP_EXPR = "expr";

  /** */
  private static final long serialVersionUID = 1L;

  private CommunitySetExpr _expr;

  @JsonCreator
  private MatchCommunitySet() {}

  public MatchCommunitySet(CommunitySetExpr expr) {
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
    MatchCommunitySet other = (MatchCommunitySet) obj;
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
  public Result evaluate(Environment environment) {
    Result result = new Result();
    boolean match = false;
    SortedSet<Long> inputCommunities = null;
    if (environment.getUseOutputAttributes()
        && environment.getOutputRoute() instanceof BgpRoute.Builder) {
      BgpRoute.Builder bgpRouteBuilder = (BgpRoute.Builder) environment.getOutputRoute();
      inputCommunities = bgpRouteBuilder.getCommunities();
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      inputCommunities = environment.getIntermediateBgpAttributes().getCommunities();
    } else if (environment.getOriginalRoute() instanceof BgpRoute) {
      BgpRoute bgpRoute = (BgpRoute) environment.getOriginalRoute();
      inputCommunities = bgpRoute.getCommunities();
    }
    if (inputCommunities != null) {
      match = _expr.matchAnyCommunity(environment, inputCommunities);
    }
    result.setBooleanValue(match);
    return result;
  }

  @JsonProperty(PROP_EXPR)
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

  @JsonProperty(PROP_EXPR)
  public void setExpr(CommunitySetExpr expr) {
    _expr = expr;
  }
}
