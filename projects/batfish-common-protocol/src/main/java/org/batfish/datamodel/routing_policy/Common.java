package org.batfish.datamodel.routing_policy;

import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Prefix6Range;
import org.batfish.datamodel.Prefix6Space;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork6;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Utility functions for generating {@link RoutingPolicy routing policies}. */
public final class Common {

  private Common() {} // prevent instantiation of utility class

  /**
   * Creates a generation policy for the aggregate network with the given {@link Prefix}. The
   * generation policy matches any route with a destination more specific than {@code prefix}.
   *
   * @param c {@link Configuration} in which to create the generation policy
   * @param vrfName Name of VRF in which the aggregate network exists
   * @param prefix The aggregate network prefix
   */
  public static RoutingPolicy generateGenerationPolicy(
      Configuration c, String vrfName, Prefix prefix) {
    return RoutingPolicy.builder()
        .setOwner(c)
        .setName(generatedBgpGenerationPolicyName(true, vrfName, prefix.toString()))
        .addStatement(
            new If(
                // Match routes with destination networks more specific than prefix.
                new MatchPrefixSet(
                    DestinationNetwork.instance(),
                    new ExplicitPrefixSet(new PrefixSpace(PrefixRange.moreSpecificThan(prefix)))),
                singletonList(Statements.ReturnTrue.toStaticStatement())))
        .build();
  }

  /**
   * Creates a generation policy for the aggregate network with the given {@link Prefix6}. The
   * generation policy matches any route with a destination more specific than {@code prefix}.
   *
   * @param c {@link Configuration} in which to create the generation policy
   * @param vrfName Name of VRF in which the aggregate network exists
   * @param prefix The aggregate network prefix
   */
  public static RoutingPolicy generateGenerationPolicy(
      Configuration c, String vrfName, Prefix6 prefix) {
    SubRange prefixRange = new SubRange(prefix.getPrefixLength() + 1, Prefix6.MAX_PREFIX_LENGTH);
    return RoutingPolicy.builder()
        .setOwner(c)
        .setName(generatedBgpGenerationPolicyName(false, vrfName, prefix.toString()))
        .addStatement(
            new If(
                new MatchPrefix6Set(
                    new DestinationNetwork6(),
                    new ExplicitPrefix6Set(
                        new Prefix6Space(
                            Collections.singleton(new Prefix6Range(prefix, prefixRange))))),
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement())))
        .build();
  }

  public static String generatedBgpGenerationPolicyName(
      boolean ipv4, String vrfName, String prefix) {
    return String.format("~AGGREGATE_ROUTE%s_GEN:%s:%s~", ipv4 ? "" : "6", vrfName, prefix);
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
  public static If suppressSummarizedPrefixes(
      Configuration c, String vrfName, Stream<Prefix> summaryOnlyPrefixes) {
    Iterator<Prefix> prefixesToSuppress = summaryOnlyPrefixes.iterator();
    if (!prefixesToSuppress.hasNext()) {
      return null;
    }
    // Create a RouteFilterList that matches any network longer than a prefix marked summary only.
    RouteFilterList matchLonger =
        new RouteFilterList("~MATCH_SUPPRESSED_SUMMARY_ONLY:" + vrfName + "~");
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
