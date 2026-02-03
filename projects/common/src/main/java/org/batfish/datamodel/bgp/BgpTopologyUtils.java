package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.Network;
import com.google.common.graph.ValueGraphBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.plugin.TracerouteEngine;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.traceroute.TraceDag;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpPeerConfigId.BgpPeerConfigType;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/** Utility functions for computing BGP topology */
public final class BgpTopologyUtils {

  /**
   * Result of initiating a BGP session by an active peer. Captures the flow used and forward and
   * reverse traces.
   *
   * <p>If the list reverse traces is empty, that implies session initiation failed in the forward
   * direction.
   */
  public static final class BgpSessionInitiationResult {
    private final @Nonnull Flow _flow;
    private final @Nonnull List<Trace> _forwardTraces;
    private final @Nonnull List<Trace> _reverseTraces;
    private final boolean _successful;

    public BgpSessionInitiationResult(
        Flow flow, List<Trace> forwardTraces, List<Trace> reverseTraces, boolean successful) {
      _flow = flow;
      _forwardTraces = ImmutableList.copyOf(forwardTraces);
      _reverseTraces = ImmutableList.copyOf(reverseTraces);
      _successful = successful;
    }

    public boolean isSuccessful() {
      return _successful;
    }

    public @Nonnull Flow getFlow() {
      return _flow;
    }

    public @Nonnull List<Trace> getForwardTraces() {
      return _forwardTraces;
    }

    public @Nonnull List<Trace> getReverseTraces() {
      return _reverseTraces;
    }
  }

  /**
   * Compute the BGP topology -- a network of {@link BgpPeerConfig}s connected by {@link
   * BgpSessionProperties}. See {@link #initBgpTopology(Map, Map, boolean, boolean,
   * TracerouteEngine, Map, L3Adjacencies)} for more details.
   *
   * @param configurations configuration keyed by hostname
   * @param ipOwners Ip owners (see {@link IpOwners#computeIpNodeOwners(Map, boolean)}
   * @param keepInvalid whether to keep improperly configured neighbors. If performing configuration
   *     checks, you probably want this set to {@code true}, otherwise (e.g., computing dataplane)
   *     you want this to be {@code false}.
   * @return A {@link BgpTopology} representing all BGP peerings.
   */
  public static @Nonnull BgpTopology initBgpTopology(
      Map<String, Configuration> configurations,
      Map<Ip, Map<String, Set<String>>> ipOwners,
      boolean keepInvalid,
      L3Adjacencies l3Adjacencies) {
    return initBgpTopology(
        configurations, ipOwners, keepInvalid, false, null, ImmutableMap.of(), l3Adjacencies);
  }

  /**
   * Compute the BGP topology -- a network of {@link BgpPeerConfigId}s connected by {@link
   * BgpSessionProperties}.
   *
   * @param configurations node configurations, keyed by hostname
   * @param ipVrfOwners network Ip owners (see {@link IpOwners#computeIpNodeOwners(Map, boolean)}
   *     for reference)
   * @param keepInvalid whether to keep improperly configured neighbors. If performing configuration
   *     checks, you probably want this set to {@code true}, otherwise (e.g., computing dataplane)
   *     you want this to be {@code false}.
   * @param checkReachability whether to perform dataplane-level checks to ensure that neighbors are
   *     reachable and sessions can be established correctly. <b>Note:</b> this is different from
   *     {@code keepInvalid=false}, which only does filters invalid neighbors at the control-plane
   *     level
   * @param tracerouteEngine an instance of {@link TracerouteEngine} for doing reachability checks.
   * @param l3Adjacencies {@link L3Adjacencies} of the network, for checking BGP unnumbered
   *     reachability.
   * @return A graph ({@link Network}) representing all BGP peerings.
   */
  public static @Nonnull BgpTopology initBgpTopology(
      Map<String, Configuration> configurations,
      Map<Ip, Map<String, Set<String>>> ipVrfOwners,
      boolean keepInvalid,
      boolean checkReachability,
      @Nullable TracerouteEngine tracerouteEngine,
      Map<String, Map<String, Fib>> fibs,
      L3Adjacencies l3Adjacencies) {
    checkArgument(
        !checkReachability || !keepInvalid,
        "Cannot check reachability while keeping invalid peers");
    checkArgument(
        !checkReachability || tracerouteEngine != null,
        "Cannot check reachability without a traceroute engine");
    // TODO: handle duplicate ips on different vrfs

    NetworkConfigurations networkConfigurations = NetworkConfigurations.of(configurations);
    /*
     * First pass: identify all addresses "owned" by BgpNeighbors, add neighbor ids as vertices to
     * the graph; dynamically determine local IPs as needed
     */
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    /*
     * Multimap of active peers' BgpPeerConfigIds to all IPs that each peer may use as local IP
     * when initiating a session. For a peer with an explicitly configured local IP, that IP is
     * the only value associated with the peer in this map. Otherwise:
     * - If FIBs are provided, the map contains all local IPs with which the peer may initiate,
     *   as inferred by getPotentialSrcIps().
     * - Else no IPs are associated with the peer.
     */
    ImmutableSetMultimap.Builder<BgpPeerConfigId, Ip> localIpsBuilder =
        ImmutableSetMultimap.builder();
    for (Configuration node : configurations.values()) {
      String hostname = node.getHostname();
      for (Vrf vrf : node.getVrfs().values()) {
        String vrfName = vrf.getName();
        BgpProcess proc = vrf.getBgpProcess();
        if (proc == null) {
          // nothing to do if no bgp process on this VRF
          continue;
        }
        Fib fib = fibs.getOrDefault(hostname, ImmutableMap.of()).get(vrfName);

        for (Entry<Ip, BgpActivePeerConfig> e : proc.getActiveNeighbors().entrySet()) {
          Ip peerAddress = e.getKey();
          BgpActivePeerConfig config = e.getValue();
          if (!keepInvalid
              && !bgpConfigPassesSanityChecks(config, hostname, vrfName, ipVrfOwners)) {
            continue;
          }
          BgpPeerConfigId neighborId =
              new BgpPeerConfigId(hostname, vrfName, peerAddress.toPrefix(), false);
          graph.addNode(neighborId);

          if (config.getLocalIp() != null) {
            localIpsBuilder.put(neighborId, config.getLocalIp());
          } else {
            // No explicitly configured local IP. Check for dynamically resolvable local IPs.
            localIpsBuilder.putAll(
                neighborId,
                vrf.getSourceIpInference().getPotentialSourceIps(peerAddress, fib, node));
          }
        }
        // Dynamic peers: map of prefix to BgpPassivePeerConfig
        proc.getPassiveNeighbors().entrySet().stream()
            .filter(
                entry ->
                    keepInvalid
                        || bgpConfigPassesSanityChecks(
                            entry.getValue(), hostname, vrfName, ipVrfOwners))
            .forEach(
                entry ->
                    graph.addNode(new BgpPeerConfigId(hostname, vrfName, entry.getKey(), true)));
        // Unnumbered BGP peers: map of interface name to BgpUnnumberedPeerConfig
        proc.getInterfaceNeighbors().entrySet().stream()
            .filter(
                e ->
                    keepInvalid
                        || bgpConfigPassesSanityChecks(
                            e.getValue(), hostname, vrfName, ipVrfOwners))
            .forEach(e -> graph.addNode(new BgpPeerConfigId(hostname, vrf.getName(), e.getKey())));
      }
    }

    // Second pass: add edges to the graph. Note, these are directed edges.
    Map<String, Multimap<String, BgpPeerConfigId>> receivers = new HashMap<>();
    for (BgpPeerConfigId peer : graph.nodes()) {
      if (peer.getType() == BgpPeerConfigType.UNNUMBERED) {
        // Unnumbered configs only form sessions with each other
        continue;
      }
      Multimap<String, BgpPeerConfigId> vrf =
          receivers.computeIfAbsent(peer.getHostname(), name -> LinkedListMultimap.create());
      vrf.put(peer.getVrfName(), peer);
    }
    SetMultimap<BgpPeerConfigId, Ip> localIps = localIpsBuilder.build();

    // In parallel, test which sessions can be established, and collect the new edges into a list.
    // Have to materialize so that adding the edges to the graph is done sequentially.
    List<BgpEdge> newEdges =
        graph.nodes().parallelStream()
            .flatMap(
                neighborId -> {
                  return switch (neighborId.getType()) {
                    case DYNAMIC ->
                        // Passive end of the peering cannot initiate a connection
                        Stream.of();
                    case ACTIVE ->
                        addActivePeerEdges(
                            neighborId,
                            networkConfigurations,
                            ipVrfOwners,
                            receivers,
                            localIps.get(neighborId),
                            checkReachability,
                            tracerouteEngine);
                    case UNNUMBERED ->
                        addUnnumberedPeerEdges(
                            neighborId, graph.nodes(), networkConfigurations, l3Adjacencies);
                  };
                })
            .collect(Collectors.toList());
    for (BgpEdge newEdge : newEdges) {
      graph.putEdgeValue(newEdge._source, newEdge._target, newEdge._sessionProps);
    }
    return new BgpTopology(graph);
  }

  private static Stream<BgpEdge> addActivePeerEdges(
      BgpPeerConfigId neighborId,
      NetworkConfigurations nc,
      Map<Ip, Map<String, Set<String>>> ipOwners,
      Map<String, Multimap<String, BgpPeerConfigId>> receivers,
      Set<Ip> potentialLocalIps,
      boolean checkReachability,
      TracerouteEngine tracerouteEngine) {
    BgpActivePeerConfig neighbor = nc.getBgpPointToPointPeerConfig(neighborId);
    if (neighbor == null
        || potentialLocalIps.isEmpty()
        || neighbor.getLocalAs() == null
        || neighbor.getPeerAddress() == null
        || neighbor.getRemoteAsns().isEmpty()) {
      return Stream.of();
    }
    // Find nodes that own the neighbor's peer address
    Map<String, Set<String>> possibleVrfs = ipOwners.get(neighbor.getPeerAddress());
    if (possibleVrfs == null) {
      return Stream.of();
    }

    return possibleVrfs.entrySet().stream()
        .flatMap(
            entry -> {
              String node = entry.getKey();
              Set<String> vrfs = entry.getValue();
              Multimap<String, BgpPeerConfigId> receiversByVrf = receivers.get(node);
              if (receiversByVrf == null) {
                return Stream.of();
              }
              return vrfs.stream()
                  .flatMap(
                      vrf ->
                          receiversByVrf.get(vrf).stream()
                              .flatMap(
                                  candidateId -> {
                                    // Ensure candidate has compatible local/remote AS, isn't in
                                    // same vrf as initiator
                                    BgpPeerConfig candidate = nc.getBgpPeerConfig(candidateId);
                                    if (!bgpCandidatePassesSanityChecks(
                                        neighborId, neighbor, candidateId, candidate)) {
                                      return Stream.of();
                                    }
                                    assert candidate
                                        != null; // guaranteed by bgpCandidatePassesSanityChecks
                                    // Check if neighbor has any feasible local IPs compatible with
                                    // this candidate
                                    Set<Ip> feasibleLocalIpsForPeeringWithCandidate =
                                        getFeasibleLocalIps(potentialLocalIps, candidate);
                                    if (feasibleLocalIpsForPeeringWithCandidate.isEmpty()) {
                                      return Stream.of();
                                    }
                                    if (!checkReachability) {
                                      return feasibleLocalIpsForPeeringWithCandidate.stream()
                                          .flatMap(
                                              ip ->
                                                  addEdges(
                                                      neighbor, neighborId, ip, candidateId, nc));
                                    } else {
                                      return feasibleLocalIpsForPeeringWithCandidate.stream()
                                          .filter(
                                              initiatorLocalIp ->
                                                  canEstablishBgpSession(
                                                      neighborId,
                                                      candidateId,
                                                      neighbor,
                                                      candidate,
                                                      initiatorLocalIp,
                                                      tracerouteEngine))
                                          .flatMap(
                                              srcIp ->
                                                  addEdges(
                                                      neighbor,
                                                      neighborId,
                                                      srcIp,
                                                      candidateId,
                                                      nc));
                                    }
                                  }));
            });
  }

  private static Stream<BgpEdge> addUnnumberedPeerEdges(
      BgpPeerConfigId neighborId,
      Set<BgpPeerConfigId> nodes,
      NetworkConfigurations nc,
      L3Adjacencies l3Adjacencies) {
    // neighbor will be null if neighborId has no peer interface defined
    BgpUnnumberedPeerConfig neighbor = nc.getBgpUnnumberedPeerConfig(neighborId);
    if (neighbor == null || neighbor.getLocalAs() == null || neighbor.getRemoteAsns().isEmpty()) {
      return Stream.of();
    }

    String hostname = neighborId.getHostname();
    NodeInterfacePair peerNip = NodeInterfacePair.of(hostname, neighborId.getPeerInterface());
    return nodes.stream()
        .filter(
            candidateId ->
                //  Ensure candidate is unnumbered and has compatible local/remote AS
                bgpCandidatePassesSanityChecks(neighborId, neighbor, candidateId, nc)
                    // Check layer 2 connectivity
                    && l3Adjacencies.inSamePointToPointDomain(
                        peerNip,
                        NodeInterfacePair.of(
                            candidateId.getHostname(), candidateId.getPeerInterface())))
        .flatMap(remoteId -> addEdges(neighbor, neighborId, neighbor.getLocalIp(), remoteId, nc));
  }

  private static final class BgpEdge {
    private final @Nonnull BgpPeerConfigId _source;
    private final @Nonnull BgpPeerConfigId _target;
    private final @Nonnull BgpSessionProperties _sessionProps;

    private BgpEdge(
        @Nonnull BgpPeerConfigId source,
        @Nonnull BgpPeerConfigId target,
        @Nonnull BgpSessionProperties sessionProps) {
      _source = source;
      _target = target;
      _sessionProps = sessionProps;
    }
  }

  /** Adds edges in {@code graph} between the given {@link BgpPeerConfigId}s in both directions. */
  private static Stream<BgpEdge> addEdges(
      BgpPeerConfig p1,
      BgpPeerConfigId id1,
      Ip p1LocalIp,
      BgpPeerConfigId id2,
      NetworkConfigurations networkConfigurations) {
    BgpPeerConfig remotePeer = Objects.requireNonNull(networkConfigurations.getBgpPeerConfig(id2));
    AsPair asPair =
        computeAsPair(
            p1.getLocalAs(),
            p1.getConfederationAsn(),
            p1.getRemoteAsns(),
            remotePeer.getLocalAs(),
            remotePeer.getConfederationAsn(),
            remotePeer.getRemoteAsns());
    assert asPair != null;
    return Stream.of(
        new BgpEdge(
            id1,
            id2,
            BgpSessionProperties.from(
                p1,
                p1LocalIp,
                remotePeer,
                false,
                asPair.getLocalAs(),
                asPair.getRemoteAs(),
                asPair.getConfedSessionType())),
        new BgpEdge(
            id2,
            id1,
            BgpSessionProperties.from(
                p1,
                p1LocalIp,
                remotePeer,
                true,
                asPair.getLocalAs(),
                asPair.getRemoteAs(),
                asPair.getConfedSessionType())));
  }

  /**
   * Sanity checks to avoid extra reachability checks when building BGP topology. Ensures that the
   * given {@link BgpPeerConfig} either:
   *
   * <ul>
   *   <li>is unnumbered, so does not use local IP; or
   *   <li>has no local IP (still viable because local IP may be determined dynamically, or other
   *       peers could initiate a session); or
   *   <li>has a local IP associated with the config's hostname, according to {@code ipOwners}
   * </ul>
   */
  private static boolean bgpConfigPassesSanityChecks(
      BgpPeerConfig config,
      String hostname,
      String vrfName,
      Map<Ip, Map<String, Set<String>>> ipOwners) {
    if (config instanceof BgpUnnumberedPeerConfig) {
      return true;
    }

    Ip localIp = config.getLocalIp();
    return localIp == null
        || (ipOwners.containsKey(localIp)
            && ipOwners.get(localIp).getOrDefault(hostname, ImmutableSet.of()).contains(vrfName));
  }

  /**
   * Check if {@code candidateId} represents a compatible candidate for {@code neighbor}:
   *
   * <ul>
   *   <li>Candidate and neighbor must be in separate VRFs
   *   <li>Candidate and neighbor must have matching local and remote ASNs
   * </ul>
   */
  private static boolean bgpCandidatePassesSanityChecks(
      @Nonnull BgpPeerConfigId neighborId,
      @Nonnull BgpActivePeerConfig neighbor,
      @Nonnull BgpPeerConfigId candidateId,
      @Nullable BgpPeerConfig candidate) {
    if (neighborId.getHostname().equals(candidateId.getHostname())
        && neighborId.getVrfName().equals(candidateId.getVrfName())) {
      // Do not let the same node/VRF peer with itself.
      return false;
    }
    // Ensure candidate exists and has compatible local and remote AS
    if (candidate == null) {
      return false;
    }
    return bgpCandidateHasCompatibleAs(neighbor, candidate);
  }

  /**
   * Returns if the given candidate BGP peer has a compatible AS configuration to the given active
   * BGP peer
   */
  public static boolean bgpCandidateHasCompatibleAs(
      BgpPeerConfig neighbor, BgpPeerConfig candidate) {
    return computeAsPair(
            neighbor.getLocalAs(),
            neighbor.getConfederationAsn(),
            neighbor.getRemoteAsns(),
            candidate.getLocalAs(),
            candidate.getConfederationAsn(),
            candidate.getRemoteAsns())
        != null;
  }

  /**
   * Returns the subset of {@code initiatorLocalIps} that would make the given {@link
   * BgpActivePeerConfig neighbor} compatible with the potential remote {@link BgpPeerConfig
   * candidate}. This subset may be empty, in which case the peering is not compatible.
   *
   * @throws IllegalArgumentException if the candidate is unnumbered
   */
  public static Set<Ip> getFeasibleLocalIps(Set<Ip> initiatorLocalIps, BgpPeerConfig candidate) {
    if (candidate instanceof BgpPassivePeerConfig) {
      BgpPassivePeerConfig passiveCandidate = (BgpPassivePeerConfig) candidate;
      return initiatorLocalIps.stream()
          .filter(passiveCandidate::hasCompatibleRemotePrefix)
          .collect(ImmutableSet.toImmutableSet());
    } else if (candidate instanceof BgpActivePeerConfig) {
      Ip candidatePeerAddress = ((BgpActivePeerConfig) candidate).getPeerAddress();
      return candidatePeerAddress != null && initiatorLocalIps.contains(candidatePeerAddress)
          ? ImmutableSet.of(candidatePeerAddress)
          : ImmutableSet.of();
    }
    // Shouldn't be unnumbered
    throw new IllegalArgumentException(
        String.format("Unrecognized peer type: %s", candidate.getClass().getSimpleName()));
  }

  /**
   * Check if {@code candidateId} represents a BGP unnumbered peer compatible with BGP unnumbered
   * peer {@code neighbor}. This means:
   *
   * <ul>
   *   <li>The candidate's possible remote ASNs include the neighbor's local AS, and vice versa
   *   <li>These are not two sessions in the same node and VRF.
   * </ul>
   */
  private static boolean bgpCandidatePassesSanityChecks(
      @Nonnull BgpPeerConfigId neighborId,
      @Nonnull BgpUnnumberedPeerConfig neighbor,
      @Nonnull BgpPeerConfigId candidateId,
      @Nonnull NetworkConfigurations nc) {
    if (neighborId.getHostname().equals(candidateId.getHostname())
        && neighborId.getVrfName().equals(candidateId.getVrfName())) {
      // Do not let the same node/VRF peer with itself.
      return false;
    }
    BgpPeerConfig candidate = nc.getBgpPeerConfig(candidateId);
    return candidate instanceof BgpUnnumberedPeerConfig
        && bgpCandidateHasCompatibleAs(neighbor, candidate);
  }

  private static final class ReverseFlowAndFirewallSessions {
    final @Nonnull Flow _reverseFlow;
    final @Nonnull Set<FirewallSessionTraceInfo> _firewallSessions;

    private ReverseFlowAndFirewallSessions(
        @Nonnull Flow reverseFlow, @Nonnull Set<FirewallSessionTraceInfo> firewallSessions) {
      _reverseFlow = reverseFlow;
      _firewallSessions = firewallSessions;
    }

    public @Nonnull Flow getReverseFlow() {
      return _reverseFlow;
    }

    public @Nonnull Set<FirewallSessionTraceInfo> getFirewallSessions() {
      return _firewallSessions;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ReverseFlowAndFirewallSessions)) {
        return false;
      }
      ReverseFlowAndFirewallSessions that = (ReverseFlowAndFirewallSessions) o;
      return _reverseFlow.equals(that._reverseFlow)
          && _firewallSessions.equals(that._firewallSessions);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_reverseFlow, _firewallSessions);
    }
  }

  /**
   * Tests whether an active BGP peer ({@code initiatorId}) can establish a BGP session with a peer
   * ({@code listenerId}).
   *
   * <p><b>Warning:</b> Notion of directionality is important here, we are assuming {@code
   * initiator} is initiating the connection according to its local configuration.
   *
   * <p>Assumes {@code initiator}'s peer address have already been confirmed nonnull. {@code
   * initiatorFeasibleLocalIps} is a set of IP addresses that the initiator may use as its local IP
   * for initation.
   *
   * @return whether the session can be established.
   */
  public static boolean canEstablishBgpSession(
      @Nonnull BgpPeerConfigId initiatorId,
      @Nonnull BgpPeerConfigId listenerId,
      @Nonnull BgpActivePeerConfig initiator,
      @Nonnull BgpPeerConfig listener,
      @Nonnull Ip initiatorLocalIp,
      @Nonnull TracerouteEngine tracerouteEngine) {
    assert initiatorId.getType() == BgpPeerConfigType.ACTIVE;
    Flow flowFromSrc =
        Flow.builder()
            .setIpProtocol(IpProtocol.TCP)
            .setTcpFlagsSyn(true)
            .setIngressNode(initiatorId.getHostname())
            .setIngressVrf(initiatorId.getVrfName())
            .setSrcIp(initiatorLocalIp)
            .setDstIp(initiator.getPeerAddress())
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(NamedPort.BGP.number())
            .build();

    TraceDag forwardTraceDag =
        tracerouteEngine
            .computeTraceDags(ImmutableSet.of(flowFromSrc), ImmutableSet.of(), false)
            .get(flowFromSrc);

    // TODO Session should be eBGP single-hop if either initiator or listener is eBGP single-hop
    boolean bgpSingleHop =
        BgpSessionProperties.getSessionType(initiator) == SessionType.EBGP_SINGLEHOP;

    return forwardTraceDag
        .getTraces()
        .filter(
            traceAndReverseFlow -> {
              Trace forwardTrace = traceAndReverseFlow.getTrace();
              if (forwardTrace.getDisposition() != FlowDisposition.ACCEPTED) {
                // The flow wasn't accepted, so BGP stack didn't get it.
                return false;
              }

              // Make sure the listener will accept this TCP connection.
              Flow reverseFlow = traceAndReverseFlow.getReverseFlow();
              assert traceAndReverseFlow.getReverseFlow() != null; // success implies return flow
              assert reverseFlow.getIngressVrf() != null; // accepted
              if (!reverseFlow.getIngressNode().equals(listenerId.getHostname())
                  || !reverseFlow.getIngressVrf().equals(listenerId.getVrfName())) {
                // This trace is success at the wrong device or in the wrong VRF.
                return false;
              } else if (listener.getCheckLocalIpOnAccept() && listener.getLocalIp() != null) {
                // The destination IP must match the listener's local IP, otherwise the listener
                // will reject the connection.
                //
                // The src IP on the reverse flow is the actual destination IP (post any NAT)
                // used in the forward flow. Looking at IPs as seen by listener is most
                // accurate way to check this.
                if (!listener.getLocalIp().equals(reverseFlow.getSrcIp())) {
                  return false;
                }
              }

              // Check the path count in case of ebgp singlehop session.
              return !bgpSingleHop || forwardTrace.getHops().size() <= 2;
            })
        // many traces can have the same reverse flow and firewall sessions. dedup
        .map(
            tarf ->
                new ReverseFlowAndFirewallSessions(
                    tarf.getReverseFlow(), tarf.getNewFirewallSessions()))
        .distinct()
        .anyMatch(
            reverseFlowAndFirewallSessions ->
                tracerouteEngine
                    .computeTraceDags(
                        ImmutableSet.of(reverseFlowAndFirewallSessions.getReverseFlow()),
                        reverseFlowAndFirewallSessions.getFirewallSessions(),
                        false)
                    .get(reverseFlowAndFirewallSessions.getReverseFlow())
                    .getTraces()
                    .anyMatch(
                        traceAndReverseFlow -> {
                          Trace reverseTrace = traceAndReverseFlow.getTrace();
                          List<Hop> hops = reverseTrace.getHops();
                          return !hops.isEmpty()
                              && hops.get(hops.size() - 1)
                                  .getNode()
                                  .getName()
                                  .equals(initiatorId.getHostname())
                              && reverseTrace.getDisposition() == FlowDisposition.ACCEPTED;
                        }));
  }

  /**
   * Attempts to initiate a TCP connection from the active BGP peer represented by {@code
   * initiatorId} to its counterpart BGP peer represented by {@code listenerId}.
   *
   * <p><b>Warning:</b> Notion of directionality is important here, we are assuming {@code
   * initiator} is initiating the connection according to its local configuration.
   *
   * <p>Assumes {@code initiator}'s peer address have already been confirmed nonnull. {@code
   * initiatorFeasibleLocalIps} is a set of IP addresses that the initiator may use as its local IP
   * for initation.
   *
   * @return The results of session initiations for each feasible local IP as a list of {@link
   *     BgpSessionInitiationResult}.
   */
  public static List<BgpSessionInitiationResult> initiateBgpSessions(
      @Nonnull BgpPeerConfigId initiatorId,
      @Nonnull BgpPeerConfigId listenerId,
      @Nonnull BgpActivePeerConfig initiator,
      @Nonnull Set<Ip> initiatorFeasibleLocalIps,
      @Nonnull TracerouteEngine tracerouteEngine) {
    assert initiatorId.getType() == BgpPeerConfigType.ACTIVE;
    ImmutableList.Builder<BgpSessionInitiationResult> initiationResults = ImmutableList.builder();
    for (Ip potentialLocalIp : initiatorFeasibleLocalIps) {
      Flow flowFromSrc =
          Flow.builder()
              .setIpProtocol(IpProtocol.TCP)
              .setTcpFlagsSyn(true)
              .setIngressNode(initiatorId.getHostname())
              .setIngressVrf(initiatorId.getVrfName())
              .setSrcIp(potentialLocalIp)
              .setDstIp(initiator.getPeerAddress())
              .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
              .setDstPort(NamedPort.BGP.number())
              .build();

      List<TraceAndReverseFlow> forwardTracesAndReverseFlows =
          tracerouteEngine
              .computeTracesAndReverseFlows(ImmutableSet.of(flowFromSrc), false)
              .get(flowFromSrc);

      // TODO Session should be eBGP single-hop if either initiator or listener is eBGP single-hop
      boolean bgpSingleHop =
          BgpSessionProperties.getSessionType(initiator) == SessionType.EBGP_SINGLEHOP;

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
                              .equals(listenerId.getHostname())
                          && traceAndReverseFlow
                              .getReverseFlow()
                              .getIngressVrf()
                              .equals(listenerId.getVrfName()))
              .flatMap(
                  traceAndReverseFlow ->
                      tracerouteEngine
                          .computeTracesAndReverseFlows(
                              ImmutableSet.of(traceAndReverseFlow.getReverseFlow()),
                              traceAndReverseFlow.getNewFirewallSessions(),
                              false)
                          .get(traceAndReverseFlow.getReverseFlow())
                          .stream())
              .collect(ImmutableList.toImmutableList());

      boolean successful =
          reverseTraces.stream()
              .anyMatch(
                  traceAndReverseFlow -> {
                    Trace reverseTrace = traceAndReverseFlow.getTrace();
                    List<Hop> hops = reverseTrace.getHops();
                    return !hops.isEmpty()
                        && hops.get(hops.size() - 1)
                            .getNode()
                            .getName()
                            .equals(initiatorId.getHostname())
                        && reverseTrace.getDisposition() == FlowDisposition.ACCEPTED;
                  });

      initiationResults.add(
          new BgpSessionInitiationResult(
              flowFromSrc,
              forwardTracesAndReverseFlows.stream()
                  .map(TraceAndReverseFlow::getTrace)
                  .collect(ImmutableList.toImmutableList()),
              reverseTraces.stream()
                  .map(TraceAndReverseFlow::getTrace)
                  .collect(ImmutableList.toImmutableList()),
              successful));
    }
    return initiationResults.build();
  }

  public static @Nullable AsPair computeAsPair(
      @Nullable Long initiatorLocalAs,
      @Nullable Long initiatorConfed,
      @Nonnull LongSpace initiatorRemoteAsns,
      @Nullable Long listenerLocalAs,
      @Nullable Long listenerConfed,
      @Nonnull LongSpace listenerRemoteAsns) {
    if (initiatorLocalAs == null || listenerLocalAs == null) {
      return null; // This is plainly a misconfiguration. No session.
    }
    // Simple case: no confederation config
    if (initiatorConfed == null && listenerConfed == null) {
      // Note: order of evaluation matters.
      if (listenerRemoteAsns.contains(initiatorLocalAs)
          && initiatorRemoteAsns.contains(listenerLocalAs)) {
        return new AsPair(initiatorLocalAs, listenerLocalAs, ConfedSessionType.NO_CONFED);
      } else {
        return null;
      }
    }
    // Confederation config is present on both peers
    if (initiatorConfed != null && listenerConfed != null) {
      // both peers inside *the same* confederation
      if (initiatorConfed.equals(listenerConfed)) {
        if (listenerRemoteAsns.contains(initiatorLocalAs)
            && initiatorRemoteAsns.contains(listenerLocalAs)) {
          return new AsPair(initiatorLocalAs, listenerLocalAs, ConfedSessionType.WITHIN_CONFED);
        } else {
          return null;
        }
      } else {
        // Both peers inside *different* confederations, must use external identifier only
        if (listenerRemoteAsns.contains(initiatorConfed)
            && initiatorRemoteAsns.contains(listenerConfed)) {
          return new AsPair(
              initiatorConfed, listenerConfed, ConfedSessionType.ACROSS_CONFED_BORDER);
        } else {
          return null;
        }
      }
    }
    // Initiator is inside a confederation, but listener is not
    if (initiatorConfed != null) {
      // Listener is not in a confederation, so this is across the confederation border if the
      // listener is configured to peer with the initiator's confederation ID. Both peers must
      // agree on the AS numbers: the initiator uses its confederation ID externally, and the
      // listener uses its local AS (or confederation ID if it has one, but it doesn't here).
      if (listenerRemoteAsns.contains(initiatorConfed)
          && initiatorRemoteAsns.contains(listenerLocalAs)) {
        return new AsPair(initiatorConfed, listenerLocalAs, ConfedSessionType.ACROSS_CONFED_BORDER);
      } else {
        return null;
      }
    } else {
      // Listener is inside a confederation, but initiator is not
      // Similar to above: initiator must be configured to peer with listener's confederation ID
      if (listenerRemoteAsns.contains(initiatorLocalAs)
          && initiatorRemoteAsns.contains(listenerConfed)) {
        return new AsPair(initiatorLocalAs, listenerConfed, ConfedSessionType.ACROSS_CONFED_BORDER);
      } else {
        return null;
      }
    }
  }

  /** Whether the session is within a confederation (or across its border), if applicable */
  public enum ConfedSessionType {
    NO_CONFED,
    WITHIN_CONFED,
    ACROSS_CONFED_BORDER
  }

  public static final class AsPair {
    private final long _localAs;
    private final long _remoteAs;
    private final ConfedSessionType _confedSessionType;

    AsPair(long localAs, long remoteAs, ConfedSessionType type) {
      _localAs = localAs;
      _remoteAs = remoteAs;
      _confedSessionType = type;
    }

    public long getLocalAs() {
      return _localAs;
    }

    public long getRemoteAs() {
      return _remoteAs;
    }

    public ConfedSessionType getConfedSessionType() {
      return _confedSessionType;
    }

    public AsPair reverse() {
      return new AsPair(_remoteAs, _localAs, _confedSessionType);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("localAs", _localAs)
          .add("remoteAs", _remoteAs)
          .add("confedType", _confedSessionType)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AsPair)) {
        return false;
      }
      AsPair asPair = (AsPair) o;
      return _localAs == asPair._localAs
          && _remoteAs == asPair._remoteAs
          && _confedSessionType == asPair._confedSessionType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_localAs, _remoteAs, _confedSessionType.ordinal());
    }
  }

  private BgpTopologyUtils() {}
}
