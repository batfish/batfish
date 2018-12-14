package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.flow.StepAction.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.flow.StepAction.EXITS_NETWORK;
import static org.batfish.datamodel.flow.StepAction.INSUFFICIENT_INFO;
import static org.batfish.datamodel.flow.StepAction.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.flow.StepAction.RECEIVED;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.batfish.datamodel.flow.FilterStep;
import org.batfish.datamodel.flow.FilterStep.FilterStepDetail;
import org.batfish.datamodel.flow.FilterStep.FilterType;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

@ParametersAreNonnullByDefault
final class TracerouteUtils {

  /**
   * Does a basic validation of input to {@link TracerouteEngineImplContext#buildFlows()}
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
   * Returns true if the next node and interface responds for ARP request for given arpIp
   *
   * @param arpIp ARP for given {@link Ip}
   * @param forwardingAnalysis {@link ForwardingAnalysis} for the given network
   * @param node {@link Configuration} of the next node
   * @param iface Name of the interface in the next node to be tested for ARP
   * @return true if ARP request will get a response
   */
  static boolean isArpSuccessful(
      Ip arpIp, ForwardingAnalysis forwardingAnalysis, Configuration node, String iface) {
    return forwardingAnalysis
        .getArpReplies()
        .get(node.getHostname())
        .get(iface)
        .containsIp(arpIp, node.getIpSpaces());
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
  static EnterInputIfaceStep createEnterSrcIfaceStep(Configuration node, String inputIfaceName) {
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
  static FilterStep applyFilter(
      Flow currentFlow,
      String inInterfaceName,
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
}
