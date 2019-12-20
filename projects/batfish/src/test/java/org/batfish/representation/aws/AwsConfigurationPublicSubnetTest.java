package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.batfish.datamodel.Flow.builder;
import static org.batfish.datamodel.FlowDiff.flowDiff;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.representation.aws.InternetGateway.AWS_BACKBONE_NODE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.IspModelingUtils;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TransformationStep;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests that connectivity to and from the internet works as expected. The configs have a single
 * public subnet. They were pulled after creating this scenario:
 * https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Scenario1.html
 */
public class AwsConfigurationPublicSubnetTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/aws/test-public-subnet";

  private static final List<String> fileNames =
      ImmutableList.of(
          "Addresses.json",
          "AvailabilityZones.json",
          "CustomerGateways.json",
          "InternetGateways.json",
          "NatGateways.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "VpcEndpoints.json",
          "Vpcs.json",
          "VpnConnections.json",
          "VpnGateways.json");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  // various entities in the configs
  private static String _instance = "i-06669a407b2ef3ae5";
  private static String _subnet = "subnet-0bb82ae8b3ea5dc78";
  private static String _igw = "igw-0a409e562d251f766";
  private static Ip _publicIp = Ip.parse("18.216.80.133");
  private static Ip _privateIp = Ip.parse("10.0.0.91");

  @BeforeClass
  public static void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setAwsText(TESTCONFIGS_DIR, fileNames).build(), _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  private static Trace getTrace(String ingressNode, Ip dstIp) {
    Flow flow = builder().setIngressNode(ingressNode).setDstIp(dstIp).build();
    SortedMap<Flow, List<Trace>> traces =
        _batfish
            .getTracerouteEngine(_batfish.getSnapshot())
            .computeTraces(ImmutableSet.of(flow), false);

    return getOnlyElement(traces.get(flow).iterator());
  }

  private static void testTraceNodesAndDisposition(
      String ingressNode,
      Ip dstIp,
      FlowDisposition expectedDisposition,
      List<String> expectedNodes) {
    testTraceNodesAndDisposition(getTrace(ingressNode, dstIp), expectedDisposition, expectedNodes);
  }

  private static void testTraceNodesAndDisposition(
      Trace trace, FlowDisposition expectedDisposition, List<String> expectedNodes) {
    assertThat(
        trace.getHops().stream()
            .map(h -> h.getNode().getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedNodes));
    assertThat(trace, hasDisposition(expectedDisposition));
  }

  @Test
  public void testFromInternetToValidPublicIp() {
    Trace trace = getTrace(IspModelingUtils.INTERNET_HOST_NAME, _publicIp);
    testTraceNodesAndDisposition(
        trace,
        FlowDisposition.DENIED_IN, // by the default security settings
        ImmutableList.of(
            IspModelingUtils.INTERNET_HOST_NAME, AWS_BACKBONE_NODE_NAME, _igw, _subnet, _instance));

    // Test NAT behavior
    assertThat(
        trace.getHops().get(2).getSteps().get(1),
        equalTo(
            new TransformationStep(
                new TransformationStepDetail(
                    TransformationType.DEST_NAT,
                    ImmutableSortedSet.of(flowDiff(DESTINATION, _publicIp, _privateIp))),
                StepAction.TRANSFORMED)));
  }

  @Test
  public void testFromInternetToInvalidPublicIp() {
    testTraceNodesAndDisposition(
        IspModelingUtils.INTERNET_HOST_NAME,
        Ip.parse("54.191.107.23"),
        FlowDisposition.EXITS_NETWORK,
        ImmutableList.of(IspModelingUtils.INTERNET_HOST_NAME));
  }

  @Test
  public void testFromInternetToPrivateIp() {
    // we get insufficient info for private IPs that exist somewhere in the network
    testTraceNodesAndDisposition(
        IspModelingUtils.INTERNET_HOST_NAME,
        _privateIp,
        FlowDisposition.INSUFFICIENT_INFO,
        ImmutableList.of(IspModelingUtils.INTERNET_HOST_NAME));

    // we get exits network for arbitrary private IPs
    testTraceNodesAndDisposition(
        IspModelingUtils.INTERNET_HOST_NAME,
        Ip.parse("192.18.0.8"),
        FlowDisposition.EXITS_NETWORK,
        ImmutableList.of(IspModelingUtils.INTERNET_HOST_NAME));
  }

  @Test
  public void testToInternet() {
    Ip dstIp = Ip.parse("8.8.8.8");
    Flow flow = builder().setIngressNode(_instance).setDstIp(dstIp).setSrcIp(_privateIp).build();
    SortedMap<Flow, List<Trace>> traces =
        _batfish
            .getTracerouteEngine(_batfish.getSnapshot())
            .computeTraces(ImmutableSet.of(flow), false);
    Trace trace = getOnlyElement(traces.get(flow).iterator());

    testTraceNodesAndDisposition(
        trace,
        FlowDisposition.EXITS_NETWORK,
        ImmutableList.of(
            _instance, _subnet, _igw, AWS_BACKBONE_NODE_NAME, IspModelingUtils.INTERNET_HOST_NAME));

    // Test NAT behavior
    assertThat(
        trace.getHops().get(2).getSteps().get(2),
        equalTo(
            new TransformationStep(
                new TransformationStepDetail(
                    TransformationType.SOURCE_NAT,
                    ImmutableSortedSet.of(flowDiff(SOURCE, _privateIp, _publicIp))),
                StepAction.TRANSFORMED)));
  }
}
