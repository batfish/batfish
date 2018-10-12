package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.flow.StepAction.BLOCKED;
import static org.batfish.datamodel.flow.StepAction.SENT_IN;
import static org.batfish.datamodel.flow.StepAction.SENT_OUT;
import static org.batfish.dataplane.traceroute.TracerouteEngineImplContext.TRACEROUTE_DUMMY_NODE;
import static org.batfish.dataplane.traceroute.TracerouteEngineImplContext.TRACEROUTE_DUMMY_OUT_INTERFACE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.NavigableMap;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.pojo.Node;

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
   * Creates a dummy {@link Hop} for starting a trace when ingressInterface is provided
   *
   * @return a dummy {@link Hop}
   */
  static Hop createDummyHop() {
    ImmutableList.Builder<Step<?>> steps = ImmutableList.builder();

    // creating the exit from out interface step
    ExitOutputIfaceStep.Builder exitOutStepBuilder = ExitOutputIfaceStep.builder();
    ExitOutputIfaceStepDetail.Builder stepDetailBuilder = ExitOutputIfaceStepDetail.builder();
    stepDetailBuilder.setOutputInterface(
        new NodeInterfacePair(TRACEROUTE_DUMMY_NODE, TRACEROUTE_DUMMY_OUT_INTERFACE));
    exitOutStepBuilder.setDetail(stepDetailBuilder.build()).setAction(SENT_OUT);

    return new Hop(new Node(TRACEROUTE_DUMMY_NODE), steps.add(exitOutStepBuilder.build()).build());
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
    return (arpIp != null
        && forwardingAnalysis
            .getArpReplies()
            .get(node.getHostname())
            .get(iface)
            .containsIp(arpIp, node.getIpSpaces()));
  }

  /**
   * Returns the {@link Step} representing the entering of a packet on an input interface in a
   * {@link Hop}
   *
   * @param node Name of the {@link Hop}
   * @param inputIfaceName Name of the source interface
   * @param ignoreAcls if set to true, ACLs are ignored
   * @param currentFlow {@link Flow} for the current packet entering the source interface
   * @param aclDefinitions {@link Map} from ACL names to definitions ({@link IpAccessList}) for the
   *     node
   * @param namedIpSpaces {@link NavigableMap} of named {@link IpSpace} for the current node ({@link
   *     Hop})
   * @param dataPlane Computed {@link DataPlane} for the node
   * @return {@link EnterInputIfaceStep} containing {@link EnterInputIfaceStepDetail} and action for
   *     the step; null if {@link EnterInputIfaceStep} can't be created
   */
  @Nullable
  static EnterInputIfaceStep createEnterSrcIfaceStep(
      Configuration node,
      @Nullable String inputIfaceName,
      @Nullable String inputVrfName,
      boolean ignoreAcls,
      Flow currentFlow,
      Map<String, IpAccessList> aclDefinitions,
      NavigableMap<String, IpSpace> namedIpSpaces,
      DataPlane dataPlane) {
    // Can't create EnterSrcIface step if both input VRF and input interface are not set
    if (inputIfaceName == null && inputVrfName == null) {
      return null;
    }
    String selectedInputVrfName;
    // prefer input interface's VRF if both input interface and input VRF are set
    if (inputIfaceName != null && node.getAllInterfaces().get(inputIfaceName) != null) {
      selectedInputVrfName = node.getAllInterfaces().get(inputIfaceName).getVrfName();
    } else {
      selectedInputVrfName = inputVrfName;
    }

    Interface inputInterface;
    IpAccessList inputFilter = null;
    if (inputIfaceName != null) {
      inputInterface = node.getAllInterfaces().get(inputIfaceName);
      inputFilter = inputInterface != null ? inputInterface.getIncomingFilter() : null;
    }

    EnterInputIfaceStep.Builder enterSrcIfaceStepBuilder = EnterInputIfaceStep.builder();
    EnterInputIfaceStepDetail.Builder enterSrcStepDetailBuilder =
        EnterInputIfaceStepDetail.builder();
    enterSrcStepDetailBuilder
        .setInputInterface(new NodeInterfacePair(node.getHostname(), inputIfaceName))
        .setInputVrf(selectedInputVrfName)
        .setInputFilter(inputFilter != null ? inputFilter.getName() : null);

    if (dataPlane
        .getIpVrfOwners()
        .getOrDefault(currentFlow.getDstIp(), ImmutableMap.of())
        .getOrDefault(node.getHostname(), ImmutableSet.of())
        .contains(selectedInputVrfName)) {
      return enterSrcIfaceStepBuilder
          .setDetail(enterSrcStepDetailBuilder.build())
          .setAction(StepAction.TERMINATED)
          .build();
    }

    // If not accepted then add this step and go to the routing step
    // (input interface should be present to go to the next step)
    if (inputIfaceName == null) {
      return null;
    }

    // check input filter
    if (!ignoreAcls && inputFilter != null) {
      FilterResult filterResult =
          inputFilter.filter(currentFlow, inputIfaceName, aclDefinitions, namedIpSpaces);
      enterSrcStepDetailBuilder.setInputFilter(inputFilter.getName());
      if (filterResult.getAction() == LineAction.DENY) {
        return enterSrcIfaceStepBuilder
            .setDetail(enterSrcStepDetailBuilder.build())
            .setAction(BLOCKED)
            .build();
      }
    }

    // Send in the flow to the next steps
    return enterSrcIfaceStepBuilder
        .setDetail(enterSrcStepDetailBuilder.build())
        .setAction(SENT_IN)
        .build();
  }
}
