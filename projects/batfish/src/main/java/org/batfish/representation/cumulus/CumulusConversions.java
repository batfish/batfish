package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Utilities that convert Cumulus-specific representations to vendor-independent model. */
@ParametersAreNonnullByDefault
public final class CumulusConversions {
  private static final int AGGREGATE_ROUTE_ADMIN_COST = 200; // TODO verify this

  static String computeBgpGenerationPolicyName(boolean ipv4, String vrfName, String prefix) {
    return String.format("~AGGREGATE_ROUTE%s_GEN:%s:%s~", ipv4 ? "" : "6", vrfName, prefix);
  }

  static String computeMatchSuppressedSummaryOnlyPolicyName(String vrfName) {
    return String.format("~MATCH_SUPPRESSED_SUMMARY_ONLY:%s~", vrfName);
  }

  private static WithEnvironmentExpr bgpRedistributeWithEnvironmentExpr(
      BooleanExpr expr, OriginType originType) {
    WithEnvironmentExpr we = new WithEnvironmentExpr();
    we.setExpr(expr);
    we.setPreStatements(
        ImmutableList.of(Statements.SetWriteIntermediateBgpAttributes.toStaticStatement()));
    we.setPostStatements(
        ImmutableList.of(Statements.UnsetWriteIntermediateBgpAttributes.toStaticStatement()));
    we.setPostTrueStatements(
        ImmutableList.of(
            Statements.SetReadIntermediateBgpAttributes.toStaticStatement(),
            new SetOrigin(new LiteralOrigin(originType, null))));
    return we;
  }

  static BooleanExpr generateExportAggregateConditions(
      Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> aggregateNetworks) {
    return new Disjunction(
        aggregateNetworks.entrySet().stream()
            .map(
                entry -> {
                  Prefix prefix = entry.getKey();

                  // Conditions to generate this route
                  List<BooleanExpr> exportAggregateConjuncts = new ArrayList<>();
                  exportAggregateConjuncts.add(
                      new MatchPrefixSet(
                          DestinationNetwork.instance(),
                          new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(prefix)))));
                  exportAggregateConjuncts.add(new MatchProtocol(RoutingProtocol.AGGREGATE));

                  // TODO consider attribute map
                  BooleanExpr weInterior = BooleanExprs.TRUE;
                  exportAggregateConjuncts.add(
                      bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.IGP));

                  // Do export a generated aggregate.
                  return new Conjunction(exportAggregateConjuncts);
                })
            .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Creates generated routes and route generation policies for aggregate routes for the input vrf.
   */
  static void generateGeneratedRoutes(
      Configuration c,
      org.batfish.datamodel.Vrf vrf,
      Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> aggregateNetworks) {
    aggregateNetworks.forEach(
        (prefix, agg) -> {
          generateGenerationPolicy(c, vrf.getName(), prefix);

          // TODO generate attribute policy
          GeneratedRoute gr =
              GeneratedRoute.builder()
                  .setNetwork(prefix)
                  .setAdmin(AGGREGATE_ROUTE_ADMIN_COST)
                  .setGenerationPolicy(
                      computeBgpGenerationPolicyName(true, vrf.getName(), prefix.toString()))
                  .setDiscard(true)
                  .build();

          vrf.getGeneratedRoutes().add(gr);
        });
  }

  /**
   * Creates a generation policy for the aggregate network with the given {@link Prefix}. The
   * generation policy matches any route with a destination more specific than {@code prefix}.
   *
   * @param c {@link Configuration} in which to create the generation policy
   * @param vrfName Name of VRF in which the aggregate network exists
   * @param prefix The aggregate network prefix
   */
  static void generateGenerationPolicy(Configuration c, String vrfName, Prefix prefix) {
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(computeBgpGenerationPolicyName(true, vrfName, prefix.toString()))
        .addStatement(
            new If(
                // Match routes with destination networks more specific than prefix.
                new MatchPrefixSet(
                    DestinationNetwork.instance(),
                    new ExplicitPrefixSet(new PrefixSpace(PrefixRange.moreSpecificThan(prefix)))),
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement())))
        .build();
  }

  /**
   * Generates and returns a {@link Statement} that suppresses routes that are summarized by the
   * given set of {@link Prefix prefixes} configured as {@code summary-only}.
   *
   * <p>Returns {@code null} if {@code prefixesToSuppress} has no entries.
   *
   * <p>If any Batfish-generated structures are generated, does the bookkeeping in the provided
   * {@link Configuration} to ensure they are available and tracked.
   */
  @Nullable
  static If suppressSummarizedPrefixes(
      Configuration c, String vrfName, Stream<Prefix> summaryOnlyPrefixes) {
    Iterator<Prefix> prefixesToSuppress = summaryOnlyPrefixes.iterator();
    if (!prefixesToSuppress.hasNext()) {
      return null;
    }
    // Create a RouteFilterList that matches any network longer than a prefix marked summary only.
    RouteFilterList matchLonger =
        new RouteFilterList(computeMatchSuppressedSummaryOnlyPolicyName(vrfName));
    prefixesToSuppress.forEachRemaining(
        p ->
            matchLonger.addLine(
                new RouteFilterLine(LineAction.PERMIT, PrefixRange.moreSpecificThan(p))));
    // Bookkeeping: record that we created this RouteFilterList to match longer networks.
    c.getRouteFilterLists().put(matchLonger.getName(), matchLonger);

    return new If(
        "Suppress more specific networks for summary-only aggregate-address networks",
        new MatchPrefixSet(
            DestinationNetwork.instance(), new NamedPrefixSet(matchLonger.getName())),
        ImmutableList.of(Statements.Suppress.toStaticStatement()),
        ImmutableList.of());
  }
}
