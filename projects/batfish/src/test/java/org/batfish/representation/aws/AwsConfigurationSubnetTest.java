package org.batfish.representation.aws;

import static com.google.common.collect.Iterators.getOnlyElement;
import static org.batfish.datamodel.Flow.builder;
import static org.batfish.representation.aws.NetworkAcl.getAclName;
import static org.batfish.representation.aws.Region.SG_EGRESS_ACL_NAME;
import static org.batfish.representation.aws.Region.SG_INGRESS_ACL_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.ip.Ip;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.FilterStep;
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
  private static String _networkAcl = "acl-07102431133dec52a";

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

  private static void assertTracePath(Trace trace, List<String> expectedNodes) {
    assertThat(
        trace.getHops().stream()
            .map(h -> h.getNode().getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(expectedNodes));
  }

  private static void assertFilterAtHop(Trace trace, int hop, String filter) {
    List<String> hopFilters =
        trace.getHops().get(hop).getSteps().stream()
            .filter(s -> s instanceof FilterStep)
            .map(s -> ((FilterStep) s).getDetail().getFilter())
            .collect(ImmutableList.toImmutableList());
    String hopFilter = Iterables.getOnlyElement(hopFilters); // should be only one
    assertThat(hopFilter, equalTo(filter));
  }

  @Test
  public void testInstanceToInstance() {
    Trace trace = getTrace(_instance1, _instance2Ip);

    // Instance to instance traffic is direct, without passing through the subnet node
    assertTracePath(trace, ImmutableList.of(_instance1, _instance2));

    // security group at instance1
    assertFilterAtHop(trace, 0, SG_EGRESS_ACL_NAME);

    // security group at instance2
    assertFilterAtHop(trace, 1, SG_INGRESS_ACL_NAME);

    // denied because of security group on instance2
    assertThat(trace.getDisposition(), equalTo(FlowDisposition.DENIED_IN));
  }

  @Test
  public void testInstanceToOutsideSubnet() {
    Trace trace = getTrace(_instance1, Ip.parse("10.10.0.1"));

    assertThat(trace.getDisposition(), equalTo(FlowDisposition.NULL_ROUTED));
    assertTracePath(trace, ImmutableList.of(_instance1, _subnet, _vpc));

    // network acl is applied when leaving the subnet
    assertFilterAtHop(trace, 1, getAclName(_networkAcl, true));
  }

  @Test
  public void testOutsideSubnetToInstance() {
    Trace trace = getTrace(_vpc, _instance2Ip);

    assertThat(trace.getDisposition(), equalTo(FlowDisposition.DENIED_IN)); // security group
    assertTracePath(trace, ImmutableList.of(_vpc, _subnet, _instance2));

    // network acl is applied when leaving the subnet
    assertFilterAtHop(trace, 1, getAclName(_networkAcl, false));
  }
}
