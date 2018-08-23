package org.batfish.dataplane;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.collections.NodeInterfacePair;

class TracerouteEngineImplContext {
  private static class TransmissionContext {

    private final Map<String, IpAccessList> _aclDefinitions;
    private final String _currentNodeName;
    private String _filterOutNotes;
    private final Set<FlowTrace> _flowTraces;
    private final List<FlowTraceHop> _hopsSoFar;
    private final NavigableMap<String, IpSpace> _namedIpSpaces;
    private final Flow _originalFlow;
    private final SortedSet<String> _routesForThisNextHopInterface;
    private final Flow _transformedFlow;

    private TransmissionContext(
        Map<String, IpAccessList> aclDefinitions,
        String currentNodeName,
        Set<FlowTrace> flowTraces,
        List<FlowTraceHop> hopsSoFar,
        NavigableMap<String, IpSpace> namedIpSpaces,
        Flow originalFlow,
        SortedSet<String> routesForThisNextHopInterface,
        Flow transformedFlow) {
      _aclDefinitions = aclDefinitions;
      _currentNodeName = currentNodeName;
      _flowTraces = flowTraces;
      _hopsSoFar = hopsSoFar;
      _namedIpSpaces = namedIpSpaces;
      _originalFlow = originalFlow;
      _routesForThisNextHopInterface = routesForThisNextHopInterface;
      _transformedFlow = transformedFlow;
    }

    private TransmissionContext branch() {
      TransmissionContext transmissionContext =
          new TransmissionContext(
              _aclDefinitions,
              _currentNodeName,
              _flowTraces,
              new ArrayList<>(_hopsSoFar),
              _namedIpSpaces,
              _originalFlow,
              _routesForThisNextHopInterface,
              _transformedFlow);
      transmissionContext._filterOutNotes = _filterOutNotes;
      return transmissionContext;
    }
  }

  private static final String TRACEROUTE_INGRESS_NODE_INTERFACE_NAME =
      "traceroute_source_interface";

  private static final String TRACEROUTE_INGRESS_NODE_NAME = "traceroute_source_node";

  /**
   * Applies the given list of source NAT rules to the given flow and returns the new transformed
   * flow. If {@code sourceNats} is null, empty, or does not contain any ACL rules matching the
   * {@link Flow}, the original flow is returned.
   *
   * <p>Each {@link SourceNat} is expected to be valid: it must have a NAT IP or pool.
   */
  @VisibleForTesting
  static Flow applySourceNat(
      Flow flow,
      @Nullable String srcInterface,
      Map<String, IpAccessList> aclDefinitions,
      Map<String, IpSpace> namedIpSpaces,
      @Nullable List<SourceNat> sourceNats) {
    if (CommonUtil.isNullOrEmpty(sourceNats)) {
      return flow;
    }
    Optional<SourceNat> matchingSourceNat =
        sourceNats
            .stream()
            .filter(
                sourceNat ->
                    sourceNat.getAcl() != null
                        && sourceNat
                                .getAcl()
                                .filter(flow, srcInterface, aclDefinitions, namedIpSpaces)
                                .getAction()
                            != LineAction.DENY)
            .findFirst();
    if (!matchingSourceNat.isPresent()) {
      // No NAT rule matched.
      return flow;
    }
    SourceNat sourceNat = matchingSourceNat.get();
    Ip natPoolStartIp = sourceNat.getPoolIpFirst();
    if (natPoolStartIp == null) {
      throw new BatfishException(
          String.format(
              "Error processing Source NAT rule %s: missing NAT address or pool", sourceNat));
    }
    Flow.Builder transformedFlowBuilder = new Flow.Builder(flow);
    transformedFlowBuilder.setSrcIp(natPoolStartIp);
    return transformedFlowBuilder.build();
  }

  @VisibleForTesting
  static boolean interfaceRepliesToArpRequestForIp(Interface iface, Fib ifaceFib, Ip arpIp) {
    // interfaces without addresses never reply
    if (iface.getAllAddresses().isEmpty()) {
      return false;
    }
    // the interface that owns the arpIp always replies
    if (iface.getAllAddresses().stream().anyMatch(addr -> addr.getIp().equals(arpIp))) {
      return true;
    }

    /*
     * iface does not own arpIp, so it replies if and only if:
     * 1. proxy-arp is enabled
     * 2. the interface's vrf has a route to the destination
     * 3. the destination is not on the incoming edge.
     */
    @Nonnull
    Map<String, Map<Ip, Set<AbstractRoute>>> nextHopInterfaces =
        ifaceFib.getNextHopInterfaces(arpIp);
    return iface.getProxyArp()
        && !nextHopInterfaces.isEmpty()
        && nextHopInterfaces.keySet().stream().noneMatch(iface.getName()::equals);
  }

  private final Map<String, Configuration> _configurations;
  private final DataPlane _dataPlane;
  private final Map<String, Map<String, Fib>> _fibs;
  private final Set<Flow> _flows;
  private final Map<Flow, Set<FlowTrace>> _flowTraces;
  private final ForwardingAnalysis _forwardingAnalysis;
  private final boolean _ignoreAcls;

  TracerouteEngineImplContext(
      DataPlane dataPlane,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreAcls) {
    _configurations = dataPlane.getConfigurations();
    _dataPlane = dataPlane;
    _flows = flows;
    _flowTraces = new ConcurrentHashMap<>();
    _fibs = fibs;
    _ignoreAcls = ignoreAcls;
    _forwardingAnalysis = _dataPlane.getForwardingAnalysis();
  }

  private void collectFlowTraces(
      String currentNodeName,
      String currentVrfName,
      Set<Edge> visitedEdges,
      List<FlowTraceHop> hopsSoFar,
      Set<FlowTrace> flowTraces,
      Flow originalFlow,
      Flow transformedFlow) {
    Ip dstIp = transformedFlow.getDstIp();
    Configuration currentConfiguration = _configurations.get(currentNodeName);
    if (currentConfiguration == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", currentNodeName));
    }
    if (_dataPlane
        .getIpVrfOwners()
        .getOrDefault(dstIp, ImmutableMap.of())
        .getOrDefault(currentNodeName, ImmutableSet.of())
        .contains(currentVrfName)) {
      FlowTrace trace =
          new FlowTrace(FlowDisposition.ACCEPTED, hopsSoFar, FlowDisposition.ACCEPTED.toString());
      flowTraces.add(trace);
    } else {
      Map<String, IpAccessList> aclDefinitions = currentConfiguration.getIpAccessLists();
      NavigableMap<String, IpSpace> namedIpSpaces = currentConfiguration.getIpSpaces();
      String vrfName;
      String srcInterface;
      if (hopsSoFar.isEmpty()) {
        vrfName = transformedFlow.getIngressVrf();
        srcInterface = null;
      } else {
        FlowTraceHop lastHop = hopsSoFar.get(hopsSoFar.size() - 1);
        srcInterface = lastHop.getEdge().getInt2();
        vrfName = currentConfiguration.getInterfaces().get(srcInterface).getVrf().getName();
      }
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute =
          _fibs.get(currentNodeName).get(vrfName).getNextHopInterfacesByRoute(dstIp);
      Map<String, Map<Ip, Set<AbstractRoute>>> nextHopInterfacesWithRoutes =
          _fibs.get(currentNodeName).get(vrfName).getNextHopInterfaces(dstIp);
      if (!nextHopInterfacesWithRoutes.isEmpty()) {
        for (String nextHopInterfaceName : nextHopInterfacesWithRoutes.keySet()) {
          // SortedSet<String> routesForThisNextHopInterface = new
          // TreeSet<>(
          // nextHopInterfacesWithRoutes.get(nextHopInterfaceName)
          // .stream().map(ar -> ar.toString())
          // .collect(Collectors.toSet()));
          SortedSet<String> routesForThisNextHopInterface = new TreeSet<>();
          Ip finalNextHopIp = null;
          for (Entry<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> e :
              nextHopInterfacesByRoute.entrySet()) {
            AbstractRoute routeCandidate = e.getKey();
            Map<String, Map<Ip, Set<AbstractRoute>>> routeCandidateNextHopInterfaces = e.getValue();
            if (routeCandidateNextHopInterfaces.containsKey(nextHopInterfaceName)) {
              Ip nextHopIp = routeCandidate.getNextHopIp();
              if (!nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
                Set<Ip> finalNextHopIps =
                    routeCandidateNextHopInterfaces.get(nextHopInterfaceName).keySet();
                if (finalNextHopIps.size() > 1) {
                  throw new BatfishException(
                      "Can not currently handle multiple final next hop ips across multiple "
                          + "routes leading to one next hop interface");
                }
                Ip newFinalNextHopIp = finalNextHopIps.iterator().next();
                if (finalNextHopIp != null && !newFinalNextHopIp.equals(finalNextHopIp)) {
                  throw new BatfishException(
                      "Can not currently handle multiple final next hop ips for same next hop "
                          + "interface");
                }
                finalNextHopIp = newFinalNextHopIp;
              }
              routesForThisNextHopInterface.add(routeCandidate + "_fnhip:" + finalNextHopIp);
            }
          }
          NodeInterfacePair nextHopInterface =
              new NodeInterfacePair(currentNodeName, nextHopInterfaceName);
          if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
            List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
            Edge newEdge =
                new Edge(
                    nextHopInterface,
                    new NodeInterfacePair(
                        Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
            FlowTraceHop newHop =
                new FlowTraceHop(
                    newEdge,
                    routesForThisNextHopInterface,
                    null,
                    null,
                    hopFlow(originalFlow, transformedFlow));
            newHops.add(newHop);
            FlowTrace nullRouteTrace =
                new FlowTrace(
                    FlowDisposition.NULL_ROUTED, newHops, FlowDisposition.NULL_ROUTED.toString());
            flowTraces.add(nullRouteTrace);
          } else {
            Interface outgoingInterface =
                _configurations
                    .get(nextHopInterface.getHostname())
                    .getInterfaces()
                    .get(nextHopInterface.getInterface());

            // Apply any relevant source NAT rules.
            Flow newTransformedFlow =
                applySourceNat(
                    transformedFlow,
                    srcInterface,
                    aclDefinitions,
                    namedIpSpaces,
                    outgoingInterface.getSourceNats());

            SortedSet<Edge> edges =
                _dataPlane.getTopology().getInterfaceEdges().get(nextHopInterface);
            TransmissionContext transmissionContext =
                new TransmissionContext(
                    aclDefinitions,
                    currentNodeName,
                    flowTraces,
                    hopsSoFar,
                    namedIpSpaces,
                    originalFlow,
                    routesForThisNextHopInterface,
                    newTransformedFlow);
            if (edges != null) {
              processCurrentNextHopInterfaceEdges(
                  visitedEdges,
                  srcInterface,
                  dstIp,
                  nextHopInterfaceName,
                  finalNextHopIp,
                  nextHopInterface,
                  edges,
                  transmissionContext);
            } else {
              /*
               * Interface has no edges
               */
              /* Check if denied out. If not, make standard neighbor-unreachable trace. */
              IpAccessList outFilter = outgoingInterface.getOutgoingFilter();
              boolean denied = false;
              if (!_ignoreAcls && outFilter != null) {
                FlowDisposition disposition = FlowDisposition.DENIED_OUT;
                denied =
                    flowTraceFilterHelper(
                        srcInterface,
                        outFilter,
                        disposition,
                        nextHopInterface,
                        transmissionContext);
              }
              if (!denied) {
                Edge neighborUnreachbleEdge =
                    new Edge(
                        nextHopInterface,
                        new NodeInterfacePair(
                            Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
                FlowTraceHop neighborUnreachableHop =
                    new FlowTraceHop(
                        neighborUnreachbleEdge,
                        routesForThisNextHopInterface,
                        null,
                        null,
                        hopFlow(originalFlow, newTransformedFlow));
                neighborUnreachableHop.setFilterOut(transmissionContext._filterOutNotes);
                hopsSoFar.add(neighborUnreachableHop);
                FlowTrace trace =
                    new FlowTrace(
                        FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
                        hopsSoFar,
                        FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK.toString());
                flowTraces.add(trace);
              }
            }
          }
        }
      } else {
        FlowTrace trace =
            new FlowTrace(FlowDisposition.NO_ROUTE, hopsSoFar, FlowDisposition.NO_ROUTE.toString());
        flowTraces.add(trace);
      }
    }
  }

  private boolean flowTraceFilterHelper(
      @Nullable String srcInterface,
      IpAccessList filter,
      FlowDisposition disposition,
      @Nullable NodeInterfacePair outInterface,
      TransmissionContext transmissionContext) {
    boolean out = disposition == FlowDisposition.DENIED_OUT;
    FilterResult outResult =
        filter.filter(
            transmissionContext._transformedFlow,
            srcInterface,
            transmissionContext._aclDefinitions,
            transmissionContext._namedIpSpaces);
    String outFilterName = filter.getName();
    Integer matchLine = outResult.getMatchLine();
    String lineDesc;
    if (matchLine != null) {
      lineDesc = filter.getLines().get(matchLine).getName();
      if (lineDesc == null) {
        lineDesc = "line:" + matchLine;
      }
    } else {
      lineDesc = "no-match";
    }
    boolean denied = outResult.getAction() == LineAction.DENY;
    if (denied) {
      String notes = disposition + "{" + outFilterName + "}{" + lineDesc + "}";
      if (out) {
        Edge deniedOutEdge =
            new Edge(
                outInterface,
                new NodeInterfacePair(Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
        FlowTraceHop deniedOutHop =
            new FlowTraceHop(
                deniedOutEdge,
                transmissionContext._routesForThisNextHopInterface,
                null,
                null,
                hopFlow(transmissionContext._originalFlow, transmissionContext._transformedFlow));
        transmissionContext._hopsSoFar.add(deniedOutHop);
      }
      FlowTrace trace = new FlowTrace(disposition, transmissionContext._hopsSoFar, notes);
      transmissionContext._flowTraces.add(trace);
    } else {
      String filterNotes = "{" + outFilterName + "}{" + lineDesc + "}";
      if (out) {
        transmissionContext._filterOutNotes = filterNotes;
      } else {
        FlowTraceHop hop =
            transmissionContext._hopsSoFar.get(transmissionContext._hopsSoFar.size() - 1);
        hop.setFilterIn(filterNotes);
      }
    }
    return denied;
  }

  @Nullable
  private Flow hopFlow(Flow originalFlow, Flow transformedFlow) {
    if (originalFlow == transformedFlow) {
      return null;
    } else {
      return transformedFlow;
    }
  }

  private FlowTrace neighborUnreachableTrace(
      NodeInterfacePair srcInterface, TransmissionContext transmissionContext) {
    Edge neighborUnreachbleEdge =
        new Edge(
            srcInterface,
            new NodeInterfacePair(Configuration.NODE_NONE_NAME, Interface.NULL_INTERFACE_NAME));
    FlowTraceHop neighborUnreachableHop =
        new FlowTraceHop(
            neighborUnreachbleEdge,
            transmissionContext._routesForThisNextHopInterface,
            null,
            null,
            hopFlow(transmissionContext._originalFlow, transmissionContext._transformedFlow));
    neighborUnreachableHop.setFilterOut(transmissionContext._filterOutNotes);
    List<FlowTraceHop> newHops =
        ImmutableList.<FlowTraceHop>builder()
            .addAll(transmissionContext._hopsSoFar)
            .add(neighborUnreachableHop)
            .build();
    FlowTrace trace =
        new FlowTrace(
            FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
            newHops,
            FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK.toString());
    return trace;
  }

  private void processCurrentNextHopInterfaceEdges(
      Set<Edge> visitedEdges,
      String srcInterface,
      Ip dstIp,
      String nextHopInterfaceName,
      @Nullable Ip finalNextHopIp,
      NodeInterfacePair nextHopInterface,
      SortedSet<Edge> edges,
      TransmissionContext transmissionContext) {
    if (!processFlowTransmission(
        srcInterface,
        dstIp,
        nextHopInterfaceName,
        finalNextHopIp,
        nextHopInterface,
        transmissionContext)) {
      return;
    }
    for (Edge edge : edges) {
      if (!edge.getNode1().equals(transmissionContext._currentNodeName)) {
        continue;
      }
      processFlowReception(
          edge, transmissionContext, visitedEdges, finalNextHopIp != null ? finalNextHopIp : dstIp);
    }
  }

  void processFlowReception(
      Edge edge,
      TransmissionContext oldTransmissionContext,
      Set<Edge> visitedEdges,
      @Nullable Ip arpIp) {
    // do nothing if this neighbor would not have replied to ARP (excluding injection case)
    if (arpIp != null
        && !_forwardingAnalysis
            .getArpReplies()
            .get(edge.getNode2())
            .get(edge.getInt2())
            .containsIp(arpIp, _configurations.get(edge.getNode2()).getIpSpaces())) {
      return;
    }

    // branch because other edges share the same oldTransmissionContext
    TransmissionContext transmissionContext = oldTransmissionContext.branch();
    Set<Edge> newVisitedEdges = new LinkedHashSet<>(visitedEdges);
    FlowTraceHop newHop =
        new FlowTraceHop(
            edge,
            transmissionContext._routesForThisNextHopInterface,
            null,
            null,
            hopFlow(transmissionContext._originalFlow, transmissionContext._transformedFlow));
    newHop.setFilterOut(transmissionContext._filterOutNotes);
    newVisitedEdges.add(edge);
    transmissionContext._hopsSoFar.add(newHop);
    if (visitedEdges.contains(edge)) {
      FlowTrace trace =
          new FlowTrace(
              FlowDisposition.LOOP,
              transmissionContext._hopsSoFar,
              FlowDisposition.LOOP.toString());
      transmissionContext._flowTraces.add(trace);
      return;
    }
    String nextNodeName = edge.getNode2();
    // check input filter
    Interface nextInterface = _configurations.get(nextNodeName).getInterfaces().get(edge.getInt2());
    IpAccessList inFilter = nextInterface.getIncomingFilter();
    if (!_ignoreAcls && inFilter != null) {
      FlowDisposition disposition = FlowDisposition.DENIED_IN;
      boolean denied =
          flowTraceFilterHelper(null, inFilter, disposition, null, transmissionContext);
      if (denied) {
        return;
      }
    }
    // recurse
    collectFlowTraces(
        nextNodeName,
        nextInterface.getVrfName(),
        newVisitedEdges,
        transmissionContext._hopsSoFar,
        transmissionContext._flowTraces,
        transmissionContext._originalFlow,
        transmissionContext._transformedFlow);
  }

  SortedMap<Flow, Set<FlowTrace>> processFlows() {
    _flows
        .parallelStream()
        .forEach(
            flow -> {
              Set<FlowTrace> currentFlowTraces = new TreeSet<>();
              _flowTraces.put(flow, currentFlowTraces);
              String ingressNodeName = flow.getIngressNode();
              if (ingressNodeName == null) {
                throw new BatfishException(
                    "Cannot construct flow trace since ingressNode is not specified");
              }
              Ip dstIp = flow.getDstIp();
              if (dstIp == null) {
                throw new BatfishException(
                    "Cannot construct flow trace since dstIp is not specified");
              }
              Set<Edge> visitedEdges = Collections.emptySet();
              List<FlowTraceHop> hops = new ArrayList<>();
              String ingressInterfaceName = flow.getIngressInterface();
              if (ingressInterfaceName != null) {
                Edge edge =
                    new Edge(
                        TRACEROUTE_INGRESS_NODE_NAME,
                        TRACEROUTE_INGRESS_NODE_INTERFACE_NAME,
                        ingressNodeName,
                        ingressInterfaceName);
                TransmissionContext transmissionContext =
                    new TransmissionContext(
                        _configurations.get(ingressNodeName).getIpAccessLists(),
                        ingressNodeName,
                        currentFlowTraces,
                        hops,
                        _configurations.get(ingressNodeName).getIpSpaces(),
                        flow,
                        new TreeSet<>(),
                        flow);
                processFlowReception(edge, transmissionContext, new TreeSet<>(), null);
              } else {
                collectFlowTraces(
                    ingressNodeName,
                    firstNonNull(flow.getIngressVrf(), Configuration.DEFAULT_VRF_NAME),
                    visitedEdges,
                    hops,
                    currentFlowTraces,
                    flow,
                    flow);
              }
            });
    return new TreeMap<>(_flowTraces);
  }

  private boolean processFlowTransmission(
      String srcInterface,
      Ip dstIp,
      String nextHopInterfaceName,
      Ip finalNextHopIp,
      NodeInterfacePair nextHopInterface,
      TransmissionContext transmissionContext) {
    // check output filter
    IpAccessList outFilter =
        _configurations
            .get(transmissionContext._currentNodeName)
            .getInterfaces()
            .get(nextHopInterfaceName)
            .getOutgoingFilter();
    if (!_ignoreAcls && outFilter != null) {
      FlowDisposition disposition = FlowDisposition.DENIED_OUT;
      boolean denied =
          flowTraceFilterHelper(
              srcInterface,
              outFilter,
              disposition,
              new NodeInterfacePair(transmissionContext._currentNodeName, nextHopInterfaceName),
              transmissionContext);
      if (denied) {
        return false;
      }
    }
    Ip arpIp = finalNextHopIp != null ? finalNextHopIp : dstIp;
    Configuration c = _configurations.get(transmissionContext._currentNodeName);
    // halt processing and add neighbor-unreachable trace if no one would respond
    if (_forwardingAnalysis
        .getNeighborUnreachable()
        .get(transmissionContext._currentNodeName)
        .get(c.getInterfaces().get(nextHopInterfaceName).getVrfName())
        .get(nextHopInterfaceName)
        .containsIp(arpIp, c.getIpSpaces())) {
      FlowTrace trace = neighborUnreachableTrace(nextHopInterface, transmissionContext);
      transmissionContext._flowTraces.add(trace);
      return false;
    }
    return true;
  }
}
