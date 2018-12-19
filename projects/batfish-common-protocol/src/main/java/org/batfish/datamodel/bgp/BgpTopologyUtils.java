package org.batfish.datamodel.bgp;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.ITracerouteEngine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Utility functions for computing BGP topology */
public final class BgpTopologyUtils {
  /**
   * Compute the BGP topology -- a network of {@link BgpPeerConfig}s connected by {@link
   * BgpSessionProperties}s. See {@link #initBgpTopology(Map, Map, boolean, boolean,
   * ITracerouteEngine, DataPlane)} for more details.
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
    return initBgpTopology(configurations, ipOwners, keepInvalid, false, null, null);
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
   * @param tracerouteEngine an instance of {@link ITracerouteEngine} for doing reachability checks.
   * @param dp (partially) computed dataplane.
   * @return A graph ({@link Network}) representing all BGP peerings.
   */
  public static ValueGraph<BgpPeerConfigId, BgpSessionProperties> initBgpTopology(
      Map<String, Configuration> configurations,
      Map<Ip, Set<String>> ipOwners,
      boolean keepInvalid,
      boolean checkReachability,
      @Nullable ITracerouteEngine tracerouteEngine,
      @Nullable DataPlane dp) {
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
                neighborId, candidateNeighborId, neighbor, tracerouteEngine, dp)) {
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
  private static boolean isReachableBgpNeighbor(
      BgpPeerConfigId initiator,
      BgpPeerConfigId listener,
      BgpActivePeerConfig src,
      @Nullable ITracerouteEngine tracerouteEngine,
      @Nullable DataPlane dp) {
    Ip srcAddress = src.getLocalIp();
    Ip dstAddress = src.getPeerAddress();
    if (dstAddress == null) {
      return false;
    }
    if (tracerouteEngine == null || dp == null) {
      throw new BatfishException("Cannot compute neighbor reachability without a dataplane");
    }

    /*
     * Ensure that the session can be established by running traceroute in both directions
     */
    Flow.Builder fb = new Flow.Builder();
    fb.setIpProtocol(IpProtocol.TCP);
    fb.setTag("neighbor-resolution");

    fb.setIngressNode(initiator.getHostname());
    fb.setIngressVrf(initiator.getVrfName());
    fb.setSrcIp(srcAddress);
    fb.setDstIp(dstAddress);
    fb.setSrcPort(NamedPort.EPHEMERAL_LOWEST.number());
    fb.setDstPort(NamedPort.BGP.number());
    Flow forwardFlow = fb.build();

    // Execute the "initiate connection" traceroute
    SortedMap<Flow, Set<FlowTrace>> traces =
        tracerouteEngine.processFlows(dp, ImmutableSet.of(forwardFlow), dp.getFibs(), false);

    SortedSet<FlowTrace> acceptedFlows =
        traces
            .get(forwardFlow)
            .stream()
            .filter(
                trace ->
                    trace.getDisposition() == FlowDisposition.ACCEPTED
                        && trace.getAcceptingNode() != null
                        && trace.getAcceptingNode().getHostname().equals(listener.getHostname()))
            .collect(ImmutableSortedSet.toImmutableSortedSet(FlowTrace::compareTo));

    if (acceptedFlows.isEmpty()) {
      return false;
    }
    NodeInterfacePair acceptPoint = acceptedFlows.first().getAcceptingNode();
    if (SessionType.isEbgp(BgpSessionProperties.getSessionType(src))
        && !src.getEbgpMultihop()
        && acceptedFlows.first().getHops().size() > 1) {
      // eBGP expects direct connection (single hop) unless explicitly configured multi-hop
      return false;
    }

    if (acceptPoint == null) {
      return false;
    }
    String acceptedHostname = acceptPoint.getHostname();
    // The reply traceroute
    fb.setIngressNode(acceptedHostname);
    fb.setIngressVrf(listener.getVrfName());
    fb.setSrcIp(forwardFlow.getDstIp());
    fb.setDstIp(forwardFlow.getSrcIp());
    fb.setSrcPort(forwardFlow.getDstPort());
    fb.setDstPort(forwardFlow.getSrcPort());
    Flow backwardFlow = fb.build();
    traces = tracerouteEngine.processFlows(dp, ImmutableSet.of(backwardFlow), dp.getFibs(), false);

    /*
     * If backward traceroutes fail, do not consider the neighbor reachable
     */
    return !traces
        .get(backwardFlow)
        .stream()
        .filter(
            trace ->
                trace.getDisposition() == FlowDisposition.ACCEPTED
                    && trace.getAcceptingNode() != null
                    && trace.getAcceptingNode().getHostname().equals(initiator.getHostname()))
        .collect(ImmutableSet.toImmutableSet())
        .isEmpty();
  }

  private BgpTopologyUtils() {}
}
