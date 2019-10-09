package org.batfish.representation.cumulus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

class RouteMapConvertor {
  private Configuration _c;
  private CumulusNcluConfiguration _vc;
  private RouteMap _routeMap;
  private Warnings _w;

  private Map<Integer, Integer> _noMatchNextBySeq;
  private Set<Integer> _continueTargets;

  private static final Statement ROUTE_MAP_PERMIT_STATEMENT =
      new If(
          BooleanExprs.CALL_EXPR_CONTEXT,
          ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
          ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
  private static final Statement ROUTE_MAP_DENY_STATEMENT =
      new If(
          BooleanExprs.CALL_EXPR_CONTEXT,
          ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
          ImmutableList.of(Statements.ExitReject.toStaticStatement()));

  RouteMapConvertor(Configuration c, CumulusNcluConfiguration vc, RouteMap routeMap, Warnings w) {
    _c = c;
    _vc = vc;
    _w = w;
    _routeMap = routeMap;
    _noMatchNextBySeq = computeNoMatchNextBySeq(routeMap);
    _continueTargets = computeContinueTargets(routeMap);
  }

  @VisibleForTesting
  RoutingPolicy toRouteMap() {
    String routeMapName = _routeMap.getName();
    String currentRoutingPolicyName = _routeMap.getName();

    ImmutableList.Builder<Statement> currentRoutingPolicyStatements = ImmutableList.builder();
    for (RouteMapEntry currentEntry : _routeMap.getEntries().values()) {
      int currentSequence = currentEntry.getNumber();
      if (_continueTargets.contains(currentSequence)) {
        // finalize the routing policy consisting of queued statements up to this point
        RoutingPolicy.builder()
            .setName(currentRoutingPolicyName)
            .setOwner(_c)
            .setStatements(currentRoutingPolicyStatements.build())
            .build();
        // reset statement queue
        currentRoutingPolicyStatements = ImmutableList.builder();
        // generate name for policy that will contain subsequent statements
        currentRoutingPolicyName = computeRoutingPolicyName(routeMapName, currentSequence);
      } // or else undefined reference
      currentRoutingPolicyStatements.add(toStatement(currentEntry));
    }
    // finalize last routing policy
    // TODO: do default action, which changes when continuing from a permit
    currentRoutingPolicyStatements.add(ROUTE_MAP_DENY_STATEMENT);
    RoutingPolicy.builder()
        .setName(currentRoutingPolicyName)
        .setOwner(_c)
        .setStatements(currentRoutingPolicyStatements.build())
        .build();
    return _c.getRoutingPolicies().get(_routeMap.getName());
  }

  @Nullable
  private Statement convertCallStatement(RouteMapEntry entry) {
    RouteMapCall callStmt = entry.getCall();
    if (callStmt == null || !_vc.getRouteMaps().containsKey(callStmt.getRouteMapName())) {
      return null;
    }

    return new If(
        new CallExpr(callStmt.getRouteMapName()),
        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
        ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
  }

  private Statement convertActionStatement(RouteMapEntry entry) {
    Integer continueTarget = entry.getContinue();
    Statement finalTrueStatement;
    if (continueTarget != null) {
      if (_continueTargets.contains(entry.getContinue())) {
        // TODO: verify correct semantics: possibly, should add two statements in this case; first
        // should set default _action to permit/deny if this is a permit/deny entry, and second
        // should call policy for next entry.
        finalTrueStatement = call(computeRoutingPolicyName(_routeMap.getName(), continueTarget));
      } else {
        // invalid continue target, so just deny
        // TODO: verify actual behavior
        finalTrueStatement = ROUTE_MAP_DENY_STATEMENT;
      }
    } else if (entry.getAction() == LineAction.PERMIT) {
      finalTrueStatement = ROUTE_MAP_PERMIT_STATEMENT;
    } else {
      assert entry.getAction() == LineAction.DENY;
      finalTrueStatement = ROUTE_MAP_DENY_STATEMENT;
    }
    return finalTrueStatement;
  }

  @VisibleForTesting
  @Nonnull
  Statement toStatement(RouteMapEntry entry) {
    List<BooleanExpr> matchConjuncts =
        entry
            .getMatches()
            .map(m -> m.toBooleanExpr(_c, _vc, _w))
            .collect(ImmutableList.toImmutableList());

    List<Statement> trueStatements =
        Stream.concat(
                entry.getSets().flatMap(m -> m.toStatements(_c, _vc, _w)),
                // Call statement is executed after all set statements.
                // http://docs.frrouting.org/en/latest/routemap.html#route-maps
                Stream.of(convertCallStatement(entry), convertActionStatement(entry)))
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());

    // final action if not matched
    Integer noMatchNext = _noMatchNextBySeq.get(entry.getNumber());
    List<Statement> noMatchStatements =
        noMatchNext != null && _continueTargets.contains(noMatchNext)
            ? ImmutableList.of(call(computeRoutingPolicyName(_routeMap.getName(), noMatchNext)))
            : ImmutableList.of();
    return new If(new Conjunction(matchConjuncts), trueStatements, noMatchStatements);
  }

  private static Map<Integer, Integer> computeNoMatchNextBySeq(RouteMap routeMap) {
    ImmutableMap.Builder<Integer, Integer> noMatchNextBySeqBuilder = ImmutableMap.builder();
    RouteMapEntry lastEntry = null;
    for (RouteMapEntry currentEntry : routeMap.getEntries().values()) {
      if (lastEntry != null) {
        int lastSequenceNumber = lastEntry.getNumber();
        noMatchNextBySeqBuilder.put(lastSequenceNumber, currentEntry.getNumber());
      }
      lastEntry = currentEntry;
    }

    return noMatchNextBySeqBuilder.build();
  }

  private static Set<Integer> computeContinueTargets(RouteMap routeMap) {
    return routeMap.getEntries().values().stream()
        .map(RouteMapEntry::getContinue)
        .filter(Objects::nonNull)
        .filter(routeMap.getEntries().keySet()::contains)
        .collect(ImmutableSet.toImmutableSet());
  }

  private static @Nonnull Statement call(String routingPolicyName) {
    return new If(
        new CallExpr(routingPolicyName),
        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
        ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
  }

  private static @Nonnull String computeRoutingPolicyName(String routeMapName, int sequence) {
    return String.format("~%s~SEQ:%d~", routeMapName, sequence);
  }
}
