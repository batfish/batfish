package org.batfish.compiler;

import static org.batfish.symbolic.CommunityVarCollector.collectCommunityVars;
import static org.batfish.symbolic.bdd.CommunityVarConverter.toCommunityVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
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
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.collections.PList;

/**
 * Class that computes a symbolic transfer function between two symbolic control plane records. The
 * transfer function is used to encode both import and export filters.
 *
 * <p>Batfish represents the AST much like vendors where there is a simple imperative language for
 * matching fields and making modifications to fields. Since this is not a good fit for a
 * declarative symbolic encoding of the network, we convert this stateful representation into a
 * stateless representation
 *
 * <p>The TransferSSA class makes policies stateless by converting the vendor-independent format to
 * a Static Single Assignment (SSA) form where all updates are reflected in new variables. Rather
 * than create a full control flow graph (CFG) as is typically done in SSA, we use a simple
 * conversion based on adding join points for every variable modified in an if statement.
 *
 * <p>The joint point defined as the [phi] function from SSA merges variables that may differ across
 * different branches of an if statement. For example, if there is the following filter:
 *
 * <p>if match(c1) then add community c2 else prepend path 2
 *
 * <p>Then this function will introduce a new variable at the end of the if statement that updates
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
class TransferFunctionBuilder {

  private static int id = 0;

  private Configuration _conf;

  private List<Statement> _statements;

  private Interface _iface;

  private GraphEdge _graphEdge;

  private boolean _isExport;

  TransferFunctionBuilder(
      Configuration conf, List<Statement> statements, GraphEdge ge, boolean isExport) {
    _conf = conf;
    _statements = statements;
    _graphEdge = ge;
    _iface = ge.getStart();
    _isExport = isExport;
  }

  private String firstBitsEqual(String x, long y, int n) {
    assert (n >= 0 && n <= 32);
    if (n == 0) {
      return "true";
    }
    String lower = "" + y;
    String upper = "" + y + (int) Math.pow(2, 32 - n);
    return mkAnd(mkGe(x, lower), mkLt(x, upper));
  }

  /*
   * Check if a prefix range match is applicable for the packet destination
   * Ip address, given the prefix length variable.
   */
  String isRelevantFor(Environment env, PrefixRange range) {
    String prefixLen = env.get_prefixLength();
    Prefix p = range.getPrefix();
    SubRange r = range.getLengthRange();
    long pfx = p.getStartIp().asLong();
    int len = p.getPrefixLength();
    int lower = r.getStart();
    int upper = r.getEnd();
    // well formed prefix
    assert (p.getPrefixLength() <= lower && lower <= upper);
    String lowerBitsMatch = firstBitsEqual(env.get_prefixValue(), pfx, len);
    if (lower == upper) {
      String equalLen = mkEq(prefixLen, mkInt(lower));
      return mkAnd(equalLen, lowerBitsMatch);
    } else {
      String lengthLowerBound = mkGe(prefixLen, mkInt(lower));
      String lengthUpperBound = mkLe(prefixLen, mkInt(upper));
      return mkAnd(lengthLowerBound, mkAnd(lengthUpperBound, lowerBitsMatch));
    }
  }

  /*
   * Converts a route filter list to a boolean expression.
   */
  private String matchFilterList(RouteFilterList x, Environment other) {
    String acc = "(false";

    List<RouteFilterLine> lines = new ArrayList<>(x.getLines());
    Collections.reverse(lines);

    for (RouteFilterLine line : lines) {
      if (!line.getIpWildcard().isPrefix()) {
        throw new BatfishException("non-prefix IpWildcards are unsupported");
      }
      Prefix p = line.getIpWildcard().toPrefix();
      SubRange r = line.getLengthRange();
      PrefixRange range = new PrefixRange(p, r);
      String matches = isRelevantFor(other, range);
      String action = mkBool(line.getAction() == LineAction.PERMIT);
      acc = mkIf(matches, action, acc);
    }
    return acc + ")";
  }

  /*
   * Converts a prefix set to a boolean expression.
   */
  private TransferResult<String, String> matchPrefixSet(
      Configuration conf, PrefixSetExpr e, Environment other) {

    TransferResult<String, String> result = new TransferResult<>();

    if (e instanceof ExplicitPrefixSet) {
      ExplicitPrefixSet x = (ExplicitPrefixSet) e;

      Set<PrefixRange> ranges = x.getPrefixSpace().getPrefixRanges();
      if (ranges.isEmpty()) {
        return result.setReturnValue("true");
      }

      // Compute if the other best route is relevant for this match statement
      String acc = "(false";
      for (PrefixRange range : ranges) {
        acc = mkOr(acc, isRelevantFor(other, range));
      }

      return result.setReturnValue(acc + ")");

    } else if (e instanceof NamedPrefixSet) {
      NamedPrefixSet x = (NamedPrefixSet) e;
      String name = x.getName();
      RouteFilterList fl = conf.getRouteFilterLists().get(name);
      return result.setReturnValue(matchFilterList(fl, other));

    } else {
      throw new BatfishException("TODO: match prefix set: " + e);
    }
  }

  private long communityVarToNvValue(CommunityVar cvar) {
    Long l = cvar.asLong();
    if (l == null) {
      return 0;
    }
    return l;
  }

  /*
   * Converts a community list to a boolean expression.
   */
  private String matchCommunityList(CommunityList cl, Environment other) {
    List<CommunityListLine> lines = new ArrayList<>(cl.getLines());
    Collections.reverse(lines);
    String acc = "false";
    for (CommunityListLine line : lines) {
      boolean action = (line.getAction() == LineAction.PERMIT);
      CommunityVar cvar = toCommunityVar(line.getMatchCondition());
      String c = other.get_communities() + "[" + communityVarToNvValue(cvar) + "]";
      acc = mkIf(c, mkBool(action), acc);
    }
    return acc;
  }

  /*
   * Converts a community set to a boolean expression
   */
  private String matchCommunitySet(Configuration conf, CommunitySetExpr e, Environment other) {
    if (e instanceof CommunityList) {
      Set<CommunityVar> comms = collectCommunityVars(conf, e);
      String acc = "(true";
      for (CommunityVar comm : comms) {
        String c = other.get_communities() + "[" + comm.getValue() + "]";
        acc = mkAnd(acc, c);
      }
      return acc + ")";
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
  private TransferResult<String, String> fromExpr(String b) {
    return new TransferResult<String, String>().setReturnAssignedValue("true").setReturnValue(b);
  }

  private TransferResult<String, String> initialResult() {
    return new TransferResult<String, String>()
        .setReturnValue("false")
        .setFallthroughValue("false")
        .setReturnAssignedValue("false");
  }

  /*
   * Convert a Batfish AST boolean expression to a symbolic Z3 boolean expression
   * by performing inlining of stateful side effects.
   */
  private TransferResult<String, String> compute(BooleanExpr expr, TransferParam<Environment> p) {
    TransferParam<Environment> pCur = p;
    // TODO: right now everything is IPV4
    if (expr instanceof MatchIpv4) {
      pCur.debug("MatchIpv4");
      return fromExpr("true");
    }
    if (expr instanceof MatchIpv6) {
      pCur.debug("MatchIpv6");
      return fromExpr("false");
    }

    if (expr instanceof Conjunction) {
      pCur.debug("Conjunction");
      Conjunction c = (Conjunction) expr;
      String acc = "(";
      TransferResult<String, String> result = new TransferResult<>();
      for (BooleanExpr be : c.getConjuncts()) {
        TransferResult<String, String> r = compute(be, pCur.indent());
        result = result.addChangedVariables(r);
        acc = acc.equals("(") ? acc + r.getReturnValue() : mkAnd(acc, r.getReturnValue());
      }
      pCur.debug("has changed variable");
      return result.setReturnValue(acc + ")");
    }

    if (expr instanceof Disjunction) {
      pCur.debug("Disjunction");
      Disjunction d = (Disjunction) expr;
      String acc = "(";
      TransferResult<String, String> result = new TransferResult<>();
      for (BooleanExpr be : d.getDisjuncts()) {
        TransferResult<String, String> r = compute(be, pCur.indent());
        result = result.addChangedVariables(r);
        acc = acc.equals("(") ? acc + r.getReturnValue() : mkOr(acc, r.getReturnValue());
      }
      pCur.debug("has changed variable");
      return result.setReturnValue(acc + ")");
    }

    if (expr instanceof ConjunctionChain) {
      pCur.debug("ConjunctionChain");
      ConjunctionChain d = (ConjunctionChain) expr;
      List<BooleanExpr> conjuncts = new ArrayList<>(d.getSubroutines());
      if (pCur.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(pCur.getDefaultPolicy().getDefaultPolicy());
        conjuncts.add(be);
      }
      if (conjuncts.isEmpty()) {
        return fromExpr("true");
      } else {
        TransferResult<String, String> result = new TransferResult<>();
        String acc = "(false";
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr conjunct = conjuncts.get(i);
          TransferParam<Environment> param =
              pCur.setDefaultPolicy(null).setChainContext(TransferParam.ChainContext.CONJUNCTION);
          TransferResult<String, String> r = compute(conjunct, param);
          result = result.addChangedVariables(r);
          acc = mkIf(r.getFallthroughValue(), acc, r.getReturnValue());
        }
        acc = acc + ")";
        pCur.debug("ConjunctionChain Result: " + acc);
        return result.setReturnValue(acc);
      }
    }

    if (expr instanceof DisjunctionChain) {
      pCur.debug("DisjunctionChain");
      DisjunctionChain d = (DisjunctionChain) expr;
      List<BooleanExpr> disjuncts = new ArrayList<>(d.getSubroutines());
      if (pCur.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(pCur.getDefaultPolicy().getDefaultPolicy());
        disjuncts.add(be);
      }
      if (disjuncts.isEmpty()) {
        return fromExpr("true");
      } else {
        TransferResult<String, String> result = new TransferResult<>();
        String acc = "(false";
        for (int i = disjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr disjunct = disjuncts.get(i);
          TransferParam<Environment> param =
              pCur.setDefaultPolicy(null).setChainContext(TransferParam.ChainContext.CONJUNCTION);
          TransferResult<String, String> r = compute(disjunct, param);
          result.addChangedVariables(r);
          acc = mkIf(r.getFallthroughValue(), acc, r.getReturnValue());
        }
        acc = acc + ")";
        pCur.debug("DisjunctionChain Result: " + acc);
        return result.setReturnValue(acc);
      }
    }

    if (expr instanceof Not) {
      pCur.debug("mkNot");
      Not n = (Not) expr;
      TransferResult<String, String> result = compute(n.getExpr(), pCur);
      return result.setReturnValue(mkNot(result.getReturnValue()));
    }

    if (expr instanceof MatchProtocol) {
      MatchProtocol mp = (MatchProtocol) expr;
      Protocol proto = Protocol.fromRoutingProtocol(mp.getProtocol());
      if (proto == null) {
        pCur.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): false");
        return fromExpr("false");
      }

      int x;
      if (proto.isConnected()) {
        x = 0;
      } else if (proto.isStatic()) {
        x = 1;
      } else if (proto.isOspf()) {
        x = 2;
      } else if (proto.isBgp()) {
        x = 3;
      } else {
        throw new BatfishException("invalid protocol: " + proto.name());
      }
      String protoMatch = "(isProtocol " + x + ")";
      pCur.debug("MatchProtocol(" + mp.getProtocol().protocolName() + "): " + protoMatch);
      return fromExpr(protoMatch);
    }

    if (expr instanceof MatchPrefixSet) {
      pCur.debug("MatchPrefixSet");
      MatchPrefixSet m = (MatchPrefixSet) expr;
      // For BGP, may change prefix length
      TransferResult<String, String> result =
          matchPrefixSet(_conf, m.getPrefixSet(), pCur.getData());
      return result.setReturnAssignedValue("true");

      // TODO: implement me
    } else if (expr instanceof MatchPrefix6Set) {
      pCur.debug("MatchPrefix6Set");
      return fromExpr("false");

    } else if (expr instanceof CallExpr) {
      pCur.debug("CallExpr");
      // TODO: the call can modify certain fields, need to keep track of these variables
      CallExpr c = (CallExpr) expr;
      String name = c.getCalledPolicyName();
      RoutingPolicy pol = _conf.getRoutingPolicies().get(name);
      pCur = pCur.setCallContext(TransferParam.CallContext.EXPR_CALL);
      TransferResult<String, String> r =
          compute(pol.getStatements(), pCur.indent().enterScope(name), initialResult());
      pCur.debug("CallExpr (return): " + r.getReturnValue());
      pCur.debug("CallExpr (fallthrough): " + r.getFallthroughValue());
      return r;

    } else if (expr instanceof WithEnvironmentExpr) {
      pCur.debug("WithEnvironmentExpr");
      // TODO: this is not correct
      WithEnvironmentExpr we = (WithEnvironmentExpr) expr;
      // TODO: postStatements() and preStatements()
      return compute(we.getExpr(), pCur);

    } else if (expr instanceof MatchCommunitySet) {
      pCur.debug("MatchCommunitySet");
      MatchCommunitySet mcs = (MatchCommunitySet) expr;
      return fromExpr(matchCommunitySet(_conf, mcs.getExpr(), pCur.getData()));

    } else if (expr instanceof BooleanExprs.StaticBooleanExpr) {
      BooleanExprs.StaticBooleanExpr b = (BooleanExprs.StaticBooleanExpr) expr;
      switch (b.getType()) {
        case CallExprContext:
          pCur.debug("CallExprContext");
          return fromExpr(mkBool(pCur.getCallContext() == TransferParam.CallContext.EXPR_CALL));
        case CallStatementContext:
          pCur.debug("CallStmtContext");
          return fromExpr(mkBool(pCur.getCallContext() == TransferParam.CallContext.STMT_CALL));
        case True:
          pCur.debug("True");
          return fromExpr("true");
        case False:
          pCur.debug("False");
          return fromExpr("false");
        default:
          throw new BatfishException(
              "Unhandled " + BooleanExprs.class.getCanonicalName() + ": " + b.getType());
      }
    } else if (expr instanceof MatchAsPath) {
      pCur.debug("MatchAsPath");
      System.out.println("Warning: use of unimplemented feature MatchAsPath");
      return fromExpr("false");
    }

    String s = (_isExport ? "export" : "import");
    String msg =
        String.format(
            "Unimplemented feature %s for %s transfer function on interface %s",
            expr.toString(), s, _graphEdge.toString());

    throw new BatfishException(msg);
  }

  /*
   * Apply the effect of modifying a long value (e.g., to set the metric)
   */
  private String applyLongExprModification(String x, LongExpr e) {
    if (e instanceof LiteralLong) {
      LiteralLong z = (LiteralLong) e;
      return "" + z.getValue();
    }
    if (e instanceof DecrementMetric) {
      DecrementMetric z = (DecrementMetric) e;
      return "(" + x + " - " + z.getSubtrahend() + ")";
    }
    if (e instanceof IncrementMetric) {
      IncrementMetric z = (IncrementMetric) e;
      return "(" + x + z.getAddend() + ")";
    }
    throw new BatfishException("int expr transfer function: " + e);
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
  private TransferResult<String, String> returnValue(
      TransferParam<Environment> p, TransferResult<String, String> r, boolean val) {
    String b = mkIf(r.getReturnAssignedValue(), r.getReturnValue(), "" + val);
    return r.setReturnValue(b).setReturnAssignedValue("true").addChangedVariable("RETURN", b);
  }

  private TransferResult<String, String> fallthrough(
      TransferParam<Environment> p, TransferResult<String, String> r) {
    String b = mkIf(r.getReturnAssignedValue(), r.getFallthroughValue(), "true");
    return r.setFallthroughValue(b)
        .setReturnAssignedValue("true")
        .addChangedVariable("FALLTHROUGH", b);
  }

  private void updateSingleValue(TransferParam<Environment> p, String variableName, String expr) {
    switch (variableName) {
      case "METRIC":
        p.getData().set_cost(expr);
        break;
      case "ADMIN-DIST":
        p.getData().set_ad(expr);
        break;
      case "LOCAL-PREF":
        p.getData().set_lp(expr);
        break;
      case "RETURN":
        break;
      case "COMMUNITIES":
        p.getData().set_communities(expr);
        break;
      default:
        throw new BatfishException("bad");
    }
  }

  private String mkIf(String guard, String trueBranch, String falseBranch) {
    if (guard.equals("true")) {
      return trueBranch;
    }
    if (guard.equals("false")) {
      return falseBranch;
    }
    if (trueBranch.equals("true") && falseBranch.equals("false")) {
      return guard;
    }
    if (trueBranch.equals("false") && falseBranch.equals("true")) {
      return mkNot(guard);
    }
    if (trueBranch.equals(falseBranch)) {
      return trueBranch;
    }
    if (guard.equals(trueBranch)) {
      return mkIf(guard, "true", falseBranch);
    }
    if (guard.equals(falseBranch)) {
      return mkIf(guard, trueBranch, "false");
    }
    return "(if " + guard + " then " + trueBranch + " else " + falseBranch + ")";
  }

  private String mkAnd(String x, String y) {
    if (x.equals("true")) {
      return y;
    }
    if (y.equals("true")) {
      return x;
    }
    if (x.equals("false") || y.equals("false")) {
      return "false";
    }
    return "(" + x + " and " + y + ")";
  }

  private String mkOr(String x, String y) {
    if (x.equals("false")) {
      return y;
    }
    if (y.equals("false")) {
      return x;
    }
    if (x.equals("true") || y.equals("true")) {
      return "true";
    }
    return "(" + x + " or " + y + ")";
  }

  private String mkNot(String x) {
    if (x.equals("true")) {
      return "false";
    }
    if (x.equals("false")) {
      return "true";
    }
    return "(not " + x + ")";
  }

  private String mkBool(boolean b) {
    return "" + b;
  }

  private String mkInt(int i) {
    return "" + i;
  }

  private String mkGe(String x, String y) {
    return "(" + x + " >= " + y + ")";
  }

  private String mkGt(String x, String y) {
    return "(" + x + " > " + y + ")";
  }

  private String mkLe(String x, String y) {
    return "(" + x + " <= " + y + ")";
  }

  private String mkLt(String x, String y) {
    return "(" + x + " < " + y + ")";
  }

  private String mkEq(String x, String y) {
    return "(" + x + " = " + y + ")";
  }

  /*
   * The [phi] function from SSA that merges variables that may differ across
   * different branches of an If statement.
   */
  private Pair<String, String> joinPoint(
      TransferParam<Environment> p,
      TransferResult<String, String> r,
      String guard,
      Pair<String, Pair<String, String>> values) {
    String variableName = values.getFirst();
    String trueBranch = values.getSecond().getFirst();
    String falseBranch = values.getSecond().getSecond();

    if (variableName.equals("RETURN") || variableName.equals("FALLTHROUGH")) {
      String t =
          (trueBranch == null
              ? "false"
              : trueBranch); // can use False because the value has not been assigned
      String f = (falseBranch == null ? "false" : falseBranch);
      String tass = (trueBranch == null ? r.getReturnAssignedValue() : "true");
      String fass = (falseBranch == null ? r.getReturnAssignedValue() : "true");
      String newAss = mkIf(guard, tass, fass);
      String variable =
          (variableName.equals("RETURN") ? r.getReturnValue() : r.getFallthroughValue());
      String newValue = mkIf(r.getReturnAssignedValue(), variable, mkIf(guard, t, f));
      return new Pair<>(newValue, newAss);
    }
    if (variableName.equals("ADMIN-DIST")) {
      String t = (trueBranch == null ? p.getData().get_ad() : trueBranch);
      String f = (falseBranch == null ? p.getData().get_ad() : falseBranch);
      String newValue = mkIf(guard, t, f);
      newValue = mkIf(r.getReturnAssignedValue(), p.getData().get_ad(), newValue);
      p.getData().set_ad(newValue);
      return new Pair<>(newValue, null);
    }
    if (variableName.equals("LOCAL-PREF")) {
      String t = (trueBranch == null ? p.getData().get_lp() : trueBranch);
      String f = (falseBranch == null ? p.getData().get_lp() : falseBranch);
      String newValue = mkIf(guard, t, f);
      newValue = mkIf(r.getReturnAssignedValue(), p.getData().get_lp(), newValue);
      p.getData().set_lp(newValue);
      return new Pair<>(newValue, null);
    }
    if (variableName.equals("METRIC")) {
      String t = (trueBranch == null ? p.getData().get_cost() : trueBranch);
      String f = (falseBranch == null ? p.getData().get_cost() : falseBranch);
      String newValue = mkIf(guard, t, f);
      newValue = mkIf(r.getReturnAssignedValue(), p.getData().get_cost(), newValue);
      p.getData().set_cost(newValue);
      return new Pair<>(newValue, null);
    }
    if (variableName.equals("COMMUNITIES")) {
      String t = (trueBranch == null ? p.getData().get_communities() : trueBranch);
      String f = (falseBranch == null ? p.getData().get_communities() : falseBranch);
      String newValue = mkIf(guard, t, f);
      newValue = mkIf(r.getReturnAssignedValue(), p.getData().get_communities(), newValue);
      p.getData().set_communities(newValue);
      return new Pair<>(newValue, null);
    }
    throw new BatfishException("[joinPoint]: unhandled case for " + variableName);
  }

  /*
   * Apply the effect of modifying an integer value (e.g., to set the local pref)
   */
  private String applyIntExprModification(String x, IntExpr e) {
    if (e instanceof LiteralInt) {
      LiteralInt z = (LiteralInt) e;
      return mkInt(z.getValue());
    }
    if (e instanceof IncrementLocalPreference) {
      IncrementLocalPreference z = (IncrementLocalPreference) e;
      return "(" + x + " + " + mkInt(z.getAddend()) + ")";
    }
    if (e instanceof DecrementLocalPreference) {
      DecrementLocalPreference z = (DecrementLocalPreference) e;
      return "(" + x + mkInt(z.getSubtrahend()) + ")";
    }
    throw new BatfishException("TODO: int expr transfer function: " + e);
  }

  /*
   * Convert a list of statements into a Z3 boolean expression for the transfer function.
   */
  private TransferResult<String, String> compute(
      List<Statement> statements,
      TransferParam<Environment> p,
      TransferResult<String, String> result) {

    TransferParam<Environment> curP = p;
    TransferResult<String, String> curResult = result;
    boolean doesReturn = false;

    for (Statement stmt : statements) {

      if (stmt instanceof StaticStatement) {
        StaticStatement ss = (StaticStatement) stmt;

        switch (ss.getType()) {
          case ExitAccept:
            doesReturn = true;
            curP.debug("ExitAccept");
            curResult = returnValue(curP, curResult, true);
            break;

            // TODO: implement proper unsuppression of routes covered by aggregates
          case Unsuppress:
          case ReturnTrue:
            doesReturn = true;
            curP.debug("ReturnTrue");
            curResult = returnValue(curP, curResult, true);
            break;

          case ExitReject:
            doesReturn = true;
            curP.debug("ExitReject");
            curResult = returnValue(curP, curResult, false);
            break;

            // TODO: implement proper suppression of routes covered by aggregates
          case Suppress:
          case ReturnFalse:
            doesReturn = true;
            curP.debug("ReturnFalse");
            curResult = returnValue(curP, curResult, false);
            break;

          case SetDefaultActionAccept:
            curP.debug("SetDefaulActionAccept");
            curP = curP.setDefaultAccept(true);
            break;

          case SetDefaultActionReject:
            curP.debug("SetDefaultActionReject");
            curP = curP.setDefaultAccept(false);
            break;

          case SetLocalDefaultActionAccept:
            curP.debug("SetLocalDefaultActionAccept");
            curP = curP.setDefaultAcceptLocal(true);
            break;

          case SetLocalDefaultActionReject:
            curP.debug("SetLocalDefaultActionReject");
            curP = curP.setDefaultAcceptLocal(false);
            break;

          case ReturnLocalDefaultAction:
            curP.debug("ReturnLocalDefaultAction");
            // TODO: need to set local default action in an environment
            if (curP.getDefaultAcceptLocal()) {
              curResult = returnValue(curP, curResult, true);
            } else {
              curResult = returnValue(curP, curResult, false);
            }
            break;

          case FallThrough:
            curP.debug("Fallthrough");
            curResult = fallthrough(curP, curResult);
            break;

          case Return:
            // TODO: assumming this happens at the end of the function, so it is ignored for now.
            curP.debug("Return");
            break;

          case RemovePrivateAs:
            curP.debug("RemovePrivateAs");
            System.out.println("Warning: use of unimplemented feature RemovePrivateAs");
            break;

          default:
            throw new BatfishException("TODO: computeTransferFunction: " + ss.getType());
        }

      } else if (stmt instanceof If) {
        curP.debug("If");
        If i = (If) stmt;
        TransferResult<String, String> r = compute(i.getGuard(), curP);
        curResult = curResult.addChangedVariables(r);
        String guard = r.getReturnValue();
        String str = guard;

        // If there are updates in the guard, add them to the parameter p before entering branches
        for (Pair<String, String> changed : r.getChangedVariables()) {
          curP.debug("CHANGED: " + changed.getFirst());
          updateSingleValue(curP, changed.getFirst(), changed.getSecond());
        }

        curP.debug("guard: " + str);
        // If we know the branch ahead of time, then specialize
        switch (str) {
          case "true":
            curP.debug("True Branch");
            curResult = compute(i.getTrueStatements(), curP.indent(), curResult);
            break;
          case "false":
            curP.debug("False Branch");
            compute(i.getFalseStatements(), curP.indent(), curResult);
            break;
          default:
            curP.debug("True Branch");
            // clear changed variables before proceeding
            Environment env;
            TransferParam<Environment> p1 = curP.indent().setData(curP.getData().deepCopy());
            TransferParam<Environment> p2 = curP.indent().setData(curP.getData().deepCopy());

            TransferResult<String, String> trueBranch =
                compute(i.getTrueStatements(), p1, initialResult());
            curP.debug("False Branch");
            TransferResult<String, String> falseBranch =
                compute(i.getFalseStatements(), p2, initialResult());
            curP.debug("JOIN");
            PList<Pair<String, Pair<String, String>>> pairs =
                trueBranch.mergeChangedVariables(falseBranch);

            // Extract and deal with the return value first so that other
            // variables have this reflected in their value
            int idx = pairs.find(pair -> pair.getFirst().equals("RETURN"));
            if (idx >= 0) {
              Pair<String, Pair<String, String>> ret = pairs.get(idx);
              pairs = pairs.minus(idx);
              pairs = pairs.plus(pairs.size(), ret);
            }

            for (Pair<String, Pair<String, String>> pair : pairs) {
              String s = pair.getFirst();
              curP.debug("CHANGED: " + s);
              Pair<String, String> x = joinPoint(curP, curResult, guard, pair);
              curResult = curResult.addChangedVariable(s, x.getFirst());
              if (s.equals("RETURN")) {
                curResult =
                    curResult.setReturnValue(x.getFirst()).setReturnAssignedValue(x.getSecond());
              }
              if (s.equals("FALLTHROUGH")) {
                curResult =
                    curResult
                        .setFallthroughValue(x.getFirst())
                        .setReturnAssignedValue(x.getSecond());
              }
            }

            break;
        }

      } else if (stmt instanceof SetDefaultPolicy) {
        curP.debug("SetDefaultPolicy");
        curP = curP.setDefaultPolicy((SetDefaultPolicy) stmt);

      } else if (stmt instanceof SetMetric) {
        curP.debug("SetMetric");

        SetMetric sm = (SetMetric) stmt;
        LongExpr ie = sm.getMetric();
        String newValue = applyLongExprModification(curP.getData().get_cost(), ie);
        newValue = mkIf(curResult.getReturnAssignedValue(), curP.getData().get_cost(), newValue);
        curP.getData().set_cost(newValue);
        curResult = curResult.addChangedVariable("METRIC", newValue);

      } else if (stmt instanceof AddCommunity) {
        curP.debug("AddCommunity");
        AddCommunity ac = (AddCommunity) stmt;
        Set<CommunityVar> comms = collectCommunityVars(_conf, ac.getExpr());

        // set[x := true][y := true]
        String commExpr = curP.getData().get_communities();
        String newValue = "";
        for (CommunityVar cvar : comms) {
          newValue = newValue + "[" + communityVarToNvValue(cvar) + ":= true]";
        }
        newValue = mkIf(curResult.getReturnAssignedValue(), commExpr, commExpr + newValue);
        curP.getData().set_communities(newValue);
        curResult = curResult.addChangedVariable("COMMUNITIES", commExpr);

      } else if (stmt instanceof SetCommunity) {
        curP.debug("SetCommunity");
        SetCommunity sc = (SetCommunity) stmt;
        Set<CommunityVar> comms = collectCommunityVars(_conf, sc.getExpr());

        // set[x := true][y := true]
        String commExpr = curP.getData().get_communities();
        String newValue = "";
        for (CommunityVar cvar : comms) {
          newValue = newValue + "[" + communityVarToNvValue(cvar) + ":= true]";
        }
        newValue = mkIf(curResult.getReturnAssignedValue(), commExpr, commExpr + newValue);
        curP.getData().set_communities(newValue);
        curResult = curResult.addChangedVariable("COMMUNITIES", commExpr);

      } else if (stmt instanceof DeleteCommunity) {
        curP.debug("DeleteCommunity");
        DeleteCommunity ac = (DeleteCommunity) stmt;
        Set<CommunityVar> comms = collectCommunityVars(_conf, ac.getExpr());

        // set[x := true][y := true]
        String commExpr = curP.getData().get_communities();
        String newValue = "";
        for (CommunityVar cvar : comms) {
          newValue = newValue + "[" + communityVarToNvValue(cvar) + ":= false]";
        }
        newValue = mkIf(curResult.getReturnAssignedValue(), commExpr, commExpr + newValue);
        curP.getData().set_communities(newValue);
        curResult = curResult.addChangedVariable("COMMUNITIES", commExpr);

      } else if (stmt instanceof RetainCommunity) {
        curP.debug("RetainCommunity");
        // no op

      } else if (stmt instanceof SetLocalPreference) {
        curP.debug("SetLocalPreference");
        SetLocalPreference slp = (SetLocalPreference) stmt;
        IntExpr ie = slp.getLocalPreference();
        String newValue = applyIntExprModification(curP.getData().get_lp(), ie);
        curP.debug("Value after modification: " + newValue);
        newValue = mkIf(curResult.getReturnAssignedValue(), curP.getData().get_lp(), newValue);
        curP.debug("Value after modification: " + newValue);
        curP.getData().set_lp(newValue);
        curResult = curResult.addChangedVariable("LOCAL-PREF", newValue);

      } else if (stmt instanceof PrependAsPath) {
        curP.debug("PrependAsPath");
        PrependAsPath pap = (PrependAsPath) stmt;
        Integer prependCost = prependLength(pap.getExpr());
        String newValue = curP.getData().get_cost() + " + " + prependCost;
        newValue = mkIf(curResult.getReturnAssignedValue(), curP.getData().get_cost(), newValue);
        curP.getData().set_cost(newValue);
        curResult = curResult.addChangedVariable("METRIC", newValue);

      } else if (stmt instanceof SetOrigin) {
        curP.debug("SetOrigin");
        System.out.println("Warning: use of unimplemented feature SetOrigin");

      } else if (stmt instanceof SetNextHop) {
        curP.debug("SetNextHop");
        System.out.println("Warning: use of unimplemented feature SetNextHop");

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
    if (curP.getInitialCall()) {
      curP.debug("InitialCall finalizing");

      // Apply the default action
      if (!doesReturn) {
        curP.debug("Applying default action: " + curP.getDefaultAccept());
        if (curP.getDefaultAccept()) {
          curResult = returnValue(curP, curResult, true);
        } else {
          curResult = returnValue(curP, curResult, false);
        }
      }
      String ret = result.getReturnValue();
      String retVal =
          mkIf(
              curResult.getReturnValue(),
              "(Some ("
                  + p.getData().get_ad()
                  + ","
                  + p.getData().get_lp()
                  + ","
                  + p.getData().get_cost()
                  + ","
                  + p.getData().get_med()
                  + ","
                  + p.getData().get_communities()
                  + "))",
              "None");
      curResult = curResult.setReturnValue(retVal);
    }
    return curResult;
  }

  public String compute() {
    Environment env = new Environment();
    TransferParam<Environment> p = new TransferParam<>(env, true);
    TransferResult<String, String> result = compute(_statements, p, initialResult());
    return result.getReturnValue();
  }
}
