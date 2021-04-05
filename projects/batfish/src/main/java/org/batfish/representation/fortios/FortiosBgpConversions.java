package org.batfish.representation.fortios;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Helper functions for generating VI BGP structures for {@link FortiosConfiguration}. */
public final class FortiosBgpConversions {

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
        w.redFlag(
            String.format(
                "BGP neighbor %s has an inactive update-source interface %s. Attempting to infer"
                    + " another update-source for this neighbor",
                neighbor.getIp(), updateSource));
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
      w.redFlag("Ignoring BGP process: No AS configured");
      return;
    } else if (as == 65535L || as == 4294967295L) {
      w.redFlag(String.format("Ignoring BGP process: AS %s is proscribed by RFC 7300", as));
      return;
    }
    // TODO Infer router-id if not explicitly configured
    Ip routerId = bgpProcess.getRouterId();
    if (routerId == null) {
      w.redFlag("Ignoring BGP process: No router ID configured");
      return;
    }
    Map<Ip, Interface> updateSources = new HashMap<>();
    Map<String, Set<Ip>> neighborsByVrf = new HashMap<>();
    for (BgpNeighbor neighbor : bgpProcess.getNeighbors().values()) {
      Optional<Interface> updateSource = getUpdateSource(neighbor, c, w);
      if (!updateSource.isPresent()) {
        w.redFlag(
            String.format(
                "Ignoring BGP neighbor %s: Unable to infer its update source", neighbor.getIp()));
        continue;
      }
      updateSources.put(neighbor.getIp(), updateSource.get());
      neighborsByVrf
          .computeIfAbsent(updateSource.get().getVrfName(), k -> new HashSet<>())
          .add(neighbor.getIp());
    }

    for (Map.Entry<String, Set<Ip>> e : neighborsByVrf.entrySet()) {
      convertBgpProcessForVrf(bgpProcess, routerId, e.getKey(), e.getValue(), updateSources, c, w);
    }
  }

  private static void convertBgpProcessForVrf(
      BgpProcess bgpProcess,
      Ip routerId,
      String vrf,
      Set<Ip> neighborIdsInVrf,
      Map<Ip, Interface> updateSources,
      Configuration c,
      Warnings w) {
    // TODO Admin distances can be explicitly configured on the process level
    int ebgpAdmin = RoutingProtocol.BGP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    int ibgpAdmin = RoutingProtocol.IBGP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    org.batfish.datamodel.BgpProcess viProc =
        new org.batfish.datamodel.BgpProcess(routerId, ebgpAdmin, ibgpAdmin);

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
        w.redFlag(
            String.format(
                "Ignoring BGP neighbor %s: Update-source %s has no address",
                remoteIp, updateSource.getName()));
        continue;
      }
      BgpActivePeerConfig.builder()
          .setLocalIp(localIp)
          .setLocalAs(localAs)
          .setPeerAddress(neighbor.getIp())
          .setRemoteAs(neighbor.getRemoteAs())
          .setBgpProcess(viProc)
          .build();
    }

    // TODO: Redistribution policy, import/export policies

    c.getVrfs().get(vrf).setBgpProcess(viProc);
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
                      w.redFlag(
                          String.format(
                              "Ignoring rule %s in route-map %s: List %s does not exist or was not"
                                  + " converted",
                              rule.getNumber(), rmName, listName));
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
    RoutingPolicy.builder().setOwner(c).setName(rmName).setStatements(statements).build();
  }
}
