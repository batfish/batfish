package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.batfish.minesweeper.CommunityVar;

/**
 * Create a BDD that represents a {@link CommunitySetMatchExpr}. The BDD is a predicate on community
 * atomic predicates that represents all allowed community sets. A concrete community set
 * {C1,....Cn} satisfies the BDD if AND(ap(C1),...,ap(Cn)) implies the BDD, where ap(C) denotes the
 * unique atomic predicate that the community C satisfies.
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
    BDD acc = BDDRoute.factory.zero();
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
        .reduce(BDDRoute.factory.one(), BDD::and);
  }

  @Override
  public BDD visitCommunitySetMatchAny(CommunitySetMatchAny communitySetMatchAny, Arg arg) {
    return BDDRoute.factory.orAll(
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
    throw new UnsupportedOperationException("Currently not supporting community set regexes");
  }

  @Override
  public BDD visitCommunitySetNot(CommunitySetNot communitySetNot, Arg arg) {
    // TODO: To support negation we need to change the way that we turn models into
    //  community sets in SearchRoutePolicies, because it needs to be aware not only of
    //  communities that must exist, but also those that must not exist.
    throw new UnsupportedOperationException(
        "Currently not supporting community set expression negation");
  }

  @Override
  public BDD visitHasCommunity(HasCommunity hasCommunity, Arg arg) {
    BDD matchExprBDD = hasCommunity.getExpr().accept(new CommunityMatchExprToBDD(), arg);
    /* the above BDD applies to a single community so we can't treat it as a BDD for a
      community set, or else it could be satisfied by introducing multiple communities in the set.
      to avoid this problem, we convert this BDD to the largest disjunction of atomic predicates
      that implies the BDD, thereby ensuring that in the end a community matching one of these
      atomic predicates will exist in the community set.
    */
    BDD[] aps = arg.getBDDRoute().getCommunityAtomicPredicates();
    return BDDRoute.factory.orAll(
        Arrays.stream(aps).filter(ap -> !ap.diffSat(matchExprBDD)).collect(Collectors.toList()));
  }

  static BDD communityVarsToBDD(Set<CommunityVar> commVars, Arg arg) {
    TransferBDD transferBDD = arg.getTransferBDD();
    BDDRoute bddRoute = arg.getBDDRoute();
    Set<Integer> commAPs =
        transferBDD.atomicPredicatesFor(commVars, transferBDD.getCommunityAtomicPredicates());
    BDD[] apBDDs = bddRoute.getCommunityAtomicPredicates();
    return BDDRoute.factory.orAll(
        commAPs.stream().map(ap -> apBDDs[ap]).collect(Collectors.toList()));
  }
}
