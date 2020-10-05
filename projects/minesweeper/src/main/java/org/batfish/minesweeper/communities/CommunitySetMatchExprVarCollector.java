package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.minesweeper.CommunityVar;

/** Collect all community literals and regexes in a {@link CommunitySetMatchExpr}. */
@ParametersAreNonnullByDefault
public class CommunitySetMatchExprVarCollector
    implements CommunitySetMatchExprVisitor<Set<CommunityVar>, Configuration> {
  @Override
  public Set<CommunityVar> visitCommunitySetAcl(
      CommunitySetAcl communitySetAcl, Configuration arg) {
    return visitAll(
        communitySetAcl.getLines().stream()
            .map(CommunitySetAclLine::getCommunitySetMatchExpr)
            .collect(ImmutableList.toImmutableList()),
        arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchAll(
      CommunitySetMatchAll communitySetMatchAll, Configuration arg) {
    return visitAll(communitySetMatchAll.getExprs(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchAny(
      CommunitySetMatchAny communitySetMatchAny, Configuration arg) {
    return visitAll(communitySetMatchAny.getExprs(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference, Configuration arg) {
    String name = communitySetMatchExprReference.getName();
    CommunitySetMatchExpr expr = arg.getCommunitySetMatchExprs().get(name);
    if (expr == null) {
      throw new BatfishException("Cannot find community set match expression: " + name);
    }
    return expr.accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchRegex(
      CommunitySetMatchRegex communitySetMatchRegex, Configuration arg) {
    throw new UnsupportedOperationException("Community set regexes are not supported");
  }

  @Override
  public Set<CommunityVar> visitCommunitySetNot(
      CommunitySetNot communitySetNot, Configuration arg) {
    return communitySetNot.getExpr().accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitHasCommunity(HasCommunity hasCommunity, Configuration arg) {
    return hasCommunity.getExpr().accept(new CommunityMatchExprVarCollector(), arg);
  }

  private Set<CommunityVar> visitAll(Collection<CommunitySetMatchExpr> exprs, Configuration arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
