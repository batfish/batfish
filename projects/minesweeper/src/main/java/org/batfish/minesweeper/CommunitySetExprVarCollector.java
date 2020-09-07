package org.batfish.minesweeper;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.communities.CommunityExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityExprEvaluator;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;

@ParametersAreNonnullByDefault
public class CommunitySetExprVarCollector
    implements CommunitySetExprVisitor<Set<CommunityVar>, Configuration> {

  @Override
  public Set<CommunityVar> visitCommunityExprsSet(
      CommunityExprsSet communityExprsSet, Configuration arg) {
    ImmutableSet.Builder<CommunityVar> builder = ImmutableSet.builder();
    Set<CommunityExpr> exprs = communityExprsSet.getExprs();
    for (CommunityExpr expr : exprs) {
      Community c = expr.accept(CommunityExprEvaluator.instance(), null);
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
    CommunityMatchExpr me = arg.getCommunityMatchExprs().get(name);
    if (me == null) {
      throw new BatfishException("Cannot find community match expression: " + name);
    }
    return me.accept(new CommunityMatchExprVarCollector(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetReference(
      CommunitySetReference communitySetReference, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitCommunitySetUnion(
      CommunitySetUnion communitySetUnion, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitInputCommunities(
      InputCommunities inputCommunities, Configuration arg) {
    return null;
  }

  @Override
  public Set<CommunityVar> visitLiteralCommunitySet(
      LiteralCommunitySet literalCommunitySet, Configuration arg) {
    return null;
  }
}
