package org.batfish.datamodel.bgp;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
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
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
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
            || neighbor.getRemoteAs() == null) {
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
          && Objects.equals(neighbor.getRemoteAs(), candidate.getLocalAs())
          && candidate.canConnect(neighbor.getLocalIp())
          && possibleHostnames.contains(candidateId.getHostname());
    } else {
      BgpActivePeerConfig candidate = nc.getBgpPointToPointPeerConfig(candidateId);
      return candidate != null
          && Objects.equals(neighbor.getPeerAddress(), candidate.getLocalIp())
          && Objects.equals(neighbor.getLocalIp(), candidate.getPeerAddress())
          && Objects.equals(neighbor.getRemoteAs(), candidate.getLocalAs())
          && Objects.equals(neighbor.getLocalAs(), candidate.getRemoteAs());
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
      @Nullable TracerouteEngine tracerouteEngine) {
    Ip srcAddress = src.getLocalIp();
    Ip dstAddress = src.getPeerAddress();
    if (dstAddress == null) {
      return false;
    }
    if (tracerouteEngine == null) {
      throw new BatfishException(
          "Cannot compute neighbor reachability without a traceroute engine");
    }

    // we do a bidirectional traceroute only from the initiator to the listener since the other
    // direction will be checked once we pick up the listener as the source. This is consistent with
    // the directional nature of BGP graph
    return canTracerouteTo(
        initiator.getHostname(),
        initiator.getVrfName(),
        srcAddress,
        NamedPort.EPHEMERAL_LOWEST.number(),
        dstAddress,
        NamedPort.BGP.number(),
        listener.getHostname(),
        SessionType.isEbgp(BgpSessionProperties.getSessionType(src)) && !src.getEbgpMultihop(),
        tracerouteEngine);
  }

  private BgpTopologyUtils() {}

  private static boolean canTracerouteTo(
      @Nonnull String ingressNode,
      @Nonnull String ingressVrf,
      @Nonnull Ip srcIp,
      int srcPort,
      @Nonnull Ip dstIp,
      int dstPort,
      @Nonnull String finalNode,
      boolean singleHop,
      @Nonnull TracerouteEngine tracerouteEngine) {

    Flow flowFromSrc =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(1)
            .setTag("neighbor-resolution")
            .setIngressNode(ingressNode)
            .setIngressVrf(ingressVrf)
            .setSrcIp(srcIp)
            .setDstIp(dstIp)
            .setSrcPort(srcPort)
            .setDstPort(dstPort)
            .build();

    SortedMap<Flow, List<TraceAndReverseFlow>> forwardTraces =
        tracerouteEngine.computeTracesAndReverseFlows(ImmutableSet.of(flowFromSrc), false);

    List<FlowAndSessions> reverseFlowAndSessions =
        forwardTraces.values().stream()
            .flatMap(Collection::stream)
            .filter(
                traceAndReverseFlow ->
                    (!singleHop || traceAndReverseFlow.getTrace().getHops().size() <= 2))
            .filter(
                traceAndReverseFlows ->
                    traceAndReverseFlows.getReverseFlow() != null
                        && traceAndReverseFlows.getReverseFlow().getIngressNode().equals(finalNode))
            .map(
                traceAndReverseFlow ->
                    new FlowAndSessions(
                        traceAndReverseFlow.getReverseFlow(),
                        traceAndReverseFlow.getNewFirewallSessions()))
            .collect(ImmutableList.toImmutableList());

    for (FlowAndSessions flowAndSessions : reverseFlowAndSessions) {
      Map<Flow, List<TraceAndReverseFlow>> reverseFlowAndTraces =
          tracerouteEngine.computeTracesAndReverseFlows(
              ImmutableSet.of(flowAndSessions._flow), flowAndSessions._sessions, false);
      List<TraceAndReverseFlow> reverseTraces = reverseFlowAndTraces.get(flowAndSessions._flow);
      if (reverseTraces != null
          && reverseTraces.stream()
              .anyMatch(
                  traceAndReverseFlow ->
                      traceAndReverseFlow.getTrace().getDisposition()
                          == FlowDisposition.ACCEPTED)) {
        return true;
      }
    }
    return false;
  }

  private static final class FlowAndSessions {
    final Flow _flow;
    final Set<FirewallSessionTraceInfo> _sessions;

    FlowAndSessions(Flow flow, Set<FirewallSessionTraceInfo> sessions) {
      _flow = flow;
      _sessions = sessions;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof FlowAndSessions)) {
        return false;
      }
      FlowAndSessions that = (FlowAndSessions) o;
      return Objects.equals(_flow, that._flow) && Objects.equals(_sessions, that._sessions);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_flow, _sessions);
    }
  }
}
