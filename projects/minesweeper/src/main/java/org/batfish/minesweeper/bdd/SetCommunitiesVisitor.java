package org.batfish.minesweeper.bdd;

import static org.parboiled.common.Preconditions.checkArgument;

import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IntegerSpace;
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
import org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD.Arg;
import org.batfish.minesweeper.communities.CommunitySetExprVarCollector;

/**
 * Collect the sets of community variables that should be set to true / false during the symbolic
 * route analysis of a {@link org.batfish.datamodel.routing_policy.communities.SetCommunities}
 * statement.
 */
@ParametersAreNonnullByDefault
public class SetCommunitiesVisitor
    implements CommunitySetExprVisitor<CommunityAPDispositions, Arg> {

  @Override
  public CommunityAPDispositions visitCommunityExprsSet(
      CommunityExprsSet communityExprsSet, Arg arg) {
    Set<CommunityVar> commVars =
        communityExprsSet.accept(
            new CommunitySetExprVarCollector(), arg.getTransferBDD().getConfiguration());
    return communityAPDispositionsFor(commVars, arg);
  }

  @Override
  public CommunityAPDispositions visitCommunitySetDifference(
      CommunitySetDifference communitySetDifference, Arg arg) {
    CommunityAPDispositions initial = communitySetDifference.getInitial().accept(this, arg);
    BDD toDelete =
        communitySetDifference.getRemovalCriterion().accept(new CommunityMatchExprToBDD(), arg);
    BDD[] commAPBDDs = arg.getBDDRoute().getCommunityAtomicPredicates();
    IntegerSpace.Builder shouldDelete = IntegerSpace.builder();
    IntegerSpace.Builder shouldNotDelete = IntegerSpace.builder();
    for (int ap = 0; ap < commAPBDDs.length; ap++) {
      BDD comm = commAPBDDs[ap];
      if (!comm.diffSat(toDelete)) {
        shouldDelete.including(ap);
      } else {
        shouldNotDelete.including(ap);
      }
    }
    return initial.diff(
        new CommunityAPDispositions(
            commAPBDDs.length, shouldDelete.build(), shouldNotDelete.build()));
  }

  @Override
  public CommunityAPDispositions visitCommunitySetExprReference(
      CommunitySetExprReference communitySetExprReference, Arg arg) {
    String name = communitySetExprReference.getName();
    CommunitySetExpr setExpr =
        arg.getTransferBDD().getConfiguration().getCommunitySetExprs().get(name);
    checkArgument(
        setExpr != null, "Undefined reference in community set exprs should not be possible");
    return setExpr.accept(this, arg);
  }

  @Override
  public CommunityAPDispositions visitCommunitySetReference(
      CommunitySetReference communitySetReference, Arg arg) {
    Set<CommunityVar> commVars =
        communitySetReference.accept(
            new CommunitySetExprVarCollector(), arg.getTransferBDD().getConfiguration());
    return communityAPDispositionsFor(commVars, arg);
  }

  @Override
  public CommunityAPDispositions visitCommunitySetUnion(
      CommunitySetUnion communitySetUnion, Arg arg) {
    return communitySetUnion.getExprs().stream()
        .map(e -> e.accept(this, arg))
        .reduce(CommunityAPDispositions.empty(arg.getBDDRoute()), CommunityAPDispositions::union);
  }

  @Override
  public CommunityAPDispositions visitInputCommunities(InputCommunities inputCommunities, Arg arg) {
    return new CommunityAPDispositions(
        arg.getBDDRoute().getCommunityAtomicPredicates().length,
        IntegerSpace.EMPTY,
        IntegerSpace.EMPTY);
  }

  @Override
  public CommunityAPDispositions visitLiteralCommunitySet(
      LiteralCommunitySet literalCommunitySet, Arg arg) {
    Set<CommunityVar> commVars =
        literalCommunitySet.accept(
            new CommunitySetExprVarCollector(), arg.getTransferBDD().getConfiguration());
    return communityAPDispositionsFor(commVars, arg);
  }

  private static CommunityAPDispositions communityAPDispositionsFor(
      Set<CommunityVar> commVars, Arg arg) {
    TransferBDD transferBDD = arg.getTransferBDD();
    Set<Integer> aps =
        transferBDD.atomicPredicatesFor(commVars, transferBDD.getCommunityAtomicPredicates());
    return CommunityAPDispositions.exactly(aps, arg.getBDDRoute());
  }
}
