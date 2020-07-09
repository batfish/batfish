package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;
import static org.batfish.representation.aws.NetworkAcl.getAclName;
import static org.batfish.representation.aws.Region.eniEgressAclName;
import static org.batfish.representation.aws.Region.eniIngressAclName;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
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
  private static final String INSTANCE_1 = "i-08ef86e76fceabd8d";
  private static final String INSTANCE_1_IFACE = "eni-010f649a7fc5c1e34";
  private static final String INSTANCE_2 = "i-07519d6ba3f9497e4";
  private static final String INSTANCE_2_IFACE = "eni-029bf1092de61fb40";
  private static final Ip INSTANCE_2_IP = Ip.parse("10.10.1.44");
  private static final String SUBNET = "subnet-06c8307e34ffadb04";
  private static final String VPC = "vpc-062867d29dbf9386f";
  private static final String NETWORK_ACL = "acl-07102431133dec52a";

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
            getAnyFlow(INSTANCE_1, INSTANCE_2_IP, _batfish),
            FlowDisposition.DENIED_IN, // denied because of security group on instance2
            ImmutableList.of(INSTANCE_1, INSTANCE_2), // Instance to instance traffic is direct
            _batfish);

    // security group at instance1
    assertFilterAtHop(trace, 0, eniEgressAclName(INSTANCE_1_IFACE));

    // security group at instance2
    assertFilterAtHop(trace, 1, eniIngressAclName(INSTANCE_2_IFACE));
  }

  @Test
  public void testInstanceToOutsideSubnet() {
    Trace trace =
        testTrace(
            getAnyFlow(INSTANCE_1, Ip.parse("10.10.0.1"), _batfish),
            FlowDisposition.NULL_ROUTED,
            ImmutableList.of(INSTANCE_1, SUBNET, VPC),
            _batfish);

    // network acl is applied when leaving the subnet
    assertFilterAtHop(trace, 1, getAclName(NETWORK_ACL, true));
  }

  @Test
  public void testOutsideSubnetToInstance() {
    Trace trace =
        testTrace(
            Flow.builder().setIngressNode(VPC).setDstIp(INSTANCE_2_IP).build(),
            FlowDisposition.DENIED_IN, // security group
            ImmutableList.of(VPC, SUBNET, INSTANCE_2),
            _batfish);

    // network acl is applied when leaving the subnet
    assertFilterAtHop(trace, 1, getAclName(NETWORK_ACL, false));
  }

  @Test
  public void testInstanceHasOriginatingVrf() {
    // Instances should allow originating sessions (sessions can be established by inbound packets)
    Configuration instance1 = _batfish.loadConfigurations(_batfish.getSnapshot()).get(INSTANCE_1);
    Configuration instance2 = _batfish.loadConfigurations(_batfish.getSnapshot()).get(INSTANCE_2);
    assertTrue(
        instance1.getVrfs().values().stream()
            .allMatch(vrf -> vrf.getFirewallSessionVrfInfo() != null));
    assertTrue(
        instance2.getVrfs().values().stream()
            .allMatch(vrf -> vrf.getFirewallSessionVrfInfo() != null));
  }
}
