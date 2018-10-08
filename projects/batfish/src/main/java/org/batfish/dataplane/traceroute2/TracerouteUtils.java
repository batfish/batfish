package org.batfish.dataplane.traceroute2;

import static org.batfish.datamodel.flow2.StepActionResult.SENT_OUT;
import static org.batfish.dataplane.traceroute2.TracerouteEngineImplContext2.TRACEROUTE_DUMMY_NODE;
import static org.batfish.dataplane.traceroute2.TracerouteEngineImplContext2.TRACEROUTE_DUMMY_OUT_INTERFACE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.NavigableMap;
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
import org.batfish.datamodel.flow2.EnterSrcIfaceStep;
import org.batfish.datamodel.flow2.EnterSrcIfaceStep.EnterSrcIfaceAction;
import org.batfish.datamodel.flow2.EnterSrcIfaceStep.EnterSrcIfaceDetail;
import org.batfish.datamodel.flow2.ExitOutIfaceStep;
import org.batfish.datamodel.flow2.ExitOutIfaceStep.ExitOutIfaceAction;
import org.batfish.datamodel.flow2.ExitOutIfaceStep.ExitOutIfaceStepDetail;
import org.batfish.datamodel.flow2.Step;
import org.batfish.datamodel.flow2.StepActionResult;
import org.batfish.datamodel.flow2.TraceHop;

public class TracerouteUtils {

  /**
   * Does a basic validation of input to {@link TracerouteEngineImplContext2#buildFlows()}
   *
   * @param configurations {@link Map} of {@link Configuration}s
   * @param flow {@link Flow} for which input validation is to be done
   */
  public static void validateInputs(Map<String, Configuration> configurations, Flow flow) {
    if (flow.getIngressNode() == null) {
      throw new BatfishException("Cannot construct flow trace since ingressNode is not specified");
    }
    if (configurations.get(flow.getIngressNode()) == null) {
      throw new BatfishException(
          String.format(
              "Node %s is not in the network, cannot perform traceroute", flow.getIngressNode()));
    }
    if (flow.getDstIp() == null) {
      throw new BatfishException("Cannot construct flow trace since dstIp is not specified");
    }
  }

  /**
   * Creates a dummy {@link TraceHop} for starting a trace when ingressInterface is provided
   *
   * @return a dummy {@link TraceHop}
   */
  public static TraceHop createDummyHop() {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    // creating the exit from out interface step
    ExitOutIfaceStep.Builder exitOutStepBuilder = ExitOutIfaceStep.builder();
    ExitOutIfaceStepDetail.Builder stepDetailBuilder = ExitOutIfaceStepDetail.builder();
    stepDetailBuilder.setOutputInterface(
        new NodeInterfacePair(TRACEROUTE_DUMMY_NODE, TRACEROUTE_DUMMY_OUT_INTERFACE));
    ExitOutIfaceAction exitOutIfaceAction = new ExitOutIfaceAction(SENT_OUT);
    exitOutStepBuilder.setDetail(stepDetailBuilder.build());
    exitOutStepBuilder.setAction(exitOutIfaceAction);

    return new TraceHop(TRACEROUTE_DUMMY_NODE, steps.add(exitOutStepBuilder.build()).build());
  }

  /**
   * Returns true if the next node and interface responds for ARP request for given arpIp
   *
   * @param arpIp ARP for given {@link Ip}
   * @param forwardingAnalysis {@link ForwardingAnalysis} for the given network
   * @param node {@link Configuration} of the next node
   * @param iface Name of the interface to be tested for ARP
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
   * {@link TraceHop}
   *
   * @param node Name of the {@link TraceHop}
   * @param srcIfaceName Name of the source interface
   * @param ignoreAcls if set to true, ACLs are ignored
   * @param currentFlow {@link Flow} for the current packet entering the source interface
   * @param aclDefinitions {@link Map} from ACL names to definitions ({@link IpAccessList}) for the
   *     node
   * @param namedIpSpaces {@link NavigableMap} of named {@link IpSpace} for the current node ({@link
   *     TraceHop})
   * @param dataPlane Computed {@link DataPlane} for the node
   * @return {@link EnterSrcIfaceStep} containing {@link EnterSrcIfaceDetail} and {@link
   *     EnterSrcIfaceAction} for the step
   */
  public static EnterSrcIfaceStep createEnterSrcIfaceStep(
      Configuration node,
      String srcIfaceName,
      boolean ignoreAcls,
      Flow currentFlow,
      Map<String, IpAccessList> aclDefinitions,
      NavigableMap<String, IpSpace> namedIpSpaces,
      DataPlane dataPlane) {
    EnterSrcIfaceStep.Builder enterSrcIfaceStepBuilder = EnterSrcIfaceStep.builder();
    EnterSrcIfaceDetail.Builder enterSrcStepDetailBuilder = EnterSrcIfaceDetail.builder();
    enterSrcStepDetailBuilder.setInputInterface(
        new NodeInterfacePair(node.getHostname(), srcIfaceName));

    // check input filter
    Interface sourceInterface = node.getAllInterfaces().get(srcIfaceName);
    IpAccessList inFilter = sourceInterface.getIncomingFilter();
    if (!ignoreAcls && inFilter != null) {
      FilterResult filterResult =
          inFilter.filter(currentFlow, srcIfaceName, aclDefinitions, namedIpSpaces);
      enterSrcStepDetailBuilder.setFilterIn(inFilter.getName());
      if (filterResult.getAction() == LineAction.DENY) {
        return enterSrcIfaceStepBuilder
            .setDetail(enterSrcStepDetailBuilder.build())
            .setAction(new EnterSrcIfaceStep.EnterSrcIfaceAction(StepActionResult.DENIED_IN))
            .build();
      }
    }

    if (dataPlane
        .getIpVrfOwners()
        .getOrDefault(currentFlow.getDstIp(), ImmutableMap.of())
        .getOrDefault(node.getHostname(), ImmutableSet.of())
        .contains(sourceInterface.getVrfName())) {
      return enterSrcIfaceStepBuilder
          .setDetail(enterSrcStepDetailBuilder.build())
          .setAction(new EnterSrcIfaceAction(StepActionResult.ACCEPTED))
          .build();
    }
    return enterSrcIfaceStepBuilder
        .setDetail(enterSrcStepDetailBuilder.build())
        .setAction(new EnterSrcIfaceAction(StepActionResult.SENT_IN))
        .build();
  }
}
