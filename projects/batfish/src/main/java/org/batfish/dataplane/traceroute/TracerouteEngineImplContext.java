package org.batfish.dataplane.traceroute;

import static org.batfish.dataplane.traceroute.FlowTracer.initialFlowTracer;
import static org.batfish.dataplane.traceroute.TracerouteUtils.buildSessionsByIngressInterface;
import static org.batfish.dataplane.traceroute.TracerouteUtils.buildSessionsByOriginatingVrf;
import static org.batfish.dataplane.traceroute.TracerouteUtils.validateInputs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.traceroute.TraceDag;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.InterfaceForwardingBehavior;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpaceContainsIp;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.acl.SourcesReferencedOnDevice;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.dataplane.TracerouteEngineImpl;

/**
 * An implementation of {@link TracerouteEngineImpl#computeTraces(Set, boolean)} and the context
 * (data) needed for it.
 *
 * <p>In particular, it contains all context about the network that is valid for all the flows
 * (since computeTraces computes traces for a set of flows). Each flow is traced concurrently, and
 * the context is shared among each of the concurrent {@link FlowTracer FlowTracers}.
 */
public class TracerouteEngineImplContext {
  private final Map<String, Configuration> _configurations;
  Function<String, Set<String>> _interfacesMatchedOnDevice;
  private final Multimap<NodeInterfacePair, FirewallSessionTraceInfo> _sessionsByIngressInterface;
  private final Map<String, Multimap<String, FirewallSessionTraceInfo>> _sessionsByOriginatingVrf;
  private final Map<String, Map<String, Fib>> _fibs;
  private final Set<Flow> _flows;
  private final ForwardingAnalysis _forwardingAnalysis;
  private final Map<Ip, IpSpaceContainsIp> _containsIp;
  private final boolean _ignoreFilters;
  private final Topology _topology;

  public TracerouteEngineImplContext(
      DataPlane dataPlane,
      Topology topology,
      Set<FirewallSessionTraceInfo> sessions,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreFilters,
      Map<String, Configuration> configurations,
      Function<String, Set<String>> interfacesMatchedOnDevice) {
    _configurations = configurations;
    _interfacesMatchedOnDevice = interfacesMatchedOnDevice;
    _flows = flows;
    _fibs = fibs;
    _ignoreFilters = ignoreFilters;
    _forwardingAnalysis = dataPlane.getForwardingAnalysis();
    _containsIp = new ConcurrentHashMap<>();
    _sessionsByIngressInterface = buildSessionsByIngressInterface(sessions);
    _sessionsByOriginatingVrf = buildSessionsByOriginatingVrf(sessions);
    _topology = topology;
  }

  /** For testing only. */
  @VisibleForTesting
  TracerouteEngineImplContext(
      DataPlane dataPlane,
      Topology topology,
      Set<FirewallSessionTraceInfo> sessions,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreFilters,
      Map<String, Configuration> configurations) {
    this(
        dataPlane,
        topology,
        sessions,
        flows,
        fibs,
        ignoreFilters,
        configurations,
        hostname ->
            SourcesReferencedOnDevice.activeReferencedSources(configurations.get(hostname)));
  }

  /**
   * Builds the possible {@link Trace}s for a {@link Set} of {@link Flow}s in {@link
   * TracerouteEngineImplContext#_flows}
   *
   * @return {@link SortedMap} of {@link Flow} to a {@link List} of {@link Trace}s
   */
  public Map<Flow, TraceDag> buildTraceDags() {
    return _flows.parallelStream()
        .map(
            flow -> {
              validateInputs(_configurations, flow);
              String ingressNodeName = flow.getIngressNode();
              String ingressInterfaceName = flow.getIngressInterface();
              DagTraceRecorder recorder = new DagTraceRecorder(flow);
              initialFlowTracer(this, ingressNodeName, ingressInterfaceName, flow, recorder)
                  .processHop();
              return new SimpleEntry<>(flow, recorder.build());
            })
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Returns dispositions for the special case when a {@link Flow} either exits the network, gets
   * delivered to subnet, gets terminated due to an unreachable neighbor or when information is not
   * sufficient to compute disposition
   *
   * @param hostname Hostname of the current {@link Hop}
   * @param outgoingInterfaceName output interface for the current {@link Hop}
   * @param dstIp Destination IP for the {@link Flow}
   * @return one of {@link FlowDisposition#DELIVERED_TO_SUBNET}, {@link
   *     FlowDisposition#EXITS_NETWORK}, {@link FlowDisposition#INSUFFICIENT_INFO} or {@link
   *     FlowDisposition#NEIGHBOR_UNREACHABLE}
   */
  FlowDisposition computeDisposition(String hostname, String outgoingInterfaceName, Ip dstIp) {
    IpSpaceContainsIp containsIp =
        _containsIp.computeIfAbsent(dstIp, ip -> new IpSpaceContainsIp(ip, ImmutableMap.of()));
    String vrfName =
        _configurations.get(hostname).getAllInterfaces().get(outgoingInterfaceName).getVrfName();
    InterfaceForwardingBehavior interfaceForwardingBehavior =
        _forwardingAnalysis
            .getVrfForwardingBehavior()
            .get(hostname)
            .get(vrfName)
            .getInterfaceForwardingBehavior()
            .get(outgoingInterfaceName);
    if (containsIp.visit(interfaceForwardingBehavior.getDeliveredToSubnet())) {
      return FlowDisposition.DELIVERED_TO_SUBNET;
    } else if (containsIp.visit(interfaceForwardingBehavior.getExitsNetwork())) {
      return FlowDisposition.EXITS_NETWORK;
    } else if (containsIp.visit(interfaceForwardingBehavior.getInsufficientInfo())) {
      return FlowDisposition.INSUFFICIENT_INFO;
    } else if (containsIp.visit(interfaceForwardingBehavior.getNeighborUnreachable())) {
      return FlowDisposition.NEIGHBOR_UNREACHABLE;
    } else {
      throw new BatfishException(
          String.format(
              "No disposition at hostname=%s outgoingInterface=%s for destIp=%s",
              hostname, outgoingInterfaceName, dstIp));
    }
  }

  public Map<String, Configuration> getConfigurations() {
    return _configurations;
  }

  /** Return a FIB for a given node and VRF */
  Optional<Fib> getFib(String node, String vrf) {
    return Optional.ofNullable(getFibs(node).get(vrf));
  }

  /** Get all fibs for a given node */
  public @Nonnull Map<String, Fib> getFibs(String node) {
    return _fibs.getOrDefault(node, ImmutableMap.of());
  }

  boolean getIgnoreFilters() {
    return _ignoreFilters;
  }

  Set<String> getInterfacesMatchedOnDevice(String hostname) {
    return _interfacesMatchedOnDevice.apply(hostname);
  }

  Collection<FirewallSessionTraceInfo> getSessionsForIncomingInterface(
      String node, String inputIface) {
    return _sessionsByIngressInterface.get(NodeInterfacePair.of(node, inputIface));
  }

  Collection<FirewallSessionTraceInfo> getSessionsForOriginatingVrf(String node, String vrf) {
    return _sessionsByOriginatingVrf.getOrDefault(node, ImmutableMultimap.of()).get(vrf);
  }

  /**
   * Returns the name of the interface on the given {@code node} that will accept (NB: not forward)
   * packets destined for {@code ip} in the given {@code vrf}.
   *
   * <p>If the packet is not accepted at this node and vrf, returns {@link Optional#empty()}.
   */
  @Nonnull
  Optional<String> interfaceAcceptingIp(String node, String vrf, Ip ip) {
    IpSpaceContainsIp containsIp =
        _containsIp.computeIfAbsent(ip, i -> new IpSpaceContainsIp(i, ImmutableMap.of()));
    return _forwardingAnalysis
        .getVrfForwardingBehavior()
        .get(node)
        .get(vrf)
        .getInterfaceForwardingBehavior()
        .entrySet()
        .stream()
        .filter(e -> containsIp.visit(e.getValue().getAcceptedIps()))
        .map(Entry::getKey)
        .findAny(); // Should be zero or one.
  }

  /** Returns true if the given VRF will accept traffic to the given IP. */
  boolean vrfAcceptsIp(String node, String vrf, Ip ip) {
    return interfaceAcceptingIp(node, vrf, ip).isPresent();
  }

  /**
   * Returns true if the next node and interface responds for ARP request for given arpIp
   *
   * @param node {@link Configuration} of the next node
   * @param iface Name of the interface in the next node to be tested for ARP
   * @param arpIp ARP for given {@link Ip}
   * @return true if the node will respond to the ARP request
   */
  boolean repliesToArp(String node, String iface, Ip arpIp) {
    IpSpaceContainsIp containsIp =
        _containsIp.computeIfAbsent(arpIp, ip -> new IpSpaceContainsIp(ip, ImmutableMap.of()));
    return containsIp.visit(_forwardingAnalysis.getArpReplies().get(node).get(iface));
  }

  @Nonnull
  SortedSet<NodeInterfacePair> getInterfaceNeighbors(
      String currentNodeName, String outgoingIfaceName) {
    return _topology.getNeighbors(NodeInterfacePair.of(currentNodeName, outgoingIfaceName));
  }
}
