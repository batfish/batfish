package org.batfish.representation.fortios;

import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Helper functions for generating VI BGP structures for {@link FortiosConfiguration}. */
public final class FortiosBgpConversions {
  private static final String BGP_COMMON_EXPORT_POLICY_NAME = "~BGP_COMMON_EXPORT_POLICY~";
  private static final BooleanExpr MATCH_BGP =
      new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP);
  private static final BooleanExpr MATCH_NOT_BGP_OR_AGGREGATE =
      new Not(
          new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP, RoutingProtocol.AGGREGATE));
  private static final BooleanExpr REDIST_WITH_ENVIRONMENT_ORIGIN_TYPE_IGP =
      bgpRedistributeWithEnvironmentExpr(BooleanExprs.TRUE, OriginType.IGP);

  /**
   * Infer the interface that the given {@link BgpNeighbor} will use as its update source. If none
   * can be inferred, returns an empty optional.
   */
  private static Optional<Interface> getUpdateSource(
      BgpNeighbor neighbor, Configuration c, Warnings w) {
    String updateSource = neighbor.getUpdateSource();
    if (updateSource != null) {
      Interface viUpdateSourceIface = c.getAllInterfaces().get(updateSource);
      if (viUpdateSourceIface == null) {
        // Conversion issue with the update-source interface. Better to ignore the neighbor than try
        // to infer a different update-source.
        return Optional.empty();
      } else if (!viUpdateSourceIface.getActive()) {
        // TODO Check behavior if BGP neighbor's configured update-source is an inactive interface
        w.redFlagf(
            "BGP neighbor %s has an inactive update-source interface %s. Attempting to infer"
                + " another update-source for this neighbor",
            neighbor.getIp(), updateSource);
      } else {
        // Configured update-source is viable
        return Optional.of(viUpdateSourceIface);
      }
    }

    // Either no update-source is configured or the configured update-source is inactive.
    // Infer an interface based on iface address.
    return c.getActiveInterfaces().values().stream()
        .filter(
            iface ->
                iface.getConcreteAddress() != null
                    && iface.getConcreteAddress().getPrefix().containsIp(neighbor.getIp()))
        .findFirst();
  }

  /** Returns the VRFs in which the given {@link BgpProcess} is active. */
  public static Set<String> getVrfs(BgpProcess bgpProcess, Configuration c, Warnings w) {
    return bgpProcess.getNeighbors().values().stream()
        .map(neighbor -> getUpdateSource(neighbor, c, w))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Interface::getVrfName)
        .collect(ImmutableSet.toImmutableSet());
  }

  public static void convertBgp(BgpProcess bgpProcess, Configuration c, Warnings w) {
    long as = bgpProcess.getAsEffective();
    if (as == 0L) {
      // this is the standard way to disable BGP in FortiOS
      return;
    } else if (as == 65535L || as == 4294967295L) {
      w.redFlagf("Ignoring BGP process: AS %s is proscribed by RFC 7300", as);
      return;
    }
    // TODO Infer router-id if not explicitly configured
    Ip routerId = bgpProcess.getRouterId();
    if (routerId == null) {
      w.redFlag("Ignoring BGP process: No router ID configured");
      return;
    }

    List<PrefixSpace> originatedSpaces =
        generateCommonBgpExportPolicyAndGetOriginatedSpaces(bgpProcess, c);

    Map<Ip, Interface> updateSources = new HashMap<>();
    Map<String, Set<Ip>> neighborsByVrf = new HashMap<>();
    for (BgpNeighbor neighbor : bgpProcess.getNeighbors().values()) {
      Optional<Interface> updateSource = getUpdateSource(neighbor, c, w);
      if (!updateSource.isPresent()) {
        w.redFlagf("Ignoring BGP neighbor %s: Unable to infer its update source", neighbor.getIp());
        continue;
      }
      updateSources.put(neighbor.getIp(), updateSource.get());
      neighborsByVrf
          .computeIfAbsent(updateSource.get().getVrfName(), k -> new HashSet<>())
          .add(neighbor.getIp());
    }

    for (Map.Entry<String, Set<Ip>> e : neighborsByVrf.entrySet()) {
      convertBgpProcessForVrf(
          bgpProcess, routerId, e.getKey(), e.getValue(), updateSources, originatedSpaces, c, w);
    }
  }

  /**
   * Generates the common BGP export policy for the given {@link BgpProcess} on the given config and
   * adds it to the config's routing policies.
   *
   * @return A list of all {@link PrefixSpace prefix spaces} originated by the BGP process.
   */
  private static List<PrefixSpace> generateCommonBgpExportPolicyAndGetOriginatedSpaces(
      BgpProcess bgpProcess, Configuration c) {
    ImmutableList.Builder<PrefixSpace> originatedPrefixSpaces = ImmutableList.builder();

    // If a route matches any of these export conditions, it should be exported.
    // TODO: Export disjuncts should include aggregate routes and routes to redistribute if present
    ImmutableList.Builder<BooleanExpr> exportDisjuncts = ImmutableList.builder();

    // Always export BGP or IBGP routes
    exportDisjuncts.add(MATCH_BGP);

    // Export routes matching network statements
    bgpProcess
        .getNetworks()
        .values()
        .forEach(
            network -> {
              assert network.getPrefix() != null; // guaranteed by extraction
              PrefixSpace exportSpace =
                  new PrefixSpace(PrefixRange.fromPrefix(network.getPrefix()));
              List<BooleanExpr> exportNetworkConditions =
                  ImmutableList.of(
                      new MatchPrefixSet(
                          DestinationNetwork.instance(), new ExplicitPrefixSet(exportSpace)),
                      MATCH_NOT_BGP_OR_AGGREGATE,
                      REDIST_WITH_ENVIRONMENT_ORIGIN_TYPE_IGP);
              originatedPrefixSpaces.add(exportSpace);
              exportDisjuncts.add(new Conjunction(exportNetworkConditions));
            });

    // Build common export policy (it will be added to the configuration on build())
    RoutingPolicy.builder()
        .setName(BGP_COMMON_EXPORT_POLICY_NAME)
        .setOwner(c)
        // TODO: If there are aggregate summary-only routes, more specific routes should be blocked
        //  before matching export conditions
        .addStatement(
            new If(
                new Disjunction(exportDisjuncts.build()),
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement())))
        // Default deny
        .addStatement(Statements.ReturnFalse.toStaticStatement())
        .build();
    return originatedPrefixSpaces.build();
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

  private static final int DEFAULT_EBGP_ADMIN_COST = 20;
  private static final int DEFAULT_IBGP_ADMIN_COST = 200;
  private static final int DEFAULT_LOCAL_ADMIN_COST = 200; // is this correct?

  private static @Nonnull org.batfish.datamodel.BgpProcess.Builder bgpProcessBuilder() {
    return org.batfish.datamodel.BgpProcess.builder()
        .setEbgpAdminCost(DEFAULT_EBGP_ADMIN_COST)
        .setIbgpAdminCost(DEFAULT_IBGP_ADMIN_COST)
        .setLocalAdminCost(DEFAULT_LOCAL_ADMIN_COST)
        // TODO: confirm values below
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP);
  }

  private static void convertBgpProcessForVrf(
      BgpProcess bgpProcess,
      Ip routerId,
      String vrf,
      Set<Ip> neighborIdsInVrf,
      Map<Ip, Interface> updateSources,
      List<PrefixSpace> originatedSpaces,
      Configuration c,
      Warnings w) {
    // TODO Admin distances can be explicitly configured on the process level
    org.batfish.datamodel.BgpProcess viProc = bgpProcessBuilder().setRouterId(routerId).build();
    viProc.setMultipathEbgp(bgpProcess.getEbgpMultipathEffective());
    viProc.setMultipathIbgp(bgpProcess.getIbgpMultipathEffective());
    originatedSpaces.forEach(viProc::addToOriginationSpace);

    // Convert neighbors
    long localAs = bgpProcess.getAsEffective();
    for (Ip remoteIp : neighborIdsInVrf) {
      BgpNeighbor neighbor = bgpProcess.getNeighbors().get(remoteIp);
      Interface updateSource = updateSources.get(remoteIp);
      Ip localIp =
          Optional.ofNullable(updateSource.getConcreteAddress())
              .map(ConcreteInterfaceAddress::getIp)
              .orElse(null);
      if (localIp == null) {
        w.redFlagf(
            "Ignoring BGP neighbor %s: Update-source %s has no address",
            remoteIp, updateSource.getName());
        continue;
      }
      BgpActivePeerConfig.builder()
          .setLocalIp(localIp)
          .setLocalAs(localAs)
          .setPeerAddress(neighbor.getIp())
          .setRemoteAsns(
              Optional.ofNullable(neighbor.getRemoteAs())
                  .map(LongSpace::of)
                  .orElse(LongSpace.EMPTY))
          .setBgpProcess(viProc)
          .setIpv4UnicastAddressFamily(
              Ipv4UnicastAddressFamily.builder()
                  .setImportPolicy(neighbor.getRouteMapIn())
                  .setExportPolicy(generateNeighborExportPolicy(neighbor, vrf, c))
                  .build())
          .build();
    }

    // TODO: Redistribution policy

    c.getVrfs().get(vrf).setBgpProcess(viProc);
  }

  /**
   * Generates the BGP export policy for the given {@link BgpNeighbor} on the given config and adds
   * it to the config's routing policies.
   *
   * @return The name of the generated policy.
   */
  private static String generateNeighborExportPolicy(
      BgpNeighbor neighbor, String vrf, Configuration c) {
    // Build neighbor export policy (it will be added to the configuration on build())
    String policyName = generatedBgpPeerExportPolicyName(vrf, neighbor.getIp().toString());
    BooleanExpr matchCommonExportPolicy = new CallExpr(BGP_COMMON_EXPORT_POLICY_NAME);
    String exportRm = neighbor.getRouteMapOut();
    RoutingPolicy.builder()
        .setName(policyName)
        .setOwner(c)
        // TODO If default-originate is configured, regular export policies probably don't apply to
        //  the default route. (But default-originate is not yet parsed/extracted.)
        .addStatement(
            new If(
                exportRm == null
                    ? matchCommonExportPolicy
                    : new Conjunction(
                        ImmutableList.of(matchCommonExportPolicy, new CallExpr(exportRm))),
                ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                ImmutableList.of(Statements.ExitReject.toStaticStatement())))
        .build();
    return policyName;
  }

  public static void convertRouteMap(RouteMap routeMap, Configuration c, Warnings w) {
    String rmName = routeMap.getName();
    List<Statement> statements =
        routeMap.getRules().values().stream()
            .map(
                rule -> {
                  String listName = rule.getMatchIpAddress();
                  Statement action =
                      rule.getActionEffective() == RouteMapRule.Action.PERMIT
                          ? Statements.ReturnTrue.toStaticStatement()
                          : Statements.ReturnFalse.toStaticStatement();
                  Statement statement;
                  if (listName != null) {
                    if (!c.getRouteFilterLists().containsKey(listName)) {
                      w.redFlagf(
                          "Ignoring rule %s in route-map %s: List %s does not exist or was not"
                              + " converted",
                          rule.getNumber(), rmName, listName);
                      return null;
                    }
                    BooleanExpr guard =
                        new MatchPrefixSet(
                            DestinationNetwork.instance(), new NamedPrefixSet(listName));
                    statement = new If(guard, ImmutableList.of(action));
                  } else {
                    // Rule matches unconditionally. TODO Confirm behavior
                    statement = new If(BooleanExprs.TRUE, ImmutableList.of(action));
                  }
                  statement.setComment(String.format("Match rule %s", rule.getNumber()));
                  return statement;
                })
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(rmName)
        .setStatements(statements)
        // Default deny
        .addStatement(Statements.ReturnFalse.toStaticStatement())
        .build();
  }
}
