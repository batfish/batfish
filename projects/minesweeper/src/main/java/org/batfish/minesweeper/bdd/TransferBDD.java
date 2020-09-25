package org.batfish.minesweeper.bdd;

import static org.batfish.minesweeper.CommunityVarCollector.collectCommunityVars;
import static org.batfish.minesweeper.bdd.CommunityVarConverter.toCommunityVar;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
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
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.OspfType;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.SymbolicRegex;
import org.batfish.minesweeper.TransferParam;
import org.batfish.minesweeper.TransferParam.CallContext;
import org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD.Arg;
import org.batfish.minesweeper.collections.Table2;
import org.batfish.minesweeper.utils.PrefixUtils;

/** @author Ryan Beckett */
public class TransferBDD {

  private static BDDFactory factory = BDDRoute.factory;

  private static Table2<String, String, TransferResult> CACHE = new Table2<>();

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

  public TransferBDD(Graph g, Configuration conf, List<Statement> statements) {
    _graph = g;
    _conf = conf;
    _statements = statements;

    _communityAtomicPredicates = _graph.getCommunityAtomicPredicates().getRegexAtomicPredicates();
    _asPathRegexAtomicPredicates =
        _graph.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
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
    if (e instanceof LiteralLong) {
      LiteralLong z = (LiteralLong) e;
      p.debug("LiteralLong: " + z.getValue());
      return BDDInteger.makeFromValue(x.getFactory(), 32, z.getValue());
    }
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
    throw new BatfishException("int expr transfer function: " + e);
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
   * TODO: Currently we assume that the boolean expression has no side effects.  For example,
   *  any route announcement updates in the expression will not be accounted for.  Instead,
   *  the returned BDDRoute will be identical to the one passed in as part of the TransferParam
   *  object.  More generally we should also handle other side effects that could occur, for example
   *  early returns/exits and changes to the default action but are not currently doing so.
   *  Hence the only part of the returned TransferResult o that should be used currently is
   *  o.getReturnValue().getSecond(), which represents this Boolean expression as a BDD.
   */
  private TransferResult compute(BooleanExpr expr, TransferParam<BDDRoute> p) {

    // TODO: right now everything is IPV4
    if (expr instanceof MatchIpv4) {
      p.debug("MatchIpv4");
      TransferReturn ret = new TransferReturn(p.getData(), factory.one());
      p.debug("MatchIpv4 Result: " + ret);
      return fromExpr(ret);
    }
    if (expr instanceof MatchIpv6) {
      p.debug("MatchIpv6");
      TransferReturn ret = new TransferReturn(p.getData(), factory.zero());
      return fromExpr(ret);
    }

    if (expr instanceof Conjunction) {
      p.debug("Conjunction");
      Conjunction c = (Conjunction) expr;
      BDD acc = factory.one();
      for (BooleanExpr be : c.getConjuncts()) {
        TransferResult r = compute(be, p.indent());
        acc = acc.and(r.getReturnValue().getSecond());
      }
      TransferReturn ret = new TransferReturn(p.getData(), acc);
      p.debug("Conjunction return: " + acc);
      return new TransferResult(ret, factory.zero());
    }

    if (expr instanceof Disjunction) {
      p.debug("Disjunction");
      Disjunction d = (Disjunction) expr;
      BDD acc = factory.zero();
      for (BooleanExpr be : d.getDisjuncts()) {
        TransferResult r = compute(be, p.indent());
        acc = acc.or(r.getReturnValue().getSecond());
      }
      TransferReturn ret = new TransferReturn(p.getData(), acc);
      p.debug("Disjunction return: " + acc);
      return new TransferResult(ret, factory.zero());
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
      if (conjuncts.isEmpty()) {
        TransferReturn ret = new TransferReturn(p.getData(), factory.one());
        return fromExpr(ret);
      } else {
        TransferResult result = new TransferResult(p.getData());
        TransferParam<BDDRoute> record = p;
        BDD acc = factory.zero();
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
          BooleanExpr conjunct = conjuncts.get(i);
          TransferParam<BDDRoute> param =
              record
                  .setDefaultPolicy(null)
                  .setChainContext(TransferParam.ChainContext.CONJUNCTION)
                  .indent();
          TransferResult r = compute(conjunct, param);
          record = record.setData(r.getReturnValue().getFirst());
          acc = ite(r.getFallthroughValue(), acc, r.getReturnValue().getSecond());
        }
        TransferReturn ret = new TransferReturn(record.getData(), acc);
        return result.setReturnValue(ret);
      }
    }

    if (expr instanceof FirstMatchChain) {
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
      TransferResult result = new TransferResult(p.getData());
      TransferParam<BDDRoute> record = p;
      BDD acc = factory.zero();
      for (int i = chainPolicies.size() - 1; i >= 0; i--) {
        BooleanExpr policyMatcher = chainPolicies.get(i);
        TransferParam<BDDRoute> param =
            record
                .setDefaultPolicy(null)
                .setChainContext(TransferParam.ChainContext.CONJUNCTION)
                .indent();
        TransferResult r = compute(policyMatcher, param);
        record = record.setData(r.getReturnValue().getFirst());
        acc = ite(r.getFallthroughValue(), acc, r.getReturnValue().getSecond());
      }
      TransferReturn ret = new TransferReturn(record.getData(), acc);
      return result.setReturnValue(ret);
    }

    if (expr instanceof Not) {
      p.debug("mkNot");
      Not n = (Not) expr;
      TransferResult result = compute(n.getExpr(), p);
      TransferReturn r = result.getReturnValue();
      TransferReturn ret = new TransferReturn(p.getData(), r.getSecond().not());
      return new TransferResult(ret, factory.zero());
    }

    if (expr instanceof MatchProtocol) {
      MatchProtocol mp = (MatchProtocol) expr;
      Set<RoutingProtocol> rps = mp.getProtocols();
      if (rps.size() > 1) {
        // Hack: Minesweeper doesn't support MatchProtocol with multiple arguments.
        List<BooleanExpr> mps = rps.stream().map(MatchProtocol::new).collect(Collectors.toList());
        return compute(new Disjunction(mps), p);
      }
      RoutingProtocol rp = Iterables.getOnlyElement(rps);
      Protocol proto = Protocol.fromRoutingProtocol(rp);
      if (proto == null) {
        p.debug("MatchProtocol(" + rp.protocolName() + "): false");
        TransferReturn ret = new TransferReturn(p.getData(), factory.zero());
        return fromExpr(ret);
      }
      BDD protoMatch = p.getData().getProtocolHistory().value(proto);
      p.debug("MatchProtocol(" + rp.protocolName() + "): " + protoMatch);
      TransferReturn ret = new TransferReturn(p.getData(), protoMatch);
      return fromExpr(ret);
    }

    if (expr instanceof MatchPrefixSet) {
      p.debug("MatchPrefixSet");
      MatchPrefixSet m = (MatchPrefixSet) expr;

      BDD r = matchPrefixSet(p.indent(), _conf, m.getPrefixSet(), p.getData());
      TransferReturn ret = new TransferReturn(p.getData(), r);
      return fromExpr(ret);

      // TODO: implement me
    } else if (expr instanceof MatchPrefix6Set) {
      p.debug("MatchPrefix6Set");
      TransferReturn ret = new TransferReturn(p.getData(), factory.zero());
      return fromExpr(ret);

    } else if (expr instanceof CallExpr) {
      p.debug("CallExpr");
      CallExpr c = (CallExpr) expr;
      String router = _conf.getHostname();
      String name = c.getCalledPolicyName();
      TransferResult r = CACHE.get(router, name);
      if (r != null) {
        return r;
      }
      RoutingPolicy pol = _conf.getRoutingPolicies().get(name);
      r =
          compute(
              pol.getStatements(),
              p.setCallContext(TransferParam.CallContext.EXPR_CALL).indent().enterScope(name));
      CACHE.put(router, name, r);
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
      TransferReturn ret = new TransferReturn(p.getData(), c);
      return fromExpr(ret);

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
              .accept(new CommunitySetMatchExprToBDD(), new Arg(this, p.getData()));
      TransferReturn ret = new TransferReturn(p.getData(), mcPredicate);
      return fromExpr(ret);

    } else if (expr instanceof BooleanExprs.StaticBooleanExpr) {
      BooleanExprs.StaticBooleanExpr b = (BooleanExprs.StaticBooleanExpr) expr;
      TransferReturn ret;
      switch (b.getType()) {
        case CallExprContext:
          p.debug("CallExprContext");
          BDD x1 = mkBDD(p.getCallContext() == TransferParam.CallContext.EXPR_CALL);
          ret = new TransferReturn(p.getData(), x1);
          return fromExpr(ret);
        case CallStatementContext:
          p.debug("CallStmtContext");
          BDD x2 = mkBDD(p.getCallContext() == TransferParam.CallContext.STMT_CALL);
          ret = new TransferReturn(p.getData(), x2);
          return fromExpr(ret);
        case True:
          p.debug("True");
          ret = new TransferReturn(p.getData(), factory.one());
          return fromExpr(ret);
        case False:
          p.debug("False");
          ret = new TransferReturn(p.getData(), factory.zero());
          return fromExpr(ret);
        default:
          throw new BatfishException(
              "Unhandled " + BooleanExprs.class.getCanonicalName() + ": " + b.getType());
      }

    } else if (expr instanceof MatchAsPath) {
      p.debug("MatchAsPath");
      MatchAsPath matchAsPathNode = (MatchAsPath) expr;
      BDD asPathPredicate = matchAsPath(p.indent(), _conf, matchAsPathNode.getExpr(), p.getData());
      TransferReturn ret = new TransferReturn(p.getData(), asPathPredicate);
      return fromExpr(ret);
    }

    throw new BatfishException("TODO: compute expr transfer function: " + expr);
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
          result = exitValue(result, true);
          break;

        case ReturnTrue:
          curP.debug("ReturnTrue");
          result = returnValue(result, true);
          break;

        case ExitReject:
          curP.debug("ExitReject");
          result = exitValue(result, false);
          break;

        case ReturnFalse:
          curP.debug("ReturnFalse");
          result = returnValue(result, false);
          break;

        case SetDefaultActionAccept:
          curP.debug("SetDefaultActionAccept");
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

        default:
          throw new BatfishException(
              "Unhandled statement in route policy analysis: " + ss.getType());
      }

    } else if (stmt instanceof If) {
      curP.debug("If");
      If i = (If) stmt;
      /** TODO: Currently we are assuming that the guard is side-effect free. */
      TransferResult r = compute(i.getGuard(), curP.indent());
      BDD guard = r.getReturnValue().getSecond();
      curP.debug("guard: ");

      BDDRoute current = result.getReturnValue().getFirst();

      // copy the current BDDRoute so we can separately track any updates on the two branches
      TransferParam<BDDRoute> pTrue = curP.indent().setData(current.deepCopy());
      TransferParam<BDDRoute> pFalse = curP.indent().setData(current.deepCopy());
      curP.debug("True Branch");
      /**
       * TODO: any updates to the TransferParam in the branches, for example updates to the default
       * action, are lost. In general it seems we need to replace the booleans there with BDDs, so
       * we can track the conditions under which each of them is true/false.
       */
      // symbolically execute both branches from the current state
      TransferResult trueBranch =
          compute(
                  i.getTrueStatements(),
                  new TransferBDDState(
                      pTrue,
                      result.setReturnValue(new TransferReturn(pTrue.getData(), factory.zero()))))
              .getTransferResult();
      curP.debug("True Branch: " + trueBranch.getReturnValue().getFirst().hashCode());
      curP.debug("False Branch");
      TransferResult falseBranch =
          compute(
                  i.getFalseStatements(),
                  new TransferBDDState(
                      pFalse,
                      result.setReturnValue(new TransferReturn(pFalse.getData(), factory.zero()))))
              .getTransferResult();
      curP.debug("False Branch: " + trueBranch.getReturnValue().getFirst().hashCode());

      // update return values

      BDD alreadyReturned = unreachable(result);

      result = ite(alreadyReturned, result, ite(guard, trueBranch, falseBranch));

      curP.debug("If return: " + result.getReturnValue().getFirst().hashCode());

    } else if (stmt instanceof SetDefaultPolicy) {
      curP.debug("SetDefaultPolicy");
      curP = curP.setDefaultPolicy((SetDefaultPolicy) stmt);

    } else if (stmt instanceof SetMetric) {
      curP.debug("SetMetric");
      SetMetric sm = (SetMetric) stmt;
      LongExpr ie = sm.getMetric();
      BDD isBGP = curP.getData().getProtocolHistory().value(Protocol.BGP);
      // update the MED if the protocol is BGP, and otherwise update the metric
      // TODO: is this the right thing to do?
      BDD ignoreMed = isBGP.not().or(unreachable(result));
      BDD ignoreMet = isBGP.or(unreachable(result));
      BDDInteger med =
          ite(
              ignoreMed,
              curP.getData().getMed(),
              applyLongExprModification(curP.indent(), curP.getData().getMed(), ie));
      BDDInteger met =
          ite(
              ignoreMet,
              curP.getData().getMetric(),
              applyLongExprModification(curP.indent(), curP.getData().getMetric(), ie));
      curP.getData().setMed(med);
      curP.getData().setMetric(met);

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

    } else if (stmt instanceof AddCommunity) {
      curP.debug("AddCommunity");
      AddCommunity ac = (AddCommunity) stmt;
      Set<CommunityVar> comms = collectCommunityVars(_conf, ac.getExpr());
      // set all atomic predicates associated with these communities to 1 if this statement
      // is reached
      Set<Integer> commAPs = atomicPredicatesFor(comms, _communityAtomicPredicates);
      BDD[] commAPBDDs = curP.getData().getCommunityAtomicPredicates();
      for (int ap : commAPs) {
        curP.indent().debug("Value: " + ap);
        BDD comm = commAPBDDs[ap];
        // on paths where the route policy has already hit a Return or Exit statement earlier,
        // this AddCommunity statement will not be reached so the atomic predicate's value should
        // be unchanged; otherwise it should be set to 1.
        BDD newValue = ite(unreachable(result), comm, factory.one());
        curP.indent().debug("New Value: " + newValue);
        commAPBDDs[ap] = newValue;
      }

    } else if (stmt instanceof SetCommunity) {
      curP.debug("SetCommunity");
      SetCommunity sc = (SetCommunity) stmt;
      CommunitySetExpr setExpr = sc.getExpr();
      /**
       * TODO: simply collecting all community variables in setExpr is not correct in general, since
       * for example some of them may be negated in the expression. for now we only support setting
       * literal communities. we should create a special visitor to gather the community atomic
       * predicates that are being set.
       */
      if (!(setExpr instanceof LiteralCommunity || setExpr instanceof LiteralCommunitySet)) {
        throw new BatfishException("Unhandled community expression in 'set community': " + setExpr);
      }
      Set<CommunityVar> comms = collectCommunityVars(_conf, setExpr);
      setCommunities(comms, curP, result);

    } else if (stmt instanceof SetCommunities) {
      curP.debug("SetCommunities");
      SetCommunities sc = (SetCommunities) stmt;
      org.batfish.datamodel.routing_policy.communities.CommunitySetExpr setExpr =
          sc.getCommunitySetExpr();
      /**
       * TODO: the SetCommunitiesVarCollector does not support some kinds of expressions, such as
       * set differences, for the same reason as described above regarding limitations of
       * SetCommunity. again the right solution is to create a visitor to gather community atomic
       * predicates. (note that SetCommunity and SetCommunities use two different data models for
       * expressions, both named CommunitySetExpr but in different packages.)
       */
      Set<CommunityVar> comms = setExpr.accept(new SetCommunitiesVarCollector(), _conf);
      setCommunities(comms, curP, result);

    } else if (stmt instanceof DeleteCommunity) {
      curP.debug("DeleteCommunity");
      DeleteCommunity ac = (DeleteCommunity) stmt;
      Set<CommunityVar> comms = collectCommunityVars(_conf, ac.getExpr());
      // set all atomic predicates associated with these communities to 0 on this path
      Set<Integer> commAPs = atomicPredicatesFor(comms, _communityAtomicPredicates);
      BDD[] commAPBDDs = curP.getData().getCommunityAtomicPredicates();
      for (int ap : commAPs) {
        curP.indent().debug("Value: " + ap);
        BDD comm = commAPBDDs[ap];
        BDD newValue = ite(unreachable(result), comm, factory.zero());
        curP.indent().debug("New Value: " + newValue);
        commAPBDDs[ap] = newValue;
      }

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

      TransferParam<BDDRoute> newParam = curP.indent().setCallContext(CallContext.STMT_CALL);
      // TODO: Currently dropping the returned TransferParam on the floor
      TransferResult callResult =
          compute(
                  pol.getStatements(),
                  new TransferBDDState(
                      newParam,
                      result
                          .setReturnValue(new TransferReturn(newParam.getData(), factory.zero()))
                          .setReturnAssignedValue(factory.zero())))
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
    } else if (stmt instanceof PrependAsPath) {
      curP.debug("PrependAsPath");
      PrependAsPath pap = (PrependAsPath) stmt;
      int prependCost = prependLength(pap.getExpr());
      curP.indent().debug("Cost: " + prependCost);
      BDDInteger met = curP.getData().getMetric();
      BDDInteger newValue = met.add(BDDInteger.makeFromValue(met.getFactory(), 32, prependCost));
      newValue = ite(unreachable(result), curP.getData().getMetric(), newValue);
      curP.getData().setMetric(newValue);

    } else if (stmt instanceof SetOrigin) {
      curP.debug("SetOrigin");
      // System.out.println("Warning: use of unimplemented feature SetOrigin");
      // TODO: implement me

    } else if (stmt instanceof SetNextHop) {
      curP.debug("SetNextHop");
      // System.out.println("Warning: use of unimplemented feature SetNextHop");
      // TODO: implement me

    } else {
      throw new BatfishException("TODO: statement transfer function: " + stmt);
    }
    // make sure that the TransferParam is updated with the current BDDRoute
    curP = curP.setData(result.getReturnValue().getFirst());
    return new TransferBDDState(curP, result);
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
      if (curP.getDefaultAccept()) {
        result = exitValue(result, true);
      } else {
        result = exitValue(result, false);
      }
      // Set all the values to 0 if the return is not true;
      TransferReturn ret = result.getReturnValue();
      BDDRoute retVal = iteZero(ret.getSecond(), ret.getFirst());
      result = result.setReturnValue(new TransferReturn(retVal, ret.getSecond()));
    }
    return result;
  }

  private TransferResult fallthrough(TransferResult r, boolean val) {
    BDD notReached = unreachable(r);
    BDD fall = ite(notReached, r.getFallthroughValue(), mkBDD(val));
    BDD retAsgn = ite(notReached, r.getReturnAssignedValue(), factory.one());
    return r.setFallthroughValue(fall).setReturnAssignedValue(retAsgn);
  }

  /*
   * Wrap a simple boolean expression return value in a transfer function result
   */
  private TransferResult fromExpr(TransferReturn ret) {
    return new TransferResult(ret, factory.zero());
  }

  /*
   * Check if a prefix range match is applicable for the packet destination
   * Ip address, given the prefix length variable.
   *
   * Since aggregation is modelled separately, we assume that prefixLen
   * is not modified, and thus will contain only the underlying variables:
   * [var(0), ..., var(n)]
   */
  public static BDD isRelevantFor(BDDRoute record, PrefixRange range) {
    Prefix p = range.getPrefix();
    BDD prefixMatch = firstBitsEqual(record.getPrefix().getBitvec(), p, p.getPrefixLength());

    BDDInteger prefixLength = record.getPrefixLength();
    SubRange r = range.getLengthRange();
    int lower = r.getStart();
    int upper = r.getEnd();
    BDD lenMatch = prefixLength.range(lower, upper);

    return lenMatch.and(prefixMatch);
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

    x = r1.getMetric();
    y = r2.getMetric();
    ret.getMetric().setValue(ite(guard, x, y));

    x = r1.getMed();
    y = r2.getMed();
    ret.getMed().setValue(ite(guard, x, y));

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

    BDD exitAsgn = ite(guard, r1.getExitAssignedValue(), r2.getExitAssignedValue());
    BDD retAsgn = ite(guard, r1.getReturnAssignedValue(), r2.getReturnAssignedValue());
    BDD fallThrough = ite(guard, r1.getFallthroughValue(), r2.getFallthroughValue());

    return new TransferResult(new TransferReturn(route, accepted), exitAsgn, fallThrough, retAsgn);
  }

  // Produce a BDD that is the symbolic representation of the given AsPathSetExpr predicate.
  private BDD matchAsPath(
      TransferParam<BDDRoute> p, Configuration conf, AsPathSetExpr e, BDDRoute other) {
    if (e instanceof NamedAsPathSet) {
      NamedAsPathSet namedAsPathSet = (NamedAsPathSet) e;
      AsPathAccessList accessList = conf.getAsPathAccessLists().get(namedAsPathSet.getName());
      p.debug("Named As Path Set: " + namedAsPathSet.getName());
      return matchAsPathAccessList(accessList, other);
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
      Set<Integer> aps = atomicPredicatesFor(ImmutableSet.of(regex), _asPathRegexAtomicPredicates);
      BDD regexAPBdd =
          factory.orAll(
              aps.stream()
                  .map(ap -> other.getAsPathRegexAtomicPredicates()[ap])
                  .collect(Collectors.toList()));
      acc = ite(regexAPBdd, mkBDD(action), acc);
    }
    return acc;
  }

  /*
   * Converts a community list to a boolean expression.
   */
  private BDD matchCommunityList(TransferParam<BDDRoute> p, CommunityList cl, BDDRoute other) {
    List<CommunityListLine> lines = new ArrayList<>(cl.getLines());
    Collections.reverse(lines);
    BDD acc = factory.zero();
    for (CommunityListLine line : lines) {
      boolean action = (line.getAction() == LineAction.PERMIT);
      CommunityVar cvar = toRegexCommunityVar(toCommunityVar(line.getMatchCondition()));
      p.debug("Match Line: " + cvar);
      p.debug("Action: " + line.getAction());
      // the community cvar is logically represented as the disjunction of its corresponding
      // atomic predicates
      Set<Integer> aps = atomicPredicatesFor(ImmutableSet.of(cvar), _communityAtomicPredicates);
      BDD c =
          factory.orAll(
              aps.stream()
                  .map(ap -> other.getCommunityAtomicPredicates()[ap])
                  .collect(Collectors.toList()));
      acc = ite(c, mkBDD(action), acc);
    }
    return acc;
  }

  /*
   * Converts a community set to a boolean expression
   */
  private BDD matchCommunitySet(
      TransferParam<BDDRoute> p, Configuration conf, CommunitySetExpr e, BDDRoute other) {

    if (e instanceof CommunityList) {
      Set<CommunityVar> comms =
          ((CommunityList) e)
              .getLines().stream()
                  .map(line -> toCommunityVar(line.getMatchCondition()))
                  .collect(Collectors.toSet());
      BDD acc = factory.one();
      for (CommunityVar comm : comms) {
        p.debug("Inline Community Set: " + comm);
        // the community comm is logically represented as the disjunction of its corresponding
        // atomic predicates
        Set<Integer> aps = atomicPredicatesFor(ImmutableSet.of(comm), _communityAtomicPredicates);
        BDD c =
            factory.orAll(
                aps.stream()
                    .map(ap -> other.getCommunityAtomicPredicates()[ap])
                    .collect(Collectors.toSet()));
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
        BDD matches = isRelevantFor(other, range);
        BDD action = mkBDD(line.getAction() == LineAction.PERMIT);
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
        return factory.one();
      }

      BDD acc = factory.zero();
      for (PrefixRange range : ranges) {
        p.debug("Prefix Range: " + range);
        if (!PrefixUtils.isContainedBy(range.getPrefix(), _ignoredNetworks)) {
          acc = acc.or(isRelevantFor(other, range));
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
  BDD mkBDD(boolean b) {
    return b ? factory.one() : factory.zero();
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
   * A helper for route analysis of SetCommunity and SetCommunities.  Given a set of
   * CommunityVars that are set by the statement, we update all community atomic predicates
   * appropriately:  the ones corresponding to the given CommunityVars are set to 1, and
   * the others are set to 0.
   */
  private void setCommunities(
      Set<CommunityVar> comms, TransferParam<BDDRoute> curP, TransferResult result) {
    Set<Integer> commAPs = atomicPredicatesFor(comms, _communityAtomicPredicates);
    BDD[] commAPBDDs = curP.getData().getCommunityAtomicPredicates();
    for (int ap = 0; ap < commAPBDDs.length; ap++) {
      curP.indent().debug("Value: " + ap);
      BDD comm = commAPBDDs[ap];
      BDD newValue =
          ite(unreachable(result), comm, commAPs.contains(ap) ? factory.one() : factory.zero());
      curP.indent().debug("New Value: " + newValue);
      commAPBDDs[ap] = newValue;
    }
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
   * Create the result of reaching a return statement, returning with the given value.
   */
  private TransferResult returnValue(TransferResult r, boolean val) {
    BDD notReached = unreachable(r);
    BDD b = ite(notReached, r.getReturnValue().getSecond(), mkBDD(val));
    TransferReturn ret = new TransferReturn(r.getReturnValue().getFirst(), b);
    BDD retAsgn = ite(notReached, r.getReturnAssignedValue(), factory.one());
    return r.setReturnValue(ret).setReturnAssignedValue(retAsgn);
  }

  /*
   * Create the result of reaching an exit statement, returning with the given value.
   */
  private TransferResult exitValue(TransferResult r, boolean val) {
    BDD notReached = unreachable(r);
    BDD b = ite(notReached, r.getReturnValue().getSecond(), mkBDD(val));
    TransferReturn ret = new TransferReturn(r.getReturnValue().getFirst(), b);
    BDD exitAsgn = ite(notReached, r.getExitAssignedValue(), factory.one());
    return r.setReturnValue(ret).setExitAssignedValue(exitAsgn);
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
    rec.getMetric().setValue(0);
    rec.getLocalPref().setValue(0);
    rec.getAdminDist().setValue(0);
    rec.getPrefixLength().setValue(0);
    rec.getMed().setValue(0);
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

  /*
   * Convert EXACT community vars to their REGEX equivalents.
   */
  private static CommunityVar toRegexCommunityVar(CommunityVar cvar) {
    switch (cvar.getType()) {
      case REGEX:
        return cvar;
      case EXACT:
        assert cvar.getLiteralValue() != null; // invariant of the EXACT type
        return CommunityVar.from(String.format("^%s$", cvar.getLiteralValue().matchString()));
      default:
        throw new BatfishException("Unexpected CommunityVar type: " + cvar.getType());
    }
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
}
