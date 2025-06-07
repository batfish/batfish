package org.batfish.datamodel.routing_policy.communities;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HasReadableCommunities;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * Runtime context for community match and transformation operations that implements the BGP
 * attribute handling decision tree for community-specific operations. This class provides precise
 * control over community attribute access patterns during routing policy evaluation.
 *
 * <p><strong>BGP Attribute Handling Integration:</strong>
 *
 * <p>This class implements the same decision tree logic as the core Environment class for community
 * attributes:
 *
 * <ol>
 *   <li><strong>Primary:</strong> Use output route communities if {@code useOutputAttributes} is
 *       true
 *   <li><strong>Secondary:</strong> Use intermediate BGP attributes communities if {@code
 *       readFromIntermediateBgpAttributes} is true
 *   <li><strong>Fallback:</strong> Use original route communities (default behavior)
 * </ol>
 *
 * <p><strong>Vendor-Specific Behavior:</strong>
 *
 * <ul>
 *   <li><strong>Juniper:</strong> Matches against communities as they're being built during policy
 *       evaluation (output attributes)
 *   <li><strong>Cisco:</strong> Matches against original received communities (input attributes)
 * </ul>
 *
 * <p><strong>Community Operations:</strong>
 *
 * <p>This context supports comprehensive community operations including:
 *
 * <ul>
 *   <li><strong>Matching:</strong> Community and community set matching expressions
 *   <li><strong>Transformation:</strong> Community set expressions and modifications
 *   <li><strong>Evaluation:</strong> Named community expressions and sets
 * </ul>
 *
 * <p><strong>Usage Pattern:</strong>
 *
 * <pre>{@code
 * CommunityContext context = CommunityContext.fromEnvironment(environment);
 * CommunitySet communities = context.getInputCommunitySet();
 * CommunityMatchExprEvaluator evaluator = context.getCommunityMatchExprEvaluator();
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> CommunityContext instances are immutable after construction,
 * ensuring safe concurrent access during policy evaluation.
 *
 * @see Environment
 * @see CommunitySet
 * @see CommunityMatchExpr
 * @see <a
 *     href="../../../../../../../../../docs/routing_policy_invariants/bgp_attribute_handling.md">
 *     BGP Attribute Handling Documentation</a>
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

  /**
   * Creates a {@link CommunityContext} from the provided {@link Environment} using the BGP
   * attribute handling decision tree. This method implements the same precedence hierarchy as the
   * core BGP attribute handling system for community attributes.
   *
   * <p><strong>Decision Tree Implementation:</strong>
   *
   * <p>This method implements the BGP attribute handling precedence hierarchy:
   *
   * <ol>
   *   <li><strong>Highest Precedence:</strong> If {@link Environment#getUseOutputAttributes()} is
   *       {@code true} and output route has readable communities, use output route communities
   *   <li><strong>Medium Precedence:</strong> If {@link
   *       Environment#getReadFromIntermediateBgpAttributes()} is {@code true}, use intermediate BGP
   *       attributes communities
   *   <li><strong>Lowest Precedence:</strong> If original route has readable communities, use
   *       original route communities
   *   <li><strong>No Communities Available:</strong> Use empty community set
   * </ol>
   *
   * <p><strong>Vendor-Specific Behavior:</strong>
   *
   * <ul>
   *   <li><strong>Juniper:</strong> Typically uses output route communities (first case)
   *   <li><strong>Cisco:</strong> Typically uses original route communities (third case)
   * </ul>
   *
   * <p><strong>Route Type Compatibility:</strong> Only routes implementing {@link
   * HasReadableCommunities} can provide community context. Routes without community attributes will
   * result in an empty community set.
   *
   * <p><strong>Context Initialization:</strong> The returned context includes all necessary
   * community expressions, sets, and evaluators from the environment for complete community
   * operation support.
   *
   * @param environment the routing policy environment containing route and attribute information
   * @return {@link CommunityContext} with appropriate community set based on decision tree logic
   * @see Environment#getUseOutputAttributes()
   * @see Environment#getReadFromIntermediateBgpAttributes()
   * @see HasReadableCommunities
   * @see CommunitySet#empty()
   */
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
