package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HasWritableCommunities;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.StatementVisitor;

/** A {@link Statement} that overwrites the communities of a route. */
public final class SetCommunities extends Statement {

  public SetCommunities(CommunitySetExpr communitySetExpr) {
    _communitySetExpr = communitySetExpr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetCommunities(this, arg);
  }

  @Override
  public @Nonnull Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof HasWritableCommunities)) {
      return new Result();
    }
    CommunityContext ctx = CommunityContext.fromEnvironment(environment);
    CommunitySet communities = _communitySetExpr.accept(CommunitySetExprEvaluator.instance(), ctx);
    HasWritableCommunities<?, ?> outputRoute =
        (HasWritableCommunities<?, ?>) environment.getOutputRoute();
    outputRoute.setCommunities(communities);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setCommunities(communities);
    }
    return new Result();
  }

  @JsonProperty(PROP_COMMUNITY_SET_EXPR)
  public @Nonnull CommunitySetExpr getCommunitySetExpr() {
    return _communitySetExpr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SetCommunities)) {
      return false;
    }
    return _communitySetExpr.equals(((SetCommunities) obj)._communitySetExpr);
  }

  @Override
  public int hashCode() {
    return _communitySetExpr.hashCode();
  }

  private static final String PROP_COMMUNITY_SET_EXPR = "communitySetExpr";

  @JsonCreator
  private static @Nonnull SetCommunities create(
      @JsonProperty(PROP_COMMUNITY_SET_EXPR) @Nullable CommunitySetExpr communitySetExpr) {
    checkArgument(communitySetExpr != null, "Missing %s", PROP_COMMUNITY_SET_EXPR);
    return new SetCommunities(communitySetExpr);
  }

  private final @Nonnull CommunitySetExpr _communitySetExpr;
}
