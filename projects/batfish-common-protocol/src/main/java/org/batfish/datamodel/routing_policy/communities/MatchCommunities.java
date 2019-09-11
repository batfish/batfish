package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

/** A {@link BooleanExpr} representing a condition on the communities of a route. */
public final class MatchCommunities extends BooleanExpr {

  public MatchCommunities(
      CommunitySetExpr communitySetExpr, CommunitySetMatchExpr communitySetMatchExpr) {
    _communitySetExpr = communitySetExpr;
    _communitySetMatchExpr = communitySetMatchExpr;
  }

  @Override
  public Result evaluate(Environment environment) {
    CommunityContext ctx = CommunityContext.fromEnvironment(environment);
    CommunitySetExprEvaluator communitySetEvaluator = new CommunitySetExprEvaluator(ctx);
    CommunitySet communitySet = _communitySetExpr.accept(communitySetEvaluator);
    CommunitySetMatchExprEvaluator communitySetMatchExprEvaluator =
        new CommunitySetMatchExprEvaluator(ctx, communitySet);
    boolean ret = _communitySetMatchExpr.accept(communitySetMatchExprEvaluator);
    return Result.builder().setBooleanValue(ret).build();
  }

  @JsonProperty(PROP_COMMUNITY_SET_EXPR)
  public @Nonnull CommunitySetExpr getCommunitySetExpr() {
    return _communitySetExpr;
  }

  @JsonProperty(PROP_COMMUNITY_SET_MATCH_EXPR)
  public @Nonnull CommunitySetMatchExpr getCommunitySetMatchExpr() {
    return _communitySetMatchExpr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchCommunities)) {
      return false;
    }
    MatchCommunities rhs = (MatchCommunities) obj;
    return _communitySetExpr.equals(rhs._communitySetExpr)
        && _communitySetMatchExpr.equals(rhs._communitySetMatchExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_communitySetExpr, _communitySetMatchExpr);
  }

  private static final String PROP_COMMUNITY_SET_EXPR = "communitySetExpr";
  private static final String PROP_COMMUNITY_SET_MATCH_EXPR = "communitySetMatchExpr";

  @JsonCreator
  private static @Nonnull MatchCommunities create(
      @JsonProperty(PROP_COMMUNITY_SET_EXPR) @Nullable CommunitySetExpr communitySetExpr,
      @JsonProperty(PROP_COMMUNITY_SET_MATCH_EXPR) @Nullable
          CommunitySetMatchExpr communitySetMatchExpr) {
    checkArgument(communitySetExpr != null, "Missing %s", PROP_COMMUNITY_SET_EXPR);
    checkArgument(communitySetMatchExpr != null, "Missing %s", PROP_COMMUNITY_SET_MATCH_EXPR);
    return new MatchCommunities(communitySetExpr, communitySetMatchExpr);
  }

  private final @Nonnull CommunitySetExpr _communitySetExpr;
  private final @Nonnull CommunitySetMatchExpr _communitySetMatchExpr;
}
