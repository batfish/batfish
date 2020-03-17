package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;
import static org.batfish.representation.aws.NetworkAcl.getAclName;
import static org.batfish.representation.aws.Region.SG_INGRESS_ACL_NAME;
import static org.batfish.representation.aws.Region.instanceEgressAclName;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.FilterStep;
import org.batfish.datamodel.flow.Trace;
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

  // various entities in the configs
  private static String _instance1 = "i-08ef86e76fceabd8d";
  private static String _instance1Iface = "eni-010f649a7fc5c1e34";
  private static String _instance2 = "i-07519d6ba3f9497e4";
  private static Ip _instance2Ip = Ip.parse("10.10.1.44");
  private static String _subnet = "subnet-06c8307e34ffadb04";
  private static String _vpc = "vpc-062867d29dbf9386f";
  private static String _networkAcl = "acl-07102431133dec52a";

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
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
    Trace trace =
        testTrace(
            getAnyFlow(_instance1, _instance2Ip, _batfish),
            FlowDisposition.DENIED_IN, // denied because of security group on instance2
            ImmutableList.of(_instance1, _instance2), // Instance to instance traffic is direct
            _batfish);

    // security group at instance1
    assertFilterAtHop(trace, 0, instanceEgressAclName(_instance1Iface));

    // security group at instance2
    assertFilterAtHop(trace, 1, SG_INGRESS_ACL_NAME);
  }

  @Test
  public void testInstanceToOutsideSubnet() {
    Trace trace =
        testTrace(
            getAnyFlow(_instance1, Ip.parse("10.10.0.1"), _batfish),
            FlowDisposition.NULL_ROUTED,
            ImmutableList.of(_instance1, _subnet, _vpc),
            _batfish);

    // network acl is applied when leaving the subnet
    assertFilterAtHop(trace, 1, getAclName(_networkAcl, true));
  }

  @Test
  public void testOutsideSubnetToInstance() {
    Trace trace =
        testTrace(
            Flow.builder().setIngressNode(_vpc).setDstIp(_instance2Ip).build(),
            FlowDisposition.DENIED_IN, // security group
            ImmutableList.of(_vpc, _subnet, _instance2),
            _batfish);

    // network acl is applied when leaving the subnet
    assertFilterAtHop(trace, 1, getAclName(_networkAcl, false));
  }
}
