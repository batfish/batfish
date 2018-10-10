package org.batfish.dataplane.traceroute2;

import static org.batfish.dataplane.traceroute2.TracerouteUtils.createDummyHop;
import static org.batfish.dataplane.traceroute2.TracerouteUtils.createEnterSrcIfaceStep;
import static org.batfish.dataplane.traceroute2.TracerouteUtils.isArpSuccessful;
import static org.batfish.dataplane.traceroute2.TracerouteUtils.validateInputs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Collections;
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
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow2.EnterSrcIfaceStep;
import org.batfish.datamodel.flow2.EnterSrcIfaceStep.EnterSrcIfaceAction;
import org.batfish.datamodel.flow2.ExitOutIfaceStep;
import org.batfish.datamodel.flow2.ExitOutIfaceStep.ExitOutIfaceAction;
import org.batfish.datamodel.flow2.ExitOutIfaceStep.ExitOutIfaceStepDetail;
import org.batfish.datamodel.flow2.RoutingStep;
import org.batfish.datamodel.flow2.RoutingStep.RoutingStepAction;
import org.batfish.datamodel.flow2.RoutingStep.RoutingStepDetail;
import org.batfish.datamodel.flow2.Step;
import org.batfish.datamodel.flow2.StepActionResult;
import org.batfish.datamodel.flow2.Trace;
import org.batfish.datamodel.flow2.TraceHop;

public class TracerouteEngineImplContext2 {

  private static class TransmissionContext {
    private final Map<String, IpAccessList> _aclDefinitions;
    private final String _currentNodeName;
    private String _filterOutNotes;
    private final Set<Trace> _flowTraces;
    private final List<TraceHop> _hopsSoFar;
    private final NavigableMap<String, IpSpace> _namedIpSpaces;
    private final Flow _originalFlow;
    private final SortedSet<String> _routesForThisNextHopInterface;
    private final Flow _transformedFlow;

    private TransmissionContext(
        Map<String, IpAccessList> aclDefinitions,
        String currentNodeName,
        Set<Trace> flowTraces,
        List<TraceHop> hopsSoFar,
        NavigableMap<String, IpSpace> namedIpSpaces,
        Flow originalFlow,
        SortedSet<String> routesForThisNextHopInterface,
        Flow transformedFlow) {
      _aclDefinitions = aclDefinitions;
      _currentNodeName = currentNodeName;
      _flowTraces = flowTraces;
      _hopsSoFar = new ArrayList<>(hopsSoFar);
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
              _hopsSoFar,
              _namedIpSpaces,
              _originalFlow,
              _routesForThisNextHopInterface,
              _transformedFlow);
      transmissionContext._filterOutNotes = _filterOutNotes;
      return transmissionContext;
    }
  }

  static final String TRACEROUTE_DUMMY_OUT_INTERFACE = "traceroute_dummy_out_interface";

  static final String TRACEROUTE_DUMMY_NODE = "traceroute_dummy_node";

  private final Map<String, Configuration> _configurations;
  private final DataPlane _dataPlane;
  private final Map<String, Map<String, Fib>> _fibs;
  private final Set<Flow> _flows;
  private final ForwardingAnalysis _forwardingAnalysis;
  private final boolean _ignoreAcls;

  public TracerouteEngineImplContext2(
      DataPlane dataPlane,
      Set<Flow> flows,
      Map<String, Map<String, Fib>> fibs,
      boolean ignoreAcls) {
    _configurations = dataPlane.getConfigurations();
    _dataPlane = dataPlane;
    _flows = flows;
    _fibs = fibs;
    _ignoreAcls = ignoreAcls;
    _forwardingAnalysis = _dataPlane.getForwardingAnalysis();
  }

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

  private void processCurrentNextHopInterfaceEdges(
      String currentNodeName,
      Set<TraceHop> visitedHops,
      @Nullable String srcInterface,
      Ip dstIp,
      String nextHopInterfaceName,
      @Nullable Ip finalNextHopIp,
      SortedSet<Edge> edges,
      TransmissionContext transmissionContext,
      ImmutableList.Builder<Step> stepBuilder) {
    if (!processFlowTransmission(
        currentNodeName,
        srcInterface,
        dstIp,
        nextHopInterfaceName,
        finalNextHopIp,
        transmissionContext,
        stepBuilder)) {
      return;
    }
    stepBuilder.add(
        ExitOutIfaceStep.builder()
            .setDetail(
                ExitOutIfaceStepDetail.builder()
                    .setOutputInterface(
                        new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                    .build())
            .setAction(new ExitOutIfaceAction(StepActionResult.SENT_OUT, null))
            .build());
    TraceHop traceHop = new TraceHop(currentNodeName, stepBuilder.build());
    transmissionContext._hopsSoFar.add(traceHop);
    for (Edge edge : edges) {
      if (!edge.getNode1().equals(currentNodeName)) {
        continue;
      }
      if (isArpSuccessful(
          finalNextHopIp != null ? finalNextHopIp : dstIp,
          _forwardingAnalysis,
          _configurations.get(edge.getNode2()),
          edge.getInt2())) {
        processHop(
            edge.getNode2(),
            edge.getInt2(),
            null,
            transmissionContext,
            transmissionContext._transformedFlow,
            visitedHops);
      }
    }
  }

  private boolean processFlowTransmission(
      String currentNodeName,
      @Nullable String srcInterface,
      Ip dstIp,
      String nextHopInterfaceName,
      @Nullable Ip finalNextHopIp,
      TransmissionContext transmissionContext,
      ImmutableList.Builder<Step> stepBuilder) {
    // check output filter
    IpAccessList outFilter =
        _configurations
            .get(transmissionContext._currentNodeName)
            .getAllInterfaces()
            .get(nextHopInterfaceName)
            .getOutgoingFilter();
    if (!_ignoreAcls && outFilter != null) {
      FilterResult filterResult =
          outFilter.filter(
              transmissionContext._transformedFlow,
              srcInterface,
              transmissionContext._aclDefinitions,
              transmissionContext._namedIpSpaces);
      if (filterResult.getAction() == LineAction.DENY) {
        stepBuilder.add(
            ExitOutIfaceStep.builder()
                .setDetail(
                    ExitOutIfaceStepDetail.builder()
                        .setOutputInterface(
                            new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                        .build())
                .setAction(new ExitOutIfaceAction(StepActionResult.DENIED_OUT, null))
                .build());
        TraceHop deniedOutHop = new TraceHop(currentNodeName, stepBuilder.build());
        transmissionContext._hopsSoFar.add(deniedOutHop);
        Trace trace = new Trace(FlowDisposition.DENIED_OUT, transmissionContext._hopsSoFar);
        transmissionContext._flowTraces.add(trace);
        return false;
      }
    }
    Ip arpIp = finalNextHopIp != null ? finalNextHopIp : dstIp;
    Configuration c = _configurations.get(transmissionContext._currentNodeName);
    // halt processing and add neighbor-unreachable trace if no one would respond
    if (_forwardingAnalysis
        .getNeighborUnreachable()
        .get(transmissionContext._currentNodeName)
        .get(c.getAllInterfaces().get(nextHopInterfaceName).getVrfName())
        .get(nextHopInterfaceName)
        .containsIp(arpIp, c.getIpSpaces())) {
      stepBuilder.add(
          ExitOutIfaceStep.builder()
              .setDetail(
                  ExitOutIfaceStepDetail.builder()
                      .setOutputInterface(
                          new NodeInterfacePair(currentNodeName, nextHopInterfaceName))
                      .build())
              .setAction(
                  new ExitOutIfaceAction(
                      StepActionResult.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK, null))
              .build());
      TraceHop neighborUnreachableOrExitsNetwork =
          new TraceHop(currentNodeName, stepBuilder.build());
      transmissionContext._hopsSoFar.add(neighborUnreachableOrExitsNetwork);
      Trace trace =
          new Trace(
              FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
              transmissionContext._hopsSoFar);
      transmissionContext._flowTraces.add(trace);
      return false;
    }
    return true;
  }

  /**
   * Builds all possible flows with the corresponding {@link Trace}
   *
   * @return {@link SortedMap} of {@link Flow} to a {@link Set} of {@link Trace}
   */
  public SortedMap<Flow, Set<Trace>> buildFlows() {
    Map<Flow, Set<Trace>> flowTraces = new ConcurrentHashMap<>();
    _flows
        .parallelStream()
        .forEach(
            flow -> {
              Set<Trace> currentFlowTraces = flowTraces.computeIfAbsent(flow, k -> new TreeSet<>());
              validateInputs(_configurations, flow);
              String ingressNodeName = flow.getIngressNode();
              Set<TraceHop> visitedHops = Collections.emptySet();
              List<TraceHop> hops = new ArrayList<>();
              String ingressInterfaceName = flow.getIngressInterface();
              String ingressVrfName = flow.getIngressVrf();
              if (ingressInterfaceName != null) {
                TraceHop dummyHop = createDummyHop();
                hops.add(dummyHop);
                TransmissionContext transmissionContext =
                    new TransmissionContext(
                        Maps.newHashMap(),
                        dummyHop.getNodeName(),
                        currentFlowTraces,
                        hops,
                        Maps.newTreeMap(),
                        flow,
                        new TreeSet<>(),
                        flow);
                if (isArpSuccessful(
                    flow.getDstIp(),
                    _forwardingAnalysis,
                    _configurations.get(ingressNodeName),
                    ingressInterfaceName)) {
                  processHop(
                      ingressNodeName,
                      ingressInterfaceName,
                      ingressVrfName,
                      transmissionContext,
                      flow,
                      visitedHops);
                }
              } else {
                TransmissionContext transmissionContext =
                    new TransmissionContext(
                        Maps.newHashMap(),
                        flow.getIngressNode(),
                        currentFlowTraces,
                        hops,
                        Maps.newTreeMap(),
                        flow,
                        new TreeSet<>(),
                        flow);
                processHop(
                    ingressNodeName,
                    ingressInterfaceName,
                    ingressVrfName,
                    transmissionContext,
                    flow,
                    visitedHops);
              }
            });
    return new TreeMap<>(flowTraces);
  }

  private void processHop(
      String currentNodeName,
      @Nullable String inputIfaceName,
      @Nullable String inputVrfName,
      TransmissionContext oldTransmissionContext,
      Flow currentFlow,
      Set<TraceHop> visitedHops) {
    List<Step> steps = new ArrayList<>();
    Configuration currentConfiguration = _configurations.get(currentNodeName);
    if (currentConfiguration == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", currentNodeName));
    }

    Map<String, IpAccessList> aclDefinitions = currentConfiguration.getIpAccessLists();
    NavigableMap<String, IpSpace> namedIpSpaces = currentConfiguration.getIpSpaces();

    TransmissionContext transmissionContext = oldTransmissionContext.branch();

    Ip dstIp = currentFlow.getDstIp();

    // trace was received on a source interface of this hop
    if (inputIfaceName != null || inputVrfName != null) {
      EnterSrcIfaceStep enterSrcIfacStep =
          createEnterSrcIfaceStep(
              currentConfiguration,
              inputIfaceName,
              inputVrfName,
              _ignoreAcls,
              currentFlow,
              aclDefinitions,
              namedIpSpaces,
              _dataPlane);
      if (enterSrcIfacStep != null) {
        steps.add(enterSrcIfacStep);
        if (enterSrcIfacStep.getAction() != null) {
          EnterSrcIfaceAction enterSrcIfaceAction =
              (EnterSrcIfaceAction) enterSrcIfacStep.getAction();
          if (enterSrcIfaceAction.getActionResult() == StepActionResult.ACCEPTED) {
            TraceHop acceptedHop = new TraceHop(currentNodeName, ImmutableList.copyOf(steps));
            transmissionContext._hopsSoFar.add(acceptedHop);
            Trace trace = new Trace(FlowDisposition.ACCEPTED, transmissionContext._hopsSoFar);
            transmissionContext._flowTraces.add(trace);
            return;
          }
        }
      }
    }

    // Figure out where the trace came from..
    String vrfName;
    if (inputIfaceName == null) {
      vrfName = currentFlow.getIngressVrf();
    } else {
      vrfName = currentConfiguration.getAllInterfaces().get(inputIfaceName).getVrfName();
    }

    // .. and what the next hops are based on the FIB.
    Fib currentFib = _fibs.get(currentNodeName).get(vrfName);
    Set<String> nextHopInterfaces = currentFib.getNextHopInterfaces(dstIp);
    if (nextHopInterfaces.isEmpty()) {
      // add a step for  NO_ROUTE from source to output interface
      RoutingStep.Builder routingStepBuilder = RoutingStep.builder();
      routingStepBuilder
          .setDetail(RoutingStepDetail.builder().build())
          .setAction(new RoutingStepAction(StepActionResult.NO_ROUTE, null));
      steps.add(routingStepBuilder.build());
      TraceHop noRouteHop = new TraceHop(currentNodeName, ImmutableList.copyOf(steps));
      transmissionContext._hopsSoFar.add(noRouteHop);
      Trace trace = new Trace(FlowDisposition.NO_ROUTE, transmissionContext._hopsSoFar);
      transmissionContext._flowTraces.add(trace);
      return;
    }

    Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute =
        currentFib.getNextHopInterfacesByRoute(dstIp);

    // For every interface with a route to the dst IP
    for (String nextHopInterfaceName : nextHopInterfaces) {
      TreeMultimap<Ip, AbstractRoute> resolvedNextHopWithRoutes = TreeMultimap.create();

      // Loop over all matching routes that use nextHopInterfaceName as one of the next hop
      // interfaces.
      for (Entry<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> e :
          nextHopInterfacesByRoute.entrySet()) {
        Map<Ip, Set<AbstractRoute>> finalNextHops = e.getValue().get(nextHopInterfaceName);
        if (finalNextHops == null || finalNextHops.isEmpty()) {
          continue;
        }

        AbstractRoute routeCandidate = e.getKey();
        Ip nextHopIp = routeCandidate.getNextHopIp();
        if (nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
          resolvedNextHopWithRoutes.put(nextHopIp, routeCandidate);
        } else {
          for (Ip resolvedNextHopIp : finalNextHops.keySet()) {
            resolvedNextHopWithRoutes.put(resolvedNextHopIp, routeCandidate);
          }
        }
      }

      NodeInterfacePair nextHopInterface =
          new NodeInterfacePair(currentNodeName, nextHopInterfaceName);
      resolvedNextHopWithRoutes
          .asMap()
          .forEach(
              (resolvedNextHopIp, routeCandidates) -> {
                // Later parts of the stack expect null instead of unset to trigger proxy arp, etc.
                Ip finalNextHopIp =
                    Route.UNSET_ROUTE_NEXT_HOP_IP.equals(resolvedNextHopIp)
                        ? null
                        : resolvedNextHopIp;
                SortedSet<String> routesForThisNextHopInterface =
                    routeCandidates
                        .stream()
                        .map(rc -> rc + "_fnhip:" + finalNextHopIp)
                        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));

                ImmutableList.Builder<Step> clonedStepsBuilder = ImmutableList.builder();
                clonedStepsBuilder.addAll(steps);
                clonedStepsBuilder.add(
                    RoutingStep.builder()
                        .setDetail(
                            RoutingStepDetail.builder()
                                .setRoutesConsidered(routesForThisNextHopInterface)
                                .build())
                        .setAction(new RoutingStepAction(StepActionResult.SENT_OUT, null))
                        .build());

                if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
                  clonedStepsBuilder.add(
                      ExitOutIfaceStep.builder()
                          .setDetail(
                              ExitOutIfaceStepDetail.builder()
                                  .setOutputInterface(
                                      new NodeInterfacePair(
                                          currentNodeName, Interface.NULL_INTERFACE_NAME))
                                  .build())
                          .setAction(new ExitOutIfaceAction(StepActionResult.NULL_ROUTED, null))
                          .build());
                  TraceHop nullRoutedHop =
                      new TraceHop(currentNodeName, clonedStepsBuilder.build());
                  transmissionContext._hopsSoFar.add(nullRoutedHop);
                  Trace trace =
                      new Trace(FlowDisposition.NULL_ROUTED, transmissionContext._hopsSoFar);
                  transmissionContext._flowTraces.add(trace);
                  return;
                }

                Interface outgoingInterface =
                    _configurations
                        .get(nextHopInterface.getHostname())
                        .getAllInterfaces()
                        .get(nextHopInterface.getInterface());

                // Apply any relevant source NAT rules.
                Flow newTransformedFlow =
                    applySourceNat(
                        currentFlow,
                        inputIfaceName,
                        aclDefinitions,
                        namedIpSpaces,
                        outgoingInterface.getSourceNats());

                SortedSet<Edge> edges =
                    _dataPlane.getTopology().getInterfaceEdges().get(nextHopInterface);

                TransmissionContext clonedTransmissionContext =
                    new TransmissionContext(
                        aclDefinitions,
                        currentNodeName,
                        transmissionContext._flowTraces,
                        transmissionContext._hopsSoFar,
                        namedIpSpaces,
                        currentFlow,
                        routesForThisNextHopInterface,
                        newTransformedFlow);
                if (edges == null || edges.isEmpty()) {
                  updateDeniedOrUnreachableTrace(
                      currentNodeName,
                      outgoingInterface,
                      inputIfaceName,
                      clonedTransmissionContext,
                      clonedStepsBuilder);
                } else {
                  processCurrentNextHopInterfaceEdges(
                      currentNodeName,
                      visitedHops,
                      inputIfaceName,
                      dstIp,
                      nextHopInterfaceName,
                      finalNextHopIp,
                      edges,
                      clonedTransmissionContext,
                      clonedStepsBuilder);
                }
              });
    }
  }

  private void updateDeniedOrUnreachableTrace(
      String currentNodeName,
      Interface outInterface,
      String sourceInterfaceName,
      TransmissionContext transmissionContext,
      ImmutableList.Builder<Step> stepsTillNow) {
    IpAccessList outFilter = outInterface.getOutgoingFilter();
    boolean denied = false;
    if (!_ignoreAcls && outFilter != null) {
      // TODO why pass srcInterfaceName here ?
      FilterResult filterResult =
          outFilter.filter(
              transmissionContext._transformedFlow,
              sourceInterfaceName,
              transmissionContext._aclDefinitions,
              transmissionContext._namedIpSpaces);
      denied = filterResult.getAction() == LineAction.DENY;
    }
    ExitOutIfaceStep.Builder exitOutIfaceBuilder = ExitOutIfaceStep.builder();
    exitOutIfaceBuilder.setDetail(
        ExitOutIfaceStepDetail.builder()
            .setOutputInterface(new NodeInterfacePair(currentNodeName, outInterface.getName()))
            .build());
    Trace trace;
    if (denied) {
      // add a denied out step action and terminate the current trace
      exitOutIfaceBuilder.setAction(new ExitOutIfaceAction(StepActionResult.DENIED_OUT, null));
      List<Step> currentSteps = stepsTillNow.add(exitOutIfaceBuilder.build()).build();
      TraceHop deniedOutHop = new TraceHop(currentNodeName, currentSteps);
      transmissionContext._hopsSoFar.add(deniedOutHop);
      trace = new Trace(FlowDisposition.DENIED_OUT, transmissionContext._hopsSoFar);
    } else {
      // add a neighbor unreachable step and terminate the current trace
      exitOutIfaceBuilder.setAction(
          new ExitOutIfaceAction(StepActionResult.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK, null));
      List<Step> currentSteps = stepsTillNow.add(exitOutIfaceBuilder.build()).build();
      TraceHop neighborUnreachableHop = new TraceHop(currentNodeName, currentSteps);
      transmissionContext._hopsSoFar.add(neighborUnreachableHop);
      trace =
          new Trace(
              FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
              transmissionContext._hopsSoFar);
    }
    transmissionContext._flowTraces.add(trace);
  }
}
