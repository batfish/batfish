package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.LineAction;
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
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.minesweeper.CommunityVar;

/**
 * Create a BDD from a {@link
 * org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr} expression, such that the
 * models of the BDD represent all and only the community sets that match the expression. This BDD
 * is used as part of symbolic route analysis of the {@link
 * org.batfish.datamodel.routing_policy.communities.MatchCommunities} expression.
 */
@ParametersAreNonnullByDefault
public class CommunitySetMatchExprToBDD
    implements CommunitySetMatchExprVisitor<BDD, CommunitySetMatchExprToBDD.Arg> {

  // a tuple of a TransferBDD object and a BDDRoute object, used as the argument to this visitor
  static class Arg {
    @Nonnull private final TransferBDD _transferBDD;
    @Nonnull private final BDDRoute _bddRoute;

    Arg(TransferBDD transferBDD, BDDRoute bddRoute) {
      _transferBDD = transferBDD;
      _bddRoute = bddRoute;
    }

    TransferBDD getTransferBDD() {
      return _transferBDD;
    }

    BDDRoute getBDDRoute() {
      return _bddRoute;
    }
  }

  @Override
  public BDD visitCommunitySetAcl(CommunitySetAcl communitySetAcl, Arg arg) {
    List<CommunitySetAclLine> lines = new ArrayList<>(communitySetAcl.getLines());
    Collections.reverse(lines);
    BDD acc = arg.getTransferBDD().getFactory().zero();
    for (CommunitySetAclLine line : lines) {
      boolean action = (line.getAction() == LineAction.PERMIT);
      BDD lineBDD = line.getCommunitySetMatchExpr().accept(this, arg);
      acc = lineBDD.ite(arg.getTransferBDD().mkBDD(action), acc);
    }
    return acc;
  }

  @Override
  public BDD visitCommunitySetMatchAll(CommunitySetMatchAll communitySetMatchAll, Arg arg) {
    return communitySetMatchAll.getExprs().stream()
        .map(expr -> expr.accept(this, arg))
        .reduce(arg.getTransferBDD().getFactory().one(), BDD::and);
  }

  @Override
  public BDD visitCommunitySetMatchAny(CommunitySetMatchAny communitySetMatchAny, Arg arg) {
    return arg.getTransferBDD()
        .getFactory()
        .orAll(
            communitySetMatchAny.getExprs().stream()
                .map(expr -> expr.accept(this, arg))
                .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BDD visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference, Arg arg) {
    String name = communitySetMatchExprReference.getName();
    CommunitySetMatchExpr expr =
        arg.getTransferBDD().getConfiguration().getCommunitySetMatchExprs().get(name);
    if (expr == null) {
      throw new BatfishException("Cannot find community set match expression: " + name);
    }
    return expr.accept(this, arg);
  }

  @Override
  public BDD visitCommunitySetMatchRegex(CommunitySetMatchRegex communitySetMatchRegex, Arg arg) {
    // We've already ensured in CommunitySetMatchExprVarCollector that there are no community-set
    // regexes, so this code should be unreachable.
    throw new IllegalStateException(
        String.format("Unexpected community set match regex %s", communitySetMatchRegex));
  }

  @Override
  public BDD visitCommunitySetNot(CommunitySetNot communitySetNot, Arg arg) {
    return communitySetNot.getExpr().accept(this, arg).not();
  }

  @Override
  public BDD visitHasCommunity(HasCommunity hasCommunity, Arg arg) {

    /**
     * we can't treat a BDD for a single community as a BDD for a community set, or else its
     * constraints could be satisfied by multiple communities in the set, which each satisfy some of
     * the constraints. therefore, we instead determine the largest disjunction of atomic predicates
     * that each satisfies all the single-community constraints. any community set that satisfies
     * this disjunction must contain at least one community that suffices.
     */

    /**
     * first get a BDD for the single community constraints. we use the original route here so that
     * the constraint will be in terms of the community APs.
     */
    BDD[] originalAPs = arg.getTransferBDD().getOriginalRoute().getCommunityAtomicPredicates();
    BDD matchExprBDD =
        hasCommunity
            .getExpr()
            .accept(
                new CommunityMatchExprToBDD(),
                new Arg(arg.getTransferBDD(), arg.getTransferBDD().getOriginalRoute()));

    /**
     * now figure out which atomic predicates satisfy matchExprBDD. when considering each atomic
     * predicate, we use the exactlyOneAP helper function to include the fact that all other atomic
     * predicates will be false, which must be the case since atomic predicates are disjoint.
     * otherwise, we would not be able to show that ap1 satisfies the community constraint (ap1 /\
     * !ap2), for example.
     */
    IntStream disjuncts =
        IntStream.range(0, originalAPs.length)
            .filter(i -> !exactlyOneAP(originalAPs, i, arg).diffSat(matchExprBDD));

    /**
     * finally return a disjunction of all the satisfying atomic predicates. two important notes.
     * first, here we do NOT use the exactlyOneAP function, because we are returning a BDD for a
     * community set, which can satisfy multiple atomic predicates due to multiple elements of the
     * set. second, we now use the APs in the arg, which properly handles configuration formats like
     * Juniper that require matching on the current route rather than the original input route.
     */
    BDD[] aps = arg.getBDDRoute().getCommunityAtomicPredicates();
    return arg.getTransferBDD()
        .getFactory()
        .orAll(disjuncts.mapToObj(i -> aps[i]).collect(Collectors.toList()));
  }

  @Override
  public BDD visitHasSize(HasSize hasSize, Arg arg) {
    throw new UnsupportedOperationException();
  }

  static BDD communityVarsToBDD(Set<CommunityVar> commVars, Arg arg) {
    TransferBDD transferBDD = arg.getTransferBDD();
    BDDRoute bddRoute = arg.getBDDRoute();
    Set<Integer> commAPs =
        transferBDD.atomicPredicatesFor(commVars, transferBDD.getCommunityAtomicPredicates());
    BDD[] apBDDs = bddRoute.getCommunityAtomicPredicates();
    return bddRoute
        .getFactory()
        .orAll(commAPs.stream().map(ap -> apBDDs[ap]).collect(Collectors.toList()));
  }

  /*
  Return a BDD that represents the scenario where the ith BDD in aps is satisfied and all others
  are falsified.
   */
  static BDD exactlyOneAP(BDD[] aps, int i, Arg arg) {
    ArrayList<BDD> negs = new ArrayList<>(Arrays.asList(aps));
    negs.remove(i);
    return aps[i].diff(arg.getTransferBDD().getFactory().orAll(negs));
  }
}
