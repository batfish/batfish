package org.batfish.minesweeper.bdd;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.bdd.TransferBDD.Context;

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

  /** Used as the argument to this visitor provoding the information that it needs in execution. */
  public static class Arg {
    private final @Nonnull TransferBDD _transferBDD;
    private final @Nonnull TransferBDD.Context _transferBDDContext;
    private final @Nonnull BDDRoute _bddRoute;

    public Arg(TransferBDD transferBDD, BDDRoute bddRoute, TransferBDD.Context context) {
      _bddRoute = bddRoute;
      _transferBDD = transferBDD;
      _transferBDDContext = context;
    }

    public @Nonnull BDDRoute getBDDRoute() {
      return _bddRoute;
    }

    public @Nonnull TransferBDD getTransferBDD() {
      return _transferBDD;
    }

    public @Nonnull Context getTransferBDDContext() {
      return _transferBDDContext;
    }

    /**
     * Resolves the named {@link AsPathMatchExpr} reference. Precondition is that undefined
     * references have been resolved or removed, and throws a runtime exception if not.
     */
    public @Nonnull AsPathMatchExpr getAsPathMatchExpr(String name) {
      AsPathMatchExpr expr = _transferBDDContext.config().getAsPathMatchExprs().get(name);
      verify(
          expr != null,
          "Invalid precondition: referenced AsPathMatchExpr %s is not defined on %s",
          name,
          getConfiguration().getHostname());
      return expr;
    }

    /**
     * Resolves the named {@link CommunityMatchExpr} reference. Precondition is that undefined
     * references have been resolved or removed, and throws a runtime exception if not.
     */
    public @Nonnull CommunityMatchExpr getCommunityMatchExpr(String name) {
      CommunityMatchExpr expr = _transferBDDContext.config().getCommunityMatchExprs().get(name);
      verify(
          expr != null,
          "Invalid precondition: referenced CommunityMatchExpr %s is not defined on %s",
          name,
          getConfiguration().getHostname());
      return expr;
    }

    /**
     * Resolves the named {@link CommunitySetExpr} reference. Precondition is that undefined
     * references have been resolved or removed, and throws a runtime exception if not.
     */
    public @Nonnull CommunitySetExpr getCommunitySetExpr(String name) {
      CommunitySetExpr expr = _transferBDDContext.config().getCommunitySetExprs().get(name);
      verify(
          expr != null,
          "Invalid precondition: referenced CommunitySetExpr %s is not defined on %s",
          name,
          getConfiguration().getHostname());
      return expr;
    }

    /**
     * Resolves the named {@link CommunitySetMatchExpr} reference. Precondition is that undefined
     * references have been resolved or removed, and throws a runtime exception if not.
     */
    public @Nonnull CommunitySetMatchExpr getCommunitySetMatchExpr(String name) {
      CommunitySetMatchExpr expr =
          _transferBDDContext.config().getCommunitySetMatchExprs().get(name);
      verify(
          expr != null,
          "Invalid precondition: referenced CommunitySetMatchExpr %s is not defined on %s",
          name,
          getConfiguration().getHostname());
      return expr;
    }

    public @Nonnull Configuration getConfiguration() {
      return _transferBDDContext.config();
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
    CommunitySetMatchExpr expr = arg.getCommunitySetMatchExpr(name);
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
                new Arg(
                    arg.getTransferBDD(),
                    arg.getTransferBDD().getOriginalRoute(),
                    arg.getTransferBDDContext()));

    // then convert a constraint on individual communities to a constraint on a community set
    return toCommunitySetConstraint(matchExprBDD, arg);
  }

  @Override
  public BDD visitHasSize(HasSize hasSize, Arg arg) {
    if (!(hasSize.getExpr() instanceof IntComparison)) {
      throw new UnsupportedOperationException(hasSize.toString());
    }
    IntComparison cmp = (IntComparison) hasSize.getExpr();
    if (!(cmp.getExpr() instanceof LiteralInt)) {
      throw new UnsupportedOperationException(hasSize.toString());
    }
    BDDFactory factory = arg.getTransferBDD().getFactory();
    int val = ((LiteralInt) cmp.getExpr()).getValue();
    return switch (cmp.getComparator()) {
      case EQ ->
          // Too hard to predict what this clause is for.
          throw new UnsupportedOperationException(hasSize.toString());
      case GE ->
          // This is likely protecting against too-large community sets.
          // Only return true if the value allows any set.
          val <= 0 ? factory.one() : factory.zero();
      case GT ->
          // This is likely protecting against too-large community sets.
          // Only return true if the value allows any set.
          val < 0 ? factory.one() : factory.zero();
      case LE ->
          // This is likely protecting against too-large community sets. Return true if the value
          // allows any set of 64 or fewer communities. Threshold was chosen semi-arbitrarily.
          val >= 64 ? factory.one() : factory.zero();
      case LT ->
          // This is likely protecting against too-large community sets. Return true if the value
          // allows any set of 64 or fewer communities. Threshold was chosen semi-arbitrarily.
          val > 64 ? factory.one() : factory.zero();
    };
  }

  static BDD communityVarsToBDD(Set<CommunityVar> commVars, Arg arg) {
    TransferBDD transferBDD = arg.getTransferBDD();
    BDDRoute bddRoute = arg.getBDDRoute();
    Set<Integer> commAPs =
        TransferBDD.atomicPredicatesFor(commVars, transferBDD.getCommunityAtomicPredicates());
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
    BDD[] aps = arg.getBDDRoute().getCommunityAtomicPredicates();
    TransferBDD tbdd = arg.getTransferBDD();
    Map<BDD, List<Integer>> cache = tbdd.getCommunitySetConstraintCache();
    List<Integer> cached = cache.get(commConstraint);
    if (cached != null) {
      return tbdd.getFactory().orAll(cached.stream().map(i -> aps[i]).toList());
    }

    BDD[] originalAPs = tbdd.getOriginalRoute().getCommunityAtomicPredicates();

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

    List<Integer> indexes = new ArrayList<>(aps.length);
    BDD constraint = commConstraint.id();
    for (int i = 0; i < originalAPs.length; i++) {
      if (!originalAPs[i].andSat(commConstraint)) {
        continue;
      }
      BDD intersection = constraint.and(originalAPs[i]);
      BDD model = intersection.satOne().existEq(originalAPs[i]);
      intersection.free();
      if (model.isNor()) {
        // Every AP mentioned is constrained to false, so originalAPs[i] satisfies the constraint.
        indexes.add(i);
      }
      model.free();
      constraint.diffEq(originalAPs[i]);
      if (constraint.isZero()) {
        LOGGER.debug(
            "Reached constraint zero on iteration {} and skipping {}",
            i,
            originalAPs.length - i - 1);
        constraint.free();
        break;
      }
    }

    LOGGER.debug("Community constraint matched {} APs", indexes.size());
    cache.put(commConstraint.id(), indexes);

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
    return arg.getTransferBDD().getFactory().orAll(indexes.stream().map(i -> aps[i]).toList());
  }

  private static final Logger LOGGER = LogManager.getLogger(CommunitySetMatchExprToBDD.class);
}
