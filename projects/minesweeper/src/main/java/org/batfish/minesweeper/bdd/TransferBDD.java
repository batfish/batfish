package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.MutableBDDInteger;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
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
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.IpPrefix;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.expr.MatchClusterListLength;
import org.batfish.datamodel.routing_policy.expr.MatchInterface;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;
import org.batfish.datamodel.routing_policy.expr.MatchPeerAddress;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchSourceVrf;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.RemoveTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.OspfType;
import org.batfish.minesweeper.RegexAtomicPredicates;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.SymbolicRegex;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value;
import org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD.Arg;

/** The function from an input routing advertisement to an output routing advertisement. */
public class TransferBDD {

  /**
   * Represents the routing policy environment/context for the current {@link TransferBDD} analysis.
   */
  @AutoValue
  public abstract static class Context {
    static @Nonnull TransferBDD.Context create(
        @Nonnull String policy,
        @Nonnull String hostname,
        boolean useOutputAttributes,
        @Nonnull Configuration config) {
      return new AutoValue_TransferBDD_Context(policy, hostname, useOutputAttributes, config);
    }

    public abstract @Nonnull String policy();

    public abstract @Nonnull String hostname();

    public abstract boolean useOutputAttributes();

    public abstract @Nonnull Configuration config();

    public static @Nonnull Context forPolicy(@Nonnull RoutingPolicy policy) {
      Configuration owner = policy.getOwner();
      checkArgument(
          owner != null, "Expecting an owned policy to support looking up structures, etc.");
      return create(
          policy.getName(), owner.getHostname(), Environment.useOutputAttributesFor(owner), owner);
    }
  }

  private static final Logger LOGGER = LogManager.getLogger(TransferBDD.class);

  /**
   * We track community and AS-path regexes by computing a set of atomic predicates for them. See
   * {@link org.batfish.minesweeper.RegexAtomicPredicates}. During the symbolic route analysis, we
   * simply need the map from each regex to its corresponding set of atomic predicates, each
   * represented by a unique integer.
   */
  private final Map<CommunityVar, Set<Integer>> _communityAtomicPredicates;

  private final Map<BDD, List<Integer>> _communitySetConstraintCache;

  private final Map<SymbolicAsPathRegex, Set<Integer>> _asPathRegexAtomicPredicates;

  private final ConfigAtomicPredicates _configAtomicPredicates;

  private final BDDRoute _originalRoute;

  private final BDDFactory _factory;

  private final Set<String> _unsupportedAlreadyWarned;

  public TransferBDD(ConfigAtomicPredicates aps) {
    this(JFactory.init(100000, 10000), aps);
  }

  public TransferBDD(BDDFactory factory, ConfigAtomicPredicates aps) {
    _configAtomicPredicates = aps;
    _unsupportedAlreadyWarned = new HashSet<>();

    _factory = factory;
    _factory.setCacheRatio(64);

    _originalRoute = new BDDRoute(_factory, aps);
    RegexAtomicPredicates<CommunityVar> standardCommAPs =
        _configAtomicPredicates.getStandardCommunityAtomicPredicates();
    _communityAtomicPredicates = new HashMap<>(standardCommAPs.getRegexAtomicPredicates());
    _communitySetConstraintCache = new HashMap<>();
    for (int i = 0; i < _originalRoute.getCommunityAtomicPredicates().length; i++) {
      // Pre-cache all individual APs which are exclusive with all other APs by definition.
      _communitySetConstraintCache.put(
          _originalRoute.getCommunityAtomicPredicates()[i], ImmutableList.of(i));
    }
    // add the atomic predicates for the extended/large community literals
    _configAtomicPredicates
        .getNonStandardCommunityLiterals()
        .forEach((key, value) -> _communityAtomicPredicates.put(value, ImmutableSet.of(key)));
    _asPathRegexAtomicPredicates =
        _configAtomicPredicates.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates();
  }

  /**
   * Assert that the number of outstanding BDDs has increased by exactly the expected amount.
   *
   * @param startBDDs the number of outstanding BDDs before the operation
   * @param expected the expected number of new BDDs created
   */
  private void assertNoLeaks(long startBDDs, long expected) {
    assert _factory.numOutstandingBDDs() == startBDDs + expected
        : String.format(
            "Expected to create %d new BDDs, actually created %d",
            expected, _factory.numOutstandingBDDs() - startBDDs);
  }

  /*
   * Apply the effect of modifying a long value (e.g., to set the metric).
   * Overflows for IncrementMetric are handled by clipping to the max value.
   */
  @VisibleForTesting
  MutableBDDInteger applyLongExprModification(
      TransferBDDState state,
      Context context,
      Function<BDDRoute, MutableBDDInteger> attributeGetter,
      LongExpr e)
      throws UnsupportedOperationException {
    /*
     * TODO: Increment/Decrement Metric/LocalPreference should NOT be LongExprs, they should be in Statements. As-is,
     * the correctness of this function relies on the parameter X correctly corresponding to the Metric/LocalPref being
     * modified. Auditing VS conversion code, we appear to always use them this way, but this is not type-safe.
     */
    TransferParam p = state.getTransferParam().indent();
    // use the correct route for reading the old value
    BDDRoute route = routeForReading(state, context);
    MutableBDDInteger x = attributeGetter.apply(route);
    if (e instanceof LiteralLong) {
      LiteralLong z = (LiteralLong) e;
      p.debug("LiteralLong: %s", z.getValue());
      return MutableBDDInteger.makeFromValue(x.getFactory(), 32, z.getValue());
    } else if (e instanceof IncrementMetric) {
      IncrementMetric z = (IncrementMetric) e;
      p.debug("IncrementMetric: %s", z.getAddend());
      return x.addClipping(MutableBDDInteger.makeFromValue(x.getFactory(), 32, z.getAddend()));
    } else if (e instanceof DecrementMetric) {
      DecrementMetric z = (DecrementMetric) e;
      p.debug("DecrementMetric: %s", z.getSubtrahend());
      return x.subClipping(MutableBDDInteger.makeFromValue(x.getFactory(), 32, z.getSubtrahend()));
    } else if (e instanceof IncrementLocalPreference) {
      IncrementLocalPreference z = (IncrementLocalPreference) e;
      p.debug("IncrementLocalPreference: %s", z.getAddend());
      return x.addClipping(MutableBDDInteger.makeFromValue(x.getFactory(), 32, z.getAddend()));
    } else if (e instanceof DecrementLocalPreference) {
      DecrementLocalPreference z = (DecrementLocalPreference) e;
      p.debug("DecrementLocalPreference: %s", z.getSubtrahend());
      return x.subClipping(MutableBDDInteger.makeFromValue(x.getFactory(), 32, z.getSubtrahend()));
    } else {
      throw new UnsupportedOperationException(e.toString());
    }
  }

  /**
   * Produces a BDD that represents the disjunction of all atomic predicates associated with any of
   * the given AS-path regexes.
   *
   * @param asPathRegexes the set of AS-path regexes to convert to a BDD
   * @param apMap a map from as-path regexes to their corresponding sets of atomic predicates
   * @param route the symbolic representation of a route
   * @return the BDD
   */
  public static BDD asPathRegexesToBDD(
      Set<SymbolicAsPathRegex> asPathRegexes,
      Map<SymbolicAsPathRegex, Set<Integer>> apMap,
      BDDRoute route) {
    Set<Integer> asPathAPs = atomicPredicatesFor(asPathRegexes, apMap);
    return route
        .getFactory()
        .orAllAndFree(
            asPathAPs.stream()
                .map(ap -> route.getAsPathRegexAtomicPredicates().value(ap))
                .collect(Collectors.toList()));
  }

  //

  /**
   * Produces a set consisting of all atomic predicates associated with any of the given symbolic
   * regexes.
   *
   * @param regexes the regexes of interest
   * @param apMap a map from regexes to their corresponding sets of atomic predicates
   * @param <T> the specific type of the regexes
   * @return the set of relevant atomic predicates
   */
  static <T extends SymbolicRegex> Set<Integer> atomicPredicatesFor(
      Set<T> regexes, Map<T, Set<Integer>> apMap) {
    return regexes.stream()
        .flatMap(r -> apMap.get(r).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Produce {@link TransferResult} for all paths through the given boolean expression. For most
   * expressions, for example matching on a prefix or community, this analysis will yield exactly
   * two paths, respectively representing the case when the expression evaluates to true and false.
   * Some expressions, such as {@link CallExpr}, {@link Conjunction}, and {@link Disjunction},
   * implicitly or explicitly contain branches and so can yield more than two paths.
   *
   * <p>If {@code retainAllPaths} is {@code false}, then paths with the same output behavior (aka,
   * {@link TransferResult} is equivalent except for the input conditions under which the path is
   * reached) are combined (by unioning those input conditions).
   *
   * <p>TODO: Any updates to the TransferParam in expr are lost currently
   */
  private List<TransferResult> compute(
      BooleanExpr expr, TransferBDDState state, Context context, boolean retainAllPaths)
      throws UnsupportedOperationException {
    TransferParam p = state.getTransferParam();
    TransferResult result = state.getTransferResult();

    // the route whose attributes should be read when evaluating this expression
    // (except in a few cases where the original route attributes are always used)
    BDDRoute currRoute = routeForReading(state, context);

    List<TransferResult> finalResults = new ArrayList<>();

    // TODO: right now everything is IPV4
    if (expr instanceof MatchIpv4) {
      p.debug("MatchIpv4 Result: true");
      finalResults.add(result.setReturnValueBDD(_factory.one()).setReturnValueAccepted(true));

    } else if (expr instanceof Not) {
      p.debug("mkNot");
      Not n = (Not) expr;
      List<TransferResult> results = compute(n.getExpr(), state, context, retainAllPaths);
      for (TransferResult res : results) {
        TransferResult newRes = res.setReturnValueAccepted(!res.getReturnValue().getAccepted());
        finalResults.add(newRes);
      }

    } else if (expr instanceof Conjunction) {
      Conjunction conj = (Conjunction) expr;
      List<TransferResult> currResults = new ArrayList<>();
      // the default result is true
      currResults.add(result.setReturnValueBDD(_factory.one()).setReturnValueAccepted(true));
      for (BooleanExpr e : conj.getConjuncts()) {
        List<TransferResult> nextResults = new ArrayList<>();
        try {
          for (TransferResult curr : currResults) {
            BDD currBDD = curr.getReturnValue().getInputConstraints();
            compute(e, toTransferBDDState(p.indent(), curr), context, retainAllPaths)
                .forEach(
                    r -> {
                      BDD condition = r.getReturnValue().getInputConstraints().and(currBDD);
                      if (condition.isZero()) {
                        condition.free();
                        return;
                      }
                      TransferResult updated = r.setReturnValueBDD(condition);
                      // if we're on a path where e evaluates to false, then this path is done;
                      // otherwise we will evaluate the next conjunct in the next iteration
                      if (!updated.getReturnValue().getAccepted()) {
                        finalResults.add(updated);
                      } else {
                        nextResults.add(updated);
                      }
                    });
          }
          currResults = retainAllPaths ? nextResults : combineTransferResults(nextResults);
        } catch (UnsupportedOperationException ufe) {
          // BooleanExpr e is not supported; ignore it but record the fact that we encountered it
          currResults.forEach(tr -> unsupported(ufe, tr, context));
        }
      }
      finalResults.addAll(currResults);

    } else if (expr instanceof Disjunction) {
      Disjunction disj = (Disjunction) expr;
      /*
      Disjunctions of as-path matches are handled specially when building as-path atomic
      predicates, for performance reasons. See BooleanExprAsPathCollector::visitDisjunction. We
      have to check for that special case here in order to access the correct atomic predicates.
      */
      if (!disj.getDisjuncts().isEmpty()
          && disj.getDisjuncts().stream()
              .allMatch(
                  d ->
                      d instanceof MatchAsPath
                          && ((MatchAsPath) d).getAsPathExpr().equals(InputAsPath.instance()))) {
        checkForAsPathMatchAfterUpdate(currRoute);
        // collect all as-path regexes, produce a single regex that is their union, and then create
        // the corresponding BDD
        Set<SymbolicAsPathRegex> asPathRegexes =
            disj.getDisjuncts().stream()
                .flatMap(
                    d ->
                        ((MatchAsPath) d)
                                .getAsPathMatchExpr()
                                .accept(
                                    new AsPathMatchExprToRegexes(),
                                    new Arg(this, currRoute, context))
                                .stream())
                .collect(ImmutableSet.toImmutableSet());
        BDD asPathRegexBDD =
            asPathRegexes.isEmpty()
                ? _factory.zero()
                : asPathRegexesToBDD(
                    ImmutableSet.of(SymbolicAsPathRegex.union(asPathRegexes)),
                    _asPathRegexAtomicPredicates,
                    currRoute);
        finalResults.add(result.setReturnValueBDD(asPathRegexBDD).setReturnValueAccepted(true));
      } else {
        List<TransferResult> currResults = new ArrayList<>();
        // the default result is false
        currResults.add(result.setReturnValueBDD(_factory.one()).setReturnValueAccepted(false));
        for (BooleanExpr e : disj.getDisjuncts()) {
          List<TransferResult> nextResults = new ArrayList<>();
          try {
            for (TransferResult curr : currResults) {
              BDD currBDD = curr.getReturnValue().getInputConstraints();
              compute(e, toTransferBDDState(p.indent(), curr), context, retainAllPaths)
                  .forEach(
                      r -> {
                        BDD condition = r.getReturnValue().getInputConstraints().and(currBDD);
                        if (condition.isZero()) {
                          condition.free();
                          return;
                        }
                        TransferResult updated = r.setReturnValueBDD(condition);
                        // if we're on a path where e evaluates to true, then this path is done;
                        // otherwise we will evaluate the next disjunct in the next iteration
                        if (updated.getReturnValue().getAccepted()) {
                          finalResults.add(updated);
                        } else {
                          nextResults.add(updated);
                        }
                      });
            }
            currResults = nextResults;
          } catch (UnsupportedOperationException ufe) {
            // BooleanExpr e is not supported; ignore it but record the fact that we encountered it
            currResults.forEach(tr -> unsupported(ufe, tr, context));
          }
        }
        finalResults.addAll(currResults);
      }

      // TODO: This code is here for backward-compatibility reasons but has not been tested and is
      // not currently maintained
    } else if (expr instanceof ConjunctionChain) {
      p.debug("ConjunctionChain");
      ConjunctionChain d = (ConjunctionChain) expr;
      List<BooleanExpr> conjuncts = new ArrayList<>(d.getSubroutines());
      if (p.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(p.getDefaultPolicy().getDefaultPolicy());
        conjuncts.add(be);
      }
      if (conjuncts.isEmpty()) {
        finalResults.add(result.setReturnValueBDD(_factory.one()));
      } else {
        TransferParam record = p;
        List<TransferResult> currResults = new ArrayList<>();
        currResults.add(result);
        for (BooleanExpr e : d.getSubroutines()) {
          List<TransferResult> nextResults = new ArrayList<>();
          for (TransferResult curr : currResults) {
            TransferParam param =
                record
                    .setDefaultPolicy(null)
                    .setChainContext(TransferParam.ChainContext.CONJUNCTION)
                    .indent();
            compute(e, toTransferBDDState(param, curr), context, retainAllPaths)
                .forEach(
                    r -> {
                      if (r.getFallthroughValue()) {
                        nextResults.add(r);
                      } else {
                        finalResults.add(r);
                      }
                    });
          }
          currResults = nextResults;
        }
        finalResults.addAll(currResults);
      }

    } else if (expr instanceof FirstMatchChain) {
      p.debug("FirstMatchChain");
      FirstMatchChain chain = (FirstMatchChain) expr;
      List<BooleanExpr> chainPolicies = new ArrayList<>(chain.getSubroutines());
      if (p.getDefaultPolicy() != null) {
        BooleanExpr be = new CallExpr(p.getDefaultPolicy().getDefaultPolicy());
        chainPolicies.add(be);
      }
      TransferParam record = p;
      List<TransferResult> currResults = new ArrayList<>();
      currResults.add(result);
      for (BooleanExpr pol : chainPolicies) {
        List<TransferResult> nextResults = new ArrayList<>();
        for (TransferResult curr : currResults) {
          BDD currBDD = curr.getReturnValue().getInputConstraints();
          TransferParam param = record.indent();
          // we set the fallthrough flag to true initially in order to handle implicit fallthrough
          // properly; if there is an exit or return in the policy then the flag will be unset
          TransferResult updatedCurr = curr.setFallthroughValue(true);
          compute(pol, toTransferBDDState(param, updatedCurr), context, retainAllPaths)
              .forEach(
                  r -> {
                    // r's BDD only represents the constraints on a path through the policy pol, so
                    // we explicitly incorporate the constraints accrued on the path through the
                    // prior policies in the chain
                    BDD condition = r.getReturnValue().getInputConstraints().and(currBDD);
                    if (condition.isZero()) {
                      return;
                    }
                    TransferResult updated = r.setReturnValueBDD(condition);
                    if (updated.getFallthroughValue()) {
                      nextResults.add(updated.setFallthroughValue(false));
                    } else {
                      finalResults.add(updated);
                    }
                  });
        }
        currResults = nextResults;
      }
      if (!currResults.isEmpty()) {
        throw new BatfishException(
            "The last policy in the chain should not fall through to the next policy");
      }

    } else if (expr instanceof MatchProtocol) {
      MatchProtocol mp = (MatchProtocol) expr;
      Set<RoutingProtocol> rps = mp.getProtocols();
      BDD matchRPBDD = _originalRoute.anyElementOf(rps, _originalRoute.getProtocolHistory());
      finalResults.add(result.setReturnValueBDD(matchRPBDD).setReturnValueAccepted(true));

    } else if (expr instanceof MatchPrefixSet) {
      p.debug("MatchPrefixSet");
      MatchPrefixSet m = (MatchPrefixSet) expr;

      // MatchPrefixSet::evaluate obtains the prefix to match (either the destination network or
      // next-hop IP) from the original route, so we do the same here
      BDD prefixSet = matchPrefixSet(p.indent(), m, _originalRoute, context);
      if (!prefixSet.isZero()) {
        finalResults.add(result.setReturnValueBDD(prefixSet).setReturnValueAccepted(true));
      }

    } else if (expr instanceof CallExpr) {
      p.debug("CallExpr");
      CallExpr c = (CallExpr) expr;
      String name = c.getCalledPolicyName();
      RoutingPolicy pol = context.config().getRoutingPolicies().get(name);

      // save callee state
      boolean oldReturnAssigned = result.getReturnAssignedValue();

      TransferParam newParam =
          p.setCallContext(TransferParam.CallContext.EXPR_CALL).indent().enterScope(name);
      List<TransferBDDState> callStates =
          compute(
              pol.getStatements(),
              ImmutableList.of(
                  new TransferBDDState(
                      newParam, result.setReturnAssignedValue(false).setExitAssignedValue(false))),
              context,
              retainAllPaths);

      for (TransferBDDState callState : callStates) {
        finalResults.add(
            callState
                .getTransferResult()
                // restore the callee state
                .setReturnAssignedValue(oldReturnAssigned));
      }

    } else if (expr instanceof WithEnvironmentExpr) {
      p.debug("WithEnvironmentExpr");
      // TODO: this is not correct
      WithEnvironmentExpr we = (WithEnvironmentExpr) expr;
      // TODO: postStatements() and preStatements()
      finalResults.addAll(compute(we.getExpr(), state, context, retainAllPaths));

    } else if (expr instanceof MatchCommunities) {
      p.debug("MatchCommunities");
      MatchCommunities mc = (MatchCommunities) expr;
      // we only handle the case where the expression being matched is just the input communities
      if (!mc.getCommunitySetExpr().equals(InputCommunities.instance())) {
        throw new UnsupportedOperationException(mc.toString());
      }
      BDD mcPredicate =
          mc.getCommunitySetMatchExpr()
              .accept(new CommunitySetMatchExprToBDD(), new Arg(this, currRoute, context));
      if (!mcPredicate.isZero()) {
        finalResults.add(result.setReturnValueBDD(mcPredicate).setReturnValueAccepted(true));
      }

    } else if (expr instanceof MatchTag) {
      MatchTag mt = (MatchTag) expr;
      BDD mtBDD = matchLongComparison(mt.getCmp(), mt.getTag(), currRoute.getTag());
      finalResults.add(result.setReturnValueBDD(mtBDD).setReturnValueAccepted(true));

    } else if (expr instanceof MatchMetric) {
      MatchMetric mm = (MatchMetric) expr;
      BDD mmBDD = matchLongComparison(mm.getComparator(), mm.getMetric(), currRoute.getMed());
      finalResults.add(result.setReturnValueBDD(mmBDD).setReturnValueAccepted(true));

    } else if (expr instanceof MatchClusterListLength) {
      MatchClusterListLength mcll = (MatchClusterListLength) expr;
      BDD mcllBDD =
          matchIntComparison(mcll.getComparator(), mcll.getRhs(), currRoute.getClusterListLength());
      finalResults.add(result.setReturnValueBDD(mcllBDD).setReturnValueAccepted(true));

    } else if (expr instanceof BooleanExprs.StaticBooleanExpr) {
      BooleanExprs.StaticBooleanExpr b = (BooleanExprs.StaticBooleanExpr) expr;
      switch (b.getType()) {
        case CallExprContext:
          p.debug("CallExprContext");
          finalResults.add(
              result
                  .setReturnValueBDD(_factory.one())
                  .setReturnValueAccepted(
                      p.getCallContext() == TransferParam.CallContext.EXPR_CALL));
          break;
        case CallStatementContext:
          p.debug("CallStmtContext");
          finalResults.add(
              result
                  .setReturnValueBDD(_factory.one())
                  .setReturnValueAccepted(
                      p.getCallContext() == TransferParam.CallContext.STMT_CALL));
          break;
        case True:
          p.debug("True");
          finalResults.add(result.setReturnValueBDD(_factory.one()).setReturnValueAccepted(true));
          break;
        case False:
          p.debug("False");
          finalResults.add(result.setReturnValueBDD(_factory.one()).setReturnValueAccepted(false));
          break;
      }

    } else if (expr instanceof LegacyMatchAsPath) {
      p.debug("MatchAsPath");
      checkForAsPathMatchAfterUpdate(currRoute);
      LegacyMatchAsPath legacyMatchAsPathNode = (LegacyMatchAsPath) expr;
      BDD asPathPredicate =
          matchAsPathSetExpr(p.indent(), legacyMatchAsPathNode.getExpr(), currRoute, context);
      finalResults.add(result.setReturnValueBDD(asPathPredicate).setReturnValueAccepted(true));

    } else if (expr instanceof MatchAsPath
        && ((MatchAsPath) expr).getAsPathExpr().equals(InputAsPath.instance())) {
      checkForAsPathMatchAfterUpdate(currRoute);
      MatchAsPath matchAsPath = (MatchAsPath) expr;
      BDD asPathPredicate =
          asPathRegexesToBDD(
              matchAsPath
                  .getAsPathMatchExpr()
                  .accept(new AsPathMatchExprToRegexes(), new Arg(this, currRoute, context)),
              _asPathRegexAtomicPredicates,
              currRoute);
      finalResults.add(result.setReturnValueBDD(asPathPredicate).setReturnValueAccepted(true));

    } else if (expr instanceof MatchSourceVrf) {
      MatchSourceVrf msv = (MatchSourceVrf) expr;
      int index = _configAtomicPredicates.getSourceVrfs().indexOf(msv.getSourceVrf());
      BDD sourceVrfPred = _originalRoute.getSourceVrfs().value(index);
      finalResults.add(result.setReturnValueBDD(sourceVrfPred).setReturnValueAccepted(true));

    } else if (expr instanceof TrackSucceeded) {
      TrackSucceeded ts = (TrackSucceeded) expr;
      BDD trackPred =
          itemToBDD(
              ts.getTrackName(), _configAtomicPredicates.getTracks(), _originalRoute.getTracks());
      finalResults.add(result.setReturnValueBDD(trackPred).setReturnValueAccepted(true));

    } else if (expr instanceof MatchInterface) {
      MatchInterface mi = (MatchInterface) expr;
      if (currRoute.getNextHopSet()) {
        // we don't yet properly model the situation where a modified next-hop is later matched
        // upon, so we check for that situation here
        throw new UnsupportedOperationException(expr.toString());
      }
      BDD miPred =
          mi.getInterfaces().stream()
              .map(
                  nhi -> {
                    int index = _configAtomicPredicates.getNextHopInterfaces().indexOf(nhi);
                    return currRoute.getNextHopInterfaces().value(index);
                  })
              .reduce(_factory.zero(), BDD::or);
      finalResults.add(result.setReturnValueBDD(miPred).setReturnValueAccepted(true));

    } else if (expr instanceof MatchPeerAddress) {
      MatchPeerAddress mp = (MatchPeerAddress) expr;
      Set<Ip> ips = mp.getPeers();
      BDD matchPABDD =
          _originalRoute.anyElementOf(
              ips.stream()
                  .map(ip -> _configAtomicPredicates.getPeerAddresses().indexOf(ip))
                  .collect(Collectors.toSet()),
              _originalRoute.getPeerAddress());
      finalResults.add(result.setReturnValueBDD(matchPABDD).setReturnValueAccepted(true));

    } else {
      throw new UnsupportedOperationException(expr.toString());
    }

    // in most cases above we have only provided the path corresponding to the predicate being true
    // so lastly, we add the path corresponding to the predicate being false

    // first get a predicate representing routes that don't go down any existing path that we've
    // created so far
    BDD unmatched =
        _factory
            .orAll(
                finalResults.stream()
                    .map(r -> r.getReturnValue().getInputConstraints())
                    .collect(Collectors.toList()))
            .notEq();
    if (!unmatched.isZero()) {
      // then add a non-accepting path corresponding to that predicate
      TransferResult remaining =
          result
              .setReturnValue(
                  new TransferReturn(
                      result.getReturnValue().getOutputRoute().deepCopy(), unmatched, false))
              .setIntermediateAttributes(result.getIntermediateBgpAttributes().deepCopy());
      finalResults.add(remaining);
    }
    return ImmutableList.copyOf(
        retainAllPaths ? finalResults : combineTransferResults(finalResults));
  }

  /**
   * Symbolic analysis of a single route-policy statement, producing one {@link TransferBDDState}
   * per path through the given statement.
   *
   * <p>If {@code retainAllPaths} is {@code false}, then paths with the same output behavior (aka,
   * {@link TransferBDDState} is equivalent except for the input conditions under which the path is
   * reached) are combined (by unioning those input conditions).
   */
  private List<TransferBDDState> compute(
      Statement stmt, TransferBDDState state, Context context, boolean retainAllPaths)
      throws UnsupportedOperationException {
    TransferParam curP = state.getTransferParam();
    TransferResult result = state.getTransferResult();

    if (stmt instanceof StaticStatement) {
      StaticStatement ss = (StaticStatement) stmt;

      switch (ss.getType()) {
        case ExitAccept:
          curP.debug("ExitAccept");
          result = exitValue(result, true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case ReturnTrue:
          curP.debug("ReturnTrue");
          result = returnValue(result, true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case ExitReject:
          curP.debug("ExitReject");
          result = exitValue(result, false);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case ReturnFalse:
          curP.debug("ReturnFalse");
          result = returnValue(result, false);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case SetDefaultActionAccept:
          curP.debug("SetDefaultActionAccept");
          curP = curP.setDefaultAccept(true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case SetDefaultActionReject:
          curP.debug("SetDefaultActionReject");
          curP = curP.setDefaultAccept(false);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case SetLocalDefaultActionAccept:
          curP.debug("SetLocalDefaultActionAccept");
          curP = curP.setDefaultAcceptLocal(true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case SetLocalDefaultActionReject:
          curP.debug("SetLocalDefaultActionReject");
          curP = curP.setDefaultAcceptLocal(false);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case ReturnLocalDefaultAction:
          curP.debug("ReturnLocalDefaultAction");
          result = returnValue(result, curP.getDefaultAcceptLocal());
          return ImmutableList.of(toTransferBDDState(curP, result));

        case DefaultAction:
          curP.debug("DefaultAction");
          result = exitValue(result, curP.getDefaultAccept());
          return ImmutableList.of(toTransferBDDState(curP, result));

        case FallThrough:
          curP.debug("Fallthrough");
          result = fallthrough(result);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case Return:
          curP.debug("Return");
          result = result.setReturnAssignedValue(true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case Suppress:
          curP.debug("Suppress");
          result = suppressedValue(result, true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case Unsuppress:
          curP.debug("Unsuppress");
          result = suppressedValue(result, false);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case SetReadIntermediateBgpAttributes:
          curP = curP.setReadIntermediateBgpAttributes(true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case SetWriteIntermediateBgpAttributes:
          curP = curP.setWriteIntermediateBgpAttributes(true);
          return ImmutableList.of(toTransferBDDState(curP, result));

        case UnsetWriteIntermediateBgpAttributes:
          curP = curP.setWriteIntermediateBgpAttributes(false);
          return ImmutableList.of(toTransferBDDState(curP, result));

        default:
          throw new UnsupportedOperationException(ss.getType().toString());
      }

    } else if (stmt instanceof If) {
      curP.debug("If");
      If i = (If) stmt;
      List<TransferResult> guardResults =
          compute(
              i.getGuard(), new TransferBDDState(curP.indent(), result), context, retainAllPaths);

      // for each path coming from the guard, symbolically execute the appropriate branch of the If
      List<TransferBDDState> newStates = new ArrayList<>();
      BDD currPathCondition = result.getReturnValue().getInputConstraints();
      for (TransferResult guardResult : guardResults) {
        BDD pathCondition =
            currPathCondition.and(guardResult.getReturnValue().getInputConstraints());
        if (pathCondition.isZero()) {
          // prune infeasible paths
          continue;
        }
        BDDRoute current = guardResult.getReturnValue().getOutputRoute();
        BDDRoute intermediate = guardResult.getIntermediateBgpAttributes();
        boolean accepted = guardResult.getReturnValue().getAccepted();

        TransferParam pCopy = curP.indent();
        List<Statement> branch = accepted ? i.getTrueStatements() : i.getFalseStatements();
        newStates.addAll(
            compute(
                branch,
                ImmutableList.of(
                    new TransferBDDState(
                        pCopy,
                        result
                            .setReturnValue(
                                new TransferReturn(
                                    current, pathCondition, result.getReturnValue().getAccepted()))
                            .setIntermediateAttributes(intermediate))),
                context,
                retainAllPaths));
      }

      return ImmutableList.copyOf(retainAllPaths ? newStates : combineStates(newStates));

    } else if (stmt instanceof SetAdministrativeCost) {
      curP.debug("SetAdministrativeCost");
      SetAdministrativeCost sac = (SetAdministrativeCost) stmt;
      IntExpr ie = sac.getAdmin();
      if (!(ie instanceof LiteralInt)) {
        throw new UnsupportedOperationException(ie.toString());
      }
      int val = ((LiteralInt) ie).getValue();
      writeRouteAttribute(
          state, r -> r.setAdminDist(MutableBDDInteger.makeFromValue(this._factory, 8, val)));
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof SetDefaultPolicy) {
      curP.debug("SetDefaultPolicy");
      curP = curP.setDefaultPolicy((SetDefaultPolicy) stmt);
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof SetMetric) {
      curP.debug("SetMetric");
      SetMetric sm = (SetMetric) stmt;
      LongExpr ie = sm.getMetric();
      MutableBDDInteger med = applyLongExprModification(state, context, BDDRoute::getMed, ie);
      writeRouteAttribute(
          state,
          r -> {
            r.setMed(med);
          });
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof SetOrigin) {
      curP.debug("SetOrigin");
      OriginExpr oe = ((SetOrigin) stmt).getOriginType();
      if (oe instanceof LiteralOrigin) {
        OriginType ot = ((LiteralOrigin) oe).getOriginType();
        writeRouteAttribute(
            state,
            r -> {
              BDDDomain<OriginType> originType = new BDDDomain<>(r.getOriginType());
              originType.setValue(ot);
              r.setOriginType(originType);
            });
        return ImmutableList.of(toTransferBDDState(curP, result));
      } else {
        throw new UnsupportedOperationException(oe.toString());
      }

    } else if (stmt instanceof SetOspfMetricType) {
      curP.debug("SetOspfMetricType");
      SetOspfMetricType somt = (SetOspfMetricType) stmt;
      OspfMetricType mt = somt.getMetricType();
      TransferParam finalCurP = curP;
      writeRouteAttribute(
          state,
          r -> {
            BDDDomain<OspfType> current = r.getOspfMetric();
            BDDDomain<OspfType> newValue = new BDDDomain<>(current);
            if (mt == OspfMetricType.E1) {
              finalCurP.indent().debug("Value: E1");
              newValue.setValue(OspfType.E1);
            } else {
              finalCurP.indent().debug("Value: E2");
              newValue.setValue(OspfType.E2);
            }
            r.setOspfMetric(newValue);
          });
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof SetLocalPreference) {
      curP.debug("SetLocalPreference");
      SetLocalPreference slp = (SetLocalPreference) stmt;
      LongExpr ie = slp.getLocalPreference();
      MutableBDDInteger newValue =
          applyLongExprModification(state, context, BDDRoute::getLocalPref, ie);
      writeRouteAttribute(state, r -> r.setLocalPref(newValue));
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof SetTag) {
      curP.debug("SetTag");
      SetTag st = (SetTag) stmt;
      LongExpr ie = st.getTag();
      MutableBDDInteger newValue = applyLongExprModification(state, context, BDDRoute::getTag, ie);
      writeRouteAttribute(state, r -> r.setTag(newValue));
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof RemoveTunnelEncapsulationAttribute
        || stmt instanceof SetTunnelEncapsulationAttribute) {
      curP.debug("%s", stmt.getClass().getSimpleName());
      writeRouteAttribute(
          state,
          r -> {
            BDDTunnelEncapsulationAttribute current = r.getTunnelEncapsulationAttribute();
            BDDTunnelEncapsulationAttribute newValue =
                BDDTunnelEncapsulationAttribute.copyOf(current);
            if (stmt instanceof SetTunnelEncapsulationAttribute) {
              SetTunnelEncapsulationAttribute st = (SetTunnelEncapsulationAttribute) stmt;
              LiteralTunnelEncapsulationAttribute expr =
                  (LiteralTunnelEncapsulationAttribute) st.getTunnelEncapsulationAttributeExpr();
              newValue.setValue(Value.literal(expr.getTunnelEncapsulationAttribute()));
            } else {
              assert stmt instanceof RemoveTunnelEncapsulationAttribute;
              newValue.setValue(Value.absent());
            }
            r.setTunnelEncapsulationAttribute(newValue);
          });
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof SetWeight) {
      curP.debug("SetWeight");
      SetWeight sw = (SetWeight) stmt;
      IntExpr ie = sw.getWeight();
      if (!(ie instanceof LiteralInt)) {
        throw new UnsupportedOperationException(ie.toString());
      }
      LiteralInt z = (LiteralInt) ie;
      writeRouteAttribute(
          state,
          r -> {
            MutableBDDInteger currWeight = r.getWeight();
            MutableBDDInteger newValue =
                MutableBDDInteger.makeFromValue(currWeight.getFactory(), 16, z.getValue());
            r.setWeight(newValue);
          });
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof SetCommunities) {
      curP.debug("SetCommunities");
      SetCommunities sc = (SetCommunities) stmt;
      org.batfish.datamodel.routing_policy.communities.CommunitySetExpr setExpr =
          sc.getCommunitySetExpr();
      // SetCommunitiesVisitor requires a BDDRoute that maps each community atomic predicate BDD
      // to its corresponding BDD variable, so we use the original route here
      CommunityAPDispositions dispositions =
          setExpr.accept(new SetCommunitiesVisitor(), new Arg(this, _originalRoute, context));
      updateCommunities(dispositions, state, context);
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof CallStatement cs) {
      /*
       this code is based on the concrete semantics defined by CallStatement::execute, which also
       relies on RoutingPolicy::call
      */
      curP.debug("CallStatement");
      String name = cs.getCalledPolicyName();
      RoutingPolicy pol = context.config().getRoutingPolicies().get(name);
      verify(
          pol != null,
          "%s call missing policy that should have been sanitized during conversion and caught"
              + " during finalization",
          cs);

      // save callee state
      boolean oldReturnAssigned = result.getReturnAssignedValue();

      TransferParam newParam =
          curP.indent().setCallContext(TransferParam.CallContext.STMT_CALL).enterScope(name);
      List<TransferBDDState> callResults =
          compute(
              pol.getStatements(),
              ImmutableList.of(
                  new TransferBDDState(newParam, result.setReturnAssignedValue(false))),
              context,
              retainAllPaths);
      // TODO: Currently dropping the returned TransferParam on the floor
      TransferParam finalCurP = curP;
      // restore the original returnAssigned value
      List<TransferBDDState> restoredResults =
          callResults.stream()
              .map(
                  r ->
                      toTransferBDDState(
                          finalCurP,
                          r.getTransferResult().setReturnAssignedValue(oldReturnAssigned)))
              .collect(ImmutableList.toImmutableList());
      return retainAllPaths ? restoredResults : combineStates(restoredResults);

    } else if (stmt instanceof SetNextHop) {
      curP.debug("SetNextHop");
      writeRouteAttribute(state, r -> setNextHop(((SetNextHop) stmt).getExpr(), r));
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof PrependAsPath) {
      curP.debug("PrependAsPath");
      PrependAsPath pap = (PrependAsPath) stmt;
      writeRouteAttribute(state, r -> prependASPath(pap.getExpr(), r));
      return ImmutableList.of(toTransferBDDState(curP, result));

    } else if (stmt instanceof TraceableStatement) {
      return compute(
          ((TraceableStatement) stmt).getInnerStatements(),
          ImmutableList.of(state),
          context,
          retainAllPaths);

    } else {
      throw new UnsupportedOperationException(stmt.toString());
    }
  }

  /**
   * Symbolic analysis of a single routing policy {@link Statement}.
   *
   * <p>If {@code retainAllPaths} is {@code false}, then paths with the same output behavior (aka,
   * {@link TransferBDDState} is equivalent except for the input conditions under which the path is
   * reached) are combined (by unioning those input conditions).
   */
  private List<TransferBDDState> compute(
      List<Statement> statements,
      List<TransferBDDState> states,
      Context context,
      boolean retainAllPaths) {
    List<TransferBDDState> currStates = states;
    List<TransferBDDState> returnStates = new ArrayList<>();
    for (Statement stmt : statements) {
      List<TransferBDDState> newStates = new ArrayList<>();
      for (TransferBDDState currState : currStates) {
        try {
          // if the path has already reached an exit/return then just keep it
          if (unreachable(currState.getTransferResult())) {
            returnStates.add(currState);
          } else {
            // otherwise symbolically execute the next statement
            newStates.addAll(compute(stmt, currState, context, retainAllPaths));
          }
        } catch (UnsupportedOperationException e) {
          unsupported(e, currState.getTransferResult(), context);
          newStates.add(currState);
        }
      }
      currStates = retainAllPaths ? newStates : combineStates(newStates);
    }
    returnStates.addAll(currStates);
    return retainAllPaths ? returnStates : combineStates(returnStates);
  }

  /**
   * Compute the {@link TransferResult} for all paths through the given list of routing policy
   * {@link Statement statements} from the given initial condition {@link TransferBDDState}.
   *
   * <p>If {@code retainAllPaths} is {@code false}, then paths with the same output behavior (aka,
   * {@link TransferResult} is equivalent except for the input conditions under which the path is
   * reached) are combined (by unioning those input conditions).
   */
  public List<TransferResult> computePaths(
      TransferBDDState state, List<Statement> statements, Context context, boolean retainAllPaths) {
    List<TransferBDDState> states =
        compute(statements, ImmutableList.of(state), context, retainAllPaths);
    return extractResults(states);
  }

  private List<TransferResult> extractResults(List<TransferBDDState> states) {
    ImmutableList.Builder<TransferResult> results = ImmutableList.builder();
    for (TransferBDDState state : states) {
      TransferResult result = state.getTransferResult();
      if (result.getReturnValue().getInputConstraints().isZero()) {
        // ignore infeasible paths
        continue;
      }
      TransferParam curP = state.getTransferParam();
      curP.debug("InitialCall finalizing");
      // Only accept routes that are not suppressed
      if (result.getSuppressedValue()) {
        result = result.setReturnValueAccepted(false);
      }
      results.add(result);
    }
    return results.build();
  }

  private TransferResult fallthrough(TransferResult r) {
    return r.setFallthroughValue(true).setReturnAssignedValue(true);
  }

  private TransferBDDState toTransferBDDState(TransferParam curP, TransferResult result) {
    return new TransferBDDState(curP, result);
  }

  // Produce a BDD representing conditions under which the route's destination prefix is within a
  // given prefix range.
  public static BDD isRelevantForDestination(BDDRoute record, PrefixRange range) {
    SubRange r = range.getLengthRange();
    int lower = r.getStart();
    int upper = r.getEnd();

    BDD prefixMatch = record.getPrefix().toBDD(range.getPrefix());
    BDD lenMatch = record.getPrefixLength().range(lower, upper);
    return prefixMatch.andWith(lenMatch);
  }

  // Produce a BDD representing conditions under which the route's next-hop address is within a
  // given prefix range.
  private static BDD isRelevantForNextHop(BDDRoute record, PrefixRange range) {
    SubRange r = range.getLengthRange();
    int lower = r.getStart();
    int upper = r.getEnd();
    // the next hop has a prefix length of MAX_PREFIX_LENGTH (32); check that it is in range
    boolean lenMatch = lower <= Prefix.MAX_PREFIX_LENGTH && Prefix.MAX_PREFIX_LENGTH <= upper;
    if (lenMatch) {
      return record.getNextHop().toBDD(range.getPrefix());
    } else {
      return record.getFactory().zero();
    }
  }

  // find the BDD corresponding to an item that is being tracked symbolically
  private BDD itemToBDD(String item, List<String> items, BDD[] itemsBDDs) {
    int index = items.indexOf(item);
    return itemsBDDs[index];
  }

  // Produce a BDD that is the symbolic representation of the given AsPathSetExpr predicate.
  private BDD matchAsPathSetExpr(TransferParam p, AsPathSetExpr e, BDDRoute other, Context context)
      throws UnsupportedOperationException {
    if (e instanceof NamedAsPathSet) {
      NamedAsPathSet namedAsPathSet = (NamedAsPathSet) e;
      AsPathAccessList accessList =
          context.config().getAsPathAccessLists().get(namedAsPathSet.getName());
      p.debug("Named As Path Set: %s", namedAsPathSet.getName());
      return matchAsPathAccessList(accessList, other);
    }
    // TODO: handle other kinds of AsPathSetExprs
    throw new UnsupportedOperationException(e.toString());
  }

  // Raise an exception if we are about to match on an as-path that has been previously updated
  // along this path; the analysis currently does not handle that situation.
  private void checkForAsPathMatchAfterUpdate(BDDRoute currRoute)
      throws UnsupportedOperationException {
    if (!currRoute.getPrependedASes().isEmpty()) {
      throw new UnsupportedOperationException(
          "Matching on a modified as-path is not currently supported");
    }
  }

  /**
   * Convert an AS-path access list to a boolean formula represented as a BDD.
   *
   * <p>This method takes ownership of BDDs returned by asPathRegexesToBDD and will free them if not
   * returned.
   */
  private BDD matchAsPathAccessList(AsPathAccessList accessList, BDDRoute other) {
    long priorBDDs = _factory.numOutstandingBDDs();
    List<AsPathAccessListLine> lines = new ArrayList<>(accessList.getLines());
    Collections.reverse(lines);
    BDD acc = _factory.zero();
    for (AsPathAccessListLine line : lines) {
      boolean action = (line.getAction() == LineAction.PERMIT);
      // each line's regex is represented as the disjunction of all of the regex's
      // corresponding atomic predicates
      SymbolicAsPathRegex regex = new SymbolicAsPathRegex(line.getRegex());
      BDD regexAPBdd =
          asPathRegexesToBDD(ImmutableSet.of(regex), _asPathRegexAtomicPredicates, other);
      acc = regexAPBdd.iteWith(mkBDD(action), acc);
    }
    assertNoLeaks(priorBDDs, 1);
    return acc;
  }

  /**
   * Converts a route filter list to a boolean expression.
   *
   * <p>This method takes ownership of BDDs returned by {@code symbolicMatcher} and will free them
   * if not returned.
   */
  private BDD matchFilterList(
      TransferParam p,
      RouteFilterList x,
      BDDRoute other,
      BiFunction<BDDRoute, PrefixRange, BDD> symbolicMatcher)
      throws UnsupportedOperationException {
    long priorBDDs = _factory.numOutstandingBDDs();
    BDD acc = _factory.zero();
    LineAction currentAction = LineAction.PERMIT;
    List<BDD> lineBDDsWithCurrentAction = new ArrayList<>();
    for (RouteFilterLine line : Lists.reverse(x.getLines())) {
      if (!line.getIpWildcard().isPrefix()) {
        throw new UnsupportedOperationException(line.getIpWildcard().toString());
      }
      Prefix pfx = line.getIpWildcard().toPrefix();
      SubRange r = line.getLengthRange();
      PrefixRange range = new PrefixRange(pfx, r);
      p.debug("Prefix Range: %s", range);
      p.debug("Action: %s", line.getAction());
      BDD matches = symbolicMatcher.apply(other, range);
      if (line.getAction() != currentAction) {
        acc =
            _factory
                .orAllAndFree(lineBDDsWithCurrentAction)
                .iteWith(mkBDD(currentAction == LineAction.PERMIT), acc);
        // and reset for next step
        lineBDDsWithCurrentAction.clear();
        currentAction = line.getAction();
      }
      lineBDDsWithCurrentAction.add(matches);
    }
    acc =
        _factory
            .orAllAndFree(lineBDDsWithCurrentAction)
            .iteWith(mkBDD(currentAction == LineAction.PERMIT), acc);
    assertNoLeaks(priorBDDs, 1);
    return acc;
  }

  // Returns a function that can convert a prefix range into a BDD that constrains the appropriate
  // part of a route (destination prefix or next-hop IP), depending on the given prefix
  // expression.
  private BiFunction<BDDRoute, PrefixRange, BDD> prefixExprToSymbolicMatcher(PrefixExpr pe)
      throws UnsupportedOperationException {
    if (pe.equals(DestinationNetwork.instance())) {
      return TransferBDD::isRelevantForDestination;
    } else if (pe instanceof IpPrefix) {
      IpPrefix ipp = (IpPrefix) pe;
      if (ipp.getIp().equals(NextHopIp.instance())
          && ipp.getPrefixLength().equals(new LiteralInt(Prefix.MAX_PREFIX_LENGTH))) {
        return TransferBDD::isRelevantForNextHop;
      }
    }
    throw new UnsupportedOperationException(pe.toString());
  }

  /*
   * Converts a prefix set to a boolean expression.
   */
  private BDD matchPrefixSet(TransferParam p, MatchPrefixSet m, BDDRoute other, Context context)
      throws UnsupportedOperationException {
    BiFunction<BDDRoute, PrefixRange, BDD> symbolicMatcher =
        prefixExprToSymbolicMatcher(m.getPrefix());
    PrefixSetExpr e = m.getPrefixSet();
    if (e instanceof ExplicitPrefixSet) {
      ExplicitPrefixSet x = (ExplicitPrefixSet) e;
      List<BDD> rangeBDDs =
          x.getPrefixSpace().getPrefixRanges().stream()
              .map(r -> symbolicMatcher.apply(other, r))
              .toList();
      return _factory.orAllAndFree(rangeBDDs);

    } else if (e instanceof NamedPrefixSet) {
      NamedPrefixSet x = (NamedPrefixSet) e;
      p.debug("Named: %s", x.getName());
      String name = x.getName();
      RouteFilterList fl = context.config().getRouteFilterLists().get(name);
      return matchFilterList(p, fl, other, symbolicMatcher);

    } else {
      throw new UnsupportedOperationException(e.toString());
    }
  }

  // Produce a BDD representing a constraint on the given MutableBDDInteger that enforces the
  // integer equality constraint represented by the given IntComparator and long value
  private BDD matchLongValueComparison(IntComparator comp, long val, MutableBDDInteger bddInt)
      throws UnsupportedOperationException {
    return switch (comp) {
      case EQ -> bddInt.value(val);
      case GE -> bddInt.geq(val);
      case GT -> bddInt.geq(val).andEq(bddInt.value(val).notEq());
      case LE -> bddInt.leq(val);
      case LT -> bddInt.leq(val).andEq(bddInt.value(val).notEq());
    };
  }

  // Produce a BDD representing a constraint on the given MutableBDDInteger that enforces the
  // integer equality constraint represented by the given IntComparator and IntExpr
  private BDD matchIntComparison(IntComparator comp, IntExpr expr, MutableBDDInteger bddInt)
      throws UnsupportedOperationException {
    if (!(expr instanceof LiteralInt)) {
      throw new UnsupportedOperationException(expr.toString());
    }
    int val = ((LiteralInt) expr).getValue();
    return matchLongValueComparison(comp, val, bddInt);
  }

  // Produce a BDD representing a constraint on the given MutableBDDInteger that enforces the
  // integer (in)equality constraint represented by the given IntComparator and LongExpr
  private BDD matchLongComparison(IntComparator comp, LongExpr expr, MutableBDDInteger bddInt)
      throws UnsupportedOperationException {
    if (!(expr instanceof LiteralLong)) {
      throw new UnsupportedOperationException(expr.toString());
    }
    long val = ((LiteralLong) expr).getValue();
    return matchLongValueComparison(comp, val, bddInt);
  }

  /*
   * Return a BDD from a boolean
   */
  BDD mkBDD(boolean b) {
    return b ? _factory.one() : _factory.zero();
  }

  private void setNextHop(NextHopExpr expr, BDDRoute route) throws UnsupportedOperationException {
    // record the fact that the next-hop has been explicitly set by the route-map
    route.setNextHopSet(true);
    if (expr instanceof DiscardNextHop) {
      route.setNextHopType(BDDRoute.NextHopType.DISCARDED);
    } else if (expr instanceof IpNextHop && ((IpNextHop) expr).getIps().size() == 1) {
      route.setNextHopType(BDDRoute.NextHopType.IP);
      List<Ip> ips = ((IpNextHop) expr).getIps();
      Ip ip = ips.get(0);
      route.setNextHop(MutableBDDInteger.makeFromValue(_factory, 32, ip.asLong()));
    } else if (expr instanceof BgpPeerAddressNextHop) {
      route.setNextHopType(BDDRoute.NextHopType.BGP_PEER_ADDRESS);
    } else if (expr instanceof SelfNextHop) {
      route.setNextHopType(BDDRoute.NextHopType.SELF);
    } else {
      throw new UnsupportedOperationException(expr.toString());
    }
  }

  private void prependASPath(AsPathListExpr expr, BDDRoute route)
      throws UnsupportedOperationException {
    // currently we only support prepending AS literals
    if (!(expr instanceof LiteralAsList)) {
      throw new UnsupportedOperationException(expr.toString());
    }
    List<Long> prependedASes = new ArrayList<>();
    LiteralAsList asList = (LiteralAsList) expr;
    for (AsExpr ase : asList.getList()) {
      if (!(ase instanceof ExplicitAs)) {
        throw new UnsupportedOperationException(ase.toString());
      }
      prependedASes.add(((ExplicitAs) ase).getAs());
    }
    prependedASes.addAll(route.getPrependedASes());
    route.setPrependedASes(prependedASes);
  }

  // Update community atomic predicates based on the given CommunityAPDispositions object
  private void updateCommunities(
      CommunityAPDispositions dispositions, TransferBDDState state, Context context) {
    BDD[] currAPBDDs = routeForReading(state, context).getCommunityAtomicPredicates();
    writeRouteAttribute(
        state,
        r -> {
          BDD[] commAPBDDs = r.getCommunityAtomicPredicates();
          // Initialize commAPBdds with input communities, then override forced trues and falses.
          System.arraycopy(currAPBDDs, 0, commAPBDDs, 0, currAPBDDs.length);
          BDD one = _factory.one();
          BDD zero = _factory.zero();
          dispositions.getMustExist().stream().forEach(i -> commAPBDDs[i] = one);
          dispositions.getMustNotExist().stream().forEach(i -> commAPBDDs[i] = zero);
        });
  }

  /**
   * A BDD representing the conditions under which the current statement is not reachable, because
   * we've already returned or exited before getting there.
   *
   * @param currState the current state of the analysis
   * @return the bdd
   */
  private static boolean unreachable(TransferResult currState) {
    return currState.getReturnAssignedValue() || currState.getExitAssignedValue();
  }

  // If the analysis encounters a routing policy feature that is not currently supported, we ignore
  // it and keep going, but we also log a warning and mark the output BDDRoute as having reached an
  // unsupported feature.
  private void unsupported(
      UnsupportedOperationException e, TransferResult result, Context context) {
    Level level = _unsupportedAlreadyWarned.add(e.getMessage()) ? Level.WARN : Level.DEBUG;
    LOGGER.log(
        level,
        "Unsupported statement in routing policy {} of node {}: {}",
        context.policy(),
        context.hostname(),
        e.getMessage());
    result.getReturnValue().getOutputRoute().setUnsupported(true);
  }

  /*
   * Create the result of reaching a suppress or unsuppress statement.
   */
  private TransferResult suppressedValue(TransferResult r, boolean val) {
    return r.setSuppressedValue(val);
  }

  /*
   * Create the result of reaching a return statement, returning with the given value.
   */
  private TransferResult returnValue(TransferResult r, boolean accepted) {
    return r.setReturnValue(r.getReturnValue().setAccepted(accepted))
        .setReturnAssignedValue(true)
        .setFallthroughValue(false);
  }

  /*
   * Create the result of reaching an exit statement, returning with the given value.
   */
  private TransferResult exitValue(TransferResult r, boolean accepted) {
    return r.setReturnValue(r.getReturnValue().setAccepted(accepted))
        .setExitAssignedValue(true)
        .setFallthroughValue(false);
  }

  // Returns the appropriate route to use for reading route attributes.
  private BDDRoute routeForReading(TransferBDDState state, Context context) {
    if (context.useOutputAttributes()) {
      return state.getTransferResult().getReturnValue().getOutputRoute();
    } else if (state.getTransferParam().getReadIntermediateBgpAtttributes()) {
      return state.getTransferResult().getIntermediateBgpAttributes();
    } else {
      return _originalRoute;
    }
  }

  // Applies a transformation to a route attribute. The output route is updated. Additionally, the
  // intermediate route attributes are updated if the corresponding flag is set.
  private void writeRouteAttribute(TransferBDDState state, Consumer<BDDRoute> writeFun) {
    // update the output route
    writeFun.accept(state.getTransferResult().getReturnValue().getOutputRoute());
    // also update the intermediate route if the flag is set
    if (state.getTransferParam().getWriteIntermediateBgpAttributes()) {
      writeFun.accept(state.getTransferResult().getIntermediateBgpAttributes());
    }
  }

  /**
   * The results of symbolic route-map analysis: one {@link
   * org.batfish.minesweeper.bdd.TransferReturn} per execution path through the given route map. The
   * list of paths is unordered, and by construction each path is unique, as each path has a unique
   * condition under which it is taken (the BDD in the TransferResult). The particular statements
   * executed along a given path are not included in this representation but can be reconstructed by
   * simulating one route that takes this path using {@link
   * org.batfish.question.testroutepolicies.TestRoutePoliciesQuestion}.
   *
   * <p>If {@code retainAllPaths} is {@code false}, then paths with the same output behavior (aka,
   * {@link TransferResult} is equivalent except for the input conditions under which the path is
   * reached) are combined (by unioning those input conditions).
   */
  public List<TransferReturn> computePaths(RoutingPolicy policy, boolean retainAllPaths) {
    return computePaths(policy.getStatements(), Context.forPolicy(policy), retainAllPaths);
  }

  /**
   * Symbolic analysis of a list of route-policy statements. Returns one {@link TransferReturn} per
   * path through the list of statements. The list of paths is unordered, and by construction each
   * path is unique, as each path has a unique condition under which it is taken (the BDD in the
   * {@link TransferReturn#getInputConstraints()}).
   *
   * <p>The particular statements executed along a given path are not included in this
   * representation but can be reconstructed by simulating one route that takes this path using
   * {@link org.batfish.question.testroutepolicies.TestRoutePoliciesQuestion}.
   *
   * <p>If {@code retainAllPaths} is {@code false}, then paths with the same output behavior (aka,
   * {@link TransferResult} is equivalent except for the input conditions under which the path is
   * reached) are combined (by unioning those input conditions).
   */
  public List<TransferReturn> computePaths(
      List<Statement> statements, Context context, boolean retainAllPaths) {
    BDDRoute o = new BDDRoute(_factory, _configAtomicPredicates);
    TransferParam p = new TransferParam(false);
    TransferBDDState state = new TransferBDDState(p, new TransferResult(o));
    return computePaths(state, statements, context, retainAllPaths).stream()
        .map(TransferResult::getReturnValue)
        .collect(ImmutableList.toImmutableList());
  }

  public Map<CommunityVar, Set<Integer>> getCommunityAtomicPredicates() {
    return _communityAtomicPredicates;
  }

  public BDDFactory getFactory() {
    return _factory;
  }

  public ConfigAtomicPredicates getConfigAtomicPredicates() {
    return _configAtomicPredicates;
  }

  /** A cache for {@link CommunitySetMatchExprToBDD#toCommunitySetConstraint(BDD, Arg)}. */
  public @Nonnull Map<BDD, List<Integer>> getCommunitySetConstraintCache() {
    return _communitySetConstraintCache;
  }

  public BDDRoute getOriginalRoute() {
    return _originalRoute;
  }

  /**
   * Takes a list of symbolic policy intermediate states, results, etc. and combines any that have
   * the same state under different inputs (by unioning together the inputs).
   *
   * <p>Using combination can result in exponential state space reduction in practice, for example
   * when a community is added to a route advertisement for 5 non-exclusive reasons. There are 32
   * possible inputs, but only 2 distinct outputs (at least one of the 5 is matched, or none of
   * them).
   *
   * <p>TODO: consider whether this can speed up even the analyses that require all paths. For
   * example, using a hybrid approach that refines distinct paths.
   */
  private static <T> List<T> combineSymbolicResults(
      List<T> states,
      Function<T, BDD> getInputConstraint,
      BiFunction<BDD, T, T> replaceInputConstraint) {
    if (states.size() < 2) {
      return states;
    }
    BDDFactory factory = getInputConstraint.apply(states.get(0)).getFactory();
    Map<T, Collection<BDD>> grouped =
        states.stream()
            .collect(
                ImmutableListMultimap.toImmutableListMultimap(
                    s -> replaceInputConstraint.apply(factory.one(), s), getInputConstraint))
            .asMap();
    if (grouped.size() == states.size()) {
      return states;
    }
    LOGGER.debug("Condensed {} values into {} distinct groups", states.size(), grouped.size());
    ImmutableList.Builder<T> ret = ImmutableList.builderWithExpectedSize(grouped.size());
    grouped.forEach(
        (state, inputConstraints) ->
            ret.add(replaceInputConstraint.apply(factory.orAll(inputConstraints), state)));
    return ret.build();
  }

  private static List<TransferBDDState> combineStates(List<TransferBDDState> states) {
    return combineSymbolicResults(
        states,
        s -> s.getTransferResult().getReturnValue().getInputConstraints(),
        (b, s) ->
            new TransferBDDState(s.getTransferParam(), s.getTransferResult().setReturnValueBDD(b)));
  }

  private static List<TransferResult> combineTransferResults(List<TransferResult> input) {
    return combineSymbolicResults(
        input,
        tr -> tr.getReturnValue().getInputConstraints(),
        (bdd, tr) -> tr.setReturnValueBDD(bdd));
  }
}
