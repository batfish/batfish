package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.DisjunctionChain;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.InlineCommunitySet;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.expr.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchIpv6;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MultipliedAs;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.RetainCommunity;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.TransferParam;
import org.batfish.symbolic.TransferResult;
import org.batfish.symbolic.smt.EdgeType;
import org.batfish.symbolic.utils.PrefixUtils;

class TransferBuilder {

  private BDDNetwork _network;

  private SortedMap<CommunityVar, List<CommunityVar>> _commDeps;

  private BDDNetFactory _netFactory;

  private Graph _graph;

  private Configuration _conf;

  private GraphEdge _edge;

  private EdgeType _edgeType;

  private RoutingProtocol _protocol;

  private PolicyQuotient _policyQuotient;

  private Set<Prefix> _ignoredNetworks;

  @Nullable private List<Statement> _statements;

  TransferBuilder(
      BDDNetwork network,
      Graph g,
      Configuration conf,
      @Nullable GraphEdge edge,
      EdgeType edgeType,
      RoutingProtocol proto,
      @Nullable List<Statement> statements,
      PolicyQuotient pq) {
    _network = network;
    _netFactory = network.getNetFactory();
    _graph = g;
    _conf = conf;
    _edge = edge;
    _edgeType = edgeType;
    _protocol = proto;
    _policyQuotient = pq;
    _statements = statements;
  }

  /*
   * Apply the effect of modifying an integer value (e.g., to set the local pref)
   */
  /* private BDDInteger applyIntExprModification(TransferParam<BDDRoute> p,
        BDDInteger x, IntExpr e) {
    if (e instanceof LiteralInt) {
      LiteralInt z = (LiteralInt) e;
      p.debug("LiteralInt: " + z.getValue());
      return BDDInteger.makeFromValue(x.getFactory(), 32, z.getValue());
    }
    if (e instanceof IncrementLocalPreference) {
      IncrementLocalPreference z = (IncrementLocalPreference) e;
      p.debug("IncrementLocalPreference: " + z.getAddend());
      return x.add(BDDInteger.makeFromValue(x.getFactory(), 32, z.getAddend()));
    }
    if (e instanceof DecrementLocalPreference) {
      DecrementLocalPreference z = (DecrementLocalPreference) e;
      p.debug("DecrementLocalPreference: " + z.getSubtrahend());
      return x.sub(BDDInteger.makeFromValue(x.getFactory(), 32, z.getSubtrahend()));
    }
    throw new BatfishException("TODO: int expr transfer function: " + e);
  } */

  /*
   * Apply the effect of modifying an integer value (e.g., to set the local pref)
   */
  private BDDFiniteDomain<Integer> applyIntExprModification(
      TransferParam<BDDRoute> p, BDDFiniteDomain<Integer> x, IntExpr e) {
    if (e instanceof LiteralInt) {
      LiteralInt z = (LiteralInt) e;
      p.debug("LiteralInt: " + z.getValue());
      BDDFiniteDomain<Integer> y = new BDDFiniteDomain<>(x);
      y.setValue(z.getValue());
      return y;
    }
    throw new BatfishException("TODO: int expr transfer function: " + e);
  }

  /*
   * Apply the effect of modifying a long value (e.g., to set the metric)
   */
  private BDDInteger applyLongExprModification(
      TransferParam<BDDRoute> p, BDDInteger x, LongExpr e, int size) {
    if (e instanceof LiteralLong) {
      LiteralLong z = (LiteralLong) e;
      p.debug("LiteralLong: " + z.getValue());
      return BDDInteger.makeFromValue(x.getFactory(), size, z.getValue());
    }
    if (e instanceof DecrementMetric) {
      DecrementMetric z = (DecrementMetric) e;
      p.debug("Decrement: " + z.getSubtrahend());
      return x.sub(BDDInteger.makeFromValue(x.getFactory(), size, z.getSubtrahend()));
    }
    if (e instanceof IncrementMetric) {
      IncrementMetric z = (IncrementMetric) e;
      p.debug("Increment: " + z.getAddend());
      return x.add(BDDInteger.makeFromValue(x.getFactory(), size, z.getAddend()));
    }
    throw new BatfishException("int expr transfer function: " + e);
  }

  /*
   * Convert a Batfish AST boolean expression to a symbolic Z3 boolean expression
   * by performing inlining of stateful side effects.
   */
  private TransferResult<BDDTransferFunction, BDD> compute(
      BooleanExpr expr, TransferParam<BDDRoute> p) {

    // TODO: right now everything is IPV4
    if (expr instanceof MatchIpv4) {
      p.debug("MatchIpv4");
      BDDTransferFunction ret = new BDDTransferFunction(p.getData(), _netFactory.one());
      p.debug("MatchIpv4 Result: " + ret);
      return fromExpr(ret);
    }
    if (expr instanceof MatchIpv6) {
      p.debug("MatchIpv6");
      BDDTransferFunction ret = new BDDTransferFunction(p.getData(), _netFactory.zero());
      return fromExpr(ret);
    }

    if (expr instanceof Conjunction) {
      p.debug("Conjunction");
      Conjunction c = (Conjunction) expr;
      BDD acc = _netFactory.one();
      TransferResult<BDDTransferFunction, BDD> result = new TransferResult<>();
      TransferParam<BDDRoute> record = p;
      for (BooleanExpr be : c.getConjuncts()) {
        TransferParam<BDDRoute> param = record.indent();
        TransferResult<BDDTransferFunction, BDD> r = compute(be, param);
        record = record.setData(r.getReturnValue().getRoute());
        acc = acc.and(r.getReturnValue().getFilter());
      }
      BDDTransferFunction ret = new BDDTransferFunction(record.getData(), acc);
      p.debug("Conjunction return: " + record.getData().hashCode() + "," + acc.hashCode());
      return result.setReturnValue(ret);
    }

    if (expr instanceof Disjunction) {
      p.debug("Disjunction");
      Disjunction d = (Disjunction) expr;
      BDD acc = _netFactory.zero();
      TransferResult<BDDTransferFunction, BDD> result = new TransferResult<>();
      TransferParam<BDDRoute> record = p;
      for (BooleanExpr be : d.getDisjuncts()) {
        TransferParam<BDDRoute> param = record.indent();
        TransferResult<BDDTransferFunction, BDD> r = compute(be, param);
        record = record.setData(r.getReturnValue().getRoute());
        acc = acc.or(r.getReturnValue().getFilter());
      }
      BDDTransferFunction ret = new BDDTransferFunction(record.getData(), acc);
      p.debug("Disjunction return: " + record.getData().hashCode() + "," + acc.hashCode());
      return result.setReturnValue(ret);
    }

    // TODO: thread the BDDRecord through calls
    if (expr instanceof ConjunctionChain) {
      p.debug("ConjunctionChain");
      ConjunctionChain d = (ConjunctionChain) expr;
      List<BooleanExpr> conjuncts = new ArrayList<>(d.getSubroutines());
      if (p.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(p.getDefaultPolicy().getDefaultPolicy());
        conjuncts.add(be);
      }
      if (conjuncts.size() == 0) {
        BDDTransferFunction ret = new BDDTransferFunction(p.getData(), _netFactory.one());
        return fromExpr(ret);
      } else {
        TransferResult<BDDTransferFunction, BDD> result = new TransferResult<>();
        TransferParam<BDDRoute> record = p;
        BDD acc = _netFactory.zero();
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr conjunct = conjuncts.get(i);
          TransferParam<BDDRoute> param =
              record
                  .setDefaultPolicy(null)
                  .setChainContext(TransferParam.ChainContext.CONJUNCTION)
                  .indent();
          TransferResult<BDDTransferFunction, BDD> r = compute(conjunct, param);
          record = record.setData(r.getReturnValue().getRoute());
          acc = ite(r.getFallthroughValue(), acc, r.getReturnValue().getFilter());
        }
        BDDTransferFunction ret = new BDDTransferFunction(record.getData(), acc);
        return result.setReturnValue(ret);
      }
    }

    if (expr instanceof DisjunctionChain) {
      p.debug("DisjunctionChain");
      DisjunctionChain d = (DisjunctionChain) expr;
      List<BooleanExpr> disjuncts = new ArrayList<>(d.getSubroutines());
      if (p.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(p.getDefaultPolicy().getDefaultPolicy());
        disjuncts.add(be);
      }
      if (disjuncts.size() == 0) {
        BDDTransferFunction ret = new BDDTransferFunction(p.getData(), _netFactory.zero());
        return fromExpr(ret);
      } else {
        TransferResult<BDDTransferFunction, BDD> result = new TransferResult<>();
        TransferParam<BDDRoute> record = p;
        BDD acc = _netFactory.zero();
        for (int i = disjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr disjunct = disjuncts.get(i);
          TransferParam<BDDRoute> param =
              record
                  .setDefaultPolicy(null)
                  .setChainContext(TransferParam.ChainContext.CONJUNCTION)
                  .indent();
          TransferResult<BDDTransferFunction, BDD> r = compute(disjunct, param);
          record = record.setData(r.getReturnValue().getRoute());
          acc = ite(r.getFallthroughValue(), acc, r.getReturnValue().getFilter());
        }
        BDDTransferFunction ret = new BDDTransferFunction(record.getData(), acc);
        return result.setReturnValue(ret);
      }
    }

    if (expr instanceof Not) {
      p.debug("Not");
      Not n = (Not) expr;
      TransferResult<BDDTransferFunction, BDD> result = compute(n.getExpr(), p);
      BDDTransferFunction r = result.getReturnValue();
      BDDTransferFunction ret = new BDDTransferFunction(r.getRoute(), r.getFilter().not());
      return result.setReturnValue(ret);
    }

    if (expr instanceof MatchProtocol) {
      MatchProtocol mp = (MatchProtocol) expr;
      if (!_netFactory.getAllProtos().contains(mp.getProtocol())) {
        p.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): false");
        BDDTransferFunction ret = new BDDTransferFunction(p.getData(), _netFactory.zero());
        return fromExpr(ret);
      }
      BDD protoMatch = _netFactory.one();
      if (_netFactory.getConfig().getKeepProtocol()) {
        protoMatch = p.getData().getProtocolHistory().value(mp.getProtocol());
      }
      p.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): " + protoMatch);
      BDDTransferFunction ret = new BDDTransferFunction(p.getData(), protoMatch);
      return fromExpr(ret);
    }

    if (expr instanceof MatchPrefixSet) {
      p.debug("MatchPrefixSet");
      MatchPrefixSet m = (MatchPrefixSet) expr;

      BDD r = matchPrefixSet(p.indent(), _conf, m.getPrefixSet(), p.getData());
      BDDTransferFunction ret = new BDDTransferFunction(p.getData(), r);
      return fromExpr(ret);

      // TODO: implement me
    } else if (expr instanceof MatchPrefix6Set) {
      p.debug("MatchPrefix6Set");
      BDDTransferFunction ret = new BDDTransferFunction(p.getData(), _netFactory.zero());
      return fromExpr(ret);

    } else if (expr instanceof CallExpr) {
      p.debug("CallExpr");
      CallExpr c = (CallExpr) expr;
      String router = _conf.getName();
      String name = c.getCalledPolicyName();
      TransferResult<BDDTransferFunction, BDD> r = _network.getTransferCache().get(router, name, p);
      if (r != null) {
        return r;
      }
      RoutingPolicy pol = _conf.getRoutingPolicies().get(name);
      p = p.setCallContext(TransferParam.CallContext.EXPR_CALL);
      r = compute(pol.getStatements(), p.indent().enterScope(name));
      _network.getTransferCache().put(router, name, p, r);
      return r;

    } else if (expr instanceof WithEnvironmentExpr) {
      p.debug("WithEnvironmentExpr");
      // TODO: this is not correct
      WithEnvironmentExpr we = (WithEnvironmentExpr) expr;
      // TODO: postStatements() and preStatements()
      return compute(we.getExpr(), p.deepCopy());

    } else if (expr instanceof MatchCommunitySet) {
      p.debug("MatchCommunitySet");
      MatchCommunitySet mcs = (MatchCommunitySet) expr;
      BDD c = matchCommunitySet(p.indent(), _conf, mcs.getExpr(), p.getData());
      BDDTransferFunction ret = new BDDTransferFunction(p.getData(), c);
      return fromExpr(ret);

    } else if (expr instanceof BooleanExprs.StaticBooleanExpr) {
      BooleanExprs.StaticBooleanExpr b = (BooleanExprs.StaticBooleanExpr) expr;
      BDDTransferFunction ret;
      switch (b.getType()) {
        case CallExprContext:
          p.debug("CallExprContext");
          BDD x1 = mkBDD(p.getCallContext() == TransferParam.CallContext.EXPR_CALL);
          ret = new BDDTransferFunction(p.getData(), x1);
          return fromExpr(ret);
        case CallStatementContext:
          p.debug("CallStmtContext");
          BDD x2 = mkBDD(p.getCallContext() == TransferParam.CallContext.STMT_CALL);
          ret = new BDDTransferFunction(p.getData(), x2);
          return fromExpr(ret);
        case True:
          p.debug("True");
          ret = new BDDTransferFunction(p.getData(), _netFactory.one());
          return fromExpr(ret);
        case False:
          p.debug("False");
          ret = new BDDTransferFunction(p.getData(), _netFactory.zero());
          return fromExpr(ret);
        default:
          throw new BatfishException(
              "Unhandled " + BooleanExprs.class.getCanonicalName() + ": " + b.getType());
      }

    } else if (expr instanceof MatchAsPath) {
      p.debug("MatchAsPath");
      // System.out.println("Warning: use of unimplemented feature MatchAsPath");
      BDDTransferFunction ret = new BDDTransferFunction(p.getData(), _netFactory.one());
      return fromExpr(ret);
    }

    throw new BatfishException("TODO: compute expr transfer function: " + expr);
  }

  /*
   * Convert a list of statements into a Z3 boolean expression for the transfer function.
   */
  private TransferResult<BDDTransferFunction, BDD> compute(
      List<Statement> statements, TransferParam<BDDRoute> p) {
    boolean doesReturn = false;

    TransferResult<BDDTransferFunction, BDD> result = new TransferResult<>();
    result =
        result
            .setReturnValue(new BDDTransferFunction(p.getData(), _netFactory.zero()))
            .setFallthroughValue(_netFactory.zero())
            .setReturnAssignedValue(_netFactory.zero());

    for (Statement stmt : statements) {

      if (stmt instanceof StaticStatement) {
        StaticStatement ss = (StaticStatement) stmt;

        switch (ss.getType()) {
          case ExitAccept:
            doesReturn = true;
            p.debug("ExitAccept");
            result = returnValue(result, true);
            break;

          case ReturnTrue:
            doesReturn = true;
            p.debug("ReturnTrue");
            result = returnValue(result, true);
            break;

          case ExitReject:
            doesReturn = true;
            p.debug("ExitReject");
            result = returnValue(result, false);
            break;

          case ReturnFalse:
            doesReturn = true;
            p.debug("ReturnFalse");
            result = returnValue(result, false);
            break;

          case SetDefaultActionAccept:
            p.debug("SetDefaulActionAccept");
            p = p.setDefaultAccept(true);
            break;

          case SetDefaultActionReject:
            p.debug("SetDefaultActionReject");
            p = p.setDefaultAccept(false);
            break;

          case SetLocalDefaultActionAccept:
            p.debug("SetLocalDefaultActionAccept");
            p = p.setDefaultAcceptLocal(true);
            break;

          case SetLocalDefaultActionReject:
            p.debug("SetLocalDefaultActionReject");
            p = p.setDefaultAcceptLocal(false);
            break;

          case ReturnLocalDefaultAction:
            p.debug("ReturnLocalDefaultAction");
            // TODO: need to set local default action in an environment
            if (p.getDefaultAcceptLocal()) {
              result = returnValue(result, true);
            } else {
              result = returnValue(result, false);
            }
            break;

          case FallThrough:
            p.debug("Fallthrough");
            result = fallthrough(result);
            break;

          case Return:
            // TODO: assumming this happens at the end of the function, so it is ignored for now.
            p.debug("Return");
            break;

          case RemovePrivateAs:
            p.debug("RemovePrivateAs");
            // System.out.println("Warning: use of unimplemented feature RemovePrivateAs");
            break;

          default:
            throw new BatfishException("TODO: computeTransferFunction: " + ss.getType());
        }

      } else if (stmt instanceof If) {
        p.debug("If: " + result.getReturnValue().getRoute().hashCode());
        If i = (If) stmt;
        TransferResult<BDDTransferFunction, BDD> r = compute(i.getGuard(), p.indent());
        BDD guard = r.getReturnValue().getFilter();
        BDDRoute current = r.getReturnValue().getRoute();

        TransferParam<BDDRoute> pTrue = p.indent().setData(current.deepCopy());
        TransferParam<BDDRoute> pFalse = p.indent().setData(current.deepCopy());
        p.debug("True Branch: " + p.getData().hashCode());
        TransferResult<BDDTransferFunction, BDD> trueBranch = compute(i.getTrueStatements(), pTrue);
        p.debug("False Branch: " + p.getData().hashCode());
        TransferResult<BDDTransferFunction, BDD> falseBranch =
            compute(i.getFalseStatements(), pFalse);

        BDDRoute r1 = trueBranch.getReturnValue().getRoute();
        BDDRoute r2 = falseBranch.getReturnValue().getRoute();
        BDDRoute recordVal = ite(guard, r1, r2);

        // update return values
        BDD returnVal =
            ite(
                result.getReturnAssignedValue(),
                result.getReturnValue().getFilter(),
                ite(
                    guard,
                    trueBranch.getReturnValue().getFilter(),
                    falseBranch.getReturnValue().getFilter()));

        BDD returnAss =
            result
                .getReturnAssignedValue()
                .or(
                    ite(
                        guard,
                        trueBranch.getReturnAssignedValue(),
                        falseBranch.getReturnAssignedValue()));

        BDD fallThrough =
            result
                .getFallthroughValue()
                .or(
                    ite(
                        guard,
                        trueBranch.getFallthroughValue(),
                        falseBranch.getFallthroughValue()));

        result =
            result
                .setReturnValue(new BDDTransferFunction(recordVal, returnVal))
                .setReturnAssignedValue(returnAss)
                .setFallthroughValue(fallThrough);

        p.debug("If Return: " + result.getReturnValue().getRoute().hashCode());

      } else if (stmt instanceof SetDefaultPolicy) {
        p.debug("SetDefaultPolicy");
        p = p.setDefaultPolicy((SetDefaultPolicy) stmt);

      } else if (stmt instanceof SetMetric) {
        p.debug("SetMetric");
        if (p.getData().getConfig().getKeepMetric()) {
          SetMetric sm = (SetMetric) stmt;
          LongExpr ie = sm.getMetric();
          BDDRoute route = result.getReturnValue().getRoute();
          BDD isBGP = route.getProtocolHistory().value(RoutingProtocol.BGP);
          BDD updateMet = isBGP.not().and(result.getReturnAssignedValue());
          BDDInteger newValue =
              applyLongExprModification(
                  p.indent(), route.getMetric(), ie, _netFactory.getNumBitsMetric());
          BDDInteger met = ite(updateMet, route.getMetric(), newValue);
          route.setMetric(met);
          p = p.setData(route);
        }

      } else if (stmt instanceof SetOspfMetricType) {
        p.debug("SetOspfMetricType");
        if (p.getData().getConfig().getKeepOspfMetric()) {
          SetOspfMetricType somt = (SetOspfMetricType) stmt;
          OspfMetricType mt = somt.getMetricType();
          BDDFiniteDomain<OspfType> current = result.getReturnValue().getRoute().getOspfMetric();
          BDDFiniteDomain<OspfType> newValue = new BDDFiniteDomain<>(current);
          if (mt == OspfMetricType.E1) {
            p.indent().debug("Value: E1");
            newValue.setValue(OspfType.E1);
          } else {
            p.indent().debug("Value: E2");
            newValue.setValue(OspfType.E1);
          }
          BDDRoute route = result.getReturnValue().getRoute();
          newValue = ite(result.getReturnAssignedValue(), route.getOspfMetric(), newValue);
          route.setOspfMetric(newValue);
          p = p.setData(route);
        }

      } else if (stmt instanceof SetLocalPreference) {
        p.debug("SetLocalPreference");
        if (p.getData().getConfig().getKeepLp()) {
          SetLocalPreference slp = (SetLocalPreference) stmt;
          IntExpr ie = slp.getLocalPreference();
          BDDRoute route = result.getReturnValue().getRoute();
          BDDFiniteDomain<Integer> newValue =
              applyIntExprModification(p.indent(), route.getLocalPref(), ie);
          newValue = ite(result.getReturnAssignedValue(), route.getLocalPref(), newValue);
          route.setLocalPref(newValue);
          p = p.setData(route);
          p.debug("SetLocalPreference Done: " + p.getData().hashCode());
        }

      } else if (stmt instanceof AddCommunity) {
        p.debug("AddCommunity");
        if (p.getData().getConfig().getKeepCommunities()) {
          AddCommunity ac = (AddCommunity) stmt;
          Set<CommunityVar> comms = _graph.findAllCommunities(_conf, ac.getExpr());
          for (CommunityVar cvar : comms) {
            if (!_policyQuotient.getCommsAssignedButNotMatched().contains(cvar)) {
              p.indent().debug("Value: " + cvar);
              BDDRoute route = result.getReturnValue().getRoute();
              BDD comm = route.getCommunities().get(cvar);
              BDD newValue = ite(result.getReturnAssignedValue(), comm, _netFactory.one());
              p.indent().debug("New Value is true?: " + newValue.isOne());
              route.getCommunities().put(cvar, newValue);
              p = p.setData(route);
            }
          }
        }

      } else if (stmt instanceof SetCommunity) {
        p.debug("SetCommunity");
        if (p.getData().getConfig().getKeepCommunities()) {
          SetCommunity sc = (SetCommunity) stmt;
          Set<CommunityVar> comms = _graph.findAllCommunities(_conf, sc.getExpr());
          for (CommunityVar cvar : comms) {
            if (!_policyQuotient.getCommsAssignedButNotMatched().contains(cvar)) {
              p.indent().debug("Value: " + cvar);
              BDDRoute route = result.getReturnValue().getRoute();
              BDD comm = route.getCommunities().get(cvar);
              BDD newValue = ite(result.getReturnAssignedValue(), comm, _netFactory.one());
              p.indent().debug("New Value: " + newValue);
              route.getCommunities().put(cvar, newValue);
              p = p.setData(route);
            }
          }
        }

      } else if (stmt instanceof DeleteCommunity) {
        p.debug("DeleteCommunity");
        if (p.getData().getConfig().getKeepCommunities()) {
          DeleteCommunity ac = (DeleteCommunity) stmt;
          Set<CommunityVar> comms = _graph.findAllCommunities(_conf, ac.getExpr());
          Set<CommunityVar> toDelete = new HashSet<>();
          // Find comms to delete
          for (CommunityVar cvar : comms) {
            if (cvar.getType() == Type.REGEX) {
              toDelete.addAll(_commDeps.get(cvar));
            } else {
              toDelete.add(cvar);
            }
          }
          // Delete the comms
          for (CommunityVar cvar : toDelete) {
            if (!_policyQuotient.getCommsAssignedButNotMatched().contains(cvar)) {
              p.indent().debug("Value: " + cvar.getValue() + ", " + cvar.getType());
              BDDRoute route = result.getReturnValue().getRoute();
              BDD comm = route.getCommunities().get(cvar);
              BDD newValue = ite(result.getReturnAssignedValue(), comm, _netFactory.zero());
              p.indent().debug("New Value: " + newValue);
              route.getCommunities().put(cvar, newValue);
              p = p.setData(route);
            }
          }
        }

      } else if (stmt instanceof RetainCommunity) {
        p.debug("RetainCommunity");
        // no op

      } else if (stmt instanceof PrependAsPath) {
        p.debug("PrependAsPath");
        if (p.getData().getConfig().getKeepMetric()) {
          PrependAsPath pap = (PrependAsPath) stmt;
          Integer prependCost = prependLength(pap.getExpr());
          p.indent().debug("Cost: " + prependCost);
          BDDRoute route = result.getReturnValue().getRoute();
          BDDInteger met = route.getMetric();
          BDDInteger newValue =
              met.add(
                  BDDInteger.makeFromValue(
                      met.getFactory(), _netFactory.getNumBitsMetric(), prependCost));
          newValue = ite(result.getReturnAssignedValue(), route.getMetric(), newValue);
          route.setMetric(newValue);
          p = p.setData(route);
        }

      } else if (stmt instanceof SetOrigin) {
        p.debug("SetOrigin");
        // System.out.println("Warning: use of unimplemented feature SetOrigin");
        // TODO: implement me

      } else if (stmt instanceof SetNextHop) {
        p.debug("SetNextHop");
        // System.out.println("Warning: use of unimplemented feature SetNextHop");
        // TODO: implement me

      } else {
        throw new BatfishException("TODO: statement transfer function: " + stmt);
      }
    }

    // If this is the outermost call, then we relate the routeVariables
    if (p.getInitialCall()) {
      p.debug("InitialCall finalizing");
      // Apply the default action
      if (!doesReturn) {
        p.debug("Applying default action: " + p.getDefaultAccept());
        if (p.getDefaultAccept()) {
          result = returnValue(result, true);
        } else {
          result = returnValue(result, false);
        }
      }

      // Set all the values to 0 if the return is not true;
      // BDDTransferFunction ret = result.getReturnValue();
      // BDDRoute retVal = ite(ret.getFilter(), ret.getRoute(), zeroedRecord());
      // result = result.setReturnValue(new BDDTransferFunction(retVal, ret.getFilter()));
    }
    BDDTransferFunction tf = result.getReturnValue();
    p.debug("Final result: " + tf.getRoute().hashCode() + "," + tf.getFilter().hashCode());
    return result;
  }

  private TransferResult<BDDTransferFunction, BDD> fallthrough(
      TransferResult<BDDTransferFunction, BDD> r) {
    BDD b = ite(r.getReturnAssignedValue(), r.getFallthroughValue(), _netFactory.one());
    return r.setFallthroughValue(b).setReturnAssignedValue(_netFactory.one());
  }

  /*
   * Wrap a simple boolean expression return value in a transfer function result
   */
  private TransferResult<BDDTransferFunction, BDD> fromExpr(BDDTransferFunction b) {
    return new TransferResult<BDDTransferFunction, BDD>()
        .setReturnAssignedValue(_netFactory.one())
        .setReturnValue(b);
  }

  /*
   * If-then-else statement
   */
  private BDD ite(BDD b, BDD x, BDD y) {
    return b.ite(x, y);
  }

  /*
   * Map ite over BDDInteger type
   */
  private BDDInteger ite(BDD b, BDDInteger x, BDDInteger y) {
    return x.ite(b, y);
  }

  /*
   * Map ite over BDDFiniteDomain type
   */
  private <T> BDDFiniteDomain<T> ite(BDD b, BDDFiniteDomain<T> x, BDDFiniteDomain<T> y) {
    BDDFiniteDomain<T> result = new BDDFiniteDomain<>(x);
    BDDInteger i = ite(b, x.getInteger(), y.getInteger());
    result.setInteger(i);
    return result;
  }

  private BDDRoute ite(BDD guard, BDDRoute r1, BDDRoute r2) {
    BDDRoute ret = _netFactory.createRoute();

    // Only modify fields that can actually change

    if (_netFactory.getConfig().getKeepAd()) {
      BDDFiniteDomain<Long> x = r1.getAdminDist();
      BDDFiniteDomain<Long> y = r2.getAdminDist();
      BDDFiniteDomain<Long> z = ite(guard, x, y);
      ret.getAdminDist().setInteger(z.getInteger());
    }

    if (_netFactory.getConfig().getKeepLp()) {
      BDDFiniteDomain<Integer> a = r1.getLocalPref();
      BDDFiniteDomain<Integer> b = r2.getLocalPref();
      ret.getLocalPref().setInteger(ite(guard, a, b).getInteger());
    }

    if (_netFactory.getConfig().getKeepMetric()) {
      BDDInteger x = r1.getMetric();
      BDDInteger y = r2.getMetric();
      ret.getMetric().setValue(ite(guard, x, y));
    }

    if (_netFactory.getConfig().getKeepMed()) {
      BDDFiniteDomain<Long> x = r1.getMed();
      BDDFiniteDomain<Long> y = r2.getMed();
      BDDFiniteDomain<Long> z = ite(guard, x, y);
      ret.getMed().setInteger(z.getInteger());
    }

    if (_netFactory.getConfig().getKeepCommunities()) {
      r1.getCommunities()
          .forEach(
              (c, var1) -> {
                BDD var2 = r2.getCommunities().get(c);
                ret.getCommunities().put(c, ite(guard, var1, var2));
              });
    }

    return ret;
  }

  /*
   * Converts a community list to a boolean expression.
   */
  private BDD matchCommunityList(TransferParam<BDDRoute> p, CommunityList cl, BDDRoute other) {
    List<CommunityListLine> lines = new ArrayList<>(cl.getLines());
    Collections.reverse(lines);
    BDD acc = _netFactory.zero();
    for (CommunityListLine line : lines) {
      boolean action = (line.getAction() == LineAction.ACCEPT);
      CommunityVar cvar = new CommunityVar(CommunityVar.Type.REGEX, line.getRegex(), null);
      p.debug("Match Line: " + cvar);
      p.debug("Action: " + line.getAction());
      List<CommunityVar> deps = _commDeps.get(cvar);
      for (CommunityVar dep : deps) {
        p.debug("Test for: " + dep);
        BDD c = other.getCommunities().get(dep);
        acc = ite(c, mkBDD(action), acc);
      }
    }
    return acc;
  }

  /*
   * Converts a community set to a boolean expression
   */
  private BDD matchCommunitySet(
      TransferParam<BDDRoute> p, Configuration conf, CommunitySetExpr e, BDDRoute other) {
    if (e instanceof InlineCommunitySet) {
      Set<CommunityVar> comms = _graph.findAllCommunities(conf, e);
      BDD acc = _netFactory.one();
      for (CommunityVar comm : comms) {
        p.debug("Inline Community Set: " + comm);
        BDD c = other.getCommunities().get(comm);
        if (c == null) {
          throw new BatfishException("matchCommunitySet: should not be null");
        }
        acc = acc.and(c);
      }
      return acc;
    }

    if (e instanceof NamedCommunitySet) {
      p.debug("Named");
      NamedCommunitySet x = (NamedCommunitySet) e;
      CommunityList cl = conf.getCommunityLists().get(x.getName());
      p.debug("Named Community Set: " + cl.getName());
      return matchCommunityList(p, cl, other);
    }

    throw new BatfishException("TODO: match community set");
  }

  /*
   * Converts a route filter list to a boolean expression.
   */
  private BDD matchFilterList(TransferParam<BDDRoute> p, RouteFilterList x, BDDRoute other) {
    BDD acc = _netFactory.zero();
    List<RouteFilterLine> lines = new ArrayList<>(x.getLines());
    Collections.reverse(lines);
    for (RouteFilterLine line : lines) {
      if (!line.getIpWildcard().isPrefix()) {
        throw new BatfishException("non-prefix IpWildcards are unsupported");
      }
      Prefix pfx = line.getIpWildcard().toPrefix();
      if (!PrefixUtils.isContainedBy(pfx, _ignoredNetworks)) {
        SubRange r = line.getLengthRange();
        PrefixRange range = new PrefixRange(pfx, r);
        p.debug("Prefix Range: " + range);
        p.debug("Action: " + line.getAction());
        BDD matches = BDDUtils.prefixRangeToBdd(_netFactory.getFactory(), other, range);
        BDD action = mkBDD(line.getAction() == LineAction.ACCEPT);
        acc = ite(matches, action, acc);
      }
    }
    return acc;
  }

  /*
   * Converts a prefix set to a boolean expression.
   */
  private BDD matchPrefixSet(
      TransferParam<BDDRoute> p, Configuration conf, PrefixSetExpr e, BDDRoute other) {
    if (e instanceof ExplicitPrefixSet) {
      ExplicitPrefixSet x = (ExplicitPrefixSet) e;

      Set<PrefixRange> ranges = x.getPrefixSpace().getPrefixRanges();
      if (ranges.isEmpty()) {
        p.debug("empty");
        return _netFactory.one();
      }

      BDD acc = _netFactory.zero();
      for (PrefixRange range : ranges) {
        p.debug("Prefix Range: " + range);
        if (!PrefixUtils.isContainedBy(range.getPrefix(), _ignoredNetworks)) {
          acc = acc.or(BDDUtils.prefixRangeToBdd(_netFactory.getFactory(), other, range));
        }
      }
      return acc;

    } else if (e instanceof NamedPrefixSet) {
      NamedPrefixSet x = (NamedPrefixSet) e;
      p.debug("Named: " + x.getName());
      String name = x.getName();
      RouteFilterList fl = conf.getRouteFilterLists().get(name);
      return matchFilterList(p, fl, other);

    } else {
      throw new BatfishException("TODO: match prefix set: " + e);
    }
  }

  /*
   * Return a BDD from a boolean
   */
  private BDD mkBDD(boolean b) {
    return b ? _netFactory.one() : _netFactory.zero();
  }

  /*
   * Compute how many times to prepend to a path from the AST
   */
  private int prependLength(AsPathListExpr expr) {
    if (expr instanceof MultipliedAs) {
      MultipliedAs x = (MultipliedAs) expr;
      IntExpr e = x.getNumber();
      LiteralInt i = (LiteralInt) e;
      return i.getValue();
    }
    if (expr instanceof LiteralAsList) {
      LiteralAsList x = (LiteralAsList) expr;
      return x.getList().size();
    }
    throw new BatfishException("Error[prependLength]: unreachable");
  }

  /*
   * Create a new variable reflecting the final return value of the function
   */
  private TransferResult<BDDTransferFunction, BDD> returnValue(
      TransferResult<BDDTransferFunction, BDD> r, boolean val) {
    BDD b = ite(r.getReturnAssignedValue(), r.getReturnValue().getFilter(), mkBDD(val));
    BDDTransferFunction ret = new BDDTransferFunction(r.getReturnValue().getRoute(), b);
    return r.setReturnValue(ret).setReturnAssignedValue(_netFactory.one());
  }

  /*
   * Communities assumed to not be attached
   *
  private void addCommunityAssumptions(BDDRoute route) {
    for (CommunityVar comm : _comms) {
      if (_policyQuotient.getCommsUsedOnlyLocally().contains(comm)) {
        route.getCommunities().put(comm, _netFactory.zero());
      }
    }
  } */

  /*
   * Batfish does not include all the protocol logic in the RoutingPolicy object,
   * so we need to add this implicit logic explicitly. For instance, IBGP will
   * not re-export to other IBGP neighbors as part of the export function.
   */
  private void addBatfishImplicitPolicy(TransferResult<BDDTransferFunction, BDD> result) {
    BDDRoute route = result.getReturnValue().getRoute();

    String router = _edge.getRouter();
    String neighbor = _edge.getPeer();
    Set<String> clients = _graph.getRouteReflectorClients().get(router);
    boolean neighborIsClient = neighbor != null && clients != null && clients.contains(neighbor);

    boolean keepRRClient = _netFactory.getConfig().getKeepRRClient();
    boolean keepAdminDist = _netFactory.getConfig().getKeepAd();

    // If it is from an RR client, then we mark it as such on import
    if (_edgeType == EdgeType.IMPORT && _edge.isAbstract() && keepRRClient) {
      boolean fromClient = false;
      if (neighborIsClient) {
        fromClient = true;
      }
      BDD client = mkBDD(fromClient);
      route.setFromRRClient(client);
    }

    // If learned in the Ibgp protocol, then export should allow when either
    // (1) learned from client --> to everyone
    // (2) learned from nonclient --> to client
    if (_edgeType == EdgeType.EXPORT && _edge.isAbstract() && keepRRClient) {
      BDD oldFilterAllow = result.getReturnValue().getFilter();
      BDD ibgp = route.getProtocolHistory().value(RoutingProtocol.IBGP);
      BDD fromNonClient = route.getFromRRClient().not();
      BDD toNonClient = mkBDD(!neighborIsClient);
      BDD toBlock = ibgp.andWith(fromNonClient).andWith(toNonClient);
      BDD toAllow = toBlock.not();
      toBlock.free();
      BDD newFilterAllow = oldFilterAllow.andWith(toAllow);
      result.getReturnValue().setFilter(newFilterAllow);
    }

    // Set administrative distance for protocol
    if ((_protocol == RoutingProtocol.BGP || _protocol == RoutingProtocol.OSPF)
        && _edgeType == EdgeType.EXPORT
        && keepAdminDist) {
      BDDFiniteDomain<Long> ad = route.getAdminDist();
      BDDFiniteDomain<Long> newAd = new BDDFiniteDomain<>(ad);
      newAd.setValue((long) _protocol.getDefaultAdministrativeCost(_conf.getConfigurationFormat()));
      route.setAdminDist(newAd);
    }
  }

  /*
   * Create a BDDRecord representing the symbolic output of
   * the RoutingPolicy given the input routeVariables.
   */
  @Nullable
  public TransferResult<BDDTransferFunction, BDD> compute(Set<Prefix> ignoredNetworks) {

    // We need a policy no matter what when iBGP or OSPF import (to update cost)
    boolean needPolicyWhenNull =
        (_edge != null)
            && (_edge.isAbstract()
                || (_protocol == RoutingProtocol.OSPF && _edgeType == EdgeType.IMPORT));

    if (_statements == null && !needPolicyWhenNull) {
      return null;
    }

    _ignoredNetworks = ignoredNetworks;
    _commDeps = _graph.getCommunityDependencies();
    BDDRoute o = _netFactory.createRoute();
    // addCommunityAssumptions(o);

    TransferResult<BDDTransferFunction, BDD> result;
    if (_statements == null && needPolicyWhenNull) {
      BDDTransferFunction t = new BDDTransferFunction(o, _netFactory.one());
      result = new TransferResult<>();
      result =
          result
              .setReturnValue(t)
              .setReturnAssignedValue(_netFactory.one())
              .setFallthroughValue(_netFactory.one());
    } else {
      TransferParam<BDDRoute> p = new TransferParam<>(o, false);
      result = compute(_statements, p);
    }

    if (_edge != null) {
      addBatfishImplicitPolicy(result);
    }

    // System.out.println("RESULT FOR: " + _edge + "," + (_edgeType == EdgeType.IMPORT));
    // System.out.println(BDDUtils.dot(_netFactory, result.getReturnValue().getFilter()));
    return result;
  }
}
