package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

/** Add communities specified by a {@link CommunitySetExpr} to a BGP route */
@ParametersAreNonnullByDefault
public final class AddCommunity extends Statement {
  private static final String PROP_EXPR = "expr";

  private final CommunitySetExpr _expr;

  @JsonCreator
  private static AddCommunity create(
      @Nullable @JsonProperty(PROP_EXPR) CommunitySetExpr expression) {
    checkArgument(expression != null);
    return new AddCommunity(expression);
  }

  public AddCommunity(CommunitySetExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitAddCommunity(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AddCommunity)) {
      return false;
    }
    AddCommunity that = (AddCommunity) o;
    return Objects.equals(_expr, that._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  @Override
  public Result execute(Environment environment) {
    if (environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
      BgpRoute.Builder<?, ?> bgpRoute = (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
      Set<Community> communities = _expr.asLiteralCommunities(environment);
      bgpRoute.addCommunities(communities);
      if (environment.getWriteToIntermediateBgpAttributes()) {
        environment.getIntermediateBgpAttributes().addCommunities(communities);
      }
    }
    return new Result();
  }

  @JsonProperty(PROP_EXPR)
  public CommunitySetExpr getExpr() {
    return _expr;
  }
}
