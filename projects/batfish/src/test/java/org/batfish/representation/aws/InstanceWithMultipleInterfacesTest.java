package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestVpc;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.IpAccessListMatchers;
import org.batfish.representation.aws.Instance.Status;
import org.batfish.representation.aws.IpPermissions.IpRange;
import org.junit.Test;

/** Test for {@link Instance} with multiple interfaces. */
public class InstanceWithMultipleInterfacesTest {
  /**
   * Creates a fake AWS network with one instance and two network interfaces, where each interface
   * has a different SG applied.
   */
  @Test
  public void testInterfaceSpecificSgs() {
    AwsConfiguration c = new AwsConfiguration();
    c.setWarnings(new Warnings());

    Account a = c.addOrGetAccount("account-id");
    Region region = a.addOrGetRegion("test");

    Vpc vpc = getTestVpc("vpc", ImmutableSet.of(Prefix.parse("10.0.0.0/24")));
    region.getVpcs().put(vpc.getId(), vpc);

    // add two different security groups, one that accepts SSH from anywhere and one HTTP.
    SecurityGroup sgSSH =
        new SecurityGroup(
            "sg-ssh-id",
            "sg-ssh",
            ImmutableList.of(),
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    NamedPort.SSH.number(),
                    NamedPort.SSH.number(),
                    ImmutableList.of(new IpRange(Prefix.ZERO)),
                    ImmutableList.of(),
                    ImmutableList.of())),
            vpc.getId());
    region.getSecurityGroups().put(sgSSH.getId(), sgSSH);
    SecurityGroup sgHTTP =
        new SecurityGroup(
            "sg-http-id",
            "sg-http",
            ImmutableList.of(),
            ImmutableList.of(
                new IpPermissions(
                    "tcp",
                    NamedPort.HTTP.number(),
                    NamedPort.HTTP.number(),
                    ImmutableList.of(new IpRange(Prefix.ZERO)),
                    ImmutableList.of(),
                    ImmutableList.of())),
            vpc.getId());
    region.getSecurityGroups().put(sgHTTP.getId(), sgHTTP);

    Subnet subnet =
        new Subnet(
            Prefix.parse("10.0.0.0/28"),
            "ownerId",
            "subnetArn",
            "subnet-id",
            vpc.getId(),
            "az",
            ImmutableMap.of());
    region.getSubnets().put(subnet.getId(), subnet);

    // Construct the network interfaces, both on the test instance but with different SGs.
    String instanceId = "i-1";
    NetworkInterface primary =
        new NetworkInterface(
            "eni-1",
            subnet.getId(),
            vpc.getId(),
            ImmutableList.of(sgSSH.getId()),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.0.0.1"), null)),
            "primary",
            instanceId,
            ImmutableMap.of());
    region.getNetworkInterfaces().put(primary.getId(), primary);
    NetworkInterface secondary =
        new NetworkInterface(
            "eni-2",
            subnet.getId(),
            vpc.getId(),
            ImmutableList.of(sgHTTP.getId()),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.0.0.2"), null)),
            "secondary",
            instanceId,
            ImmutableMap.of());
    region.getNetworkInterfaces().put(secondary.getId(), secondary);

    Instance i =
        new Instance(
            instanceId,
            vpc.getId(),
            primary.getSubnetId(),
            primary.getGroups(),
            ImmutableList.of(primary.getId(), secondary.getId()),
            primary.getPrimaryPrivateIp().getPrivateIp(),
            ImmutableMap.of(),
            Status.RUNNING);
    region.getInstances().put(i.getId(), i);

    Map<String, Configuration> configs =
        c.toVendorIndependentConfigurations().stream()
            .collect(ImmutableMap.toImmutableMap(Configuration::getHostname, x -> x));

    // Extract the config of interest and its primary and secondary interfaces.
    assertThat(configs, hasKey(i.getId()));
    Configuration testConfig = configs.get(i.getId());
    assertThat(testConfig, hasInterface(primary.getId()));
    Interface i1 = testConfig.getAllInterfaces().get(primary.getId());
    assertThat(testConfig, hasInterface(secondary.getId()));
    Interface i2 = testConfig.getAllInterfaces().get(secondary.getId());

    // The actual test: for the primary interface, SSH should be accepted but not HTTP.
    // The actual test: for the secondary interface, HTTP should be accepted but not SSH.
    Flow sshFlowPrimary =
        Flow.builder()
            .setIngressNode("n")
            .setIngressInterface("i")
            .setSrcIp(Ip.parse("8.8.8.8"))
            .setDstIp(primary.getPrimaryPrivateIp().getPrivateIp())
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
            .setDstPort(22)
            .build();
    Flow sshFlowSecondary =
        sshFlowPrimary.toBuilder().setDstIp(secondary.getPrimaryPrivateIp().getPrivateIp()).build();
    Flow httpFlowPrimary = sshFlowPrimary.toBuilder().setDstPort(80).build();
    Flow httpFlowSecondary =
        httpFlowPrimary.toBuilder()
            .setDstIp(secondary.getPrimaryPrivateIp().getPrivateIp())
            .build();

    assertThat(
        i1.getIncomingFilter(),
        IpAccessListMatchers.accepts(sshFlowPrimary, i1.getName(), testConfig));
    assertThat(
        i1.getIncomingFilter(),
        IpAccessListMatchers.rejects(httpFlowPrimary, i1.getName(), testConfig));
    assertThat(
        i2.getIncomingFilter(),
        IpAccessListMatchers.accepts(httpFlowSecondary, i2.getName(), testConfig));
    assertThat(
        i2.getIncomingFilter(),
        IpAccessListMatchers.rejects(sshFlowSecondary, i2.getName(), testConfig));
  }
}
