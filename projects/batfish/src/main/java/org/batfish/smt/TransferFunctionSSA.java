package org.batfish.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfMetricType;
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
import org.batfish.smt.collections.PList;

/**
 * Class that computes a symbolic transfer function between two symbolic control plane records. The
 * transfer function is used to encode both import and export filters.
 *
 * <p>Batfish represents the AST much like vendors where there is a simple imperative language for
 * matching fields and making modifications to fields. Since this is not a good fit for a
 * declarative symbolic encoding of the network, we convert this stateful representation into a
 * stateless representation
 *
 * <p>The TransferFunctionSSA class makes policies stateless by converting the vendor-independent
 * format to a Static Single Assignment (SSA) form where all updates are reflected in new variables.
 * Rather than create a full control flow graph (CFG) as is typically done in SSA, we use a simple
 * conversion based on adding join points for every variable modified in an If statement.
 *
 * <p>The joint point defined as the [phi] function from SSA merges variables that may differ across
 * different branches of an If statement. For example, if there is the following filter:
 *
 * <p>If match(c1) then add community c2 else prepend path 2
 *
 * <p>Then this function will introduce a new variable at the end of the If statement that updates
 * the value of each variable modified based on the branch taken. For example:
 *
 * <p>c2' = (c1 ? true : c2) metric' = (c1 ? metric : metric + 2)
 *
 * <p>To model the return value of functions, we introduce three new variables: [fallthrough],
 * [returnValue] and [returnAssigned]. For example, if we have the following AST function in
 * Batfish:
 *
 * <p>function foo() if match(c1) then reject accept
 *
 * <p>This is modeled by introducing [returnValue] - the value that the function returns, and the
 * [returnAssigned] variable - whether a return or fallthrough statement has been hit so far in the
 * control flow.
 *
 * <p>Naturally, this kind of encoding can grow quite large since we introduce a large number of
 * extra variables. To make formula much simpler, we use a term size heuristic to inline variable
 * equalities when the inlined term will not be too large. Thus, additional variables are still
 * introduced, but only to keep the encoding compact. The 'simplify' and 'propagate-values' tactics
 * for z3 will further improve the encoding by removing any unnecessary variables. In this example,
 * the encoding will be simplified to [returnValue''' = not c1], which removes all intermediate
 * variables
 *
 * @author Ryan Beckett
 */
class TransferFunctionSSA {

  private static final int INLINE_HEURISTIC = 3000;
  private static int id = 0;
  private EncoderSlice _enc;

  private Configuration _conf;

  private SymbolicRecord _current;

  private SymbolicRecord _other;

  private Protocol _to;

  private Protocol _from;

  private List<Statement> _statements;

  private Integer _addedCost;

  private Interface _iface;

  private GraphEdge _graphEdge;

  private Map<Prefix, Boolean> _aggregates;

  private boolean _isExport;

  public TransferFunctionSSA(
      EncoderSlice encoderSlice,
      Configuration conf,
      SymbolicRecord other,
      SymbolicRecord current,
      Protocol to,
      Protocol from,
      List<Statement> statements,
      Integer addedCost,
      GraphEdge ge,
      boolean isExport) {
    _enc = encoderSlice;
    _conf = conf;
    _current = current;
    _other = other;
    _to = to;
    _from = from;
    _statements = statements;
    _addedCost = addedCost;
    _graphEdge = ge;
    _iface = ge.getStart();
    _isExport = isExport;
    _aggregates = null;
  }

  /*
   * Returns and increments a unique id for adding additional SSA variables
   */
  private static int generateId() {
    int result = TransferFunctionSSA.id;
    TransferFunctionSSA.id = result + 1;
    return result;
  }

  /*
   * Determines whether to model each aggregate route as
   * suppressing a more specific, or including the more specific
   */
  private Map<Prefix, Boolean> aggregateRoutes() {
    Map<Prefix, Boolean> acc = new HashMap<>();
    String name = _conf.getName();
    List<GeneratedRoute> aggregates = _enc.getOptimizations().getRelevantAggregates().get(name);
    Set<Prefix> suppressed = _enc.getOptimizations().getSuppressedAggregates().get(name);
    for (GeneratedRoute gr : aggregates) {
      Prefix p = gr.getNetwork();
      acc.put(p, suppressed.contains(p));
    }
    return acc;
  }

  /*
   * Converts a route filter list to a boolean expression.
   */
  private BoolExpr matchFilterList(RouteFilterList x, SymbolicRecord other) {
    BoolExpr acc = _enc.mkFalse();

    List<RouteFilterLine> lines = new ArrayList<>(x.getLines());
    Collections.reverse(lines);

    for (RouteFilterLine line : lines) {
      Prefix p = line.getPrefix();
      SubRange r = line.getLengthRange();
      PrefixRange range = new PrefixRange(p, r);
      BoolExpr matches = _enc.isRelevantFor(other.getPrefixLength(), range);
      BoolExpr action = _enc.mkBool(line.getAction() == LineAction.ACCEPT);
      acc = _enc.mkIf(matches, action, acc);
    }
    return acc;
  }

  /*
   * Converts a prefix set to a boolean expression.
   */
  private TransferFunctionResult matchPrefixSet(
      Configuration conf, PrefixSetExpr e, SymbolicRecord other) {

    ArithExpr otherLen = other.getPrefixLength();

    TransferFunctionResult result = new TransferFunctionResult();

    if (e instanceof ExplicitPrefixSet) {
      ExplicitPrefixSet x = (ExplicitPrefixSet) e;

      Set<PrefixRange> ranges = x.getPrefixSpace().getPrefixRanges();
      if (ranges.isEmpty()) {
        return result.setReturnValue(_enc.mkTrue());
      }

      // This is a total hack to deal with the fact that
      // we keep only a single FIB entry. Since BGP exporting a network
      // depends on the existence of an IGP route, we become more precise
      // by checking for static/connected/OSPF routes specifically.
      if (ranges.size() == 1) {
        for (PrefixRange r : ranges) {
          int start = r.getLengthRange().getStart();
          int end = r.getLengthRange().getEnd();
          Prefix pfx = r.getPrefix();
          if (start == end && start == pfx.getPrefixLength()) {
            String router = _conf.getName();
            Set<Prefix> origin = _enc.getOriginatedNetworks().get(router, Protocol.BGP);
            if (origin != null && origin.contains(pfx)) {
              // Compute static and connected routes
              Set<Prefix> ostatic = _enc.getOriginatedNetworks().get(router, Protocol.STATIC);
              Set<Prefix> oconn = _enc.getOriginatedNetworks().get(router, Protocol.CONNECTED);
              boolean hasStatic = ostatic != null && ostatic.contains(pfx);
              boolean hasConnected = oconn != null && oconn.contains(pfx);
              ArithExpr originLength = _enc.mkInt(pfx.getPrefixLength());
              if (hasStatic || hasConnected) {
                BoolExpr directRoute = _enc.isRelevantFor(originLength, r);
                ArithExpr newLength = _enc.mkIf(directRoute, originLength, otherLen);
                result = result.addChangedVariable("PREFIX-LEN", newLength);
                return result.setReturnValue(directRoute);
              } else {
                // Also use network statement if OSPF has a route with the correct length
                SymbolicRecord rec = _enc.getBestNeighborPerProtocol(router, Protocol.OSPF);
                if (rec != null) {
                  BoolExpr ospfRelevant = _enc.isRelevantFor(rec.getPrefixLength(), r);
                  ArithExpr newLength = _enc.mkIf(ospfRelevant, originLength, otherLen);
                  result = result.addChangedVariable("PREFIX-LEN", newLength);
                  return result.setReturnValue(ospfRelevant);
                }
              }
            }
          }
        }
      }

      // Compute if the other best route is relevant for this match statement
      BoolExpr acc = _enc.mkFalse();
      for (PrefixRange range : ranges) {
        acc = _enc.mkOr(acc, _enc.isRelevantFor(otherLen, range));
      }

      return result.setReturnValue(acc);

    } else if (e instanceof NamedPrefixSet) {
      NamedPrefixSet x = (NamedPrefixSet) e;
      String name = x.getName();
      RouteFilterList fl = conf.getRouteFilterLists().get(name);
      return result.setReturnValue(matchFilterList(fl, other));

    } else {
      throw new BatfishException("TODO: match prefix set: " + e);
    }
  }

  /*
   * Converts a community list to a boolean expression.
   */
  private BoolExpr matchCommunityList(CommunityList cl, SymbolicRecord other) {
    List<CommunityListLine> lines = new ArrayList<>(cl.getLines());
    Collections.reverse(lines);
    BoolExpr acc = _enc.mkFalse();
    for (CommunityListLine line : lines) {
      boolean action = (line.getAction() == LineAction.ACCEPT);
      CommunityVar cvar = new CommunityVar(CommunityVar.Type.REGEX, line.getRegex(), null);
      BoolExpr c = other.getCommunities().get(cvar);
      acc = _enc.mkIf(c, _enc.mkBool(action), acc);
    }
    return acc;
  }

  /*
   * Converts a community set to a boolean expression
   */
  private BoolExpr matchCommunitySet(Configuration conf, CommunitySetExpr e, SymbolicRecord other) {
    if (e instanceof InlineCommunitySet) {
      Set<CommunityVar> comms = _enc.findAllCommunities(conf, e);
      BoolExpr acc = _enc.mkTrue();
      for (CommunityVar comm : comms) {
        BoolExpr c = other.getCommunities().get(comm);
        if (c == null) {
          throw new BatfishException("matchCommunitySet: should not be null");
        }
        acc = _enc.mkAnd(acc, c);
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
  private TransferFunctionResult fromExpr(BoolExpr b) {
    return new TransferFunctionResult().setReturnAssignedValue(_enc.mkTrue()).setReturnValue(b);
  }

  private TransferFunctionResult initialResult() {
    return new TransferFunctionResult()
        .setReturnValue(_enc.mkFalse())
        .setFallthroughValue(_enc.mkFalse())
        .setReturnAssignedValue(_enc.mkFalse());
  }

  /*
   * Convert a Batfish AST boolean expression to a symbolic Z3 boolean expression
   * by performing inlining of stateful side effects.
   */
  private TransferFunctionResult compute(BooleanExpr expr, TransferFunctionParam p) {

    // TODO: right now everything is IPV4
    if (expr instanceof MatchIpv4) {
      p.debug("MatchIpv4");
      return fromExpr(_enc.mkTrue());
    }
    if (expr instanceof MatchIpv6) {
      p.debug("MatchIpv6");
      return fromExpr(_enc.mkFalse());
    }

    if (expr instanceof Conjunction) {
      p.debug("Conjunction");
      Conjunction c = (Conjunction) expr;
      BoolExpr acc = _enc.mkTrue();
      TransferFunctionResult result = new TransferFunctionResult();
      for (BooleanExpr be : c.getConjuncts()) {
        TransferFunctionResult r = compute(be, p.indent());
        result = result.addChangedVariables(r);
        acc = _enc.mkAnd(acc, r.getReturnValue());
      }
      p.debug("has changed variable");
      return result.setReturnValue(acc);
    }

    if (expr instanceof Disjunction) {
      p.debug("Disjunction");
      Disjunction d = (Disjunction) expr;
      BoolExpr acc = _enc.mkFalse();
      TransferFunctionResult result = new TransferFunctionResult();
      for (BooleanExpr be : d.getDisjuncts()) {
        TransferFunctionResult r = compute(be, p.indent());
        result = result.addChangedVariables(r);
        acc = _enc.mkOr(acc, r.getReturnValue());
      }
      p.debug("has changed variable");
      return result.setReturnValue(acc);
    }

    if (expr instanceof ConjunctionChain) {
      p.debug("ConjunctionChain");
      ConjunctionChain d = (ConjunctionChain) expr;
      List<BooleanExpr> conjuncts = new ArrayList<>(d.getSubroutines());
      if (p.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(p.getDefaultPolicy().getDefaultPolicy());
        conjuncts.add(be);
      }
      if (conjuncts.size() == 0) {
        return fromExpr(_enc.mkTrue());
      } else {
        TransferFunctionResult result = new TransferFunctionResult();
        BoolExpr acc = _enc.mkFalse();
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr conjunct = conjuncts.get(i);
          TransferFunctionParam param =
              p.setDefaultPolicy(null)
                  .setChainContext(TransferFunctionParam.ChainContext.CONJUNCTION);
          TransferFunctionResult r = compute(conjunct, param);
          result = result.addChangedVariables(r);
          acc = _enc.mkIf(r.getFallthroughValue(), acc, r.getReturnValue());
        }
        p.debug("ConjunctionChain Result: " + acc);
        return result.setReturnValue(acc);
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
        return fromExpr(_enc.mkTrue());
      } else {
        TransferFunctionResult result = new TransferFunctionResult();
        BoolExpr acc = _enc.mkFalse();
        for (int i = disjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr disjunct = disjuncts.get(i);
          TransferFunctionParam param =
              p.setDefaultPolicy(null)
                  .setChainContext(TransferFunctionParam.ChainContext.CONJUNCTION);
          TransferFunctionResult r = compute(disjunct, param);
          result.addChangedVariables(r);
          acc = _enc.mkIf(r.getFallthroughValue(), acc, r.getReturnValue());
        }
        p.debug("DisjunctionChain Result: " + acc);
        return result.setReturnValue(acc);
      }
    }

    if (expr instanceof Not) {
      p.debug("mkNot");
      Not n = (Not) expr;
      TransferFunctionResult result = compute(n.getExpr(), p);
      return result.setReturnValue(_enc.mkNot(result.getReturnValue()));
    }

    if (expr instanceof MatchProtocol) {
      MatchProtocol mp = (MatchProtocol) expr;
      Protocol proto = Protocol.fromRoutingProtocol(mp.getProtocol());
      if (proto == null) {
        p.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): false");
        return fromExpr(_enc.mkFalse());
      }
      if (_other.getProtocolHistory() == null) {
        BoolExpr protoMatch = _enc.mkBool(proto.equals(_from));
        p.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): " + protoMatch);
        return fromExpr(protoMatch);
      }
      BoolExpr protoMatch = _other.getProtocolHistory().checkIfValue(proto);
      p.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): " + protoMatch);
      return fromExpr(protoMatch);
    }

    if (expr instanceof MatchPrefixSet) {
      p.debug("MatchPrefixSet");
      MatchPrefixSet m = (MatchPrefixSet) expr;
      // For BGP, may change prefix length
      TransferFunctionResult result = matchPrefixSet(_conf, m.getPrefixSet(), p.getOther());
      return result.setReturnAssignedValue(_enc.mkTrue());

      // TODO: implement me
    } else if (expr instanceof MatchPrefix6Set) {
      p.debug("MatchPrefix6Set");
      return fromExpr(_enc.mkFalse());

    } else if (expr instanceof CallExpr) {
      p.debug("CallExpr");
      // TODO: the call can modify certain fields, need to keep track of these variables
      CallExpr c = (CallExpr) expr;
      String name = c.getCalledPolicyName();
      RoutingPolicy pol = _conf.getRoutingPolicies().get(name);
      p = p.setCallContext(TransferFunctionParam.CallContext.EXPR_CALL);
      TransferFunctionResult r =
          compute(pol.getStatements(), p.indent().enterScope(name), initialResult());
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
      return fromExpr(matchCommunitySet(_conf, mcs.getExpr(), p.getOther()));

    } else if (expr instanceof BooleanExprs.StaticBooleanExpr) {
      BooleanExprs.StaticBooleanExpr b = (BooleanExprs.StaticBooleanExpr) expr;
      switch (b.getType()) {
        case CallExprContext:
          p.debug("CallExprContext");
          return fromExpr(
              _enc.mkBool(p.getCallContext() == TransferFunctionParam.CallContext.EXPR_CALL));
        case CallStatementContext:
          p.debug("CallStmtContext");
          return fromExpr(
              _enc.mkBool(p.getCallContext() == TransferFunctionParam.CallContext.STMT_CALL));
        case True:
          p.debug("True");
          return fromExpr(_enc.mkTrue());
        case False:
          p.debug("False");
          return fromExpr(_enc.mkFalse());
        default:
          throw new BatfishException(
              "Unhandled " + BooleanExprs.class.getCanonicalName() + ": " + b.getType());
      }
    }

    String s = (_isExport ? "export" : "import");
    String msg =
        String.format(
            "Unimplemented feature %s for %s transfer function on interface %s",
            expr.toString(), s, _graphEdge.toString());

    throw new BatfishException(msg);
  }

  /*
   * Deal with the possibility of null variables due to optimizations
   */
  private ArithExpr getOrDefault(ArithExpr x, ArithExpr d) {
    if (x != null) {
      return x;
    }
    return d;
  }

  /*
   * Apply the effect of modifying a long value (e.g., to set the metric)
   */
  private ArithExpr applyLongExprModification(ArithExpr x, LongExpr e) {
    if (e instanceof LiteralLong) {
      LiteralLong z = (LiteralLong) e;
      return _enc.mkInt(z.getValue());
    }
    if (e instanceof DecrementMetric) {
      DecrementMetric z = (DecrementMetric) e;
      return _enc.mkSub(x, _enc.mkInt(z.getSubtrahend()));
    }
    if (e instanceof IncrementMetric) {
      IncrementMetric z = (IncrementMetric) e;
      return _enc.mkSum(x, _enc.mkInt(z.getAddend()));
    }
    throw new BatfishException("int expr transfer function: " + e);
  }

  /*
   * Apply the effect of modifying an integer value (e.g., to set the local pref)
   */
  private ArithExpr applyIntExprModification(ArithExpr x, IntExpr e) {
    if (e instanceof LiteralInt) {
      LiteralInt z = (LiteralInt) e;
      return _enc.mkInt(z.getValue());
    }
    if (e instanceof IncrementLocalPreference) {
      IncrementLocalPreference z = (IncrementLocalPreference) e;
      return _enc.mkSum(x, _enc.mkInt(z.getAddend()));
    }
    if (e instanceof DecrementLocalPreference) {
      DecrementLocalPreference z = (DecrementLocalPreference) e;
      return _enc.mkSub(x, _enc.mkInt(z.getSubtrahend()));
    }
    throw new BatfishException("TODO: int expr transfer function: " + e);
  }

  /*
   * Create a constraint that the metric field does not overflow
   * for a given routing protocol.
   */
  private BoolExpr noOverflow(ArithExpr metric, Protocol proto) {
    if (proto.isConnected()) {
      return _enc.mkTrue();
    }
    if (proto.isStatic()) {
      return _enc.mkTrue();
    }
    if (proto.isOspf()) {
      return _enc.mkLe(metric, _enc.mkInt(65535));
    }
    if (proto.isBgp()) {
      return _enc.mkLe(metric, _enc.mkInt(255));
    }
    throw new BatfishException("Encoding[noOverflow]: unrecognized protocol: " + proto.name());
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
   * Get the BgpNeighbor object given the current
   * graph edge and protocol information
   */
  private BgpNeighbor getBgpNeighbor() {
    Graph g = _enc.getGraph();
    if (_graphEdge.isAbstract()) {
      return g.getIbgpNeighbors().get(_graphEdge);
    } else {
      return g.getEbgpNeighbors().get(_graphEdge);
    }
  }

  /*
   * Determine if BGP communities should be
   * sent to/from the neighboring BGP peer.
   */
  private boolean sendCommunity() {
    if (_to.isBgp()) {
      if (!_isExport) {
        return true;
      }
      BgpNeighbor n = getBgpNeighbor();
      return n.getSendCommunity();
    } else {
      return false;
    }
  }

  /*
   * Relate the symbolic control plane route variables
   */
  private BoolExpr relateVariables(TransferFunctionParam p, TransferFunctionResult result) {

    ArithExpr defaultLen = _enc.mkInt(_enc.defaultLength());
    ArithExpr defaultAd = _enc.defaultAdminDistance(_conf, _from, p.getOther());
    ArithExpr defaultMed = _enc.mkInt(_enc.defaultMed(_from));
    ArithExpr defaultLp = _enc.mkInt(_enc.defaultLocalPref());
    ArithExpr defaultId = _enc.mkInt(_enc.defaultId());
    ArithExpr defaultMet = _enc.mkInt(_enc.defaultMetric());

    // TODO: remove all isChanged calls with actual symbolic values that test for a change

    boolean isIbgp = _graphEdge.isAbstract() && _to.isBgp();

    // Update prefix length when aggregation
    BoolExpr len =
        _enc.safeEq(
            _current.getPrefixLength(), getOrDefault(p.getOther().getPrefixLength(), defaultLen));

    BoolExpr per = _enc.safeEq(_current.getPermitted(), p.getOther().getPermitted());

    // Only update the router id for import edges
    BoolExpr id = _enc.mkTrue();
    if (!_isExport) {
      id = _enc.safeEq(_current.getRouterId(), getOrDefault(p.getOther().getRouterId(), defaultId));
    }

    // Update OSPF area id
    BoolExpr area;
    if (p.getOther().getOspfArea() == null || _iface.getOspfAreaName() == null) {
      area = _enc.mkTrue();
    } else {
      area = _enc.safeEqEnum(_current.getOspfArea(), _iface.getOspfAreaName());
    }

    // Set the IGP metric accordingly
    BoolExpr igpMet = _enc.mkTrue();
    boolean isNonClient =
        _graphEdge.isAbstract()
            && (_enc.getGraph().peerType(_graphEdge) != Graph.BgpSendType.TO_EBGP);
    boolean isClient =
        _graphEdge.isAbstract()
            && (_enc.getGraph().peerType(_graphEdge) == Graph.BgpSendType.TO_RR);

    if (_graphEdge.isAbstract() && _current.getIgpMetric() != null) {
      String router = _graphEdge.getRouter();
      String peer = _graphEdge.getPeer();

      // Case where it is a non client, we lookup the next-hop
      if (isNonClient) {
        EncoderSlice s = _enc.getEncoder().getSlice(peer);
        SymbolicRecord r = s.getSymbolicDecisions().getBestNeighbor().get(router);
        igpMet = _enc.mkEq(_current.getIgpMetric(), r.getMetric());
      }

      // Case where it is a client, next-hop depends on the clientId tag we added
      if (isClient) {
        BoolExpr acc = _enc.mkTrue();
        for (Map.Entry<String, Integer> entry : _enc.getGraph().getOriginatorId().entrySet()) {
          String r = entry.getKey();
          Integer clientId = entry.getValue();
          if (!r.equals(router)) {
            EncoderSlice s = _enc.getEncoder().getSlice(r);
            SymbolicRecord record = s.getSymbolicDecisions().getBestNeighbor().get(r);
            BoolExpr eq = _enc.mkEq(_current.getIgpMetric(), record.getMetric());
            acc =
                _enc.mkAnd(
                    acc, _enc.mkImplies(p.getOther().getClientId().checkIfValue(clientId), eq));
          }
        }
        igpMet = acc;
      }
    }

    // Set whether or not is iBGP or not on import
    BoolExpr isInternal =
        _enc.safeEq(_current.getBgpInternal(), _enc.mkBool(isIbgp)); // TODO: and !isExport?

    // Update OSPF type
    BoolExpr type;
    if (result.isChanged("OSPF-TYPE")) {
      type = _enc.safeEqEnum(_current.getOspfType(), p.getOther().getOspfType());
    } else {
      boolean hasAreaIface = _iface.getOspfAreaName() != null;
      boolean hasArea = p.getOther().getOspfArea() != null;
      boolean hasType = p.getOther().getOspfType() != null;
      boolean areaPossiblyChanged = hasType && hasArea && hasAreaIface;
      // Check if area changed
      if (areaPossiblyChanged) {
        BoolExpr internal = p.getOther().getOspfType().isInternal();
        BoolExpr same = p.getOther().getOspfArea().checkIfValue(_iface.getOspfAreaName());
        BoolExpr update = _enc.mkAnd(internal, _enc.mkNot(same));
        BoolExpr copyOld = _enc.safeEqEnum(_current.getOspfType(), p.getOther().getOspfType());
        type = _enc.mkIf(update, _current.getOspfType().checkIfValue(OspfType.OIA), copyOld);
      } else {
        type = _enc.safeEqEnum(_current.getOspfType(), p.getOther().getOspfType());
      }
    }

    BoolExpr comms = _enc.mkTrue();
    // update all community values
    for (Map.Entry<CommunityVar, BoolExpr> entry : _current.getCommunities().entrySet()) {
      CommunityVar cvar = entry.getKey();
      BoolExpr e = entry.getValue();
      BoolExpr eOther = p.getOther().getCommunities().get(cvar);
      // Update the communities if they should be sent
      if (sendCommunity()) {
        if (cvar.getType() != CommunityVar.Type.REGEX) {
          comms = _enc.mkAnd(comms, _enc.mkEq(e, eOther));
        }
      } else {
        comms = _enc.mkAnd(comms, _enc.mkNot(e));
      }
    }

    ArithExpr otherAd =
        (p.getOther().getAdminDist() == null ? defaultAd : p.getOther().getAdminDist());
    ArithExpr otherMed = (p.getOther().getMed() == null ? defaultMed : p.getOther().getMed());
    ArithExpr otherLp = getOrDefault(p.getOther().getLocalPref(), defaultLp);
    ArithExpr otherMet = getOrDefault(p.getOther().getMetric(), defaultMet);
    // otherMet = applyMetricUpdate(otherMet);

    BoolExpr ad = _enc.safeEq(_current.getAdminDist(), otherAd);
    BoolExpr history = _enc.equalHistories(_current, p.getOther());
    BoolExpr med = _enc.safeEq(_current.getMed(), otherMed);
    BoolExpr met = _enc.safeEq(_current.getMetric(), otherMet);
    BoolExpr lp = _enc.safeEq(_current.getLocalPref(), otherLp);

    // If this was an external route, then we need to add the correct next-hop tag
    boolean isEbgpEdge = _enc.getGraph().getEbgpNeighbors().get(_graphEdge) != null;
    BoolExpr cid = _enc.mkTrue();
    if (_isExport && _to.isBgp() && p.getOther().getClientId() != null) {
      if (isEbgpEdge) {
        cid = _current.getClientId().checkIfValue(0);
      } else {
        cid = _enc.safeEqEnum(_current.getClientId(), p.getOther().getClientId());
      }
    }
    if (!_isExport && _to.isBgp()) {
      if (p.getOther().getClientId() != null) {
        BoolExpr fromExternal = p.getOther().getClientId().checkIfValue(0);
        BoolExpr edgeIsInternal = _enc.mkBool(!isClient && !isNonClient);
        BoolExpr copyOver = _enc.safeEqEnum(_current.getClientId(), p.getOther().getClientId());
        Integer x = _enc.getGraph().getOriginatorId().get(_graphEdge.getRouter());
        SymbolicOriginatorId soid = _current.getClientId();
        BoolExpr setNewValue = (x == null ? soid.checkIfValue(0) : soid.checkIfValue(x));
        cid = _enc.mkIf(_enc.mkAnd(fromExternal, edgeIsInternal), setNewValue, copyOver);
      }
    }

    BoolExpr updates =
        _enc.mkAnd(
            per, len, ad, med, lp, met, id, cid, type, area, comms, history, isInternal, igpMet);
    BoolExpr noOverflow = noOverflow(otherMet, _to);

    return _enc.mkIf(noOverflow, updates, _enc.mkNot(_current.getPermitted()));
  }

  /*
   * Create a new variable reflecting the final return value of the function
   */
  private TransferFunctionResult returnValue(
      TransferFunctionParam p, TransferFunctionResult r, boolean val) {
    BoolExpr b = _enc.mkIf(r.getReturnAssignedValue(), r.getReturnValue(), _enc.mkBool(val));
    BoolExpr newRet = createBoolVariableWith(p, "RETURN", b);
    return r.setReturnValue(newRet)
        .setReturnAssignedValue(_enc.mkTrue())
        .addChangedVariable("RETURN", newRet);
  }

  private TransferFunctionResult fallthrough(TransferFunctionParam p, TransferFunctionResult r) {
    BoolExpr b = _enc.mkIf(r.getReturnAssignedValue(), r.getFallthroughValue(), _enc.mkTrue());
    BoolExpr newFallthrough = createBoolVariableWith(p, "FALLTHROUGH", b);
    return r.setFallthroughValue(newFallthrough)
        .setReturnAssignedValue(_enc.mkTrue())
        .addChangedVariable("FALLTHROUGH", newFallthrough);
  }

  private void updateSingleValue(TransferFunctionParam p, String variableName, Expr expr) {
    switch (variableName) {
      case "METRIC":
        p.getOther().setMetric((ArithExpr) expr);
        break;
      case "PREFIX-LEN":
        p.getOther().setPrefixLength((ArithExpr) expr);
        break;
      case "ADMIN-DIST":
        p.getOther().setAdminDist((ArithExpr) expr);
        break;
      case "LOCAL-PREF":
        p.getOther().setLocalPref((ArithExpr) expr);
        break;
      case "OSPF-TYPE":
        p.getOther().getOspfType().setBitVec((BitVecExpr) expr);
        break;
      case "RETURN":
        break;
      default:
        for (Map.Entry<CommunityVar, BoolExpr> entry : p.getOther().getCommunities().entrySet()) {
          CommunityVar cvar = entry.getKey();
          if (variableName.equals(cvar.getValue())) {
            p.getOther().getCommunities().put(cvar, (BoolExpr) expr);
            return;
          }
        }

        throw new BatfishException("Unimplemented: update for " + variableName);
    }
  }

  /*
   * The [phi] function from SSA that merges variables that may differ across
   * different branches of an mkIf statement.
   */
  private Pair<Expr, Expr> joinPoint(
      TransferFunctionParam p,
      TransferFunctionResult r,
      BoolExpr guard,
      Pair<String, Pair<Expr, Expr>> values) {
    String variableName = values.getFirst();
    Expr trueBranch = values.getSecond().getFirst();
    Expr falseBranch = values.getSecond().getSecond();

    if (variableName.equals("RETURN") || variableName.equals("FALLTHROUGH")) {
      Expr t =
          (trueBranch == null
              ? _enc.mkFalse()
              : trueBranch); // can use False because the value has not been assigned
      Expr f = (falseBranch == null ? _enc.mkFalse() : falseBranch);
      Expr tass = (trueBranch == null ? r.getReturnAssignedValue() : _enc.mkTrue());
      Expr fass = (falseBranch == null ? r.getReturnAssignedValue() : _enc.mkTrue());
      BoolExpr newAss = _enc.mkIf(guard, (BoolExpr) tass, (BoolExpr) fass);
      BoolExpr retAss = createBoolVariableWith(p, "ASSIGNED", newAss);
      BoolExpr variable =
          (variableName.equals("RETURN") ? r.getReturnValue() : r.getFallthroughValue());
      BoolExpr newValue =
          _enc.mkIf(
              r.getReturnAssignedValue(), variable, _enc.mkIf(guard, (BoolExpr) t, (BoolExpr) f));
      BoolExpr ret = createBoolVariableWith(p, variableName, newValue);
      return new Pair<>(ret, retAss);
    }

    if (variableName.equals("PREFIX-LEN")) {
      Expr t = (trueBranch == null ? p.getOther().getPrefixLength() : trueBranch);
      Expr f = (falseBranch == null ? p.getOther().getPrefixLength() : falseBranch);
      ArithExpr newValue = _enc.mkIf(guard, (ArithExpr) t, (ArithExpr) f);
      newValue = _enc.mkIf(r.getReturnAssignedValue(), p.getOther().getPrefixLength(), newValue);
      ArithExpr ret = createArithVariableWith(p, "PREFIX-LEN", newValue);
      p.getOther().setPrefixLength(ret);
      return new Pair<>(ret, null);
    }
    if (variableName.equals("ADMIN-DIST")) {
      Expr t = (trueBranch == null ? p.getOther().getAdminDist() : trueBranch);
      Expr f = (falseBranch == null ? p.getOther().getAdminDist() : falseBranch);
      ArithExpr newValue = _enc.mkIf(guard, (ArithExpr) t, (ArithExpr) f);
      newValue = _enc.mkIf(r.getReturnAssignedValue(), p.getOther().getAdminDist(), newValue);
      ArithExpr ret = createArithVariableWith(p, "ADMIN-DIST", newValue);
      p.getOther().setAdminDist(ret);
      return new Pair<>(ret, null);
    }
    if (variableName.equals("LOCAL-PREF")) {
      Expr t = (trueBranch == null ? p.getOther().getLocalPref() : trueBranch);
      Expr f = (falseBranch == null ? p.getOther().getLocalPref() : falseBranch);
      ArithExpr newValue = _enc.mkIf(guard, (ArithExpr) t, (ArithExpr) f);
      newValue = _enc.mkIf(r.getReturnAssignedValue(), p.getOther().getLocalPref(), newValue);
      ArithExpr ret = createArithVariableWith(p, "LOCAL-PREF", newValue);
      p.getOther().setLocalPref(ret);
      return new Pair<>(ret, null);
    }
    if (variableName.equals("METRIC")) {
      Expr t = (trueBranch == null ? p.getOther().getMetric() : trueBranch);
      Expr f = (falseBranch == null ? p.getOther().getMetric() : falseBranch);
      ArithExpr newValue = _enc.mkIf(guard, (ArithExpr) t, (ArithExpr) f);
      newValue = _enc.mkIf(r.getReturnAssignedValue(), p.getOther().getMetric(), newValue);
      ArithExpr ret = createArithVariableWith(p, "METRIC", newValue);
      p.getOther().setMetric(ret);
      return new Pair<>(ret, null);
    }
    if (variableName.equals("OSPF-TYPE")) {
      Expr t = (trueBranch == null ? p.getOther().getOspfType().getBitVec() : trueBranch);
      Expr f = (falseBranch == null ? p.getOther().getOspfType().getBitVec() : falseBranch);
      BitVecExpr newValue = _enc.mkIf(guard, (BitVecExpr) t, (BitVecExpr) f);
      newValue =
          _enc.mkIf(r.getReturnAssignedValue(), p.getOther().getOspfType().getBitVec(), newValue);
      BitVecExpr ret = createBitVecVariableWith(p, "OSPF-TYPE", 2, newValue);
      p.getOther().getOspfType().setBitVec(ret);
      return new Pair<>(ret, null);
    }

    for (Map.Entry<CommunityVar, BoolExpr> entry : p.getOther().getCommunities().entrySet()) {
      CommunityVar cvar = entry.getKey();
      if (variableName.equals(cvar.getValue())) {
        Expr t = (trueBranch == null ? p.getOther().getCommunities().get(cvar) : trueBranch);
        Expr f = (falseBranch == null ? p.getOther().getCommunities().get(cvar) : falseBranch);
        BoolExpr newValue = _enc.mkIf(guard, (BoolExpr) t, (BoolExpr) f);
        newValue =
            _enc.mkIf(
                r.getReturnAssignedValue(), p.getOther().getCommunities().get(cvar), newValue);
        BoolExpr ret = createBoolVariableWith(p, cvar.getValue(), newValue);
        p.getOther().getCommunities().put(cvar, ret);
        return new Pair<>(ret, null);
      }
    }

    throw new BatfishException("[joinPoint]: unhandled case for " + variableName);
  }

  /*
   * Convert a list of statements into a Z3 boolean expression for the transfer function.
   */
  private TransferFunctionResult compute(
      List<Statement> statements, TransferFunctionParam p, TransferFunctionResult result) {
    boolean doesReturn = false;

    for (Statement stmt : statements) {

      if (stmt instanceof StaticStatement) {
        StaticStatement ss = (StaticStatement) stmt;

        switch (ss.getType()) {
          case ExitAccept:
            doesReturn = true;
            p.debug("ExitAccept");
            result = returnValue(p, result, true);
            break;

          case ReturnTrue:
            doesReturn = true;
            p.debug("ReturnTrue");
            result = returnValue(p, result, true);
            break;

          case ExitReject:
            doesReturn = true;
            p.debug("ExitReject");
            result = returnValue(p, result, false);
            break;

          case ReturnFalse:
            doesReturn = true;
            p.debug("ReturnFalse");
            result = returnValue(p, result, false);
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
              result = returnValue(p, result, true);
            } else {
              result = returnValue(p, result, false);
            }
            break;

          case FallThrough:
            p.debug("Fallthrough");
            result = fallthrough(p, result);
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
        TransferFunctionResult r = compute(i.getGuard(), p);
        result = result.addChangedVariables(r);
        BoolExpr guard = (BoolExpr) r.getReturnValue().simplify();
        String str = guard.toString();

        // If there are updates in the guard, add them to the parameter p before entering branches
        for (Pair<String, Expr> changed : r.getChangedVariables()) {
          p.debug("CHANGED: " + changed.getFirst());
          updateSingleValue(p, changed.getFirst(), changed.getSecond());
        }

        p.debug("guard: " + str);
        // If we know the branch ahead of time, then specialize
        switch (str) {
          case "true":
            p.debug("True Branch");
            result = compute(i.getTrueStatements(), p.indent(), result);
            break;
          case "false":
            p.debug("False Branch");
            compute(i.getFalseStatements(), p.indent(), result);
            break;
          default:
            p.debug("True Branch");
            // clear changed variables before proceeding
            TransferFunctionResult trueBranch =
                compute(i.getTrueStatements(), p.indent().copyRecord(), initialResult());
            p.debug("False Branch");
            TransferFunctionResult falseBranch =
                compute(i.getFalseStatements(), p.indent().copyRecord(), initialResult());
            p.debug("JOIN");
            PList<Pair<String, Pair<Expr, Expr>>> pairs =
                trueBranch.mergeChangedVariables(falseBranch);

            // Extract and deal with the return value first so that other
            // variables have this reflected in their value
            int idx = pairs.find(pair -> pair.getFirst().equals("RETURN"));
            if (idx >= 0) {
              Pair<String, Pair<Expr, Expr>> ret = pairs.get(idx);
              pairs = pairs.minus(idx);
              pairs = pairs.plus(pairs.size(), ret);
            }

            for (Pair<String, Pair<Expr, Expr>> pair : pairs) {
              String s = pair.getFirst();
              p.debug("CHANGED: " + s);
              Pair<Expr, Expr> x = joinPoint(p, result, guard, pair);
              result = result.addChangedVariable(s, x.getFirst());
              if (s.equals("RETURN")) {
                result =
                    result
                        .setReturnValue((BoolExpr) x.getFirst())
                        .setReturnAssignedValue((BoolExpr) x.getSecond());
              }
              if (s.equals("FALLTHROUGH")) {
                result =
                    result
                        .setFallthroughValue((BoolExpr) x.getFirst())
                        .setReturnAssignedValue((BoolExpr) x.getSecond());
              }
            }

            break;
        }

      } else if (stmt instanceof SetDefaultPolicy) {
        p.debug("SetDefaultPolicy");
        p = p.setDefaultPolicy((SetDefaultPolicy) stmt);

      } else if (stmt instanceof SetMetric) {
        p.debug("SetMetric");
        // TODO: what is the semantics for BGP? Is this MED?
        if (!_current.getProto().isBgp()) {
          SetMetric sm = (SetMetric) stmt;
          LongExpr ie = sm.getMetric();
          ArithExpr newValue = applyLongExprModification(p.getOther().getMetric(), ie);
          newValue = _enc.mkIf(result.getReturnAssignedValue(), p.getOther().getMetric(), newValue);
          ArithExpr x = createArithVariableWith(p, "METRIC", newValue);
          p.getOther().setMetric(x);
          result = result.addChangedVariable("METRIC", x);
        }

      } else if (stmt instanceof SetOspfMetricType) {
        p.debug("SetOspfMetricType");
        SetOspfMetricType somt = (SetOspfMetricType) stmt;
        OspfMetricType mt = somt.getMetricType();
        SymbolicOspfType t;
        if (mt == OspfMetricType.E1) {
          t = new SymbolicOspfType(_enc, OspfType.E1);
        } else {
          t = new SymbolicOspfType(_enc, OspfType.E2);
        }
        BitVecExpr newValue = t.getBitVec();
        newValue =
            _enc.mkIf(
                result.getReturnAssignedValue(), p.getOther().getOspfType().getBitVec(), newValue);
        BitVecExpr x = createBitVecVariableWith(p, "OSPF-TYPE", 2, newValue);
        p.getOther().getOspfType().setBitVec(x);
        result = result.addChangedVariable("OSPF-TYPE", x);

      } else if (stmt instanceof SetLocalPreference) {
        p.debug("SetLocalPreference");
        SetLocalPreference slp = (SetLocalPreference) stmt;
        IntExpr ie = slp.getLocalPreference();
        ArithExpr newValue = applyIntExprModification(p.getOther().getLocalPref(), ie);
        newValue =
            _enc.mkIf(result.getReturnAssignedValue(), p.getOther().getLocalPref(), newValue);
        ArithExpr x = createArithVariableWith(p, "LOCAL-PREF", newValue);
        p.getOther().setLocalPref(x);
        result = result.addChangedVariable("LOCAL-PREF", x);

      } else if (stmt instanceof AddCommunity) {
        p.debug("AddCommunity");
        AddCommunity ac = (AddCommunity) stmt;
        Set<CommunityVar> comms = _enc.findAllCommunities(_conf, ac.getExpr());
        for (CommunityVar cvar : comms) {
          BoolExpr newValue =
              _enc.mkIf(
                  result.getReturnAssignedValue(),
                  p.getOther().getCommunities().get(cvar),
                  _enc.mkTrue());
          BoolExpr x = createBoolVariableWith(p, cvar.getValue(), newValue);
          p.getOther().getCommunities().put(cvar, x);
          result = result.addChangedVariable(cvar.getValue(), x);
        }

      } else if (stmt instanceof DeleteCommunity) {
        p.debug("DeleteCommunity");
        DeleteCommunity ac = (DeleteCommunity) stmt;
        Set<CommunityVar> comms = _enc.findAllCommunities(_conf, ac.getExpr());
        for (CommunityVar cvar : comms) {
          BoolExpr newValue =
              _enc.mkIf(
                  result.getReturnAssignedValue(),
                  p.getOther().getCommunities().get(cvar),
                  _enc.mkFalse());
          BoolExpr x = createBoolVariableWith(p, cvar.getValue(), newValue);
          p.getOther().getCommunities().put(cvar, x);
          result = result.addChangedVariable(cvar.getValue(), x);
        }

      } else if (stmt instanceof RetainCommunity) {
        p.debug("RetainCommunity");
        // no op

      } else if (stmt instanceof PrependAsPath) {
        p.debug("PrependAsPath");
        PrependAsPath pap = (PrependAsPath) stmt;
        Integer prependCost = prependLength(pap.getExpr());
        ArithExpr newValue = _enc.mkSum(p.getOther().getMetric(), _enc.mkInt(prependCost));
        newValue = _enc.mkIf(result.getReturnAssignedValue(), p.getOther().getMetric(), newValue);
        ArithExpr x = createArithVariableWith(p, "METRIC", newValue);
        p.getOther().setMetric(x);
        result = result.addChangedVariable("METRIC", x);

      } else if (stmt instanceof SetOrigin) {
        p.debug("SetOrigin");
        // TODO: implement me

      } else {

        String s = (_isExport ? "export" : "import");
        String msg =
            String.format(
                "Unimplemented feature %s for %s transfer function on interface %s",
                stmt.toString(), s, _graphEdge.toString());

        throw new BatfishException(msg);
      }
    }

    // If this is the outermost call, then we relate the variables
    if (p.getInitialCall()) {
      p.debug("InitialCall finalizing");

      // Apply the default action
      if (!doesReturn) {
        p.debug("Applying default action: " + p.getDefaultAccept());
        if (p.getDefaultAccept()) {
          result = returnValue(p, result, true);
        } else {
          result = returnValue(p, result, false);
        }
      }
      BoolExpr related = relateVariables(p, result);
      BoolExpr retValue =
          _enc.mkIf(result.getReturnValue(), related, _enc.mkNot(_current.getPermitted()));
      result = result.setReturnValue(retValue);
    }
    return result;
  }

  /*
   * Check if we can inline a new SSA variable. We can simply conservatively check
   * if the size of the term will get no larger after inlining. Right now we only
   * check for True and False values because z3 seems to have some issue with
   * identifying the AST expression kind (e.g., e.isTrue() throws an exception).
   */
  private boolean canInline(Expr e) {
    // TODO: such a huge hack
    String s = e.toString();
    // p.debug("[STRING]: " + s);
    // p.debug("Can Inline: " + b);
    return s.length() <= INLINE_HEURISTIC;
  }

  /*
   * A collection of functions to create new SSA variables on-the-fly,
   * while also simultaneously setting their value based on an old value.
   */
  private ArithExpr createArithVariableWith(TransferFunctionParam p, String name, ArithExpr e) {
    e = (ArithExpr) e.simplify();
    if (canInline(e)) {
      p.debug(name + "=" + e);
      return e;
    }
    String s = "SSA_" + name + generateId();
    ArithExpr x = _enc.getCtx().mkIntConst(s);
    // _enc.getAllVariables().add(x);
    BoolExpr eq = _enc.mkEq(x, e);
    _enc.add(eq);
    p.debug(eq.toString());
    return x;
  }

  private BoolExpr createBoolVariableWith(TransferFunctionParam p, String name, BoolExpr e) {
    e = (BoolExpr) e.simplify();
    if (canInline(e)) {
      p.debug(name + "=" + e);
      return e;
    }
    String s = "SSA_" + name + generateId();
    BoolExpr x = _enc.getCtx().mkBoolConst(s);
    // _enc.getAllVariables().add(x);
    BoolExpr eq = _enc.mkEq(x, e);
    _enc.add(eq);
    p.debug(eq.toString());
    return x;
  }

  private BitVecExpr createBitVecVariableWith(
      TransferFunctionParam p, String name, int size, BitVecExpr e) {
    e = (BitVecExpr) e.simplify();
    if (canInline(e)) {
      p.debug(name + "=" + e);
      return e;
    }
    String s = "SSA_" + name + generateId();
    BitVecExpr x = _enc.getCtx().mkBVConst(s, size);
    // _enc.getAllVariables().add(x);
    BoolExpr eq = _enc.mkEq(x, e);
    _enc.add(eq);
    p.debug(eq.toString());
    return x;
  }

  /*
   * Create a new variable representing the new prefix length after
   * applying the effect of aggregation.
   */
  private void computeIntermediatePrefixLen(TransferFunctionParam param) {
    ArithExpr prefixLen = param.getOther().getPrefixLength();
    if (_isExport && _to.isBgp()) {
      _aggregates = aggregateRoutes();
      if (_aggregates.size() > 0) {
        for (Map.Entry<Prefix, Boolean> entry : _aggregates.entrySet()) {
          Prefix prefix = entry.getKey();
          Boolean isSuppressed = entry.getValue();
          Prefix p = prefix.getNetworkPrefix();
          ArithExpr len = _enc.mkInt(p.getPrefixLength());
          BoolExpr relevantPfx = _enc.isRelevantFor(p, _enc.getSymbolicPacket().getDstIp());
          BoolExpr relevantLen = _enc.mkGt(param.getOther().getPrefixLength(), len);
          BoolExpr relevant = _enc.mkAnd(relevantPfx, relevantLen, _enc.mkBool(isSuppressed));
          prefixLen = _enc.mkIf(relevant, len, prefixLen);
        }
        ArithExpr i = createArithVariableWith(param, "PREFIX-LEN", prefixLen);
        param.getOther().setPrefixLength(i);
      }
    }
  }

  private void applyMetricUpdate(TransferFunctionParam p) {
    boolean updateOspf = (!_isExport && _to.isOspf());
    boolean updateBgp = (_isExport && _to.isBgp());
    boolean updateMetric = updateOspf || updateBgp;
    if (updateMetric) {
      // If it is a BGP route learned from IGP, then we use metric 0
      ArithExpr newValue;
      ArithExpr cost = _enc.mkInt(_addedCost);
      ArithExpr sum = _enc.mkSum(p.getOther().getMetric(), cost);
      if (_to.isBgp()) {
        BoolExpr isBGP;
        String router = _conf.getName();
        boolean hasProtocolVar = _other.getProtocolHistory() != null;
        boolean onlyBGP = _enc.getOptimizations().getSliceHasSingleProtocol().contains(router);
        if (hasProtocolVar) {
          isBGP = _other.getProtocolHistory().checkIfValue(Protocol.BGP);
        } else if (onlyBGP) {
          isBGP = _enc.mkTrue();
        } else {
          isBGP = _enc.mkFalse();
        }
        newValue = _enc.mkIf(isBGP, sum, cost);
      } else {
        newValue = sum;
      }
      p.getOther().setMetric(newValue);
    }
  }

  private void setDefaultLocalPref(TransferFunctionParam p) {
    // must be the case that it is an environment variable
    if (p.getOther().getLocalPref() == null) {
      p.getOther().setLocalPref(_enc.mkInt(100));
    }
  }

  public BoolExpr compute() {
    SymbolicRecord o = new SymbolicRecord(_other);
    TransferFunctionParam p = new TransferFunctionParam(o);
    computeIntermediatePrefixLen(p);
    applyMetricUpdate(p);
    setDefaultLocalPref(p);
    TransferFunctionResult result = compute(_statements, p, initialResult());
    return result.getReturnValue();
  }
}
