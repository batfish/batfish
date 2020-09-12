package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
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
    CommunityVar cvar = CommunityVar.from(communitySetMatchRegex.getRegex());
    return communityVarsToBDD(ImmutableSet.of(cvar), arg);
  }

  @Override
  public BDD visitCommunitySetNot(CommunitySetNot communitySetNot, Arg arg) {
    return communitySetNot.getExpr().accept(this, arg).not();
  }

  @Override
  public BDD visitHasCommunity(HasCommunity hasCommunity, Arg arg) {
    return hasCommunity.getExpr().accept(new CommunityMatchExprToBDD(), arg);
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
