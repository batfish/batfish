package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.matchers.AclLineMatchers.isExprAclLineThat;
import static org.batfish.datamodel.matchers.ExprAclLineMatchers.hasMatchCondition;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.hasLines;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestSubnet;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DB_INSTANCES;
import static org.batfish.representation.aws.ElasticsearchDomainTest.matchPorts;
import static org.batfish.representation.aws.ElasticsearchDomainTest.matchTcp;
import static org.batfish.representation.aws.Region.computeAntiSpoofingFilter;
import static org.batfish.representation.aws.Region.eniEgressAclName;
import static org.batfish.representation.aws.Region.eniIngressAclName;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.traceElementEniPrivateIp;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceTextForAddress;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vendor_family.AwsFamily;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.representation.aws.IpPermissions.AddressType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RdsInstanceTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  private StaticRoute.Builder _staticRouteBuilder;

  @Before
  public void setup() {
    _staticRouteBuilder =
        StaticRoute.testBuilder()
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
            .setNetwork(Prefix.ZERO);
  }

  public Map<String, Configuration> loadAwsConfigurations() throws IOException {
    List<String> awsFilenames =
        ImmutableList.of(
            "RdsInstances.json",
            "SecurityGroups.json",
            "Subnets.json",
            "Vpcs.json",
            "NetworkAcls.json",
            "NetworkInterfaces.json",
            "Reservations.json");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsFiles("org/batfish/representation/aws/test", awsFilenames)
                .build(),
            _folder);
    return batfish.loadConfigurations(batfish.getSnapshot());
  }

  @Test
  public void testRdsSubnetEdge() throws IOException {
    Map<String, Configuration> configurations = loadAwsConfigurations();
    Topology topology = TopologyUtil.synthesizeL3Topology(configurations);

    // check that RDS instance is a neighbor of the deterministically chosen subnets in which its
    // interfaces are located.
    assertThat(
        topology.getEdges(),
        hasItem(
            new Edge(
                NodeInterfacePair.of(
                    "subnet-073b8061", Subnet.instancesInterfaceName("subnet-073b8061")),
                NodeInterfacePair.of("test-rds", "test-rds-subnet-073b8061"))));
  }

  @Test
  public void testUniqueIps() throws IOException {
    Map<String, Configuration> configurations = loadAwsConfigurations();

    // check that  IPs are unique for all the interfaces
    List<Ip> ipsAsList =
        configurations.values().stream()
            .map(Configuration::getAllInterfaces)
            .map(Map::values)
            .flatMap(Collection::stream)
            .map(Interface::getAllConcreteAddresses)
            .flatMap(Collection::stream)
            .map(ConcreteInterfaceAddress::getIp)
            .collect(ImmutableList.toImmutableList());
    Set<Ip> ipsAsSet = ImmutableSet.copyOf(ipsAsList);
    assertThat(ipsAsList, hasSize(ipsAsSet.size()));
  }

  @Test
  public void testDefaultRoute() throws IOException {
    Map<String, Configuration> configurations = loadAwsConfigurations();
    StaticRoute defaultRoute = _staticRouteBuilder.setNextHopIp(Ip.parse("192.168.2.17")).build();

    // checking that the default route is installed to the deterministically chosen subnet.
    assertThat(configurations, hasKey("test-rds"));
    assertThat(
        configurations.get("test-rds").getDefaultVrf().getStaticRoutes(), contains(defaultRoute));
  }

  @Test
  public void testSecurityGroupsAcl() throws IOException {
    Map<String, Configuration> configurations = loadAwsConfigurations();

    assertThat(configurations, hasKey("test-rds"));
    Configuration testRds = configurations.get("test-rds");
    assertThat(testRds.getAllInterfaces().entrySet(), hasSize(1));
    assertThat(
        testRds
            .getIpAccessLists()
            .get("~EGRESS~SECURITY-GROUP~Test Security Group~sg-0de0ddfa8a5a45810~"),
        hasLines(
            isExprAclLineThat(
                hasMatchCondition(
                    new AndMatchExpr(
                        ImmutableList.of(
                            matchDst(
                                UniverseIpSpace.INSTANCE,
                                traceElementForAddress(
                                    "destination", "0.0.0.0/0", AddressType.CIDR_IP))),
                        getTraceElementForRule(null))))));
    assertThat(
        testRds
            .getIpAccessLists()
            .get("~INGRESS~SECURITY-GROUP~Test Security Group~sg-0de0ddfa8a5a45810~"),
        hasLines(
            isExprAclLineThat(
                hasMatchCondition(
                    or(
                        new AndMatchExpr(
                            ImmutableList.of(
                                matchTcp,
                                matchPorts(45, 50),
                                matchSrc(
                                    Ip.parse("1.2.3.4").toIpSpace(),
                                    traceElementForAddress(
                                        "source", "1.2.3.4/32", AddressType.CIDR_IP))),
                            getTraceElementForRule("Closed interval")),
                        new AndMatchExpr(
                            ImmutableList.of(
                                matchTcp,
                                matchPorts(45, 50),
                                or(
                                    traceTextForAddress(
                                        "source", "Test-Instance-SG", AddressType.SECURITY_GROUP),
                                    matchSrc(
                                        Ip.parse("10.193.16.105").toIpSpace(),
                                        traceElementEniPrivateIp(
                                            "eni-05e8949c37b78cf4d on i-066b1b9957b9200e7 (Test"
                                                + " host)")))),
                            getTraceElementForRule(null)))))));
    for (Interface iface : testRds.getAllInterfaces().values()) {
      assertThat(iface.getIncomingFilter().getName(), equalTo(eniIngressAclName(iface.getName())));
      assertThat(iface.getOutgoingFilter().getName(), equalTo(eniEgressAclName(iface.getName())));
      assertThat(
          testRds.getIpAccessLists().get(eniEgressAclName(iface.getName())).getLines(),
          equalTo(
              ImmutableList.of(
                  computeAntiSpoofingFilter(iface),
                  new AclAclLine(
                      "Security Group Test Security Group",
                      "~EGRESS~SECURITY-GROUP~Test Security Group~" + "sg-0de0ddfa8a5a45810~",
                      Utils.getTraceElementForSecurityGroup("Test Security Group"),
                      null))));
      assertThat(
          testRds.getIpAccessLists().get(eniIngressAclName(iface.getName())).getLines(),
          equalTo(
              ImmutableList.of(
                  new AclAclLine(
                      "Security Group Test Security Group",
                      "~INGRESS~SECURITY-GROUP~Test Security Group~sg-0de0ddfa8a5a45810~",
                      Utils.getTraceElementForSecurityGroup("Test Security Group"),
                      null))));
      assertThat(
          iface.getFirewallSessionInterfaceInfo(),
          equalTo(
              new FirewallSessionInterfaceInfo(
                  Action.FORWARD_OUT_IFACE, ImmutableList.of(iface.getName()), null, null)));
    }
  }

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/RdsInstanceTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_DB_INSTANCES);
    List<RdsInstance> rdsInstances = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      rdsInstances.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), RdsInstance.class));
    }

    assertThat(
        rdsInstances,
        equalTo(
            ImmutableList.of(
                new RdsInstance(
                    "test-db",
                    "us-west-2b",
                    "vpc-1",
                    false,
                    "available",
                    ImmutableListMultimap.of("us-west-2b", "subnet-1"),
                    ImmutableList.of("sg-12345")))));
  }

  @Test
  public void testIsUp() {
    // Instance should be considered up unless status is unavailable
    assertTrue(
        new RdsInstance(
                "id",
                "az",
                "vpc",
                false,
                "available",
                ImmutableListMultimap.of(),
                ImmutableList.of())
            .isUp());
    assertTrue(
        new RdsInstance(
                "id",
                "az",
                "vpc",
                false,
                "backing-up",
                ImmutableListMultimap.of(),
                ImmutableList.of())
            .isUp());
    assertFalse(
        new RdsInstance(
                "id", "az", "vpc", false, "stopped", ImmutableListMultimap.of(), ImmutableList.of())
            .isUp());
  }

  /** Test that the hierarchy is configured properly */
  @Test
  public void testToConfigurationNode_hierarchy() {
    RdsInstance rds =
        new RdsInstance(
            "id",
            "az",
            "vpc",
            false,
            "available",
            ImmutableListMultimap.of("az", "subnet-1", "az", "subnet-2"),
            ImmutableList.of());

    Subnet s2 = getTestSubnet(Prefix.parse("10.0.0.0/24"), "subnet-2", "vpc");

    Configuration cfg =
        rds.toConfigurationNode(
            new ConvertedConfiguration(),
            Region.builder("r1").setSubnets(ImmutableMap.of(s2.getId(), s2)).build(),
            new Warnings());

    AwsFamily awsFamily = cfg.getVendorFamily().getAws();
    // should pick subnet-2 since subnet-1 is missing.
    assertThat(awsFamily.getSubnetId(), equalTo("subnet-2"));
    assertThat(awsFamily.getVpcId(), equalTo("vpc"));
    assertThat(awsFamily.getRegion(), equalTo("r1"));
  }
}
