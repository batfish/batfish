package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.batfish.datamodel.Flow.builder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests for connectivity within a subnet. The snapshot has one VPC, one subnet, and two
 * instances.
 */
public class AwsConfigurationSubnetTest {

  private static final String TESTCONFIGS_DIR = "org/batfish/representation/aws/test-subnet";

  private static final List<String> fileNames =
      ImmutableList.of(
          "Addresses.json",
          "AvailabilityZones.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "Vpcs.json");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  // various entities in the configs
  private static String _instance1 = "i-08ef86e76fceabd8d";
  private static String _instance2 = "i-07519d6ba3f9497e4";
  private static Ip _instance2Ip = Ip.parse("10.10.1.44");
  private static String _subnet = "subnet-06c8307e34ffadb04";
  private static String _vpc = "vpc-062867d29dbf9386f";

  @BeforeClass
  public static void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder().setAwsText(TESTCONFIGS_DIR, fileNames).build(), _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  private static void testTrace(
      String ingressNode,
      Ip dstIp,
      FlowDisposition expectedDisposition,
      List<String> expectedNodes) {
    Flow flow = builder().setIngressNode(ingressNode).setDstIp(dstIp).build();
    SortedMap<Flow, List<Trace>> traces =
        _batfish
            .getTracerouteEngine(_batfish.getSnapshot())
            .computeTraces(ImmutableSet.of(flow), false);

    Trace trace = getOnlyElement(traces.get(flow).iterator());

    assertThat(
        trace.getHops().stream()
            .map(h -> h.getNode().getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedNodes));
    assertThat(trace.getDisposition(), equalTo(expectedDisposition));
  }

  @Test
  public void testInstanceToInstance() {
    testTrace(
        _instance1,
        _instance2Ip,
        FlowDisposition.DENIED_IN,
        ImmutableList.of(_instance1, _instance2));
  }
}
