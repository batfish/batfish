package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.DisjunctionChain;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.InlineCommunitySet;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
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
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.TransferParam;
import org.batfish.symbolic.TransferResult;

/** @author Ryan Beckett */
class TransferBDD {

  private static BDDFactory factory = BDDRecord.factory;

  private Graph _graph;

  private Set<CommunityVar> _comms;

  private Configuration _conf;

  private List<Statement> _statements;

  public TransferBDD(Graph g, Configuration conf, List<Statement> statements) {
    _graph = g;
    _conf = conf;
    _statements = statements;
  }

  /*
   * Return a BDD from a boolean
   */
  private BDD mkBDD(boolean b) {
    return b ? factory.one() : factory.zero();
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

  private BDDRecord ite(BDD guard, BDDRecord r1, BDDRecord r2) {
    BDDRecord ret = new BDDRecord(_comms);

    BDDInteger x;
    BDDInteger y;

    // update integer values based on condition
    x = r1.getPrefixLength();
    y = r2.getPrefixLength();
    ret.getPrefixLength().setValue(ite(guard, x, y));

    x = r1.getPrefix();
    y = r2.getPrefix();
    ret.getPrefix().setValue(ite(guard, x, y));

    x = r1.getAdminDist();
    y = r2.getAdminDist();
    ret.getAdminDist().setValue(ite(guard, x, y));

    x = r1.getLocalPref();
    y = r2.getLocalPref();
    ret.getLocalPref().setValue(ite(guard, x, y));

    x = r1.getMetric();
    y = r2.getMetric();
    ret.getMetric().setValue(ite(guard, x, y));

    x = r1.getMed();
    y = r2.getMed();
    ret.getMed().setValue(ite(guard, x, y));

    r1.getCommunities()
        .forEach(
            (c, var1) -> {
              BDD var2 = r2.getCommunities().get(c);
              ret.getCommunities().put(c, ite(guard, var1, var2));
            });

    BDDInteger i =
        ite(guard, r1.getProtocolHistory().getInteger(), r2.getProtocolHistory().getInteger());
    ret.getProtocolHistory().setInteger(i);

    return ret;
  }

  /*
   * Check if the first length bits match the BDDInteger
   * representing the advertisement prefix.
   *
   * Note: We assume the prefix is never modified, so it will
   * be a bitvector containing only the underlying variables:
   * [var(0), ..., var(n)]
   */
  private BDD firstBitsEqual(BDDRecord record, Prefix p, int length) {
    BDD[] bits = record.getPrefix().getBitvec();
    BitSet b = p.getAddress().getAddressBits();
    BDD acc = factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = b.get(i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.and(bits[i].not());
      }
    }
    return acc;
  }

  /*
   * Check if a prefix range match is applicable for the packet destination
   * Ip address, given the prefix length variable.
   *
   * Since aggregation is modelled separately, we assume that prefixLen
   * is not modified, and thus will contain only the underlying variables:
   * [var(0), ..., var(n)]
   */
  private BDD isRelevantFor(BDDRecord record, PrefixRange range) {
    Prefix p = range.getPrefix();
    SubRange r = range.getLengthRange();
    int len = p.getPrefixLength();
    int lower = r.getStart();
    int upper = r.getEnd();

    BDD lowerBitsMatch = firstBitsEqual(record, p, len);
    BDD acc = factory.zero();
    if (lower == 0 && upper == 32) {
      acc = factory.one();
    } else {
      for (int i = lower; i <= upper; i++) {
        BDD equalLen = record.getPrefixLength().value(i);
        acc = acc.or(equalLen);
      }
    }
    return acc.and(lowerBitsMatch);
  }

  /*
   * Converts a route filter list to a boolean expression.
   */
  private BDD matchFilterList(TransferParam<BDDRecord> p, RouteFilterList x, BDDRecord other) {
    BDD acc = factory.zero();
    List<RouteFilterLine> lines = new ArrayList<>(x.getLines());
    Collections.reverse(lines);
    for (RouteFilterLine line : lines) {
      Prefix pfx = line.getPrefix();
      SubRange r = line.getLengthRange();
      PrefixRange range = new PrefixRange(pfx, r);
      p.debug("Prefix Range: " + range);
      BDD matches = isRelevantFor(other, range);
      // p.debug("Matches: " + matches);
      BDD action = mkBDD(line.getAction() == LineAction.ACCEPT);
      // p.debug("Action: " + action);
      acc = ite(matches, action, acc);
    }
    return acc;
  }

  /*
   * Converts a prefix set to a boolean expression.
   */
  private BDD matchPrefixSet(
      TransferParam<BDDRecord> p, Configuration conf, PrefixSetExpr e, BDDRecord other) {
    if (e instanceof ExplicitPrefixSet) {
      ExplicitPrefixSet x = (ExplicitPrefixSet) e;

      Set<PrefixRange> ranges = x.getPrefixSpace().getPrefixRanges();
      if (ranges.isEmpty()) {
        p.debug("empty");
        return factory.one();
      }

      BDD acc = factory.zero();
      for (PrefixRange range : ranges) {
        p.debug("Prefix Range: " + range);
        acc = acc.or(isRelevantFor(other, range));
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
   * Converts a community list to a boolean expression.
   */
  private BDD matchCommunityList(CommunityList cl, BDDRecord other) {
    List<CommunityListLine> lines = new ArrayList<>(cl.getLines());
    Collections.reverse(lines);
    BDD acc = factory.zero();
    for (CommunityListLine line : lines) {
      boolean action = (line.getAction() == LineAction.ACCEPT);
      CommunityVar cvar = new CommunityVar(CommunityVar.Type.REGEX, line.getRegex(), null);
      BDD c = other.getCommunities().get(cvar);
      acc = ite(c, mkBDD(action), acc);
    }
    return acc;
  }

  /*
   * Converts a community set to a boolean expression
   */
  private BDD matchCommunitySet(Configuration conf, CommunitySetExpr e, BDDRecord other) {
    if (e instanceof InlineCommunitySet) {
      Set<CommunityVar> comms = _graph.findAllCommunities(conf, e);
      BDD acc = factory.one();
      for (CommunityVar comm : comms) {
        BDD c = other.getCommunities().get(comm);
        if (c == null) {
          throw new BatfishException("matchCommunitySet: should not be null");
        }
        acc = acc.and(c);
      }
      return acc;
    }

    if (e instanceof NamedCommunitySet) {
      NamedCommunitySet x = (NamedCommunitySet) e;
      CommunityList cl = conf.getCommunityLists().get(x.getName());
      return matchCommunityList(cl, other);
    }

    throw new BatfishException("TODO: match community set");
  }

  /*
   * Wrap a simple boolean expression return value in a transfer function result
   */
  private TransferResult<BDDReturn, BDD> fromExpr(BDDReturn b) {
    return new TransferResult<BDDReturn, BDD>()
        .setReturnAssignedValue(factory.one())
        .setReturnValue(b);
  }

  /*
   * Convert a Batfish AST boolean expression to a symbolic Z3 boolean expression
   * by performing inlining of stateful side effects.
   */
  private TransferResult<BDDReturn, BDD> compute(BooleanExpr expr, TransferParam<BDDRecord> p) {

    // TODO: right now everything is IPV4
    if (expr instanceof MatchIpv4) {
      p.debug("MatchIpv4");
      BDDReturn ret = new BDDReturn(p.getData(), factory.one());
      p.debug("MatchIpv4 Result: " + ret);
      return fromExpr(ret);
    }
    if (expr instanceof MatchIpv6) {
      p.debug("MatchIpv6");
      BDDReturn ret = new BDDReturn(p.getData(), factory.zero());
      return fromExpr(ret);
    }

    if (expr instanceof Conjunction) {
      p.debug("Conjunction");
      Conjunction c = (Conjunction) expr;
      BDD acc = factory.one();
      TransferResult<BDDReturn, BDD> result = new TransferResult<>();
      for (BooleanExpr be : c.getConjuncts()) {
        TransferResult<BDDReturn, BDD> r = compute(be, p);
        acc = acc.and(r.getReturnValue().getSecond());
      }
      BDDReturn ret = new BDDReturn(p.getData(), acc);
      p.debug("Conjunction Result: " + ret);
      return result.setReturnValue(ret);
    }

    if (expr instanceof Disjunction) {
      p.debug("Disjunction");
      Disjunction d = (Disjunction) expr;
      BDD acc = factory.zero();
      TransferResult<BDDReturn, BDD> result = new TransferResult<>();
      for (BooleanExpr be : d.getDisjuncts()) {
        TransferResult<BDDReturn, BDD> r = compute(be, p);
        result = result.addChangedVariables(r);
        acc = acc.or(r.getReturnValue().getSecond());
      }
      BDDReturn ret = new BDDReturn(p.getData(), acc);
      p.debug("Disjunction Result: " + ret);
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
        BDDReturn ret = new BDDReturn(p.getData(), factory.one());
        return fromExpr(ret);
      } else {
        TransferResult<BDDReturn, BDD> result = new TransferResult<>();
        TransferParam<BDDRecord> record = p;
        BDD acc = factory.zero();
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr conjunct = conjuncts.get(i);
          TransferParam<BDDRecord> param =
              record.setDefaultPolicy(null).setChainContext(TransferParam.ChainContext.CONJUNCTION);
          TransferResult<BDDReturn, BDD> r = compute(conjunct, param);
          record = record.setData(r.getReturnValue().getFirst());
          acc = ite(r.getFallthroughValue(), acc, r.getReturnValue().getSecond());
        }
        p.debug("ConjunctionChain Result: " + acc);
        BDDReturn ret = new BDDReturn(record.getData(), acc);
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
        BDDReturn ret = new BDDReturn(p.getData(), factory.zero());
        return fromExpr(ret);
      } else {
        TransferResult<BDDReturn, BDD> result = new TransferResult<>();
        TransferParam<BDDRecord> record = p;
        BDD acc = factory.zero();
        for (int i = disjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr disjunct = disjuncts.get(i);
          TransferParam<BDDRecord> param =
              record.setDefaultPolicy(null).setChainContext(TransferParam.ChainContext.CONJUNCTION);
          TransferResult<BDDReturn, BDD> r = compute(disjunct, param);
          record = record.setData(r.getReturnValue().getFirst());
          acc = ite(r.getFallthroughValue(), acc, r.getReturnValue().getSecond());
        }
        BDDReturn ret = new BDDReturn(record.getData(), acc);
        p.debug("DisjunctionChain Result: " + ret);
        return result.setReturnValue(ret);
      }
    }

    if (expr instanceof Not) {
      p.debug("mkNot");
      Not n = (Not) expr;
      TransferResult<BDDReturn, BDD> result = compute(n.getExpr(), p);
      BDDReturn r = result.getReturnValue();
      BDDReturn ret = new BDDReturn(r.getFirst(), r.getSecond().not());
      p.debug("Not Result: " + ret);
      return result.setReturnValue(ret);
    }

    if (expr instanceof MatchProtocol) {
      MatchProtocol mp = (MatchProtocol) expr;
      Protocol proto = Protocol.fromRoutingProtocol(mp.getProtocol());
      if (proto == null) {
        p.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): false");
        BDDReturn ret = new BDDReturn(p.getData(), factory.zero());
        return fromExpr(ret);
      }
      BDD protoMatch = p.getData().getProtocolHistory().value(proto);
      p.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): " + protoMatch);
      BDDReturn ret = new BDDReturn(p.getData(), protoMatch);
      p.debug("MatchProtocol Result: " + ret);
      return fromExpr(ret);
    }

    if (expr instanceof MatchPrefixSet) {
      p.debug("MatchPrefixSet");
      MatchPrefixSet m = (MatchPrefixSet) expr;
      BDD r = matchPrefixSet(p.indent(), _conf, m.getPrefixSet(), p.getData());
      BDDReturn ret = new BDDReturn(p.getData(), r);
      // p.debug("MatchPrefixSet Result: " + ret);
      return fromExpr(ret);

      // TODO: implement me
    } else if (expr instanceof MatchPrefix6Set) {
      p.debug("MatchPrefix6Set");
      BDDReturn ret = new BDDReturn(p.getData(), factory.zero());
      return fromExpr(ret);

    } else if (expr instanceof CallExpr) {
      p.debug("CallExpr");
      CallExpr c = (CallExpr) expr;
      String name = c.getCalledPolicyName();
      RoutingPolicy pol = _conf.getRoutingPolicies().get(name);
      p = p.setCallContext(TransferParam.CallContext.EXPR_CALL);
      TransferResult<BDDReturn, BDD> r = compute(pol.getStatements(), p.indent().enterScope(name));
      p.debug("CallExpr (return): " + r.getReturnValue());
      p.debug("CallExpr (fallthrough): " + r.getFallthroughValue());
      return r;

    } else if (expr instanceof WithEnvironmentExpr) {
      p.debug("WithEnvironmentExpr");
      // TODO: this is not correct
      WithEnvironmentExpr we = (WithEnvironmentExpr) expr;
      // TODO: postStatements() and preStatements()
      return compute(we.getExpr(), p);

    } else if (expr instanceof MatchCommunitySet) {
      p.debug("MatchCommunitySet");
      MatchCommunitySet mcs = (MatchCommunitySet) expr;
      BDD c = matchCommunitySet(_conf, mcs.getExpr(), p.getData());
      BDDReturn ret = new BDDReturn(p.getData(), c);
      p.debug("MatchCommunitySet Result: " + ret);
      return fromExpr(ret);

    } else if (expr instanceof BooleanExprs.StaticBooleanExpr) {
      BooleanExprs.StaticBooleanExpr b = (BooleanExprs.StaticBooleanExpr) expr;
      BDDReturn ret;
      switch (b.getType()) {
        case CallExprContext:
          p.debug("CallExprContext");
          BDD x1 = mkBDD(p.getCallContext() == TransferParam.CallContext.EXPR_CALL);
          ret = new BDDReturn(p.getData(), x1);
          return fromExpr(ret);
        case CallStatementContext:
          p.debug("CallStmtContext");
          BDD x2 = mkBDD(p.getCallContext() == TransferParam.CallContext.STMT_CALL);
          ret = new BDDReturn(p.getData(), x2);
          return fromExpr(ret);
        case True:
          p.debug("True");
          ret = new BDDReturn(p.getData(), factory.one());
          return fromExpr(ret);
        case False:
          p.debug("False");
          ret = new BDDReturn(p.getData(), factory.zero());
          return fromExpr(ret);
        default:
          throw new BatfishException(
              "Unhandled " + BooleanExprs.class.getCanonicalName() + ": " + b.getType());
      }
    }

    throw new BatfishException("TODO: compute expr transfer function: " + expr);
  }

  /*
   * Apply the effect of modifying a long value (e.g., to set the metric)
   */
  private BDDInteger applyLongExprModification(BDDInteger x, LongExpr e) {
    if (e instanceof LiteralLong) {
      LiteralLong z = (LiteralLong) e;
      return BDDInteger.makeFromValue(32, z.getValue());
    }
    if (e instanceof DecrementMetric) {
      DecrementMetric z = (DecrementMetric) e;
      return x.sub(BDDInteger.makeFromValue(32, z.getSubtrahend()));
    }
    if (e instanceof IncrementMetric) {
      IncrementMetric z = (IncrementMetric) e;
      return x.add(BDDInteger.makeFromValue(32, z.getAddend()));
    }
    throw new BatfishException("int expr transfer function: " + e);
  }

  /*
   * Apply the effect of modifying an integer value (e.g., to set the local pref)
   */
  private BDDInteger applyIntExprModification(BDDInteger x, IntExpr e) {
    if (e instanceof LiteralInt) {
      LiteralInt z = (LiteralInt) e;
      System.out.println("Literal: " + z.getValue());
      return BDDInteger.makeFromValue(32, z.getValue());
    }
    if (e instanceof IncrementLocalPreference) {
      IncrementLocalPreference z = (IncrementLocalPreference) e;
      return x.add(BDDInteger.makeFromValue(32, z.getAddend()));
    }
    if (e instanceof DecrementLocalPreference) {
      DecrementLocalPreference z = (DecrementLocalPreference) e;
      return x.sub(BDDInteger.makeFromValue(32, z.getSubtrahend()));
    }
    throw new BatfishException("TODO: int expr transfer function: " + e);
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
  private TransferResult<BDDReturn, BDD> returnValue(
      TransferResult<BDDReturn, BDD> r, boolean val) {
    BDD b = ite(r.getReturnAssignedValue(), r.getReturnValue().getSecond(), mkBDD(val));
    BDDReturn ret = new BDDReturn(r.getReturnValue().getFirst(), b);
    return r.setReturnValue(ret).setReturnAssignedValue(factory.one());
  }

  private TransferResult<BDDReturn, BDD> fallthrough(TransferResult<BDDReturn, BDD> r) {
    BDD b = ite(r.getReturnAssignedValue(), r.getFallthroughValue(), factory.one());
    return r.setFallthroughValue(b).setReturnAssignedValue(factory.one());
  }

  /*
   * Convert a list of statements into a Z3 boolean expression for the transfer function.
   */
  private TransferResult<BDDReturn, BDD> compute(
      List<Statement> statements, TransferParam<BDDRecord> p) {
    boolean doesReturn = false;

    TransferResult<BDDReturn, BDD> result = new TransferResult<>();
    result =
        result
            .setReturnValue(new BDDReturn(p.getData(), factory.zero()))
            .setFallthroughValue(factory.zero())
            .setReturnAssignedValue(factory.zero());

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

          default:
            throw new BatfishException("TODO: computeTransferFunction: " + ss.getType());
        }

      } else if (stmt instanceof If) {
        p.debug("If");
        If i = (If) stmt;
        TransferResult<BDDReturn, BDD> r = compute(i.getGuard(), p.indent());
        BDD guard = r.getReturnValue().getSecond();
        p.debug("guard: " + guard.not());

        BDDRecord current = result.getReturnValue().getFirst();

        TransferParam<BDDRecord> pTrue = p.indent().setData(current.copy());
        TransferParam<BDDRecord> pFalse = p.indent().setData(current.copy());
        p.debug("True Branch");
        TransferResult<BDDReturn, BDD> trueBranch = compute(i.getTrueStatements(), pTrue);
        p.debug("True Branch: " + trueBranch.getReturnValue());
        p.debug("False Branch");
        TransferResult<BDDReturn, BDD> falseBranch = compute(i.getFalseStatements(), pFalse);
        p.debug("False Branch: " + trueBranch.getReturnValue());

        BDDRecord recordVal = ite(guard, pTrue.getData(), pFalse.getData());

        // update return values
        BDD returnVal =
            ite(
                guard,
                trueBranch.getReturnValue().getSecond(),
                falseBranch.getReturnValue().getSecond());

        // p.debug("New Return Value (neg): " + returnVal.not());

        BDD returnAss =
            ite(guard, trueBranch.getReturnAssignedValue(), falseBranch.getReturnAssignedValue());

        // p.debug("New Return Assigned: " + returnAss);

        BDD fallThrough =
            ite(guard, trueBranch.getFallthroughValue(), falseBranch.getFallthroughValue());

        // p.debug("New fallthrough: " + fallThrough);

        result =
            result
                .setReturnValue(new BDDReturn(recordVal, returnVal))
                .setReturnAssignedValue(returnAss)
                .setFallthroughValue(fallThrough);

      } else if (stmt instanceof SetDefaultPolicy) {
        p.debug("SetDefaultPolicy");
        p = p.setDefaultPolicy((SetDefaultPolicy) stmt);

      } else if (stmt instanceof SetMetric) {
        p.debug("SetMetric");
        SetMetric sm = (SetMetric) stmt;
        LongExpr ie = sm.getMetric();
        BDD isBGP = p.getData().getProtocolHistory().value(Protocol.BGP);
        BDD updateMed = isBGP.and(result.getReturnAssignedValue());
        BDD updateMet = isBGP.not().and(result.getReturnAssignedValue());
        BDDInteger newValue = applyLongExprModification(p.getData().getMetric(), ie);
        BDDInteger med = ite(updateMed, p.getData().getMed(), newValue);
        BDDInteger met = ite(updateMet, p.getData().getMetric(), newValue);
        p.debug("Set Metric Update: " + met.getBitvec()[0]);
        p.getData().setMetric(met);
        p.getData().setMetric(med);

      } else if (stmt instanceof SetOspfMetricType) {
        p.debug("SetOspfMetricType");

      } else if (stmt instanceof SetLocalPreference) {
        p.debug("SetLocalPreference");
        SetLocalPreference slp = (SetLocalPreference) stmt;
        IntExpr ie = slp.getLocalPreference();
        BDDInteger newValue = applyIntExprModification(p.getData().getLocalPref(), ie);
        p.debug("return assigned: " + result.getReturnAssignedValue());
        for (int i = 0; i < 32; i++) {
          p.debug("newLP" + i + ": " + newValue.getBitvec()[i]);

        }
        newValue = ite(result.getReturnAssignedValue(), p.getData().getLocalPref(), newValue);
        p.getData().setLocalPref(newValue);

      } else if (stmt instanceof AddCommunity) {
        p.debug("AddCommunity");
        AddCommunity ac = (AddCommunity) stmt;
        Set<CommunityVar> comms = _graph.findAllCommunities(_conf, ac.getExpr());
        for (CommunityVar cvar : comms) {
          BDD comm = p.getData().getCommunities().get(cvar);
          BDD newValue = ite(result.getReturnAssignedValue(), comm, factory.one());
          p.getData().getCommunities().put(cvar, newValue);
        }

      } else if (stmt instanceof DeleteCommunity) {
        p.debug("DeleteCommunity");
        DeleteCommunity ac = (DeleteCommunity) stmt;
        Set<CommunityVar> comms = _graph.findAllCommunities(_conf, ac.getExpr());
        for (CommunityVar cvar : comms) {
          BDD comm = p.getData().getCommunities().get(cvar);
          BDD newValue = ite(result.getReturnAssignedValue(), comm, factory.zero());
          p.getData().getCommunities().put(cvar, newValue);
        }

      } else if (stmt instanceof RetainCommunity) {
        p.debug("RetainCommunity");
        // no op

      } else if (stmt instanceof PrependAsPath) {
        p.debug("PrependAsPath");
        PrependAsPath pap = (PrependAsPath) stmt;
        Integer prependCost = prependLength(pap.getExpr());
        BDDInteger newValue =
            p.getData().getMetric().add(BDDInteger.makeFromValue(32, prependCost));
        newValue = ite(result.getReturnAssignedValue(), p.getData().getMetric(), newValue);
        p.getData().setMetric(newValue);

      } else if (stmt instanceof SetOrigin) {
        p.debug("SetOrigin");
        // TODO: implement me

      } else {
        throw new BatfishException("TODO: statement transfer function: " + stmt);
      }
    }

    // If this is the outermost call, then we relate the variables
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
      BDDReturn ret = result.getReturnValue();
      BDDRecord retVal = ite(ret.getSecond(), ret.getFirst(), zeroedRecord());
      result = result.setReturnValue(new BDDReturn(retVal, ret.getSecond()));
    }
    return result;
  }

  private BDDRecord zeroedRecord() {
    BDDRecord rec = new BDDRecord(_comms);
    rec.getMetric().setValue(0);
    rec.getLocalPref().setValue(0);
    rec.getAdminDist().setValue(0);
    rec.getPrefixLength().setValue(0);
    rec.getMed().setValue(0);
    rec.getPrefix().setValue(0);
    for (CommunityVar comm : _comms) {
      rec.getCommunities().put(comm, factory.zero());
    }
    rec.getProtocolHistory().getInteger().setValue(0);
    return rec;
  }

  public BDDRecord compute() {
    _comms = _graph.findAllCommunities();
    BDDRecord o = new BDDRecord(_comms);
    TransferParam<BDDRecord> p = new TransferParam<>(o, true);
    TransferResult<BDDReturn, BDD> result = compute(_statements, p);
    return result.getReturnValue().getFirst();
  }
}
