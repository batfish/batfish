package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.flow.StepAction.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.flow.StepAction.EXITS_NETWORK;
import static org.batfish.datamodel.flow.StepAction.INSUFFICIENT_INFO;
import static org.batfish.datamodel.flow.StepAction.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.flow.StepAction.RECEIVED;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.batfish.datamodel.flow.FilterStep;
import org.batfish.datamodel.flow.FilterStep.FilterStepDetail;
import org.batfish.datamodel.flow.FilterStep.FilterType;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.Transformation;

@ParametersAreNonnullByDefault
public final class TracerouteUtils {

  /**
   * Does a basic validation of input to {@link
   * TracerouteEngineImplContext#buildTracesAndReturnFlows()}
   *
   * @param configurations {@link Map} of {@link Configuration}s
   * @param flow {@link Flow} for which input validation is to be done
   */
  static void validateInputs(Map<String, Configuration> configurations, Flow flow) {
    String ingressNodeName = flow.getIngressNode();
    checkArgument(
        ingressNodeName != null, "Cannot construct flow trace since ingressNode is not specified");

    Configuration ingressNode = configurations.get(ingressNodeName);
    checkArgument(
        ingressNode != null,
        "Node %s is not in the network, cannot perform traceroute",
        ingressNodeName);

    String ingressIfaceName = flow.getIngressInterface();
    if (ingressIfaceName != null) {
      checkArgument(
          ingressNode.getAllInterfaces().get(ingressIfaceName) != null,
          "%s interface does not exist on the node %s",
          ingressIfaceName,
          ingressNodeName);
    }

    checkArgument(
        flow.getDstIp() != null, "Cannot construct flow trace since dstIp is not specified");
  }

  /**
   * Returns the {@link Step} representing the entering of a packet on an input interface in a
   * {@link Hop}
   *
   * @param node Name of the {@link Hop}
   * @param inputIfaceName Name of the source interface
   * @return {@link EnterInputIfaceStep} containing {@link EnterInputIfaceStepDetail} and action for
   *     the step; null if {@link EnterInputIfaceStep} can't be created
   */
  @Nonnull
  static EnterInputIfaceStep buildEnterSrcIfaceStep(Configuration node, String inputIfaceName) {
    Interface inputInterface = node.getAllInterfaces().get(inputIfaceName);
    checkArgument(
        inputInterface != null, "Node %s has no interface %s", node.getHostname(), inputIfaceName);

    EnterInputIfaceStep.Builder enterSrcIfaceStepBuilder = EnterInputIfaceStep.builder();
    EnterInputIfaceStepDetail.Builder enterSrcStepDetailBuilder =
        EnterInputIfaceStepDetail.builder();
    enterSrcStepDetailBuilder
        .setInputInterface(new NodeInterfacePair(node.getHostname(), inputIfaceName))
        .setInputVrf(inputInterface.getVrfName());

    // Send in the flow to the next steps
    return enterSrcIfaceStepBuilder
        .setDetail(enterSrcStepDetailBuilder.build())
        .setAction(RECEIVED)
        .build();
  }

  @VisibleForTesting
  static FilterStep createFilterStep(
      Flow currentFlow,
      @Nullable String inInterfaceName,
      IpAccessList filter,
      FilterType filterType,
      Map<String, IpAccessList> aclDefinitions,
      Map<String, IpSpace> namedIpSpaces,
      boolean ignoreFilters) {
    checkArgument(filter != null, "Missing filter");

    StepAction action = StepAction.PERMITTED;
    // check filter
    if (!ignoreFilters) {
      FilterResult filterResult =
          filter.filter(currentFlow, inInterfaceName, aclDefinitions, namedIpSpaces);
      if (filterResult.getAction() == LineAction.DENY) {
        action = StepAction.DENIED;
      }
    }

    return new FilterStep(new FilterStepDetail(filter.getName(), filterType), action);
  }

  /**
   * Gets the final actions for dispositions returned by {@link
   * TracerouteEngineImplContext#computeDisposition(String, String, Ip)}
   */
  static StepAction getFinalActionForDisposition(FlowDisposition disposition) {
    StepAction finalAction;
    switch (disposition) {
      case DELIVERED_TO_SUBNET:
        finalAction = DELIVERED_TO_SUBNET;
        break;
      case EXITS_NETWORK:
        finalAction = EXITS_NETWORK;
        break;
      case NEIGHBOR_UNREACHABLE:
        finalAction = NEIGHBOR_UNREACHABLE;
        break;
      case INSUFFICIENT_INFO:
      default:
        finalAction = INSUFFICIENT_INFO;
    }
    return finalAction;
  }

  public static TransformationStep transformationStep(
      TransformationType type, Flow inputFlow, Flow transformedFlow) {
    SortedSet<FlowDiff> flowDiffs = FlowDiff.flowDiffs(inputFlow, transformedFlow);
    TransformationStepDetail detail = new TransformationStepDetail(type, flowDiffs);
    return flowDiffs.isEmpty()
        ? new TransformationStep(detail, StepAction.PERMITTED)
        : new TransformationStep(detail, StepAction.TRANSFORMED);
  }

  /**
   * Creates a return {@link Flow} for the input {@param forwardFlow}. Swaps the source/destination
   * IPs/ports, and sets the ingress node/vrf/interface.
   */
  static Flow returnFlow(
      Flow forwardFlow,
      String returnIngressNode,
      @Nullable String returnIngressVrf,
      @Nullable String returnIngressIface) {
    checkArgument(
        returnIngressVrf == null ^ returnIngressIface == null,
        "Either returnIngressVrf or returnIngressIface required, but not both");
    return forwardFlow
        .toBuilder()
        .setDstIp(forwardFlow.getSrcIp())
        .setDstPort(forwardFlow.getSrcPort())
        .setSrcIp(forwardFlow.getDstIp())
        .setSrcPort(forwardFlow.getDstPort())
        .setIngressNode(returnIngressNode)
        .setIngressVrf(returnIngressVrf)
        .setIngressInterface(returnIngressIface)
        .build();
  }

  static Multimap<NodeInterfacePair, FirewallSessionTraceInfo> buildSessionsByIngressInterface(
      @Nullable Set<FirewallSessionTraceInfo> sessions) {
    if (sessions == null) {
      return ImmutableMultimap.of();
    }

    ImmutableMultimap.Builder<NodeInterfacePair, FirewallSessionTraceInfo> builder =
        ImmutableMultimap.builder();
    sessions.forEach(
        session ->
            session
                .getIncomingInterfaces()
                .forEach(
                    incomingIface ->
                        builder.put(
                            new NodeInterfacePair(session.getHostname(), incomingIface), session)));
    return builder.build();
  }

  @Nonnull
  static Multimap<Ip, AbstractRoute> resolveNextHopIpRoutes(
      String nextHopInterfaceName,
      Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByRoute) {
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
    return resolvedNextHopWithRoutes;
  }

  @Nullable
  static Transformation sessionTransformation(Flow inputFlow, Flow currentFlow) {
    ImmutableList.Builder<org.batfish.datamodel.transformation.TransformationStep>
        transformationStepsBuilder = ImmutableList.builder();

    Ip origDstIp = inputFlow.getDstIp();
    if (!origDstIp.equals(currentFlow.getDstIp())) {
      transformationStepsBuilder.add(assignSourceIp(origDstIp, origDstIp));
    }

    Ip origSrcIp = inputFlow.getSrcIp();
    if (!origSrcIp.equals(currentFlow.getSrcIp())) {
      transformationStepsBuilder.add(
          org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp(
              origSrcIp, origSrcIp));
    }

    List<org.batfish.datamodel.transformation.TransformationStep> transformationSteps =
        transformationStepsBuilder.build();

    return transformationSteps.isEmpty() ? null : always().apply(transformationSteps).build();
  }

  @Nullable
  static Flow hopFlow(Flow originalFlow, @Nullable Flow transformedFlow) {
    if (originalFlow == transformedFlow) {
      return null;
    } else {
      return transformedFlow;
    }
  }
}
