package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.communities.CommunityContext;
import org.batfish.datamodel.routing_policy.communities.CommunityExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityExprEvaluator;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.minesweeper.CommunityVar;

/** Collect all community literals and regexes in a {@link CommunitySetExpr}. */
@ParametersAreNonnullByDefault
public class CommunitySetExprVarCollector
    implements CommunitySetExprVisitor<Set<CommunityVar>, Configuration> {

  @Override
  public Set<CommunityVar> visitCommunityExprsSet(
      CommunityExprsSet communityExprsSet, Configuration arg) {
    ImmutableSet.Builder<CommunityVar> builder = ImmutableSet.builder();
    Set<CommunityExpr> exprs = communityExprsSet.getExprs();
    for (CommunityExpr expr : exprs) {
      Community c = expr.accept(CommunityExprEvaluator.instance(), configToCommunityContext(arg));
      builder.add(CommunityVar.from(c));
    }
    return builder.build();
  }

  @Override
  public Set<CommunityVar> visitCommunitySetDifference(
      CommunitySetDifference communitySetDifference, Configuration arg) {
    Set<CommunityVar> s1 = communitySetDifference.getInitial().accept(this, arg);
    Set<CommunityVar> s2 =
        communitySetDifference
            .getRemovalCriterion()
            .accept(new CommunityMatchExprVarCollector(), arg);
    return ImmutableSet.<CommunityVar>builder().addAll(s1).addAll(s2).build();
  }

  @Override
  public Set<CommunityVar> visitCommunitySetExprReference(
      CommunitySetExprReference communitySetExprReference, Configuration arg) {
    String name = communitySetExprReference.getName();
    CommunitySetExpr setExpr = arg.getCommunitySetExprs().get(name);
    if (setExpr == null) {
      throw new BatfishException("Cannot find community set expression: " + name);
    }
    return setExpr.accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetReference(
      CommunitySetReference communitySetReference, Configuration arg) {
    String name = communitySetReference.getName();
    CommunitySet cset = arg.getCommunitySets().get(name);
    if (cset == null) {
      throw new BatfishException("Cannot find community set: " + name);
    }
    return toCommunityVarSet(cset);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetUnion(
      CommunitySetUnion communitySetUnion, Configuration arg) {
    return communitySetUnion.getExprs().stream()
        .flatMap(e -> e.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<CommunityVar> visitInputCommunities(
      InputCommunities inputCommunities, Configuration arg) {
    return toCommunityVarSet(configToCommunityContext(arg).getInputCommunitySet());
  }

  @Override
  public Set<CommunityVar> visitLiteralCommunitySet(
      LiteralCommunitySet literalCommunitySet, Configuration arg) {
    return toCommunityVarSet(literalCommunitySet.getCommunitySet());
  }

  private static Set<CommunityVar> toCommunityVarSet(CommunitySet cset) {
    return cset.getCommunities().stream()
        .map(CommunityVar::from)
        .collect(ImmutableSet.toImmutableSet());
  }

  private static CommunityContext configToCommunityContext(Configuration config) {
    return CommunityContext.fromEnvironment(Environment.builder(config).build());
  }
}
