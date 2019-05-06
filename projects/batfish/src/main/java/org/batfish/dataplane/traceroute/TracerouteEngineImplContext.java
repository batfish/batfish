package org.batfish.dataplane.traceroute;

import static org.batfish.dataplane.traceroute.TracerouteUtils.buildSessionsByIngressInterface;
import static org.batfish.dataplane.traceroute.TracerouteUtils.validateInputs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/**
 * An implementation of {@link org.batfish.dataplane.TracerouteEngineImpl#computeTraces(Set,
 * boolean)} and the context (data) needed for it.
 *
 * <p>In particular, it contains all context about the network that is valid for all the flows
 * (since computeTraces computes traces for a set of flows). Each flow is traced concurrently, and
 * the context is shared among each of the concurrent {@link FlowTracer FlowTracers}.
 */
public class TracerouteEngineImplContext {
  private final Map<String, Configuration> _configurations;
  private final DataPlane _dataPlane;
  private final Multimap<NodeInterfacePair, FirewallSessionTraceInfo> _sessionsByIngressInterface;
  private final Map<String, Map<String, Fib>> _fibs;
  private final Set<Flow> _flows;
  private final ForwardingAnalysis _forwardingAnalysis;
  private final boolean _ignoreFilters;
  private final Topology _topology;

  public TracerouteEngineImplContext(
      DataPlane dataPlane,
      Topology topology,
      Set<FirewallSessionTraceInfo> sessions,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreFilters) {
    _configurations = dataPlane.getConfigurations();
    _dataPlane = dataPlane;
    _flows = flows;
    _fibs = fibs;
    _ignoreFilters = ignoreFilters;
    _forwardingAnalysis = _dataPlane.getForwardingAnalysis();
    _sessionsByIngressInterface = buildSessionsByIngressInterface(sessions);
    _topology = topology;
  }

  /**
   * Builds the possible {@link Trace}s for a {@link Set} of {@link Flow}s in {@link
   * TracerouteEngineImplContext#_flows}
   *
   * @return {@link SortedMap} of {@link Flow} to a {@link List} of {@link Trace}s
   */
  public SortedMap<Flow, List<TraceAndReverseFlow>> buildTracesAndReturnFlows() {
    Map<Flow, List<TraceAndReverseFlow>> traces = new ConcurrentHashMap<>();
    _flows
        .parallelStream()
        .forEach(
            flow -> {
              List<TraceAndReverseFlow> currentTraces =
                  traces.computeIfAbsent(flow, k -> new ArrayList<>());
              validateInputs(_configurations, flow);
              String ingressNodeName = flow.getIngressNode();
              String ingressInterfaceName = flow.getIngressInterface();
              new FlowTracer(this, ingressNodeName, ingressInterfaceName, flow, currentTraces::add)
                  .processHop();
            });
    return new TreeMap<>(traces);
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
    String vrfName =
        _configurations.get(hostname).getAllInterfaces().get(outgoingInterfaceName).getVrfName();
    if (_forwardingAnalysis
        .getDeliveredToSubnet()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
      return FlowDisposition.DELIVERED_TO_SUBNET;
    } else if (_forwardingAnalysis
        .getExitsNetwork()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
      return FlowDisposition.EXITS_NETWORK;
    } else if (_forwardingAnalysis
        .getInsufficientInfo()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
      return FlowDisposition.INSUFFICIENT_INFO;
    } else if (_forwardingAnalysis
        .getNeighborUnreachable()
        .get(hostname)
        .get(vrfName)
        .get(outgoingInterfaceName)
        .containsIp(dstIp, ImmutableMap.of())) {
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

  Optional<Fib> getFib(String node, String vrf) {
    return Optional.ofNullable(_fibs.getOrDefault(node, ImmutableMap.of()).get(vrf));
  }

  boolean getIgnoreFilters() {
    return _ignoreFilters;
  }

  Collection<FirewallSessionTraceInfo> getSessions(String node, String inputIface) {
    return _sessionsByIngressInterface.get(new NodeInterfacePair(node, inputIface));
  }

  boolean ownsIp(String node, String vrf, Ip ip) {
    return _dataPlane
        .getIpVrfOwners()
        .getOrDefault(ip, ImmutableMap.of())
        .getOrDefault(node, ImmutableSet.of())
        .contains(vrf);
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
    return _forwardingAnalysis
        .getArpReplies()
        .get(node)
        .get(iface)
        .containsIp(arpIp, ImmutableMap.of());
  }

  /**
   * @return true if the an ARP request for {@param arpIp} sent out {@param outIface} will not
   *     receive a reply.
   */
  boolean willNotReceiveArpReply(String node, String vrf, String outIface, Ip arpIp) {
    return _forwardingAnalysis
        .getNeighborUnreachableOrExitsNetwork()
        .get(node)
        .get(vrf)
        .get(outIface)
        .containsIp(arpIp, ImmutableMap.of());
  }

  @Nonnull
  SortedSet<NodeInterfacePair> getInterfaceNeighbors(
      String currentNodeName, String outgoingIfaceName) {
    return _topology.getNeighbors(new NodeInterfacePair(currentNodeName, outgoingIfaceName));
  }
}
