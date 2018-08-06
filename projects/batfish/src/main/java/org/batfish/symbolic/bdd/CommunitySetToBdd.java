package org.batfish.symbolic.bdd;

import static org.batfish.symbolic.bdd.BDDRoute.factory;
import static org.batfish.symbolic.bdd.CommunityVarConverter.toCommunityVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.routing_policy.expr.CommunityHalvesExpr;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.EmptyCommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunityConjunction;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.visitors.CommunitySetExprVisitor;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.TransferParam;

public final class CommunitySetToBdd implements CommunitySetExprVisitor<BDD> {

  public static BDD convert(
      TransferParam<BDDRoute> indent,
      Configuration conf,
      CommunitySetExpr expr,
      BDDRoute data,
      TransferBDD caller) {
    return expr.accept(new CommunitySetToBdd(indent, conf, data, caller));
  }

  private final TransferBDD _caller;

  private final Configuration _conf;

  private final BDDRoute _other;

  private final TransferParam<BDDRoute> _p;

  private CommunitySetToBdd(
      TransferParam<BDDRoute> p, Configuration conf, BDDRoute other, TransferBDD caller) {
    _p = p;
    _conf = conf;
    _other = other;
    _caller = caller;
  }

  @Override
  public BDD visitCommunityHalvesExpr(CommunityHalvesExpr communityHalvesExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BDD visitCommunityList(CommunityList communityList) {
    List<CommunityListLine> lines = new ArrayList<>(communityList.getLines());
    Collections.reverse(lines);
    BDD acc = factory.zero();
    for (CommunityListLine line : lines) {
      boolean action = (line.getAction() == LineAction.ACCEPT);
      CommunityVar cvar = toCommunityVar(line.getMatchCondition());
      _p.debug("Match Line: " + cvar);
      _p.debug("Action: " + line.getAction());
      // Skip this match if it is irrelevant
      if (_caller._policyQuotient.getCommsMatchedButNotAssigned().contains(cvar)) {
        continue;
      }
      if (cvar.getType() == Type.REGEX) {
        List<CommunityVar> deps = _caller._commDeps.get(cvar);
        for (CommunityVar dep : deps) {
          _p.debug("Test for: " + dep);
          BDD c = _other.getCommunities().get(dep);
          acc = _caller.ite(c, _caller.mkBDD(action), acc);
        }
      } else if (cvar.getType() == Type.EXACT) {
        BDD c = _other.getCommunities().get(cvar);
        if (c == null) {
          throw new BatfishException("matchCommunitySet: should not be null");
        }
        acc = _caller.ite(c, _caller.mkBDD(action), acc);
      } else {
        throw new BatfishException("Unhandled cvar type: " + cvar.getType());
      }
    }
    return acc;
  }

  @Override
  public BDD visitEmptyCommunitySetExpr(EmptyCommunitySetExpr emptyCommunitySetExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BDD visitLiteralCommunity(LiteralCommunity literalCommunity) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BDD visitLiteralCommunityConjunction(
      LiteralCommunityConjunction literalCommunityConjunction) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BDD visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BDD visitNamedCommunitySet(NamedCommunitySet namedCommunitySet) {
    _p.debug("Named");
    NamedCommunitySet x = (NamedCommunitySet) namedCommunitySet;
    CommunityList cl = _conf.getCommunityLists().get(x.getName());
    _p.debug("Named Community Set: " + cl.getName());
    return visitCommunityList(cl);
  }

  @Override
  public BDD visitRegexCommunitySet(RegexCommunitySet regexCommunitySet) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
