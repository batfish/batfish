package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.communities.CommunitySetExprVarCollector;

/**
 * Collect the set of community variables that should be set to true during the symbolic route
 * analysis of a {@link org.batfish.datamodel.routing_policy.communities.SetCommunities} statement.
 */
@ParametersAreNonnullByDefault
public class SetCommunitiesVarCollector
    implements CommunitySetExprVisitor<Set<CommunityVar>, Configuration> {

  @Override
  public Set<CommunityVar> visitCommunityExprsSet(
      CommunityExprsSet communityExprsSet, Configuration arg) {
    return communityExprsSet.accept(new CommunitySetExprVarCollector(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetDifference(
      CommunitySetDifference communitySetDifference, Configuration arg) {
    Set<CommunityVar> s1 = communitySetDifference.getInitial().accept(this, arg);
    // TODO: handle set differences; for now we handle the special case when the first
    //  operand in the difference is empty, so that the second operand is irrelevant
    if (s1.isEmpty()) {
      return s1;
    } else {
      throw new BatfishException("Community set differences are not supported");
    }
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
    return communitySetReference.accept(new CommunitySetExprVarCollector(), arg);
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
    return inputCommunities.accept(new CommunitySetExprVarCollector(), arg);
  }

  @Override
  public Set<CommunityVar> visitLiteralCommunitySet(
      LiteralCommunitySet literalCommunitySet, Configuration arg) {
    return literalCommunitySet.accept(new CommunitySetExprVarCollector(), arg);
  }
}
