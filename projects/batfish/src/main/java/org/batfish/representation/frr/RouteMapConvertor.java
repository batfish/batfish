package org.batfish.representation.frr;

import static org.batfish.representation.frr.FrrConversions.computeRouteMapEntryName;
import static org.batfish.representation.frr.FrrStructureType.ROUTE_MAP_ENTRY;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.vendor.VendorStructureId;

class RouteMapConvertor {
  private Configuration _c;
  private FrrConfiguration _vc;
  private RouteMap _routeMap;
  private String _filename;
  private Warnings _w;

  private Map<Integer, Integer> _nextSeqMap;
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

  RouteMapConvertor(
      Configuration c, FrrConfiguration vc, RouteMap routeMap, String filename, Warnings w) {
    _c = c;
    _vc = vc;
    _filename = filename;
    _w = w;
    _routeMap = routeMap;
    _nextSeqMap = computeNoMatchNextBySeq(routeMap);
    _continueTargets = computeContinueTargets(routeMap);
  }

  private static @Nonnull Statement callInContext(String routingPolicyName) {
    return new If(
        new CallExpr(routingPolicyName),
        ImmutableList.of(ROUTE_MAP_PERMIT_STATEMENT),
        ImmutableList.of(ROUTE_MAP_DENY_STATEMENT));
  }

  @VisibleForTesting
  RoutingPolicy toRouteMap() {
    String routeMapName = _routeMap.getName();
    String currentRoutingPolicyName = _routeMap.getName();

    ImmutableList.Builder<Statement> currentRoutingPolicyStatements = ImmutableList.builder();
    currentRoutingPolicyStatements.add(
        Statements.SetLocalDefaultActionReject.toStaticStatement(),
        Statements.SetReadIntermediateBgpAttributes.toStaticStatement(),
        Statements.SetWriteIntermediateBgpAttributes.toStaticStatement());

    // Build the top-level RoutingPolicy that corresponds to the route-map. All it does is call
    // the first interval and return its result in a context-appropriate way.
    int firstSequence = _routeMap.getEntries().firstKey();
    String firstSequenceRoutingPolicyName = computeRoutingPolicyName(routeMapName, firstSequence);
    RoutingPolicy.builder()
        .setName(routeMapName)
        .setOwner(_c)
        .setStatements(ImmutableList.of(callInContext(firstSequenceRoutingPolicyName)))
        .build();

    currentRoutingPolicyName = firstSequenceRoutingPolicyName;

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

    currentRoutingPolicyStatements.add(Statements.ReturnLocalDefaultAction.toStaticStatement());
    RoutingPolicy.builder()
        .setName(currentRoutingPolicyName)
        .setOwner(_c)
        .setStatements(currentRoutingPolicyStatements.build())
        .build();
    return _c.getRoutingPolicies().get(_routeMap.getName());
  }

  private @Nullable Statement convertCallStatement(RouteMapEntry entry) {
    RouteMapCall callStmt = entry.getCall();
    if (callStmt == null || !_vc.getRouteMaps().containsKey(callStmt.getRouteMapName())) {
      return null;
    }

    return new If(
        new CallExpr(callStmt.getRouteMapName()),
        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
        ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
  }

  private List<Statement> convertActionStatement(RouteMapEntry entry) {
    RouteMapContinue continueTarget = entry.getContinue();
    List<Statement> finalTrueStatements = new ArrayList<>();
    LineAction action = entry.getAction();
    if (continueTarget != null) {
      if (action == LineAction.PERMIT) {
        finalTrueStatements.add(Statements.SetLocalDefaultActionAccept.toStaticStatement());
      } else {
        assert action == LineAction.DENY;
        finalTrueStatements.add(Statements.SetLocalDefaultActionReject.toStaticStatement());
      }

      int continueNext =
          Optional.ofNullable(continueTarget.getNext())
              .orElse(_nextSeqMap.getOrDefault(entry.getNumber(), -1));
      if (_continueTargets.contains(continueNext)) {
        finalTrueStatements.add(call(computeRoutingPolicyName(_routeMap.getName(), continueNext)));
      } else {
        // invalid continue target, so just deny
        // TODO: verify actual behavior
        finalTrueStatements.add(Statements.ReturnFalse.toStaticStatement());
      }
    } else {
      // No continue: on match, return the action.
      if (action == LineAction.PERMIT) {
        finalTrueStatements.add(Statements.ReturnTrue.toStaticStatement());
      } else {
        assert action == LineAction.DENY;
        finalTrueStatements.add(Statements.ReturnFalse.toStaticStatement());
      }
    }
    return finalTrueStatements;
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
                Stream.concat(
                    Stream.of(convertCallStatement(entry)), convertActionStatement(entry).stream()))
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());

    // final action if not matched
    Integer noMatchNext = _nextSeqMap.get(entry.getNumber());
    List<Statement> noMatchStatements =
        noMatchNext != null && _continueTargets.contains(noMatchNext)
            ? ImmutableList.of(call(computeRoutingPolicyName(_routeMap.getName(), noMatchNext)))
            : ImmutableList.of();
    return new If(
        new Conjunction(matchConjuncts),
        ImmutableList.of(
            toTraceableStatement(
                trueStatements, entry.getNumber(), _routeMap.getName(), _filename)),
        noMatchStatements);
  }

  static TraceableStatement toTraceableStatement(
      List<Statement> statements, int sequence, String mapName, String filename) {
    return new TraceableStatement(
        TraceElement.builder()
            .add("Matched ")
            .add(
                String.format("route-map %s entry %d", mapName, sequence),
                new VendorStructureId(
                    filename,
                    ROUTE_MAP_ENTRY.getDescription(),
                    computeRouteMapEntryName(mapName, sequence)))
            .build(),
        statements);
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

  private Set<Integer> computeContinueTargets(RouteMap routeMap) {
    return routeMap.getEntries().values().stream()
        .filter(entry -> entry.getContinue() != null)
        .map(
            entry ->
                Optional.ofNullable(entry.getContinue().getNext())
                    .orElse(_nextSeqMap.getOrDefault(entry.getNumber(), -1)))
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
