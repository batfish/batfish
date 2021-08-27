package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprVisitor;

/** A {@link BooleanExpr} representing a condition on the communities of a route. */
public final class MatchCommunities extends BooleanExpr {
  private static final class CacheKey {
    private final String _hostname;
    private final MatchCommunities _expr;
    private final CommunitySet _inputCommunitySet;

    // device context. uniquely determined by hostname, don't need to check it
    private @Nonnull Map<String, CommunityMatchExpr> _communityMatchExprs;
    private @Nonnull Map<String, CommunitySetExpr> _communitySetExprs;
    private @Nonnull Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
    private @Nonnull Map<String, CommunitySet> _communitySets;

    private CacheKey(
        String hostname,
        MatchCommunities expr,
        CommunitySet inputCommunitySet,
        Map<String, CommunityMatchExpr> communityMatchExprs,
        Map<String, CommunitySetExpr> communitySetExprs,
        Map<String, CommunitySetMatchExpr> communitySetMatchExprs,
        Map<String, CommunitySet> communitySets) {
      _hostname = hostname;
      _expr = expr;
      _inputCommunitySet = inputCommunitySet;
      _communityMatchExprs = communityMatchExprs;
      _communitySetExprs = communitySetExprs;
      _communitySetMatchExprs = communitySetMatchExprs;
      _communitySets = communitySets;
    }

    public Result computeValue() {
      CommunityContext ctx =
          CommunityContext.builder()
              .setCommunityMatchExprs(_communityMatchExprs)
              .setCommunitySetExprs(_communitySetExprs)
              .setCommunitySetMatchExprs(_communitySetMatchExprs)
              .setCommunitySets(_communitySets)
              .setInputCommunitySet(_inputCommunitySet)
              .build();
      CommunitySet communitySet =
          _expr.getCommunitySetExpr().accept(CommunitySetExprEvaluator.instance(), ctx);
      boolean ret =
          _expr
              .getCommunitySetMatchExpr()
              .accept(ctx.getCommunitySetMatchExprEvaluator(), communitySet);
      return Result.builder().setBooleanValue(ret).build();
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof CacheKey)) {
        return false;
      }
      CacheKey other = (CacheKey) o;
      return _hostname.equals(other._hostname)
          && _expr == other._expr
          && _inputCommunitySet.equals(other._inputCommunitySet);
    }

    @Override
    public int hashCode() {
      return 31 * (31 * _hostname.hashCode() + System.identityHashCode(_expr))
          + _inputCommunitySet.hashCode();
    }
  }

  private static final LoadingCache<CacheKey, Result> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(CacheLoader.from(CacheKey::computeValue));

  public MatchCommunities(
      CommunitySetExpr communitySetExpr, CommunitySetMatchExpr communitySetMatchExpr) {
    _communitySetExpr = communitySetExpr;
    _communitySetMatchExpr = communitySetMatchExpr;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchCommunities(this, arg);
  }

  @Override
  public @Nonnull Result evaluate(Environment environment) {
    CommunityContext ctx = CommunityContext.fromEnvironment(environment);
    CacheKey key =
        new CacheKey(
            environment.getHostname(),
            this,
            ctx.getInputCommunitySet(),
            ctx.getCommunityMatchExprs(),
            ctx.getCommunitySetExprs(),
            ctx.getCommunitySetMatchExprs(),
            ctx.getCommunitySets());
    return CACHE.getUnchecked(key);
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
