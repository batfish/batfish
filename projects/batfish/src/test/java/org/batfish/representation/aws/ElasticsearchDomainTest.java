package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.matchers.AclLineMatchers.isExprAclLineThat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.ExprAclLineMatchers.hasMatchCondition;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.hasLines;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestSubnet;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DOMAIN_STATUS_LIST;
import static org.batfish.representation.aws.ElasticsearchDomain.getNodeName;
import static org.batfish.representation.aws.Region.computeAntiSpoofingFilter;
import static org.batfish.representation.aws.Region.eniEgressAclName;
import static org.batfish.representation.aws.Region.eniIngressAclName;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.traceElementEniPrivateIp;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;
import static org.batfish.representation.aws.Utils.traceTextForAddress;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import org.batfish.common.Warnings;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
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

/** Tests for {@link ElasticsearchDomain} */
public class ElasticsearchDomainTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  private StaticRoute.Builder _staticRouteBuilder;
  private Map<String, Configuration> _configurations;
  private String _node0Name;
  private String _node1Name;

  public static final MatchHeaderSpace matchTcp =
      new MatchHeaderSpace(
          HeaderSpace.builder().setIpProtocols(TCP).build(), traceElementForProtocol(TCP));

  public static MatchHeaderSpace matchPorts(int fromPort, int toPort) {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setDstPorts(new SubRange(fromPort, toPort)).build(),
        traceElementForDstPorts(fromPort, toPort));
  }

  @Before
  public void setup() throws IOException {
    _staticRouteBuilder =
        StaticRoute.testBuilder()
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
            .setNetwork(Prefix.ZERO);
    _node0Name =
        getNodeName(
            0,
            "arn:aws:es:us-west-2:118292266645:domain/es-domain",
            "vpc-es-domain-uiqo2tedr5ttdqrhdzcu5upaee.us-west-2.es.amazonaws.com");
    _node1Name =
        getNodeName(
            1,
            "arn:aws:es:us-west-2:118292266645:domain/es-domain",
            "vpc-es-domain-uiqo2tedr5ttdqrhdzcu5upaee.us-west-2.es.amazonaws.com");
    _configurations = loadAwsConfigurations();
  }

  private Map<String, Configuration> loadAwsConfigurations() throws IOException {
    List<String> awsFilenames =
        ImmutableList.of(
            "ElasticsearchDomains.json",
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
  public void testEsSubnetEdge() throws IOException {
    Topology topology = TopologyUtil.synthesizeL3Topology(_configurations);

    // check that ES instance is a neighbor of both  subnets in which its interfaces are
    assertThat(
        topology.getEdges(),
        hasItem(
            new Edge(
                NodeInterfacePair.of(
                    "subnet-073b8061", Subnet.instancesInterfaceName("subnet-073b8061")),
                NodeInterfacePair.of(_node0Name, "subnet-073b8061"))));
    assertThat(
        topology.getEdges(),
        hasItem(
            new Edge(
                NodeInterfacePair.of(
                    "subnet-1f315846", Subnet.instancesInterfaceName("subnet-1f315846")),
                NodeInterfacePair.of(_node1Name, "subnet-1f315846"))));
  }

  /** Check that IPs are unique for all the interfaces */
  @Test
  public void testUniqueIps() throws IOException {
    List<Ip> ipsAsList =
        _configurations.values().stream()
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
  public void testDefaultRoute() {
    StaticRoute defaultRoute0 = _staticRouteBuilder.setNextHopIp(Ip.parse("192.168.2.17")).build();
    StaticRoute defaultRoute1 = _staticRouteBuilder.setNextHopIp(Ip.parse("172.31.0.1")).build();

    assertThat(_configurations, allOf(hasKey(_node0Name), hasKey(_node1Name)));
    assertThat(
        _configurations.get(_node0Name).getDefaultVrf().getStaticRoutes(),
        containsInAnyOrder(defaultRoute0));
    assertThat(
        _configurations.get(_node1Name).getDefaultVrf().getStaticRoutes(),
        containsInAnyOrder(defaultRoute1));
  }

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/aws/ElasticsearchDomainTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_DOMAIN_STATUS_LIST);
    List<ElasticsearchDomain> domains = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      domains.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), ElasticsearchDomain.class));
    }

    assertThat(
        domains,
        equalTo(
            ImmutableList.of(
                new ElasticsearchDomain(
                    "arn:aws:es:us-west-2:118292266645:domain/es-domain",
                    "es-domain",
                    "vpc-b390fad5",
                    "vpc-es-domain-uiqo2tedr5ttdqrhdzcu5upaee.us-west-2.es.amazonaws.com",
                    2,
                    ImmutableList.of("sg-55510831"),
                    ImmutableList.of("subnet-7044ff16"),
                    true))));
  }

  @Test
  public void testSecurityGroupsAcl() {
    Configuration node0 = _configurations.get(_node0Name);
    assertThat(node0, hasDeviceModel(DeviceModel.AWS_ELASTICSEARCH_DOMAIN));
    assertThat(node0.getAllInterfaces().entrySet(), hasSize(1));
    assertThat(
        node0
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
        node0
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
    for (Interface iface : node0.getAllInterfaces().values()) {
      assertThat(iface.getIncomingFilter().getName(), equalTo(eniIngressAclName(iface.getName())));
      assertThat(iface.getOutgoingFilter().getName(), equalTo(eniEgressAclName(iface.getName())));
      assertThat(
          node0.getIpAccessLists().get(eniEgressAclName(iface.getName())).getLines(),
          equalTo(
              ImmutableList.of(
                  computeAntiSpoofingFilter(iface),
                  new AclAclLine(
                      "Security Group Test Security Group",
                      "~EGRESS~SECURITY-GROUP~Test Security Group~" + "sg-0de0ddfa8a5a45810~",
                      Utils.getTraceElementForSecurityGroup("Test Security Group"),
                      null))));
      assertThat(
          node0.getIpAccessLists().get(eniIngressAclName(iface.getName())).getLines(),
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

  /** Test that the hierarchy and human name is configured properly */
  @Test
  public void testToConfigurationNode() {
    Subnet subnet = getTestSubnet(Prefix.parse("1.1.1.0/24"), "subnet", "vpc");
    ElasticsearchDomain esd =
        new ElasticsearchDomain(
            "arn",
            "es-domain",
            "vpc",
            "vpcEndpoint",
            4,
            ImmutableList.of("sg"),
            ImmutableList.of(subnet.getId()),
            true);

    Configuration cfg =
        esd.toConfigurationNode(
            0,
            subnet.getId(),
            new ConvertedConfiguration(),
            Region.builder("r1").setSubnets(ImmutableMap.of(subnet.getId(), subnet)).build(),
            new Warnings());

    AwsFamily awsFamily = cfg.getVendorFamily().getAws();
    assertThat(awsFamily.getSubnetId(), equalTo(esd.getSubnets().get(0)));
    assertThat(awsFamily.getVpcId(), equalTo("vpc"));
    assertThat(awsFamily.getRegion(), equalTo("r1"));
    assertThat(cfg.getHumanName(), equalTo(esd.getDomainName()));
  }

  /** Test that the expected set of nodes is generated by the entry function */
  @Test
  public void testToConfigurationNodes() {
    Subnet subnet0 = getTestSubnet(Prefix.parse("1.1.1.0/24"), "subnet0", "vpc");
    Subnet subnet1 = getTestSubnet(Prefix.parse("1.1.1.0/24"), "subnet1", "vpc");
    ElasticsearchDomain esd =
        new ElasticsearchDomain(
            "arn",
            "es-domain",
            "vpc",
            "vpcendpoint",
            4,
            ImmutableList.of("sg"),
            ImmutableList.of(subnet0.getId(), subnet1.getId()),
            true);

    List<Configuration> configurations =
        esd.toConfigurationNodes(
            new ConvertedConfiguration(),
            Region.builder("r1")
                .setSubnets(ImmutableMap.of(subnet0.getId(), subnet0, subnet1.getId(), subnet1))
                .build(),
            new Warnings());

    assertThat(
        configurations.stream()
            .map(Configuration::getHostname)
            .collect(ImmutableList.toImmutableList()),
        equalTo(
            IntStream.range(0, esd.getInstanceCount())
                .mapToObj(i -> getNodeName(i, esd.getId(), esd.getVpcEndpoint()))
                .collect(ImmutableList.toImmutableList())));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn", "domain", "vpc", "point", 1, ImmutableList.of(), ImmutableList.of(), true),
            new ElasticsearchDomain(
                "arn", "domain", "vpc", "point", 1, ImmutableList.of(), ImmutableList.of(), true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "other", "domain", "vpc", "point", 1, ImmutableList.of(), ImmutableList.of(), true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn", "other", "vpc", "point", 1, ImmutableList.of(), ImmutableList.of(), true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn", "domain", "other", "point", 1, ImmutableList.of(), ImmutableList.of(), true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn", "domain", "vpc", "other", 1, ImmutableList.of(), ImmutableList.of(), true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn", "domain", "vpc", "point", 0, ImmutableList.of(), ImmutableList.of(), true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn",
                "domain",
                "vpc",
                "point",
                1,
                ImmutableList.of("other"),
                ImmutableList.of(),
                true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn",
                "domain",
                "vpc",
                "point",
                1,
                ImmutableList.of(),
                ImmutableList.of("other"),
                true))
        .addEqualityGroup(
            new ElasticsearchDomain(
                "arn", "domain", "vpc", "point", 1, ImmutableList.of(), ImmutableList.of(), false))
        .testEquals();
  }
}
