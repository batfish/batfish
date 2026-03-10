package org.batfish.datamodel.routing_policy;

import static java.util.Collections.singletonList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Utility functions for generating {@link RoutingPolicy routing policies}. */
public final class Common {

  /**
   * A Java regex translation for the underscore character that can appear in regexes in routing
   * policies. This is a default translation that works for many vendors, but some vendors may treat
   * underscore differently and so may need their own translation.
   */
  public static final String DEFAULT_UNDERSCORE_REPLACEMENT = "(,|\\\\{|\\\\}|^|\\$| )";

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
   * If {@code summaryOnly} is {@code false}, returns {@code null}. Else, returns the name of a
   * policy that accepts (suppresses) all routes.
   */
  public static @Nullable String generateSuppressionPolicy(boolean summaryOnly, Configuration c) {
    if (!summaryOnly) {
      return null;
    }
    if (c.getRoutingPolicies().containsKey(SUMMARY_ONLY_SUPPRESSION_POLICY_NAME)) {
      return SUMMARY_ONLY_SUPPRESSION_POLICY_NAME;
    }
    RoutingPolicy.builder()
        .setName(SUMMARY_ONLY_SUPPRESSION_POLICY_NAME)
        .setOwner(c)
        .addStatement(Statements.ExitAccept.toStaticStatement())
        .build();
    return SUMMARY_ONLY_SUPPRESSION_POLICY_NAME;
  }

  public static String generatedBgpGenerationPolicyName(
      boolean ipv4, String vrfName, String prefix) {
    return String.format("~AGGREGATE_ROUTE%s_GEN:%s:%s~", ipv4 ? "" : "6", vrfName, prefix);
  }

  /**
   * If the given {@link Configuration} does not already have a deny-all BGP redistribution policy,
   * creates and adds one. Returns the policy name for convenience.
   */
  public static @Nonnull String initDenyAllBgpRedistributionPolicy(Configuration c) {
    if (!c.getRoutingPolicies().containsKey(DENY_ALL_BGP_REDISTRIBUTION_POLICY_NAME)) {
      RoutingPolicy.builder()
          .setName(DENY_ALL_BGP_REDISTRIBUTION_POLICY_NAME)
          .setOwner(c)
          .addStatement(Statements.ExitReject.toStaticStatement())
          .build();
    }
    return DENY_ALL_BGP_REDISTRIBUTION_POLICY_NAME;
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
  public static @Nullable If suppressSummarizedPrefixes(
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

  public static MatchPrefixSet matchDefaultRoute() {
    return MATCH_DEFAULT_ROUTE;
  }

  @VisibleForTesting
  public static String SUMMARY_ONLY_SUPPRESSION_POLICY_NAME = "~suppress~rp~summary-only~";

  private static String DENY_ALL_BGP_REDISTRIBUTION_POLICY_NAME =
      "~deny~all~bgp~redistribution~policy~";

  // Private implementation details
  private Common() {} // prevent instantiation of utility class

  private static final MatchPrefixSet MATCH_DEFAULT_ROUTE = makeMatchDefaultRouteV4();

  private static MatchPrefixSet makeMatchDefaultRouteV4() {
    MatchPrefixSet ret =
        new MatchPrefixSet(
            DestinationNetwork.instance(),
            new ExplicitPrefixSet(
                new PrefixSpace(new PrefixRange(Prefix.ZERO, SubRange.singleton(0)))));
    ret.setComment("match default route");
    return ret;
  }
}
