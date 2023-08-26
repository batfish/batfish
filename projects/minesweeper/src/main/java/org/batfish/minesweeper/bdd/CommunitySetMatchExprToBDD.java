package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
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
    BDD matchExprBDD =
        hasCommunity
            .getExpr()
            .accept(
                new CommunityMatchExprToBDD(),
                new Arg(arg.getTransferBDD(), arg.getTransferBDD().getOriginalRoute()));

    // convert a constraint on individual communities to a constraint on a community set
    return toCommunitySetConstraint(matchExprBDD, arg);
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

  /**
   * Converts a constraint on individual communities to a constraint on community sets. This
   * involves figuring out which community atomic predicates satisfy the given community constraint,
   * and then producing a disjunction of them.
   *
   * @param commConstraint the community constraint
   * @param arg provides access to the context that we need, such as the atomic predicates
   * @return a community set constraint
   */
  public static BDD toCommunitySetConstraint(BDD commConstraint, Arg arg) {
    BDD[] originalAPs = arg.getTransferBDD().getOriginalRoute().getCommunityAtomicPredicates();
    /*
     * this is a bit more complicated than just checking that an atomic predicate implies the
     * commConstraint -- if we did that, then we would not be able to show that ap1 satisfies the
     * community constraint (ap1 /\ !ap2), for example. since all APs are disjoint, however, ap1
     * should be considered to satisfy this constraint.
     */
    IntStream satisfyingAPs =
        IntStream.range(0, originalAPs.length)
            .filter(
                i -> {
                  // check that the ith AP is compatible with commConstraint
                  BDD model = commConstraint.and(originalAPs[i]).satOne();
                  if (model.isZero()) {
                    return false;
                  }
                  // if so, check that all other variables in the produced model are negated;
                  // this implies that the ith AP on its own is sufficient
                  return allNegativeLiterals(model.exist(originalAPs[i]));
                });

    /**
     * finally return a disjunction of all the satisfying atomic predicates. we now use the APs in
     * the arg, which properly handles configuration formats like Juniper that require matching on
     * the current route rather than the original input route.
     */
    BDD[] aps = arg.getBDDRoute().getCommunityAtomicPredicates();
    return arg.getTransferBDD()
        .getFactory()
        .orAll(satisfyingAPs.mapToObj(i -> aps[i]).collect(Collectors.toList()));
  }

  // Checks whether all variables in the given variable assignment are negated.
  static boolean allNegativeLiterals(BDD model) {
    if (model.isZero() || model.isOne()) {
      return true;
    } else if (model.high().isZero()) {
      return allNegativeLiterals(model.low());
    } else {
      return false;
    }
  }
}
