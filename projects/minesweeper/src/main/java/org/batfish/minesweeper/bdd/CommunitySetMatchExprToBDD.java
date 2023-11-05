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
  public static class Arg {
    private final @Nonnull TransferBDD _transferBDD;
    private final @Nonnull BDDRoute _bddRoute;

    public Arg(TransferBDD transferBDD, BDDRoute bddRoute) {
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
    throw new UnsupportedOperationException(communitySetMatchRegex.toString());
  }

  @Override
  public BDD visitCommunitySetNot(CommunitySetNot communitySetNot, Arg arg) {
    return communitySetNot.getExpr().accept(this, arg).not();
  }

  @Override
  public BDD visitHasCommunity(HasCommunity hasCommunity, Arg arg) {

    /*
     * first get a BDD for the constraint on a single community that must be in the set. we use the
     * original route here so that the constraint will be in terms of the community APs.
     */
    BDD matchExprBDD =
        hasCommunity
            .getExpr()
            .accept(
                new CommunityMatchExprToBDD(),
                new Arg(arg.getTransferBDD(), arg.getTransferBDD().getOriginalRoute()));

    // then convert a constraint on individual communities to a constraint on a community set
    return toCommunitySetConstraint(matchExprBDD, arg);
  }

  @Override
  public BDD visitHasSize(HasSize hasSize, Arg arg) {
    throw new UnsupportedOperationException(hasSize.toString());
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
     * The given community constraint is a predicate on community atomic predicates that must be
     * satisfied by *some* element of the route's community set. We can't directly treat this
     * predicate as a predicate on community sets for a few reasons. First, negation on an
     * individual community is not the same as negation on a community set. For example, the
     * single-community constraint (ap1 /\ !ap2) is satisfied if there exists a community that
     * satisfies ap1 and falsifies ap2. But as a community-set constraint, this same predicate
     * requires the set to contain a community satisfying ap1 and to not contain any communities
     * satisfying ap2. Second, community atomic predicates represent disjoint communities, by
     * construction. So the single-community constraint (ap1 /\ ap2) is not satisfiable. But as a
     * community-set constraint, this same predicate is satisfied by a community set containing a
     * community satisfying ap1 and another community satisfying ap2.
     *
     * Therefore, to convert a single-community constraint to a community-set constraint, we
     * identify all communities that satisfy the single-community constraint, taking into account
     * the fact that atomic predicates are disjoint, and then we produce a disjunction of them as
     * the community-set constraint.
     */

    // consider each atomic predicate in turn
    IntStream satisfyingAPs =
        IntStream.range(0, originalAPs.length)
            .filter(
                i -> {
                  // check that the ith AP is compatible with the single-community constraint
                  BDD model = commConstraint.and(originalAPs[i]).satOne();
                  if (model.isZero()) {
                    return false;
                  }
                  // if so, check that all other variables in the produced model are negated;
                  // this implies that the ith AP on its own is sufficient to satisfy the constraint
                  return allNegativeLiterals(model.exist(originalAPs[i]));
                });

    // TODO: Two potential performance optimizations to consider in the future.  First, a binary
    // encoding of atomic predicates for single-community constraints, for example using the
    // BDDDomain class, would avoid the need to explicitly enforce disjointness of atomic
    // predicates. Second, it may be possible to traverse commConstraint once to find all relevant
    // APs, instead of traversing models of the constraint.

    /*
     * finally return a disjunction of all the satisfying atomic predicates. we use the APs in
     * the arg, which properly handles configuration formats like Juniper that require matching on
     * the current route rather than the original input route.
     */
    BDD[] aps = arg.getBDDRoute().getCommunityAtomicPredicates();
    return arg.getTransferBDD()
        .getFactory()
        .orAll(satisfyingAPs.mapToObj(i -> aps[i]).collect(Collectors.toList()));
  }

  /**
   * Checks whether all variables in the given variable assignment are negated.
   *
   * @param model the variable assignment
   * @return a boolean indicating whether the check succeeded
   */
  static boolean allNegativeLiterals(BDD model) {
    if (model.isZero() || model.isOne()) {
      return true;
    }
    if (model.high().isZero()) {
      return allNegativeLiterals(model.low());
    }
    return false;
  }
}
