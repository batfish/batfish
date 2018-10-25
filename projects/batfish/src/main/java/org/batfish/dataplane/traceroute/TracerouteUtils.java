package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.flow.StepAction.DENIED;
import static org.batfish.datamodel.flow.StepAction.RECEIVED;

import java.util.Map;
import java.util.NavigableMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
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
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;

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
   * @param ignoreAcls if set to true, ACLs are ignored
   * @param currentFlow {@link Flow} for the current packet entering the source interface
   * @param aclDefinitions {@link Map} from ACL names to definitions ({@link IpAccessList}) for the
   *     node
   * @param namedIpSpaces {@link NavigableMap} of named {@link IpSpace} for the current node ({@link
   *     Hop})
   * @return {@link EnterInputIfaceStep} containing {@link EnterInputIfaceStepDetail} and action for
   *     the step; null if {@link EnterInputIfaceStep} can't be created
   */
  @Nonnull
  static EnterInputIfaceStep createEnterSrcIfaceStep(
      Configuration node,
      String inputIfaceName,
      boolean ignoreAcls,
      Flow currentFlow,
      Map<String, IpAccessList> aclDefinitions,
      NavigableMap<String, IpSpace> namedIpSpaces) {
    Interface inputInterface = node.getAllInterfaces().get(inputIfaceName);
    checkArgument(
        inputInterface != null, "Node %s has no interface %s", node.getHostname(), inputIfaceName);

    EnterInputIfaceStep.Builder enterSrcIfaceStepBuilder = EnterInputIfaceStep.builder();
    EnterInputIfaceStepDetail.Builder enterSrcStepDetailBuilder =
        EnterInputIfaceStepDetail.builder();
    enterSrcStepDetailBuilder
        .setInputInterface(new NodeInterfacePair(node.getHostname(), inputIfaceName))
        .setInputVrf(inputInterface.getVrfName());

    // If inbound filter is present, add the detail and verify it permits the flow.
    IpAccessList inputFilter = inputInterface.getIncomingFilter();
    if (inputFilter != null) {
      enterSrcStepDetailBuilder.setInputFilter(inputFilter.getName());
      // check input filter
      if (!ignoreAcls) {
        FilterResult filterResult =
            inputFilter.filter(currentFlow, inputIfaceName, aclDefinitions, namedIpSpaces);
        enterSrcStepDetailBuilder.setInputFilter(inputFilter.getName());
        if (filterResult.getAction() == LineAction.DENY) {
          return enterSrcIfaceStepBuilder
              .setDetail(enterSrcStepDetailBuilder.build())
              .setAction(DENIED)
              .build();
        }
      }
    }

    // Send in the flow to the next steps
    return enterSrcIfaceStepBuilder
        .setDetail(enterSrcStepDetailBuilder.build())
        .setAction(RECEIVED)
        .build();
  }
}
