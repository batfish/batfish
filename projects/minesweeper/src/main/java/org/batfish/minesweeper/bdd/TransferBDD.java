package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.AsPathSetElem;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitAsPathSet;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.IpPrefix;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchIpv6;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.PrefixExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.IDeepCopy;
import org.batfish.minesweeper.OspfType;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.SymbolicRegex;
import org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD.Arg;
import org.batfish.minesweeper.utils.PrefixUtils;

/** @author Ryan Beckett */
public class TransferBDD {

  private static BDDFactory factory = BDDRoute.factory;

  /**
   * We track community and AS-path regexes by computing a set of atomic predicates for them. See
   * {@link org.batfish.minesweeper.RegexAtomicPredicates}. During the symbolic route analysis, we
   * simply need the map from each regex to its corresponding set of atomic predicates, each
   * represented by a unique integer.
   */
  private final Map<CommunityVar, Set<Integer>> _communityAtomicPredicates;

  private final Map<SymbolicAsPathRegex, Set<Integer>> _asPathRegexAtomicPredicates;

  private final Configuration _conf;

  private final Graph _graph;

  private Set<Prefix> _ignoredNetworks;

  private final List<Statement> _statements;

  private final BDDRoute _originalRoute;

  private final boolean _useOutputAttributes;

  public TransferBDD(Graph g, Configuration conf, List<Statement> statements) {
    this(g, conf, statements, Environment.useOutputAttributesFor(conf));
  }

  @VisibleForTesting
  TransferBDD(
      Graph g, Configuration conf, List<Statement> statements, boolean useOutputAttributes) {
    _graph = g;
    _conf = conf;
    _statements = statements;

    _originalRoute = new BDDRoute(g);
    _communityAtomicPredicates = _graph.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    _asPathRegexAtomicPredicates =
        _graph.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
    _useOutputAttributes = useOutputAttributes;
  }

  /*
   * Check if the first length bits match the BDDInteger
   * representing the advertisement prefix.
   *
   * Note: We assume the prefix is never modified, so it will
   * be a bitvector containing only the underlying variables:
   * [var(0), ..., var(n)]
   */
  public static BDD firstBitsEqual(BDD[] bits, Prefix p, int length) {
    long b = p.getStartIp().asLong();
    BDD acc = factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.diff(bits[i]);
      }
    }
    return acc;
  }

  /*
   * Apply the effect of modifying a long value (e.g., to set the metric)
   */
  private BDDInteger applyLongExprModification(
      TransferParam<BDDRoute> p, BDDInteger x, LongExpr e) {
    checkArgument(e instanceof LiteralLong, "Unsupported integer update: " + e);
    LiteralLong z = (LiteralLong) e;
    p.debug("LiteralLong: " + z.getValue());
    return BDDInteger.makeFromValue(x.getFactory(), 32, z.getValue());

    /* TODO: These old cases are not correct; removing for now since they are not currently used.
    First, they should dec/inc the corresponding field of the route, not whatever BDDInteger x
    is passed in.  Second, they need to prevent overflow.  See LongExpr::evaluate for details.

    if (e instanceof DecrementMetric) {
      DecrementMetric z = (DecrementMetric) e;
      p.debug("Decrement: " + z.getSubtrahend());
      return x.sub(BDDInteger.makeFromValue(x.getFactory(), 32, z.getSubtrahend()));
    }
    if (e instanceof IncrementMetric) {
      IncrementMetric z = (IncrementMetric) e;
      p.debug("Increment: " + z.getAddend());
      return x.add(BDDInteger.makeFromValue(x.getFactory(), 32, z.getAddend()));
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
     */
  }

  // produce a BDD that represents the disjunction of all atomic predicates associated with any of
  // the given as-path regexes
  BDD asPathRegexesToBDD(Set<SymbolicAsPathRegex> asPathRegexes, BDDRoute route) {
    Set<Integer> asPathAPs = atomicPredicatesFor(asPathRegexes, _asPathRegexAtomicPredicates);
    BDD[] apBDDs = route.getAsPathRegexAtomicPredicates();
    return route
        .getFactory()
        .orAll(asPathAPs.stream().map(ap -> apBDDs[ap]).collect(Collectors.toList()));
  }

  // produce the union of all atomic predicates associated with any of the given symbolic regexes
  <T extends SymbolicRegex> Set<Integer> atomicPredicatesFor(
      Set<T> regexes, Map<T, Set<Integer>> apMap) {
    return regexes.stream()
        .flatMap(r -> apMap.get(r).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  /*
   * Convert a Batfish AST boolean expression to a BDD.
   * TODO: Any updates to the TransferParam in expr are lost currently
   */
  private TransferResult compute(BooleanExpr expr, TransferBDDState state) {

    TransferParam<BDDRoute> p = state.getTransferParam();
    TransferResult result = state.getTransferResult();

    // TODO: right now everything is IPV4
    if (expr instanceof MatchIpv4) {
      p.debug("MatchIpv4 Result: true");
      return result.setReturnValueBDD(factory.one());

    } else if (expr instanceof MatchIpv6) {
      p.debug("MatchIpv6 Result: false");
      return result.setReturnValueBDD(factory.zero());

    } else if (expr instanceof Conjunction) {
      p.debug("Conjunction");
      Conjunction c = (Conjunction) expr;
      BDD acc = factory.one();
      for (BooleanExpr be : c.getConjuncts()) {
        TransferResult resultCopy =
            result.setReturnValueBDDRoute(result.getReturnValue().getFirst().deepCopy());
        TransferResult newResult = compute(be, toTransferBDDState(p.indent(), resultCopy));
        // short-circuiting: only update the result if the prior conjuncts were all true
        result = ite(acc, newResult, result);
        acc = acc.and(newResult.getReturnValue().getSecond());
      }
      p.debug("Conjunction return: " + acc);
      return result.setReturnValueBDD(acc);

    } else if (expr instanceof Disjunction) {
      p.debug("Disjunction");
      Disjunction d = (Disjunction) expr;
      BDD acc = factory.zero();
      for (BooleanExpr be : d.getDisjuncts()) {
        TransferResult resultCopy =
            result.setReturnValueBDDRoute(result.getReturnValue().getFirst().deepCopy());
        TransferResult newResult = compute(be, toTransferBDDState(p.indent(), resultCopy));
        // short-circuiting: only update the result if the prior disjuncts were all false
        result = ite(acc, result, newResult);
        acc = acc.or(result.getReturnValue().getSecond());
      }
      p.debug("Disjunction return: " + acc);
      return result.setReturnValueBDD(acc);

    } else if (expr instanceof ConjunctionChain) {
      p.debug("ConjunctionChain");
      ConjunctionChain d = (ConjunctionChain) expr;
      List<BooleanExpr> conjuncts = new ArrayList<>(d.getSubroutines());
      if (p.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(p.getDefaultPolicy().getDefaultPolicy());
        conjuncts.add(be);
      }
      if (conjuncts.isEmpty()) {
        return result.setReturnValueBDD(factory.one());
      } else {
        TransferParam<BDDRoute> record = p;
        BDD acc = factory.zero();
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr conjunct = conjuncts.get(i);
          TransferParam<BDDRoute> param =
              record
                  .setDefaultPolicy(null)
                  .setChainContext(TransferParam.ChainContext.CONJUNCTION)
                  .indent();
          result = compute(conjunct, toTransferBDDState(param, result));
          record = record.setData(result.getReturnValue().getFirst());
          acc = ite(result.getFallthroughValue(), acc, result.getReturnValue().getSecond());
        }
        TransferReturn ret = new TransferReturn(record.getData(), acc);
        return result.setReturnValue(ret);
      }

    } else if (expr instanceof FirstMatchChain) {
      p.debug("FirstMatchChain");
      FirstMatchChain chain = (FirstMatchChain) expr;
      List<BooleanExpr> chainPolicies = new ArrayList<>(chain.getSubroutines());
      if (p.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(p.getDefaultPolicy().getDefaultPolicy());
        chainPolicies.add(be);
      }
      if (chainPolicies.isEmpty()) {
        // No identity for an empty FirstMatchChain; default policy should always be set.
        throw new BatfishException("Default policy is not set");
      }
      TransferParam<BDDRoute> record = p;
      BDD acc = factory.zero();
      for (int i = chainPolicies.size() - 1; i >= 0; i--) {
        BooleanExpr policyMatcher = chainPolicies.get(i);
        TransferParam<BDDRoute> param =
            record
                .setDefaultPolicy(null)
                .setChainContext(TransferParam.ChainContext.CONJUNCTION)
                .indent();
        result = compute(policyMatcher, toTransferBDDState(param, result));
        record = record.setData(result.getReturnValue().getFirst());
        acc = ite(result.getFallthroughValue(), acc, result.getReturnValue().getSecond());
      }
      TransferReturn ret = new TransferReturn(record.getData(), acc);
      return result.setReturnValue(ret);

    } else if (expr instanceof Not) {
      p.debug("mkNot");
      Not n = (Not) expr;
      result = compute(n.getExpr(), state);
      BDD returnedBDD = result.getReturnValue().getSecond();
      return result.setReturnValueBDD(returnedBDD.not());

    } else if (expr instanceof MatchProtocol) {
      MatchProtocol mp = (MatchProtocol) expr;
      Set<RoutingProtocol> rps = mp.getProtocols();
      if (rps.size() > 1) {
        // Hack: Minesweeper doesn't support MatchProtocol with multiple arguments.
        List<BooleanExpr> mps = rps.stream().map(MatchProtocol::new).collect(Collectors.toList());
        return compute(new Disjunction(mps), state);
      }
      RoutingProtocol rp = Iterables.getOnlyElement(rps);
      Protocol proto = Protocol.fromRoutingProtocol(rp);
      BDD protBDD = proto == null ? factory.zero() : p.getData().getProtocolHistory().value(proto);
      return result.setReturnValueBDD(protBDD);

    } else if (expr instanceof MatchPrefixSet) {
      p.debug("MatchPrefixSet");
      MatchPrefixSet m = (MatchPrefixSet) expr;

      // MatchPrefixSet::evaluate obtains the prefix to match (either the destination network or
      // next-hop IP) from the original route, so we do the same here
      BDD prefixSet = matchPrefixSet(p.indent(), _conf, m, _originalRoute);
      return result.setReturnValueBDD(prefixSet);

      // TODO: implement me
    } else if (expr instanceof MatchPrefix6Set) {
      p.debug("MatchPrefix6Set");
      return result.setReturnValueBDD(factory.zero());

    } else if (expr instanceof CallExpr) {
      p.debug("CallExpr");
      CallExpr c = (CallExpr) expr;
      String name = c.getCalledPolicyName();
      RoutingPolicy pol = _conf.getRoutingPolicies().get(name);

      // save callee state
      BDD oldReturnAssigned = result.getReturnAssignedValue();

      TransferParam<BDDRoute> newParam =
          p.setCallContext(TransferParam.CallContext.EXPR_CALL).indent().enterScope(name);
      TransferResult callResult =
          compute(
                  pol.getStatements(),
                  new TransferBDDState(newParam, result.setReturnAssignedValue(factory.zero())))
              .getTransferResult();

      // restore the original returnAssigned value
      return callResult.setReturnAssignedValue(oldReturnAssigned);

    } else if (expr instanceof WithEnvironmentExpr) {
      p.debug("WithEnvironmentExpr");
      // TODO: this is not correct
      WithEnvironmentExpr we = (WithEnvironmentExpr) expr;
      // TODO: postStatements() and preStatements()
      return compute(we.getExpr(), state);

    } else if (expr instanceof MatchCommunities) {
      p.debug("MatchCommunities");
      MatchCommunities mc = (MatchCommunities) expr;
      // we only handle the case where the expression being matched is just the input communities
      if (!mc.getCommunitySetExpr().equals(InputCommunities.instance())) {
        throw new BatfishException(
            "Matching for communities other than the input communities is not supported: " + mc);
      }
      BDD mcPredicate =
          mc.getCommunitySetMatchExpr()
              .accept(
                  new CommunitySetMatchExprToBDD(), new Arg(this, routeForMatching(p.getData())));
      return result.setReturnValueBDD(mcPredicate);

    } else if (expr instanceof MatchTag) {
      MatchTag mt = (MatchTag) expr;
      BDD mtBDD =
          matchIntComparison(mt.getCmp(), mt.getTag(), routeForMatching(p.getData()).getTag());
      return result.setReturnValueBDD(mtBDD);

    } else if (expr instanceof BooleanExprs.StaticBooleanExpr) {
      BooleanExprs.StaticBooleanExpr b = (BooleanExprs.StaticBooleanExpr) expr;
      switch (b.getType()) {
        case CallExprContext:
          p.debug("CallExprContext");
          BDD x1 = mkBDD(p.getCallContext() == TransferParam.CallContext.EXPR_CALL);
          return result.setReturnValueBDD(x1);
        case CallStatementContext:
          p.debug("CallStmtContext");
          BDD x2 = mkBDD(p.getCallContext() == TransferParam.CallContext.STMT_CALL);
          return result.setReturnValueBDD(x2);
        case True:
          p.debug("True");
          return result.setReturnValueBDD(factory.one());
        case False:
          p.debug("False");
          return result.setReturnValueBDD(factory.zero());
        default:
          throw new BatfishException(
              "Unhandled " + BooleanExprs.class.getCanonicalName() + ": " + b.getType());
      }

    } else if (expr instanceof LegacyMatchAsPath) {
      p.debug("MatchAsPath");
      LegacyMatchAsPath legacyMatchAsPathNode = (LegacyMatchAsPath) expr;
      BDD asPathPredicate =
          matchAsPathSetExpr(
              p.indent(), _conf, legacyMatchAsPathNode.getExpr(), routeForMatching(p.getData()));
      return result.setReturnValueBDD(asPathPredicate);

    } else if (expr instanceof MatchAsPath) {
      MatchAsPath matchAsPath = (MatchAsPath) expr;
      checkArgument(
          matchAsPath.getAsPathExpr().equals(InputAsPath.instance()),
          "Currently only supporting matching on the original AS path");
      BDD asPathPredicate =
          matchAsPath
              .getAsPathMatchExpr()
              .accept(new AsPathMatchExprToBDD(), new Arg(this, routeForMatching(p.getData())));
      return result.setReturnValueBDD(asPathPredicate);

    } else {
      throw new BatfishException("TODO: compute expr transfer function: " + expr);
    }
  }

  /*
   * Symbolic analysis of a single route-policy statement.
   */
  private TransferBDDState compute(Statement stmt, TransferBDDState state) {
    TransferParam<BDDRoute> curP = state.getTransferParam();
    TransferResult result = state.getTransferResult();

    if (stmt instanceof StaticStatement) {
      StaticStatement ss = (StaticStatement) stmt;

      switch (ss.getType()) {
        case ExitAccept:
          curP.debug("ExitAccept");
          result = exitValue(result, factory.one());
          break;

        case ReturnTrue:
          curP.debug("ReturnTrue");
          result = returnValue(result, factory.one());
          break;

        case ExitReject:
          curP.debug("ExitReject");
          result = exitValue(result, factory.zero());
          break;

        case ReturnFalse:
          curP.debug("ReturnFalse");
          result = returnValue(result, factory.zero());
          break;

        case SetDefaultActionAccept:
          curP.debug("SetDefaultActionAccept");
          curP = curP.setDefaultAccept(factory.one());
          break;

        case SetDefaultActionReject:
          curP.debug("SetDefaultActionReject");
          curP = curP.setDefaultAccept(factory.zero());
          break;

        case SetLocalDefaultActionAccept:
          curP.debug("SetLocalDefaultActionAccept");
          curP = curP.setDefaultAcceptLocal(factory.one());
          break;

        case SetLocalDefaultActionReject:
          curP.debug("SetLocalDefaultActionReject");
          curP = curP.setDefaultAcceptLocal(factory.zero());
          break;

        case ReturnLocalDefaultAction:
          curP.debug("ReturnLocalDefaultAction");
          result = returnValue(result, curP.getDefaultAcceptLocal());
          break;

        case DefaultAction:
          curP.debug("DefaultAction");
          result = exitValue(result, curP.getDefaultAccept());
          break;

        case FallThrough:
          curP.debug("Fallthrough");
          result = fallthrough(result, true);
          break;

        case Return:
          curP.debug("Return");
          result =
              result.setReturnAssignedValue(
                  ite(unreachable(result), result.getReturnAssignedValue(), factory.one()));
          break;

        case Suppress:
          curP.debug("Suppress");
          result = suppressedValue(result, true);
          break;

        case Unsuppress:
          curP.debug("Unsuppress");
          result = suppressedValue(result, false);
          break;

        default:
          throw new BatfishException(
              "Unhandled statement in route policy analysis: " + ss.getType());
      }

    } else if (stmt instanceof If) {
      curP.debug("If");
      If i = (If) stmt;
      TransferResult guardResult =
          compute(i.getGuard(), new TransferBDDState(curP.indent(), result));
      BDD guard = guardResult.getReturnValue().getSecond();
      BDDRoute current = guardResult.getReturnValue().getFirst();
      curP.debug("guard: ");

      TransferBDDState trueState = null;
      TransferBDDState falseState = null;

      // Symbolically execute each branch if it is feasible.
      // Some guards are statically resolved (e.g. CallExprContext and CallStatementContext), which
      // means that only one branch will be analyzed.  Skipping analysis of the other branch avoids
      // signaling an error unnecessarily if we reach a route-map construct that is not currently
      // modelled.
      if (!guard.isZero()) {
        curP.debug("True Branch");
        // copy the current BDDRoute so we can separately track any updates on the two branches
        TransferParam<BDDRoute> pTrue = curP.indent().setData(current.deepCopy());
        trueState =
            compute(
                i.getTrueStatements(),
                new TransferBDDState(
                    pTrue,
                    result.setReturnValue(new TransferReturn(pTrue.getData(), factory.zero()))));
      }
      if (!guard.isOne()) {
        curP.debug("False Branch");
        TransferParam<BDDRoute> pFalse = curP.indent().setData(current.deepCopy());
        falseState =
            compute(
                i.getFalseStatements(),
                new TransferBDDState(
                    pFalse,
                    result.setReturnValue(new TransferReturn(pFalse.getData(), factory.zero()))));
      }

      // compute the new state of the analysis
      TransferResult newResult;
      TransferParam<BDDRoute> newCurP;
      /**
       * TODO: any updates to the default policy in the branches are lost. In general it seems we
       * need to track a map from BDDs to policies, indicating the conditions under which each
       * policy is the default.
       */
      if (guard.isOne()) {
        // the guard is logically true so we ignore the "else" branch
        newResult = trueState.getTransferResult();
        // record any updates to the default actions that occur
        // in the "then" branch
        newCurP = curP.setDefaultActionsFrom(trueState.getTransferParam());
      } else if (guard.isZero()) {
        // same here, but for the case when the guard is logically false
        newResult = falseState.getTransferResult();
        newCurP = curP.setDefaultActionsFrom(falseState.getTransferParam());
      } else {
        newResult = ite(guard, trueState.getTransferResult(), falseState.getTransferResult());
        newCurP = ite(curP, guard, trueState.getTransferParam(), falseState.getTransferParam());
      }

      // finally, take into account the possibility that the branches are never reached, because the
      // policy already returned / exited
      // TODO: Currently we are assuming that the guard of this conditional does not return/exit
      // from this policy.  Handling that situation requires more thought.
      BDD alreadyReturned = unreachable(result);

      result = ite(alreadyReturned, result, newResult);
      curP = ite(curP, alreadyReturned, curP, newCurP);

      curP.debug("If return: " + result.getReturnValue().getFirst().hashCode());

    } else if (stmt instanceof SetDefaultPolicy) {
      curP.debug("SetDefaultPolicy");
      curP = curP.setDefaultPolicy((SetDefaultPolicy) stmt);

    } else if (stmt instanceof SetMetric) {
      curP.debug("SetMetric");
      SetMetric sm = (SetMetric) stmt;
      LongExpr ie = sm.getMetric();
      BDDInteger curMed = curP.getData().getMed();
      BDDInteger med =
          ite(unreachable(result), curMed, applyLongExprModification(curP.indent(), curMed, ie));
      curP.getData().setMed(med);

    } else if (stmt instanceof SetOspfMetricType) {
      curP.debug("SetOspfMetricType");
      SetOspfMetricType somt = (SetOspfMetricType) stmt;
      OspfMetricType mt = somt.getMetricType();
      BDDDomain<OspfType> current = result.getReturnValue().getFirst().getOspfMetric();
      BDDDomain<OspfType> newValue = new BDDDomain<>(current);
      if (mt == OspfMetricType.E1) {
        curP.indent().debug("Value: E1");
        newValue.setValue(OspfType.E1);
      } else {
        curP.indent().debug("Value: E2");
        newValue.setValue(OspfType.E1);
      }
      newValue = ite(unreachable(result), curP.getData().getOspfMetric(), newValue);
      curP.getData().setOspfMetric(newValue);

    } else if (stmt instanceof SetLocalPreference) {
      curP.debug("SetLocalPreference");
      SetLocalPreference slp = (SetLocalPreference) stmt;
      LongExpr ie = slp.getLocalPreference();
      BDDInteger newValue =
          applyLongExprModification(curP.indent(), curP.getData().getLocalPref(), ie);
      newValue = ite(unreachable(result), curP.getData().getLocalPref(), newValue);
      curP.getData().setLocalPref(newValue);

    } else if (stmt instanceof SetTag) {
      curP.debug("SetTag");
      SetTag st = (SetTag) stmt;
      LongExpr ie = st.getTag();
      BDDInteger currTag = curP.getData().getTag();
      BDDInteger newValue = applyLongExprModification(curP.indent(), currTag, ie);
      newValue = ite(unreachable(result), currTag, newValue);
      curP.getData().setTag(newValue);

    } else if (stmt instanceof SetCommunities) {
      curP.debug("SetCommunities");
      SetCommunities sc = (SetCommunities) stmt;
      org.batfish.datamodel.routing_policy.communities.CommunitySetExpr setExpr =
          sc.getCommunitySetExpr();
      // SetCommunitiesVisitor requires a BDDRoute that maps each community atomic predicate BDD
      // to its corresponding BDD variable, so we use the original route here
      CommunityAPDispositions dispositions =
          setExpr.accept(new SetCommunitiesVisitor(), new Arg(this, _originalRoute));
      updateCommunities(dispositions, curP, result);

    } else if (stmt instanceof CallStatement) {
      /*
       this code is based on the concrete semantics defined by CallStatement::execute, which also
       relies on RoutingPolicy::call
      */
      curP.debug("CallStatement");
      CallStatement cs = (CallStatement) stmt;
      String name = cs.getCalledPolicyName();
      RoutingPolicy pol = _conf.getRoutingPolicies().get(name);
      if (pol == null) {
        throw new BatfishException("Called route policy does not exist: " + name);
      }

      // save callee state
      BDD oldReturnAssigned = result.getReturnAssignedValue();

      TransferParam<BDDRoute> newParam =
          curP.indent().setCallContext(TransferParam.CallContext.STMT_CALL).enterScope(name);
      // TODO: Currently dropping the returned TransferParam on the floor
      TransferResult callResult =
          compute(
                  pol.getStatements(),
                  new TransferBDDState(newParam, result.setReturnAssignedValue(factory.zero())))
              .getTransferResult();

      // restore the original returnAssigned value
      result = callResult.setReturnAssignedValue(oldReturnAssigned);

    } else if (stmt instanceof BufferedStatement) {
      curP.debug("BufferedStatement");
      BufferedStatement bufStmt = (BufferedStatement) stmt;
      /**
       * The {@link Environment} class for simulating route policies keeps track of whether a
       * statement is buffered, but it currently does not seem to ever use that information. So we
       * ignore it.
       */
      return compute(bufStmt.getStatement(), state);

    } else if (stmt instanceof SetOrigin) {
      curP.debug("SetOrigin");
      // System.out.println("Warning: use of unimplemented feature SetOrigin");
      // TODO: implement me

    } else if (stmt instanceof SetNextHop) {
      curP.debug("SetNextHop");
      setNextHop(((SetNextHop) stmt).getExpr(), curP.getData());

    } else if (stmt instanceof TraceableStatement) {
      return compute(((TraceableStatement) stmt).getInnerStatements(), state);
    } else {
      throw new BatfishException("TODO: statement transfer function: " + stmt);
    }
    return toTransferBDDState(curP, result);
  }

  private TransferBDDState compute(List<Statement> statements, TransferBDDState state) {
    TransferBDDState currState = state;
    for (Statement stmt : statements) {
      currState = compute(stmt, currState);
    }
    return currState;
  }

  /** Symbolic analysis of a list of route-policy statements */
  @VisibleForTesting
  TransferResult compute(List<Statement> statements, TransferParam<BDDRoute> p) {
    TransferParam<BDDRoute> curP = p;

    TransferResult result = new TransferResult(curP.getData());

    TransferBDDState state = compute(statements, new TransferBDDState(curP, result));
    curP = state.getTransferParam();
    result = state.getTransferResult();

    // If this is the outermost call, then we relate the variables
    if (curP.getInitialCall()) {
      curP.debug("InitialCall finalizing");
      // incorporate the default action
      result = exitValue(result, curP.getDefaultAccept());

      TransferReturn ret = result.getReturnValue();
      // Only accept routes that are not suppressed
      BDD finalAccepts = ret.getSecond().diff(result.getSuppressedValue());
      // Set all the values to 0 if the route is not accepted;
      BDDRoute retVal = iteZero(finalAccepts, ret.getFirst());
      result = result.setReturnValue(new TransferReturn(retVal, finalAccepts));
    }
    return result;
  }

  private TransferResult fallthrough(TransferResult r, boolean val) {
    BDD notReached = unreachable(r);
    BDD fall = ite(notReached, r.getFallthroughValue(), mkBDD(val));
    BDD retAsgn = ite(notReached, r.getReturnAssignedValue(), factory.one());
    return r.setFallthroughValue(fall).setReturnAssignedValue(retAsgn);
  }

  // Create a TransferBDDState, using the BDDRoute in the given TransferResult and throwing away the
  // one that is in the given TransferParam.
  private TransferBDDState toTransferBDDState(TransferParam<BDDRoute> curP, TransferResult result) {
    return new TransferBDDState(curP.setData(result.getReturnValue().getFirst()), result);
  }

  // Produce a BDD representing conditions under which the route's destination prefix is within a
  // given prefix range.
  public static BDD isRelevantForDestination(BDDRoute record, PrefixRange range) {
    Prefix p = range.getPrefix();
    int pLen = p.getPrefixLength();

    SubRange r = range.getLengthRange();
    int lower = r.getStart();
    int upper = r.getEnd();

    BDD prefixMatch = firstBitsEqual(record.getPrefix().getBitvec(), p, pLen);
    BDDInteger prefixLength = record.getPrefixLength();
    BDD lenMatch = prefixLength.range(lower, upper);
    return prefixMatch.and(lenMatch);
  }

  // Produce a BDD representing conditions under which the route's next-hop address is within a
  // given prefix range.
  private static BDD isRelevantForNextHop(BDDRoute record, PrefixRange range) {
    Prefix p = range.getPrefix();
    int pLen = p.getPrefixLength();
    return firstBitsEqual(record.getNextHop().getBitvec(), p, pLen);
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
   * Map ite over BDDDomain type
   */
  private <T> BDDDomain<T> ite(BDD b, BDDDomain<T> x, BDDDomain<T> y) {
    BDDDomain<T> result = new BDDDomain<>(x);
    BDDInteger i = ite(b, x.getInteger(), y.getInteger());
    result.setInteger(i);
    return result;
  }

  @VisibleForTesting
  BDDRoute iteZero(BDD guard, BDDRoute r) {
    return ite(guard, r, zeroedRecord());
  }

  @VisibleForTesting
  BDDRoute ite(BDD guard, BDDRoute r1, BDDRoute r2) {
    BDDRoute ret =
        new BDDRoute(
            _graph.getCommunityAtomicPredicates().getNumAtomicPredicates(),
            _graph.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());

    BDDInteger x;
    BDDInteger y;

    // update integer values based on condition
    // x = r1.getPrefixLength();
    // y = r2.getPrefixLength();
    // ret.getPrefixLength().setValue(ite(guard, x, y));

    // x = r1.getIp();
    // y = r2.getIp();
    // ret.getIp().setValue(ite(guard, x, y));

    x = r1.getAdminDist();
    y = r2.getAdminDist();
    ret.getAdminDist().setValue(ite(guard, x, y));

    x = r1.getLocalPref();
    y = r2.getLocalPref();
    ret.getLocalPref().setValue(ite(guard, x, y));

    x = r1.getMed();
    y = r2.getMed();
    ret.getMed().setValue(ite(guard, x, y));

    x = r1.getNextHop();
    y = r2.getNextHop();
    ret.getNextHop().setValue(ite(guard, x, y));

    ret.setNextHopDiscarded(ite(guard, r1.getNextHopDiscarded(), r2.getNextHopDiscarded()));
    ret.setNextHopSet(ite(guard, r1.getNextHopSet(), r2.getNextHopSet()));

    x = r1.getTag();
    y = r2.getTag();
    ret.getTag().setValue(ite(guard, x, y));

    BDD[] retCommAPs = ret.getCommunityAtomicPredicates();
    BDD[] r1CommAPs = r1.getCommunityAtomicPredicates();
    BDD[] r2CommAPs = r2.getCommunityAtomicPredicates();
    for (int i = 0; i < _graph.getCommunityAtomicPredicates().getNumAtomicPredicates(); i++) {
      retCommAPs[i] = ite(guard, r1CommAPs[i], r2CommAPs[i]);
    }
    BDD[] retAsPathRegexAPs = ret.getAsPathRegexAtomicPredicates();
    BDD[] r1AsPathRegexAPs = r1.getAsPathRegexAtomicPredicates();
    BDD[] r2AsPathRegexAPs = r2.getAsPathRegexAtomicPredicates();
    for (int i = 0; i < _graph.getAsPathRegexAtomicPredicates().getNumAtomicPredicates(); i++) {
      retAsPathRegexAPs[i] = ite(guard, r1AsPathRegexAPs[i], r2AsPathRegexAPs[i]);
    }

    // BDDInteger i =
    //    ite(guard, r1.getProtocolHistory().getInteger(), r2.getProtocolHistory().getInteger());
    // ret.getProtocolHistory().setInteger(i);

    return ret;
  }

  TransferResult ite(BDD guard, TransferResult r1, TransferResult r2) {
    BDDRoute route = ite(guard, r1.getReturnValue().getFirst(), r2.getReturnValue().getFirst());
    BDD accepted = ite(guard, r1.getReturnValue().getSecond(), r2.getReturnValue().getSecond());

    BDD suppressed = ite(guard, r1.getSuppressedValue(), r2.getSuppressedValue());
    BDD exitAsgn = ite(guard, r1.getExitAssignedValue(), r2.getExitAssignedValue());
    BDD retAsgn = ite(guard, r1.getReturnAssignedValue(), r2.getReturnAssignedValue());
    BDD fallThrough = ite(guard, r1.getFallthroughValue(), r2.getFallthroughValue());

    return new TransferResult(
        new TransferReturn(route, accepted), suppressed, exitAsgn, fallThrough, retAsgn);
  }

  private <T extends IDeepCopy<T>> TransferParam<T> ite(
      TransferParam<T> orig, BDD guard, TransferParam<T> p1, TransferParam<T> p2) {
    return orig.setDefaultAccept(ite(guard, p1.getDefaultAccept(), p2.getDefaultAccept()))
        .setDefaultAcceptLocal(ite(guard, p1.getDefaultAcceptLocal(), p2.getDefaultAcceptLocal()));
  }

  // Produce a BDD that is the symbolic representation of the given AsPathSetExpr predicate.
  private BDD matchAsPathSetExpr(
      TransferParam<BDDRoute> p, Configuration conf, AsPathSetExpr e, BDDRoute other) {
    if (e instanceof NamedAsPathSet) {
      NamedAsPathSet namedAsPathSet = (NamedAsPathSet) e;
      AsPathAccessList accessList = conf.getAsPathAccessLists().get(namedAsPathSet.getName());
      p.debug("Named As Path Set: " + namedAsPathSet.getName());
      return matchAsPathAccessList(accessList, other);
    } else if (e instanceof ExplicitAsPathSet) {
      ExplicitAsPathSet explicitAsPathSet = (ExplicitAsPathSet) e;
      Set<SymbolicAsPathRegex> asPathRegexes =
          explicitAsPathSet.getElems().stream()
              .map(AsPathSetElem::regex)
              .map(SymbolicAsPathRegex::new)
              .collect(ImmutableSet.toImmutableSet());
      return asPathRegexesToBDD(asPathRegexes, other);
    }
    // TODO: handle other kinds of AsPathSetExprs
    throw new BatfishException("Unhandled match as-path expression " + e);
  }

  /* Convert an AS-path access list to a boolean formula represented as a BDD. */
  private BDD matchAsPathAccessList(AsPathAccessList accessList, BDDRoute other) {
    List<AsPathAccessListLine> lines = new ArrayList<>(accessList.getLines());
    Collections.reverse(lines);
    BDD acc = factory.zero();
    for (AsPathAccessListLine line : lines) {
      boolean action = (line.getAction() == LineAction.PERMIT);
      // each line's regex is represented as the disjunction of all of the regex's
      // corresponding atomic predicates
      SymbolicAsPathRegex regex = new SymbolicAsPathRegex(line.getRegex());
      BDD regexAPBdd = asPathRegexesToBDD(ImmutableSet.of(regex), other);
      acc = ite(regexAPBdd, mkBDD(action), acc);
    }
    return acc;
  }

  /*
   * Converts a route filter list to a boolean expression.
   */
  private BDD matchFilterList(
      TransferParam<BDDRoute> p,
      RouteFilterList x,
      BDDRoute other,
      BiFunction<BDDRoute, PrefixRange, BDD> symbolicMatcher) {
    BDD acc = factory.zero();
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
        BDD matches = symbolicMatcher.apply(other, range);
        BDD action = mkBDD(line.getAction() == LineAction.PERMIT);
        acc = ite(matches, action, acc);
      }
    }
    return acc;
  }

  // Returns a function that can convert a prefix range into a BDD that constrains the appropriate
  // part of a route (destination prefix or next-hop IP), depending on the given prefix
  // expression.
  private BiFunction<BDDRoute, PrefixRange, BDD> prefixExprToSymbolicMatcher(PrefixExpr pe) {
    if (pe.equals(DestinationNetwork.instance())) {
      return TransferBDD::isRelevantForDestination;
    } else if (pe instanceof IpPrefix) {
      IpPrefix ipp = (IpPrefix) pe;
      if (ipp.getIp().equals(NextHopIp.instance())
          && ipp.getPrefixLength().equals(new LiteralInt(Prefix.MAX_PREFIX_LENGTH))) {
        return TransferBDD::isRelevantForNextHop;
      }
    }
    throw new UnsupportedOperationException("Unsupported prefix expression: " + pe);
  }

  /*
   * Converts a prefix set to a boolean expression.
   */
  private BDD matchPrefixSet(
      TransferParam<BDDRoute> p, Configuration conf, MatchPrefixSet m, BDDRoute other) {
    BiFunction<BDDRoute, PrefixRange, BDD> symbolicMatcher =
        prefixExprToSymbolicMatcher(m.getPrefix());
    PrefixSetExpr e = m.getPrefixSet();
    if (e instanceof ExplicitPrefixSet) {
      ExplicitPrefixSet x = (ExplicitPrefixSet) e;

      Set<PrefixRange> ranges = x.getPrefixSpace().getPrefixRanges();
      BDD acc = factory.zero();
      for (PrefixRange range : ranges) {
        p.debug("Prefix Range: " + range);
        if (!PrefixUtils.isContainedBy(range.getPrefix(), _ignoredNetworks)) {
          acc = acc.or(symbolicMatcher.apply(other, range));
        }
      }
      return acc;

    } else if (e instanceof NamedPrefixSet) {
      NamedPrefixSet x = (NamedPrefixSet) e;
      p.debug("Named: " + x.getName());
      String name = x.getName();
      RouteFilterList fl = conf.getRouteFilterLists().get(name);
      return matchFilterList(p, fl, other, symbolicMatcher);

    } else {
      throw new BatfishException("TODO: match prefix set: " + e);
    }
  }

  // Produce a BDD representing a constraint on the given BDDInteger that enforces the
  // integer (in)equality constraint represented by the given IntComparator and LongExpr
  private BDD matchIntComparison(IntComparator comp, LongExpr expr, BDDInteger bddInt) {
    checkArgument(
        expr instanceof LiteralLong,
        "Currently only supporting matching against integer literals: " + expr);
    long val = ((LiteralLong) expr).getValue();
    switch (comp) {
      case EQ:
        return bddInt.value(val);
      case GE:
        return bddInt.geq(val);
      case GT:
        return bddInt.geq(val).and(bddInt.value(val).not());
      case LE:
        return bddInt.leq(val);
      case LT:
        return bddInt.leq(val).and(bddInt.value(val).not());
      default:
        throw new UnsupportedOperationException(
            "Unknown integer comparator: " + comp.getClass().getSimpleName());
    }
  }

  /*
   * Return a BDD from a boolean
   */
  BDD mkBDD(boolean b) {
    return b ? factory.one() : factory.zero();
  }

  private void setNextHop(NextHopExpr expr, BDDRoute route) {
    // record the fact that the next-hop has been explicitly set by the route-map
    route.setNextHopSet(factory.one());
    if (expr instanceof DiscardNextHop) {
      route.setNextHopDiscarded(factory.one());
    } else if (expr instanceof IpNextHop) {
      List<Ip> ips = ((IpNextHop) expr).getIps();
      checkArgument(ips.size() == 1, "Currently not allowing multiple next-hop IPs to be set");
      Ip ip = ips.get(0);
      route.setNextHop(BDDInteger.makeFromValue(factory, 32, ip.asLong()));
    } else {
      throw new UnsupportedOperationException("Unsupported next-hop expression: " + expr);
    }
  }

  // Set the corresponding BDDs of the given community atomic predicates to either 1 or 0,
  // depending on the value of the boolean parameter.
  private void addOrRemoveCommunityAPs(
      IntegerSpace commAPs, TransferParam<BDDRoute> curP, TransferResult result, boolean add) {
    BDD newCommVal = mkBDD(add);
    BDD[] commAPBDDs = curP.getData().getCommunityAtomicPredicates();
    for (int ap : commAPs.enumerate()) {
      curP.indent().debug("Value: " + ap);
      BDD comm = commAPBDDs[ap];
      BDD newValue = ite(unreachable(result), comm, newCommVal);
      curP.indent().debug("New Value: " + newValue);
      commAPBDDs[ap] = newValue;
    }
  }

  // Update community atomic predicates based on the given CommunityAPDispositions object
  private void updateCommunities(
      CommunityAPDispositions dispositions, TransferParam<BDDRoute> curP, TransferResult result) {
    addOrRemoveCommunityAPs(dispositions.getMustExist(), curP, result, true);
    addOrRemoveCommunityAPs(dispositions.getMustNotExist(), curP, result, false);
  }

  /**
   * A BDD representing the conditions under which the current statement is not reachable, because
   * we've already returned or exited before getting there.
   *
   * @param currState the current state of the analysis
   * @return the bdd
   */
  private static BDD unreachable(TransferResult currState) {
    return currState.getReturnAssignedValue().or(currState.getExitAssignedValue());
  }

  /*
   * Create the result of reaching a suppress or unsuppress statement.
   */
  private TransferResult suppressedValue(TransferResult r, boolean val) {
    BDD notReached = unreachable(r);
    BDD b = ite(notReached, r.getSuppressedValue(), mkBDD(val));
    return r.setSuppressedValue(b);
  }

  /*
   * Create the result of reaching a return statement, returning with the given value.
   */
  private TransferResult returnValue(TransferResult r, BDD val) {
    BDD notReached = unreachable(r);
    BDD b = ite(notReached, r.getReturnValue().getSecond(), val);
    TransferReturn ret = new TransferReturn(r.getReturnValue().getFirst(), b);
    BDD retAsgn = ite(notReached, r.getReturnAssignedValue(), factory.one());
    return r.setReturnValue(ret).setReturnAssignedValue(retAsgn);
  }

  /*
   * Create the result of reaching an exit statement, returning with the given value.
   */
  private TransferResult exitValue(TransferResult r, BDD val) {
    BDD notReached = unreachable(r);
    BDD b = ite(notReached, r.getReturnValue().getSecond(), val);
    TransferReturn ret = new TransferReturn(r.getReturnValue().getFirst(), b);
    BDD exitAsgn = ite(notReached, r.getExitAssignedValue(), factory.one());
    return r.setReturnValue(ret).setExitAssignedValue(exitAsgn);
  }

  // Returns the appropriate route to use for matching on attributes.
  private BDDRoute routeForMatching(BDDRoute current) {
    return _useOutputAttributes ? current : _originalRoute;
  }

  /*
   * A record of default values that represent the value of the
   * outputs if the route is filtered / dropped in the policy
   */
  @VisibleForTesting
  BDDRoute zeroedRecord() {
    BDDRoute rec =
        new BDDRoute(
            _graph.getCommunityAtomicPredicates().getNumAtomicPredicates(),
            _graph.getAsPathRegexAtomicPredicates().getNumAtomicPredicates());
    rec.getLocalPref().setValue(0);
    rec.getAdminDist().setValue(0);
    rec.getPrefixLength().setValue(0);
    rec.getMed().setValue(0);
    rec.getNextHop().setValue(0);
    rec.setNextHopDiscarded(factory.zero());
    rec.setNextHopSet(factory.zero());
    rec.getTag().setValue(0);
    rec.getPrefix().setValue(0);
    for (int i = 0; i < rec.getCommunityAtomicPredicates().length; i++) {
      rec.getCommunityAtomicPredicates()[i] = factory.zero();
    }
    for (int i = 0; i < rec.getAsPathRegexAtomicPredicates().length; i++) {
      rec.getAsPathRegexAtomicPredicates()[i] = factory.zero();
    }
    rec.getProtocolHistory().getInteger().setValue(0);
    return rec;
  }

  /*
   * Create a BDDRecord representing the symbolic output of
   * the RoutingPolicy given the input variables.
   */
  public TransferResult compute(@Nullable Set<Prefix> ignoredNetworks) {
    _ignoredNetworks = ignoredNetworks;
    BDDRoute o = new BDDRoute(_graph);
    TransferParam<BDDRoute> p = new TransferParam<>(o, false);
    TransferResult result = compute(_statements, p);
    // BDDRoute route = result.getReturnValue().getFirst();
    // System.out.println("DOT: \n" + route.dot(route.getLocalPref().getBitvec()[31]));
    //    return result.getReturnValue().getFirst();
    return result;
  }

  public Map<CommunityVar, Set<Integer>> getCommunityAtomicPredicates() {
    return _communityAtomicPredicates;
  }

  public Configuration getConfiguration() {
    return _conf;
  }

  public Graph getGraph() {
    return _graph;
  }

  public boolean getUseOutputAttributes() {
    return _useOutputAttributes;
  }
}
