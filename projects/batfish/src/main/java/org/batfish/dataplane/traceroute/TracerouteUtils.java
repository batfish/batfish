package org.batfish.dataplane.traceroute;

import static org.batfish.datamodel.flow.StepActionResult.SENT_OUT;
import static org.batfish.dataplane.traceroute.TracerouteEngineImplContext.TRACEROUTE_DUMMY_NODE;
import static org.batfish.dataplane.traceroute.TracerouteEngineImplContext.TRACEROUTE_DUMMY_OUT_INTERFACE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.NavigableMap;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
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
import org.batfish.datamodel.flow.EnterSrcIfaceStep;
import org.batfish.datamodel.flow.EnterSrcIfaceStep.EnterSrcIfaceAction;
import org.batfish.datamodel.flow.EnterSrcIfaceStep.EnterSrcIfaceDetail;
import org.batfish.datamodel.flow.ExitOutIfaceStep;
import org.batfish.datamodel.flow.ExitOutIfaceStep.ExitOutIfaceAction;
import org.batfish.datamodel.flow.ExitOutIfaceStep.ExitOutIfaceStepDetail;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepActionResult;
import org.batfish.datamodel.flow.Hop;

public class TracerouteUtils {

  /**
   * Does a basic validation of input to {@link TracerouteEngineImplContext#buildFlows()}
   *
   * @param configurations {@link Map} of {@link Configuration}s
   * @param flow {@link Flow} for which input validation is to be done
   */
  public static void validateInputs(Map<String, Configuration> configurations, Flow flow) {
    String ingressNodeName = flow.getIngressNode();
    if (ingressNodeName == null) {
      throw new BatfishException("Cannot construct flow trace since ingressNode is not specified");
    }
    Configuration ingressNode = configurations.get(ingressNodeName);
    if (ingressNode == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", ingressNodeName));
    }
    String ingressIfaceName = flow.getIngressInterface();
    if (ingressIfaceName != null) {
      if (ingressNode.getAllInterfaces().get(ingressIfaceName) == null) {
        throw new BatfishException(
            String.format(
                "%s interface does not exist on the node %s", ingressIfaceName, ingressNodeName));
      }
    }
    if (flow.getDstIp() == null) {
      throw new BatfishException("Cannot construct flow trace since dstIp is not specified");
    }
  }

  /**
   * Creates a dummy {@link Hop} for starting a trace when ingressInterface is provided
   *
   * @return a dummy {@link Hop}
   */
  public static Hop createDummyHop() {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    // creating the exit from out interface step
    ExitOutIfaceStep.Builder exitOutStepBuilder = ExitOutIfaceStep.builder();
    ExitOutIfaceStepDetail.Builder stepDetailBuilder = ExitOutIfaceStepDetail.builder();
    stepDetailBuilder.setOutputInterface(
        new NodeInterfacePair(TRACEROUTE_DUMMY_NODE, TRACEROUTE_DUMMY_OUT_INTERFACE));
    ExitOutIfaceAction exitOutIfaceAction = new ExitOutIfaceAction(SENT_OUT, null);
    exitOutStepBuilder.setDetail(stepDetailBuilder.build());
    exitOutStepBuilder.setAction(exitOutIfaceAction);

    return new Hop(TRACEROUTE_DUMMY_NODE, steps.add(exitOutStepBuilder.build()).build());
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
  public static boolean isArpSuccessful(
      Ip arpIp, ForwardingAnalysis forwardingAnalysis, Configuration node, String iface) {
    return (arpIp != null
        && forwardingAnalysis
            .getArpReplies()
            .get(node.getHostname())
            .get(iface)
            .containsIp(arpIp, node.getIpSpaces()));
  }

  /**
   * Returns the {@link Step} representing the entering of a packet on a source interface in a
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
   * @return {@link EnterSrcIfaceStep} containing {@link EnterSrcIfaceDetail} and {@link
   *     EnterSrcIfaceAction} for the step
   */
  @Nullable
  public static EnterSrcIfaceStep createEnterSrcIfaceStep(
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
    // prefer input interface's VRF if both input interface and input VRF are set
    if (inputIfaceName != null && node.getAllInterfaces().get(inputIfaceName) != null) {
      inputVrfName = node.getAllInterfaces().get(inputIfaceName).getVrfName();
    }

    EnterSrcIfaceStep.Builder enterSrcIfaceStepBuilder = EnterSrcIfaceStep.builder();
    EnterSrcIfaceDetail.Builder enterSrcStepDetailBuilder = EnterSrcIfaceDetail.builder();
    enterSrcStepDetailBuilder
        .setInputInterface(new NodeInterfacePair(node.getHostname(), inputIfaceName))
        .setInputVrf(inputVrfName);

    if (dataPlane
        .getIpVrfOwners()
        .getOrDefault(currentFlow.getDstIp(), ImmutableMap.of())
        .getOrDefault(node.getHostname(), ImmutableSet.of())
        .contains(inputVrfName)) {
      return enterSrcIfaceStepBuilder
          .setDetail(enterSrcStepDetailBuilder.build())
          .setAction(new EnterSrcIfaceAction(StepActionResult.ACCEPTED, null))
          .build();
    }

    // If not accepted then add this step and go to the routing step
    // (input interface should be present to go to the next step)
    if (inputIfaceName == null) {
      return null;
    }

    // check input filter
    Interface inputInterface = node.getAllInterfaces().get(inputIfaceName);
    IpAccessList inFilter = inputInterface.getIncomingFilter();
    if (!ignoreAcls && inFilter != null) {
      FilterResult filterResult =
          inFilter.filter(currentFlow, inputIfaceName, aclDefinitions, namedIpSpaces);
      enterSrcStepDetailBuilder.setFilterIn(inFilter.getName());
      if (filterResult.getAction() == LineAction.DENY) {
        return enterSrcIfaceStepBuilder
            .setDetail(enterSrcStepDetailBuilder.build())
            .setAction(new EnterSrcIfaceStep.EnterSrcIfaceAction(StepActionResult.DENIED_IN, null))
            .build();
      }
    }

    // Packet was forwarded further after being received at the input interface
    return enterSrcIfaceStepBuilder
        .setDetail(enterSrcStepDetailBuilder.build())
        .setAction(new EnterSrcIfaceAction(StepActionResult.SENT_IN, null))
        .build();
  }
}
