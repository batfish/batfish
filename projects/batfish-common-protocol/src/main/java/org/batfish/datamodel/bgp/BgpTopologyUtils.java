package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.Network;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/** Utility functions for computing BGP topology */
public final class BgpTopologyUtils {
  /**
   * Compute the BGP topology -- a network of {@link BgpPeerConfig}s connected by {@link
   * BgpSessionProperties}s. See {@link #initBgpTopology(Map, Map, boolean, boolean,
   * TracerouteEngine)} for more details.
   *
   * @param configurations configuration keyed by hostname
   * @param ipOwners Ip owners (see {@link
   *     org.batfish.common.topology.TopologyUtil#computeIpNodeOwners(Map, boolean)}
   * @param keepInvalid whether to keep improperly configured neighbors. If performing configuration
   *     checks, you probably want this set to {@code true}, otherwise (e.g., computing dataplane)
   *     you want this to be {@code false}.
   * @return A graph ({@link Network}) representing all BGP peerings.
   */
  public static ValueGraph<BgpPeerConfigId, BgpSessionProperties> initBgpTopology(
      Map<String, Configuration> configurations,
      Map<Ip, Set<String>> ipOwners,
      boolean keepInvalid) {
    return initBgpTopology(configurations, ipOwners, keepInvalid, false, null);
  }

  /**
   * Compute the BGP topology -- a network of {@link BgpPeerConfigId}s connected by {@link
   * BgpSessionProperties}s.
   *
   * @param configurations node configurations, keyed by hostname
   * @param ipOwners network Ip owners (see {@link
   *     org.batfish.common.topology.TopologyUtil#computeIpNodeOwners(Map, boolean)} for reference)
   * @param keepInvalid whether to keep improperly configured neighbors. If performing configuration
   *     checks, you probably want this set to {@code true}, otherwise (e.g., computing dataplane)
   *     you want this to be {@code false}.
   * @param checkReachability whether to perform dataplane-level checks to ensure that neighbors are
   *     reachable and sessions can be established correctly. <b>Note:</b> this is different from
   *     {@code keepInvalid=false}, which only does filters invalid neighbors at the control-plane
   *     level
   * @param tracerouteEngine an instance of {@link TracerouteEngine} for doing reachability checks.
   * @return A graph ({@link Network}) representing all BGP peerings.
   */
  public static ValueGraph<BgpPeerConfigId, BgpSessionProperties> initBgpTopology(
      Map<String, Configuration> configurations,
      Map<Ip, Set<String>> ipOwners,
      boolean keepInvalid,
      boolean checkReachability,
      @Nullable TracerouteEngine tracerouteEngine) {
    checkArgument(
        !checkReachability || tracerouteEngine != null,
        "Cannot check reachability without a traceroute engine");
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("BgpTopologyUtils.initBgpTopology").startActive()) {
      assert span != null; // avoid unused warning

      // TODO: handle duplicate ips on different vrfs

      NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
      /*
       * First pass: identify all addresses "owned" by BgpNeighbors,
       * add neighbor ids as vertices to the graph
       */
      Map<Ip, Set<BgpPeerConfigId>> localAddresses = new HashMap<>();
      MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
          ValueGraphBuilder.directed().allowsSelfLoops(false).build();
      for (Configuration node : configurations.values()) {
        String hostname = node.getHostname();
        for (Vrf vrf : node.getVrfs().values()) {
          BgpProcess proc = vrf.getBgpProcess();
          if (proc == null) {
            // nothing to do if no bgp process on this VRF
            continue;
          }
          for (Entry<Prefix, ? extends BgpPeerConfig> entry :
              Iterables.concat(
                  proc.getActiveNeighbors().entrySet(), proc.getPassiveNeighbors().entrySet())) {
            Prefix prefix = entry.getKey();
            BgpPeerConfig bgpPeerConfig = entry.getValue();

            if (!bgpConfigPassesSanityChecks(bgpPeerConfig, hostname, ipOwners) && !keepInvalid) {
              continue;
            }

            BgpPeerConfigId neighborID =
                new BgpPeerConfigId(
                    hostname, vrf.getName(), prefix, bgpPeerConfig instanceof BgpPassivePeerConfig);
            graph.addNode(neighborID);

            // Add this neighbor as owner of its local address
            localAddresses
                .computeIfAbsent(bgpPeerConfig.getLocalIp(), k -> new HashSet<>())
                .add(neighborID);
          }
        }
      }

      // Second pass: add edges to the graph. Note, these are directed edges.
      for (BgpPeerConfigId neighborId : graph.nodes()) {
        if (neighborId.isDynamic()) {
          // Passive end of the peering cannot initiate a connection
          continue;
        }
        BgpActivePeerConfig neighbor =
            networkConfigurations.getBgpPointToPointPeerConfig(neighborId);
        if (neighbor == null
            || neighbor.getLocalIp() == null
            || neighbor.getLocalAs() == null
            || neighbor.getPeerAddress() == null
            || neighbor.getRemoteAsns().isEmpty()) {
          continue;
        }
        // Find nodes that own the neighbor's peer address
        Set<String> possibleHostnames = ipOwners.get(neighbor.getPeerAddress());
        if (possibleHostnames == null) {
          continue;
        }
        Set<BgpPeerConfigId> candidates = localAddresses.get(neighbor.getPeerAddress());
        if (candidates == null) {
          // Check maybe it's trying to reach a dynamic neighbor
          candidates = localAddresses.get(Ip.AUTO);
          if (candidates == null) {
            continue;
          }
        }
        for (BgpPeerConfigId candidateNeighborId : candidates) {
          if (!bgpCandidatePassesSanityChecks(
              neighbor, candidateNeighborId, possibleHostnames, networkConfigurations)) {
            // Short-circuit if there is no way the remote end will accept our connection
            continue;
          }
          /*
           * Perform reachability checks.
           */
          if (checkReachability) {
            if (isReachableBgpNeighbor(
                neighborId, candidateNeighborId, neighbor, tracerouteEngine)) {
              graph.putEdgeValue(
                  neighborId,
                  candidateNeighborId,
                  BgpSessionProperties.from(
                      neighbor,
                      Objects.requireNonNull(
                          networkConfigurations.getBgpPeerConfig(candidateNeighborId))));
            }
          } else {
            graph.putEdgeValue(
                neighborId,
                candidateNeighborId,
                BgpSessionProperties.from(
                    neighbor,
                    Objects.requireNonNull(
                        networkConfigurations.getBgpPeerConfig(candidateNeighborId))));
          }
        }
      }
      return ImmutableValueGraph.copyOf(graph);
    }
  }

  private static boolean bgpConfigPassesSanityChecks(
      BgpPeerConfig config, String hostname, Map<Ip, Set<String>> ipOwners) {
    /*
     * Do these checks as a short-circuit to avoid extra reachability checks when building
     * BGP topology.
     * Only keep invalid neighbors that don't have local IPs if specifically requested to.
     * Note: we use Ip.AUTO to denote the listening end of a dynamic peering.
     */
    Ip localAddress = config.getLocalIp();
    return (localAddress != null
            && ipOwners.containsKey(localAddress)
            && ipOwners.get(localAddress).contains(hostname))
        || Ip.AUTO.equals(localAddress);
  }

  /**
   * Check if the given combo of BGP peer configs can agree on their respective BGP local/remote AS
   * number configurations.
   */
  private static boolean bgpCandidatePassesSanityChecks(
      @Nonnull BgpActivePeerConfig neighbor,
      @Nonnull BgpPeerConfigId candidateId,
      @Nonnull Set<String> possibleHostnames,
      @Nonnull NetworkConfigurations nc) {
    if (candidateId.isDynamic()) {
      BgpPassivePeerConfig candidate = nc.getBgpDynamicPeerConfig(candidateId);
      return candidate != null
          && candidate.canConnect(neighbor.getLocalAs())
          && neighbor.getRemoteAsns().contains(candidate.getLocalAs())
          && candidate.canConnect(neighbor.getLocalIp())
          && possibleHostnames.contains(candidateId.getHostname());
    } else {
      BgpActivePeerConfig candidate = nc.getBgpPointToPointPeerConfig(candidateId);
      return candidate != null
          && Objects.equals(neighbor.getPeerAddress(), candidate.getLocalIp())
          && Objects.equals(neighbor.getLocalIp(), candidate.getPeerAddress())
          && neighbor.getRemoteAsns().contains(candidate.getLocalAs())
          && candidate.getRemoteAsns().contains(neighbor.getLocalAs());
    }
  }

  /**
   * Check if a bgp peer is reachable to establish a session
   *
   * <p><b>Warning:</b> Notion of directionality is important here, we are assuming {@code src} is
   * initiating the connection according to its local configuration
   */
  @VisibleForTesting
  public static boolean isReachableBgpNeighbor(
      @Nonnull BgpPeerConfigId initiator,
      @Nonnull BgpPeerConfigId listener,
      @Nonnull BgpActivePeerConfig src,
      @Nonnull TracerouteEngine tracerouteEngine) {
    Ip srcAddress = src.getLocalIp();
    Ip dstAddress = src.getPeerAddress();
    if (dstAddress == null) {
      return false;
    }

    // we do a bidirectional traceroute only from the initiator to the listener since the other
    // direction will be checked once we pick up the listener as the source. This is consistent with
    // the directional nature of BGP graph
    return canInitiateBgpSession(
        initiator.getHostname(),
        initiator.getVrfName(),
        srcAddress,
        dstAddress,
        listener.getHostname(),
        SessionType.isEbgp(BgpSessionProperties.getSessionType(src)) && !src.getEbgpMultihop(),
        tracerouteEngine);
  }

  private static boolean canInitiateBgpSession(
      @Nonnull String initiatorNode,
      @Nonnull String initiatorVrf,
      @Nonnull Ip initiatorIp,
      @Nonnull Ip listenerIp,
      @Nonnull String listenerNode,
      boolean bgpSingleHop,
      @Nonnull TracerouteEngine tracerouteEngine) {

    Flow flowFromSrc =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(1)
            .setTag("neighbor-resolution")
            .setIngressNode(initiatorNode)
            .setIngressVrf(initiatorVrf)
            .setSrcIp(initiatorIp)
            .setDstIp(listenerIp)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.BGP.number())
            .build();

    List<TraceAndReverseFlow> forwardTracesAndReverseFlows =
        tracerouteEngine
            .computeTracesAndReverseFlows(ImmutableSet.of(flowFromSrc), false)
            .get(flowFromSrc);

    List<TraceAndReverseFlow> reverseTraces =
        forwardTracesAndReverseFlows.stream()
            .filter(
                traceAndReverseFlow -> {
                  Trace forwardTrace = traceAndReverseFlow.getTrace();
                  return forwardTrace.getDisposition() == FlowDisposition.ACCEPTED
                      && (!bgpSingleHop || forwardTrace.getHops().size() <= 2);
                })
            .filter(
                traceAndReverseFlow ->
                    traceAndReverseFlow.getReverseFlow() != null
                        && traceAndReverseFlow
                            .getReverseFlow()
                            .getIngressNode()
                            .equals(listenerNode))
            .flatMap(
                traceAndReverseFlow ->
                    tracerouteEngine
                        .computeTracesAndReverseFlows(
                            ImmutableSet.of(traceAndReverseFlow.getReverseFlow()),
                            traceAndReverseFlow.getNewFirewallSessions(),
                            false)
                        .get(traceAndReverseFlow.getReverseFlow()).stream())
            .collect(ImmutableList.toImmutableList());

    return reverseTraces.stream()
        .anyMatch(
            traceAndReverseFlow -> {
              Trace reverseTrace = traceAndReverseFlow.getTrace();
              List<Hop> hops = reverseTrace.getHops();
              return !hops.isEmpty()
                  && hops.get(hops.size() - 1).getNode().getName().equals(initiatorNode)
                  && reverseTrace.getDisposition() == FlowDisposition.ACCEPTED;
            });
  }

  private BgpTopologyUtils() {}
}
