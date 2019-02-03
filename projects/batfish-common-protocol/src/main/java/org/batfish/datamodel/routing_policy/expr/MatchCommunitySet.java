package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.SortedSet;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that tests whether an {@link Environment} contains a BGP route with a
 * community matching a given {@link CommunitySetExpr}.
 */
public final class MatchCommunitySet extends BooleanExpr {

  private static final String PROP_EXPR = "expr";

  private static final long serialVersionUID = 1L;

  private final CommunitySetExpr _expr;

  @JsonCreator
  private static MatchCommunitySet create(@JsonProperty(PROP_EXPR) CommunitySetExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new MatchCommunitySet(expr);
  }

  public MatchCommunitySet(CommunitySetExpr expr) {
    _expr = expr;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchCommunitySet)) {
      return false;
    }
    MatchCommunitySet other = (MatchCommunitySet) obj;
    return Objects.equals(_expr, other._expr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr);
  }
}
