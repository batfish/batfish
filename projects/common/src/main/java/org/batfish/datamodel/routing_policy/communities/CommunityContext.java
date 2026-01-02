package org.batfish.datamodel.routing_policy.communities;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HasReadableCommunities;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * A runtime structure comprising the evaluation context for community match and transformation
 * operations.
 */
public final class CommunityContext {

  public static class Builder {

    public @Nonnull CommunityContext build() {
      return new CommunityContext(
          _communityMatchExprs,
          _communitySetExprs,
          _communitySetMatchExprs,
          _communitySets,
          _inputCommunitySet);
    }

    public @Nonnull Builder setCommunityMatchExprs(
        Map<String, CommunityMatchExpr> communityMatchExprs) {
      _communityMatchExprs = communityMatchExprs;
      return this;
    }

    public @Nonnull Builder setCommunitySetExprs(Map<String, CommunitySetExpr> communitySetExprs) {
      _communitySetExprs = communitySetExprs;
      return this;
    }

    public @Nonnull Builder setCommunitySetMatchExprs(
        Map<String, CommunitySetMatchExpr> communitySetMatchExprs) {
      _communitySetMatchExprs = communitySetMatchExprs;
      return this;
    }

    public @Nonnull Builder setCommunitySets(Map<String, CommunitySet> communitySets) {
      _communitySets = communitySets;
      return this;
    }

    public @Nonnull Builder setInputCommunitySet(CommunitySet inputCommunitySet) {
      _inputCommunitySet = inputCommunitySet;
      return this;
    }

    private @Nonnull Map<String, CommunityMatchExpr> _communityMatchExprs;
    private @Nonnull Map<String, CommunitySetExpr> _communitySetExprs;
    private @Nonnull Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
    private @Nonnull Map<String, CommunitySet> _communitySets;
    private @Nonnull CommunitySet _inputCommunitySet;

    private Builder() {
      _communityMatchExprs = ImmutableMap.of();
      _communitySetExprs = ImmutableMap.of();
      _communitySetMatchExprs = ImmutableMap.of();
      _communitySets = ImmutableMap.of();
      _inputCommunitySet = CommunitySet.empty();
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nonnull Map<String, CommunityMatchExpr> getCommunityMatchExprs() {
    return _communityMatchExprs;
  }

  public @Nonnull Map<String, CommunitySetExpr> getCommunitySetExprs() {
    return _communitySetExprs;
  }

  public @Nonnull Map<String, CommunitySetMatchExpr> getCommunitySetMatchExprs() {
    return _communitySetMatchExprs;
  }

  public @Nonnull Map<String, CommunitySet> getCommunitySets() {
    return _communitySets;
  }

  public @Nonnull CommunitySet getInputCommunitySet() {
    return _inputCommunitySet;
  }

  public static @Nonnull CommunityContext fromEnvironment(Environment environment) {
    CommunitySet inputCommunitySet;
    if (environment.getUseOutputAttributes()
        && environment.getOutputRoute() instanceof HasReadableCommunities) {
      HasReadableCommunities outputRoute = (HasReadableCommunities) environment.getOutputRoute();
      inputCommunitySet = outputRoute.getCommunities();
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      inputCommunitySet = environment.getIntermediateBgpAttributes().getCommunities();
    } else if (environment.getOriginalRoute() instanceof HasReadableCommunities) {
      HasReadableCommunities originalRoute =
          (HasReadableCommunities) environment.getOriginalRoute();
      inputCommunitySet = originalRoute.getCommunities();
    } else {
      inputCommunitySet = CommunitySet.empty();
    }
    return CommunityContext.builder()
        .setCommunityMatchExprs(environment.getCommunityMatchExprs())
        .setCommunitySetExprs(environment.getCommunitySetExprs())
        .setCommunitySetMatchExprs(environment.getCommunitySetMatchExprs())
        .setCommunitySets(environment.getCommunitySets())
        .setInputCommunitySet(inputCommunitySet)
        .build();
  }

  public @Nonnull CommunityMatchExprEvaluator getCommunityMatchExprEvaluator() {
    return _communityMatchExprEvaluator;
  }

  public @Nonnull CommunitySetMatchExprEvaluator getCommunitySetMatchExprEvaluator() {
    return _communitySetMatchExprEvaluator;
  }

  private final @Nonnull Map<String, CommunityMatchExpr> _communityMatchExprs;
  private final @Nonnull Map<String, CommunitySetExpr> _communitySetExprs;
  private final @Nonnull Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
  private final @Nonnull Map<String, CommunitySet> _communitySets;
  private final @Nonnull CommunitySet _inputCommunitySet;

  private final @Nonnull CommunityMatchExprEvaluator _communityMatchExprEvaluator;
  private final @Nonnull CommunitySetMatchExprEvaluator _communitySetMatchExprEvaluator;

  private CommunityContext(
      Map<String, CommunityMatchExpr> communityMatchExprs,
      Map<String, CommunitySetExpr> communitySetExprs,
      Map<String, CommunitySetMatchExpr> communitySetMatchExprs,
      Map<String, CommunitySet> communitySets,
      CommunitySet inputCommunitySet) {
    _communityMatchExprs = communityMatchExprs;
    _communitySetExprs = communitySetExprs;
    _communitySetMatchExprs = communitySetMatchExprs;
    _communitySets = communitySets;
    _inputCommunitySet = inputCommunitySet;

    // instantiate evaluators
    _communityMatchExprEvaluator = new CommunityMatchExprEvaluator(this);
    _communitySetMatchExprEvaluator = new CommunitySetMatchExprEvaluator(this);
  }
}
