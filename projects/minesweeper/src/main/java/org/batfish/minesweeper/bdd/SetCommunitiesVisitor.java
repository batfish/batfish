package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
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
    ImmutableSet.Builder<Integer> shouldDelete = ImmutableSet.builder();
    ImmutableSet.Builder<Integer> shouldNotDelete = ImmutableSet.builder();
    for (int ap = 0; ap < commAPBDDs.length; ap++) {
      BDD comm = commAPBDDs[ap];
      if (!comm.diffSat(toDelete)) {
        shouldDelete.add(ap);
      } else {
        shouldNotDelete.add(ap);
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
    if (setExpr == null) {
      throw new BatfishException("Cannot find community set expression: " + name);
    }
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
        ImmutableSet.of(),
        ImmutableSet.of());
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
