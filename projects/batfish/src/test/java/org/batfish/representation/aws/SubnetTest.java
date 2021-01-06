package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasIncomingFilter;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasOutgoingFilter;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasFirewallSessionInterfaceInfo;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestSubnet;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestVpc;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SUBNETS;
import static org.batfish.representation.aws.NetworkAcl.getAclName;
import static org.batfish.representation.aws.Subnet.NLB_INSTANCE_TARGETS_IFACE_SUFFIX;
import static org.batfish.representation.aws.Subnet.NLB_INSTANCE_TARGETS_VRF_NAME;
import static org.batfish.representation.aws.Subnet.findSubnetNetworkAcl;
import static org.batfish.representation.aws.Subnet.instancesInterfaceName;
import static org.batfish.representation.aws.Utils.connect;
import static org.batfish.representation.aws.Utils.getInterfaceLinkLocalIp;
import static org.batfish.representation.aws.Utils.interfaceNameToRemote;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.Vpc.vrfNameForLink;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.matchers.IpAccessListMatchers;
import org.batfish.representation.aws.Instance.Status;
import org.batfish.representation.aws.IpPermissions.IpRange;
import org.batfish.representation.aws.LoadBalancer.AvailabilityZone;
import org.batfish.representation.aws.LoadBalancer.Scheme;
import org.batfish.representation.aws.LoadBalancer.Type;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.Route.TargetType;
import org.batfish.representation.aws.RouteTable.Association;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link Subnet} */
public class SubnetTest {

  private List<Subnet> _subnetList;

  @Before
  public void setup() throws IOException {
    JsonNode json =
        BatfishObjectMapper.mapper()
            .readTree(readResource("org/batfish/representation/aws/SubnetTest.json", UTF_8));
    _subnetList =
        BatfishObjectMapper.mapper()
            .convertValue(json.get(JSON_KEY_SUBNETS), new TypeReference<List<Subnet>>() {});
  }

  @Test
  public void testDeserialization() {
    assertThat(
        _subnetList,
        equalTo(
            ImmutableList.of(
                new Subnet(
                    Prefix.parse("172.31.0.0/20"),
                    "028403472736",
                    "arn:aws:ec2:us-east-1:028403472736:subnet/subnet-1",
                    "subnet-1",
                    "vpc-1",
                    "us-west-2c",
                    ImmutableMap.of()))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new Subnet(Prefix.ZERO, "owner", "arn", "id", "vpc", "zone", ImmutableMap.of()),
            new Subnet(Prefix.ZERO, "owner", "arn", "id", "vpc", "zone", ImmutableMap.of()))
        .addEqualityGroup(
            new Subnet(
                Prefix.parse("1.1.1.1/32"), "owner", "arn", "id", "vpc", "zone", ImmutableMap.of()))
        .addEqualityGroup(
            new Subnet(Prefix.ZERO, "other", "arn", "id", "vpc", "zone", ImmutableMap.of()))
        .addEqualityGroup(
            new Subnet(Prefix.ZERO, "owner", "other", "id", "vpc", "zone", ImmutableMap.of()))
        .addEqualityGroup(
            new Subnet(Prefix.ZERO, "owner", "arn", "other", "vpc", "zone", ImmutableMap.of()))
        .addEqualityGroup(
            new Subnet(Prefix.ZERO, "owner", "arn", "id", "other", "zone", ImmutableMap.of()))
        .addEqualityGroup(
            new Subnet(Prefix.ZERO, "owner", "arn", "id", "vpc", "other", ImmutableMap.of()))
        .addEqualityGroup(
            new Subnet(
                Prefix.ZERO, "owner", "arn", "id", "vpc", "zone", ImmutableMap.of("tag", "value")))
        .testEquals();
  }

  @Test
  public void testGetNextIp() {
    Subnet subnet = _subnetList.get(0);
    assertThat(subnet.getNextIp(), equalTo(Ip.parse("172.31.0.2")));
    assertThat(subnet.getNextIp(), equalTo(Ip.parse("172.31.0.3")));
    assertThat(subnet.getNextIp(), equalTo(Ip.parse("172.31.0.4")));
  }

  /** Test the simplest case of subnet with only a private prefix and not even a vpn gateway */
  @Test
  public void testToConfigurationNodePrivateOnly() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Ip privateIp = Ip.parse("10.10.10.10");
    Prefix privatePrefix = Prefix.create(privateIp, 24);

    NetworkInterface ni =
        new NetworkInterface(
            "ni",
            "subnet",
            vpc.getId(),
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, privateIp, null)),
            "desc",
            null,
            ImmutableMap.of());

    Subnet subnet = getTestSubnet(privatePrefix, "subnet", vpc.getId());

    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setNetworkInterfaces(ImmutableMap.of(ni.getId(), ni))
            .setNetworkAcls(
                ImmutableMap.of(
                    "acl",
                    new NetworkAcl(
                        "acl",
                        vpc.getId(),
                        ImmutableList.of(new NetworkAclAssociation(subnet.getId())),
                        ImmutableList.of(),
                        true)))
            .setRouteTables(
                ImmutableMap.of(
                    "rt1",
                    new RouteTable(
                        "rt1",
                        vpc.getId(),
                        ImmutableList.of(new Association(false, subnet.getId())),
                        ImmutableList.of(
                            new RouteV4(
                                privatePrefix, State.ACTIVE, "local", TargetType.Gateway)))))
            .build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableList.of(vpcConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PRIVATE));

    // subnet should have interfaces to the instances and vpc
    assertThat(
        subnetCfg.getAllInterfaces().values().stream()
            .map(Interface::getName)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of(instancesInterfaceName(subnet.getId()), vpc.getId())));

    // the vpc should have gotten an interface pointed to the subnet
    assertThat(
        vpcConfig.getAllInterfaces().values().stream()
            .map(Interface::getName)
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(subnet.getId())));

    // instance facing interface should have the right address
    assertThat(
        subnetCfg
            .getAllInterfaces()
            .get(instancesInterfaceName(subnet.getId()))
            .getConcreteAddress()
            .getPrefix(),
        equalTo(privatePrefix));

    // the vpc router should have a static route to the private prefix of the subnet
    assertThat(
        vpcConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    privatePrefix,
                    interfaceNameToRemote(subnetCfg),
                    subnetCfg.getAllInterfaces().get(vpc.getId()).getLinkLocalAddress().getIp()))));

    // the subnet router should have routes from the table
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    privatePrefix,
                    Utils.interfaceNameToRemote(vpcConfig, ""),
                    Utils.getInterfaceLinkLocalIp(vpcConfig, subnet.getId())))));
  }

  /** Test that public subnets are labeled as such */
  @Test
  public void testToConfigurationNodePublic() {
    Vpc vpc = getTestVpc("vpc");
    Subnet subnet = getTestSubnet(Prefix.parse("10.10.10.0/24"), "subnet", vpc.getId());
    InternetGateway igw =
        new InternetGateway("igw", ImmutableList.of(vpc.getId()), ImmutableMap.of());

    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setInternetGateways(ImmutableMap.of(igw.getId(), igw))
            .setRouteTables(
                ImmutableMap.of(
                    "rt1",
                    new RouteTable(
                        "rt1",
                        vpc.getId(),
                        ImmutableList.of(new Association(false, subnet.getId())),
                        ImmutableList.of(
                            new RouteV4(
                                Prefix.ZERO, State.ACTIVE, igw.getId(), TargetType.Gateway)))))
            .build();

    Configuration vpcConfig =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());
    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableList.of(vpcConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PUBLIC));
  }

  /**
   * Test that the subnet connects to all the VRFs on the VPC, and the VPC has a static route to the
   * subnet prefix inside all VRFs
   */
  @Test
  public void testToConfigurationNodeConnectAllVpcVrfs() {
    Configuration vpcCfg = Utils.newAwsConfiguration("vpc", "domain");

    // add a connection-based VRF (in addition to the default VRF)
    String connectionVrf = "vrf-connection";
    vpcCfg
        .getVrfs()
        .put(connectionVrf, Vrf.builder().setName(connectionVrf).setOwner(vpcCfg).build());

    Prefix subnetPrefix = Prefix.parse("1.1.1.1/32");
    Subnet subnet = getTestSubnet(subnetPrefix, "subnet", vpcCfg.getHostname());

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableList.of(vpcCfg));

    Configuration subnetCfg =
        subnet.toConfigurationNode(awsConfiguration, new Region("r1"), new Warnings());

    assertThat(
        subnetCfg.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(
                instancesInterfaceName(subnet.getId()), // to instances
                interfaceNameToRemote(vpcCfg), // default VRF
                interfaceNameToRemote(vpcCfg, connectionVrf)))); // connection-based VRF

    assertThat(
        vpcCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSortedSet.of(
                toStaticRoute(
                    subnetPrefix,
                    interfaceNameToRemote(subnetCfg),
                    getInterfaceLinkLocalIp(subnetCfg, vpcCfg.getHostname())))));

    assertThat(
        vpcCfg.getVrfs().get(connectionVrf).getStaticRoutes(),
        equalTo(
            ImmutableSortedSet.of(
                toStaticRoute(
                    subnetPrefix,
                    interfaceNameToRemote(subnetCfg, connectionVrf),
                    getInterfaceLinkLocalIp(
                        subnetCfg, interfaceNameToRemote(vpcCfg, connectionVrf))))));
  }

  private static void testProcessRouteHelper(
      RouteV4 route, String linkId, Configuration vpcCfg, Configuration subnetCfg) {
    // there should be a VRF on the VPC node
    String vrfName = vrfNameForLink(linkId);
    assertThat(vpcCfg, hasVrf(vrfName, any(Vrf.class)));

    // there should be an interface on the Subnet pointed to the VPC node
    Interface subnetIface = Iterables.getOnlyElement(subnetCfg.getAllInterfaces().values());
    assertThat(subnetIface, hasName(Utils.interfaceNameToRemote(vpcCfg, vrfNameForLink(linkId))));

    // there should be an interface on the VPC node
    Interface vpcIface = Iterables.getOnlyElement(vpcCfg.getAllInterfaces().values());
    assertThat(vpcIface, hasName(Utils.interfaceNameToRemote(subnetCfg, vrfNameForLink(linkId))));
    assertThat(vpcIface, hasVrfName(vrfNameForLink(linkId)));

    // right static routes on the subnet
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    route.getDestinationCidrBlock(),
                    Utils.interfaceNameToRemote(vpcCfg, vrfNameForLink(linkId)),
                    vpcIface.getLinkLocalAddress().getIp()))));
  }

  /** Tests that we do the right thing when processing a route for a VPC-level gateway. */
  @Test
  public void testProcessRouteVpcGateway() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Subnet subnet = getTestSubnet(Prefix.parse("10.10.10.0/24"), "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    InternetGateway igw =
        new InternetGateway("igw", ImmutableList.of(vpc.getId()), ImmutableMap.of());

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    RouteV4 route =
        new RouteV4(Prefix.parse("192.168.0.0/16"), State.ACTIVE, igw.getId(), TargetType.Gateway);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    vpcCfg
        .getVrfs()
        .put(
            vrfNameForLink(igw.getId()),
            Vrf.builder().setOwner(vpcCfg).setName(vrfNameForLink(igw.getId())).build());
    connect(
        awsConfiguration,
        subnetCfg,
        DEFAULT_VRF_NAME,
        vpcCfg,
        vrfNameForLink(igw.getId()),
        vrfNameForLink(igw.getId()));
    subnet.processRoute(
        subnetCfg,
        region,
        route,
        vpcCfg,
        ImmutableList.of(igw.getId()),
        awsConfiguration,
        new Warnings());

    testProcessRouteHelper(route, igw.getId(), vpcCfg, subnetCfg);
  }

  /** Tests that we do the right thing when processing a route for VPC peering connection. */
  @Test
  public void testProcessRouteVpcPeeringConnection() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");
    String connectionId = "peering";

    Subnet subnet = getTestSubnet(subnetPrefix, "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    RouteV4 route =
        new RouteV4(remotePrefix, State.ACTIVE, connectionId, TargetType.VpcPeeringConnection);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    vpcCfg
        .getVrfs()
        .put(
            vrfNameForLink(connectionId),
            Vrf.builder().setOwner(vpcCfg).setName(vrfNameForLink(connectionId)).build());
    connect(
        awsConfiguration,
        subnetCfg,
        DEFAULT_VRF_NAME,
        vpcCfg,
        vrfNameForLink(connectionId),
        vrfNameForLink(connectionId));

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, ImmutableList.of(), awsConfiguration, new Warnings());

    testProcessRouteHelper(route, connectionId, vpcCfg, subnetCfg);
  }

  /** Tests that we do the right thing when processing a route for transit gateway. */
  @Test
  public void testProcessRouteTransitGateway() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");

    Subnet subnet = getTestSubnet(subnetPrefix, "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    TransitGatewayVpcAttachment tgwVpcAttachment =
        new TransitGatewayVpcAttachment(
            "attachment", "tgw", vpc.getId(), ImmutableList.of(subnet.getId()));
    String linkId = tgwVpcAttachment.getId();

    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setTransitGatewayVpcAttachments(
                ImmutableMap.of(tgwVpcAttachment.getId(), tgwVpcAttachment))
            .build();

    RouteV4 route =
        new RouteV4(
            remotePrefix, State.ACTIVE, tgwVpcAttachment.getGatewayId(), TargetType.TransitGateway);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    vpcCfg
        .getVrfs()
        .put(
            vrfNameForLink(linkId),
            Vrf.builder().setOwner(vpcCfg).setName(vrfNameForLink(linkId)).build());
    connect(
        awsConfiguration,
        subnetCfg,
        DEFAULT_VRF_NAME,
        vpcCfg,
        vrfNameForLink(linkId),
        vrfNameForLink(linkId));
    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, ImmutableList.of(), awsConfiguration, new Warnings());

    testProcessRouteHelper(route, linkId, vpcCfg, subnetCfg);
  }

  /**
   * Tests that we handle the case of the gateway not being connected to the subnet's availability
   * zone
   */
  @Test
  public void testProcessRouteTransitGatewayOutsideConnectedAzs() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");

    Subnet subnet = getTestSubnet(subnetPrefix, "subnet", vpc.getId(), "zone");
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    Subnet otherSubnet =
        getTestSubnet(Prefix.parse("0.0.0.0/0"), "otherSubnet", vpc.getId(), "otherZone");

    TransitGatewayVpcAttachment tgwVpcAttachment =
        new TransitGatewayVpcAttachment(
            "attachment", "tgw", vpc.getId(), ImmutableList.of(otherSubnet.getId()));

    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet, otherSubnet.getId(), otherSubnet))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setTransitGatewayVpcAttachments(
                ImmutableMap.of(tgwVpcAttachment.getId(), tgwVpcAttachment))
            .build();

    RouteV4 route =
        new RouteV4(
            remotePrefix, State.ACTIVE, tgwVpcAttachment.getGatewayId(), TargetType.TransitGateway);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, ImmutableList.of(), awsConfiguration, new Warnings());

    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(toStaticRoute(route.getDestinationCidrBlock(), NULL_INTERFACE_NAME))));
  }

  /** Tests route processing for a NAT gateway that is inside the subnet */
  @Test
  public void testProcessRouteNatGatewayInSubnet() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Subnet subnet = getTestSubnet(Prefix.parse("10.10.10.0/24"), "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    NatGateway ngw =
        new NatGateway(
            "ngw",
            subnet.getId(),
            vpc.getId(),
            ImmutableList.of(
                new NatGatewayAddress(
                    "allocation", "eni", Ip.parse("10.10.10.2"), Ip.parse("2.2.2.2"))),
            ImmutableMap.of());

    Region region =
        Region.builder("region").setNatGateways(ImmutableMap.of(ngw.getId(), ngw)).build();

    RouteV4 route =
        new RouteV4(
            Prefix.parse("192.168.0.0/16"), State.ACTIVE, ngw.getId(), TargetType.NatGateway);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, ImmutableList.of(), awsConfiguration, new Warnings());

    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSortedSet.of(
                StaticRoute.testBuilder()
                    .setNetwork(route.getDestinationCidrBlock())
                    .setNextHopInterface(instancesInterfaceName(subnet.getId()))
                    .setNextHopIp(ngw.getPrivateIp())
                    .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                    .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                    .build())));
  }

  /** Tests route processing for a NAT gateway that is outside the subnet */
  @Test
  public void testProcessRouteNatGatewayOutsideSubnet() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Subnet subnet = getTestSubnet(Prefix.parse("10.10.10.0/24"), "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    NatGateway ngw =
        new NatGateway(
            "ngw",
            "otherSubnet",
            vpc.getId(),
            ImmutableList.of(
                new NatGatewayAddress(
                    "allocation", "eni", Ip.parse("10.10.10.2"), Ip.parse("2.2.2.2"))),
            ImmutableMap.of());

    Region region =
        Region.builder("region").setNatGateways(ImmutableMap.of(ngw.getId(), ngw)).build();

    RouteV4 route =
        new RouteV4(
            Prefix.parse("192.168.0.0/16"), State.ACTIVE, ngw.getId(), TargetType.NatGateway);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();

    vpcCfg
        .getVrfs()
        .put(
            vrfNameForLink(ngw.getId()),
            Vrf.builder().setOwner(vpcCfg).setName(vrfNameForLink(ngw.getId())).build());
    connect(
        awsConfiguration,
        subnetCfg,
        DEFAULT_VRF_NAME,
        vpcCfg,
        vrfNameForLink(ngw.getId()),
        vrfNameForLink(ngw.getId()));

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, ImmutableList.of(), awsConfiguration, new Warnings());

    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSortedSet.of(
                StaticRoute.testBuilder()
                    .setNetwork(route.getDestinationCidrBlock())
                    .setNextHopInterface(interfaceNameToRemote(vpcCfg, vrfNameForLink(ngw.getId())))
                    .setNextHopIp(
                        getInterfaceLinkLocalIp(
                            vpcCfg,
                            Utils.interfaceNameToRemote(subnetCfg, vrfNameForLink(ngw.getId()))))
                    .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                    .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                    .build())));
  }

  /** Test that all prefixes of the prefix list are inserted as destinations */
  @Test
  public void testProcessRoutePrefixList() {
    Configuration vpcCfg = Utils.newAwsConfiguration("vpc", "awstest");

    Subnet subnet = getTestSubnet(Prefix.parse("10.10.10.0/24"), "subnet", "vpc");
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    List<Prefix> prefixes =
        ImmutableList.of(Prefix.parse("1.1.1.1/32"), Prefix.parse("2.2.2.2/32"));
    PrefixList prefixList = new PrefixList("plist", prefixes, "name");

    Region region =
        Region.builder("region")
            .setPrefixLists(ImmutableMap.of(prefixList.getId(), prefixList))
            .build();

    RoutePrefixListId route =
        new RoutePrefixListId(prefixList.getId(), State.BLACKHOLE, null, TargetType.Gateway);

    subnet.processRoute(
        subnetCfg,
        region,
        route,
        vpcCfg,
        ImmutableList.of(),
        new ConvertedConfiguration(),
        new Warnings());

    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            prefixes.stream()
                .map(
                    prefix ->
                        StaticRoute.testBuilder()
                            .setNetwork(prefix)
                            .setNextHopInterface(NULL_INTERFACE_NAME)
                            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                            .build())
                .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()))));
  }

  /** Test that network ACls are properly attached */
  @Test
  public void testToConfigurationNodeNetworkAcl() {
    Vpc vpc = getTestVpc("vpc");
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Subnet subnet = getTestSubnet(subnetPrefix, "subnet", vpc.getId());

    Prefix outDeniedPfx = Prefix.parse("192.168.0.0/16");
    Prefix inDeniedPfx = Prefix.parse("192.169.0.0/16");

    NetworkAcl acl =
        new NetworkAcl(
            "networkAcl",
            vpc.getId(),
            ImmutableList.of(new NetworkAclAssociation(subnet.getId())),
            ImmutableList.of(
                new NetworkAclEntryV4(outDeniedPfx, false, true, "-1", 100, null, null),
                new NetworkAclEntryV4(Prefix.ZERO, true, true, "-1", 200, null, null),
                new NetworkAclEntryV4(inDeniedPfx, false, false, "-1", 100, null, null),
                new NetworkAclEntryV4(Prefix.ZERO, true, false, "-1", 200, null, null)),
            true);

    Region region = Region.builder("r").setNetworkAcls(ImmutableMap.of(acl.getId(), acl)).build();

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableList.of(vpcCfg));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PRIVATE));

    // NACLs are installed on the configuration nodes
    assertThat(subnetCfg.getIpAccessLists(), hasKey(getAclName(acl.getId(), false)));
    assertThat(subnetCfg.getIpAccessLists(), hasKey(getAclName(acl.getId(), true)));

    // NACLs are not installed on the instances-facing interface
    Interface instancesInterface =
        subnetCfg.getAllInterfaces().get(instancesInterfaceName(subnet.getId()));
    assertThat(instancesInterface.getIncomingFilter(), nullValue());
    assertThat(instancesInterface.getOutgoingFilter(), nullValue());

    // NACLs are installed on the vpc-facing interface
    Interface ifaceToVpc = subnetCfg.getAllInterfaces().get(interfaceNameToRemote(vpcCfg));
    assertThat(
        ifaceToVpc.getIncomingFilter(),
        IpAccessListMatchers.hasName(getAclName(acl.getId(), false)));
    assertThat(
        ifaceToVpc.getOutgoingFilter(),
        IpAccessListMatchers.hasName(getAclName(acl.getId(), true)));
  }

  @Test
  public void testFindMyNetworkAclNoMatchingVpc() {
    Map<String, NetworkAcl> networkAcls =
        ImmutableMap.of(
            "aclId",
            new NetworkAcl("aclId", "novpc", ImmutableList.of(), ImmutableList.of(), true));

    assertThat(findSubnetNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of()));
  }

  @Test
  public void testFindMyNetworkAclPickRightSubnet() {
    NetworkAcl networkAcl =
        new NetworkAcl(
            "acl",
            "vpc",
            ImmutableList.of(new NetworkAclAssociation("subnet")),
            ImmutableList.of(),
            true);
    Map<String, NetworkAcl> networkAcls =
        ImmutableMap.of(
            networkAcl.getId(),
            networkAcl,
            "otherAcl",
            new NetworkAcl(
                "otherAcl",
                "vpc",
                ImmutableList.of(new NetworkAclAssociation("otherSubnet")),
                ImmutableList.of(),
                true));

    assertThat(
        findSubnetNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of(networkAcl)));
  }

  @Test
  public void testFindMyNetworkAclPickRightVpc() {
    NetworkAcl networkAcl =
        new NetworkAcl(
            "acl",
            "vpc",
            ImmutableList.of(
                new NetworkAclAssociation("subnet"), new NetworkAclAssociation("otherSubnet")),
            ImmutableList.of(),
            true);
    Map<String, NetworkAcl> networkAcls =
        ImmutableMap.of(
            networkAcl.getId(),
            networkAcl,
            "otherAcl",
            new NetworkAcl(
                "otherAcl",
                "otherVpc",
                ImmutableList.of(new NetworkAclAssociation("subnet")),
                ImmutableList.of(),
                true));

    assertThat(
        findSubnetNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of(networkAcl)));
  }

  @Test
  public void testFindMyNetworkAclPickDefaultInVpc() {
    NetworkAcl networkAcl =
        new NetworkAcl("acl", "vpc", ImmutableList.of(), ImmutableList.of(), true);
    Map<String, NetworkAcl> networkAcls =
        ImmutableMap.of(
            networkAcl.getId(),
            networkAcl,
            "otherAcl",
            new NetworkAcl(
                "otherAcl",
                "otherVpc",
                ImmutableList.of(new NetworkAclAssociation("subnet")),
                ImmutableList.of(),
                true));

    assertThat(
        findSubnetNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of(networkAcl)));
  }

  @Test
  public void testAddNlbInstanceTargetInterfaces_subnetWithNoNlbOrInstanceTarget() {
    Subnet subnet = _subnetList.get(0);
    String instanceId = "instanceId";
    String lbDnsName = "lbDnsName";
    String subnetHostname = Subnet.nodeName(subnet.getId());
    String vpcHostname = Vpc.nodeName(subnet.getVpcId());

    // Subnet has no NLBs or instance targets: no VRFs or interfaces created
    Map<String, Configuration> configs =
        createConfigs(
            subnet.getId(), subnet.getVpcId(), lbDnsName, subnet.getAvailabilityZone(), instanceId);
    Configuration subnetCfg = configs.get(subnetHostname);
    Configuration vpcCfg = configs.get(vpcHostname);
    ConvertedConfiguration awsConf = new ConvertedConfiguration(configs.values());
    Region region = new Region("region");
    subnet.addNlbInstanceTargetInterfaces(awsConf, region, subnetCfg, vpcCfg, null, null);

    // Nothing should have gotten a new VRF or any interfaces
    configs
        .values()
        .forEach(
            cfg -> {
              assertThat(cfg.getVrfs(), not(hasKey(NLB_INSTANCE_TARGETS_VRF_NAME)));
              assertThat(cfg.getAllInterfaces(), anEmptyMap());
            });
  }

  @Test
  public void testAddNlbInstanceTargetInterfaces_subnetWithLoadBalancer() {
    Subnet subnet = _subnetList.get(0);

    // Create instance target not in subnet
    String otherSubnetId = "otherSubnetId";
    Subnet otherSubnet =
        getTestSubnet(
            Prefix.parse("1.1.1.0/24"),
            otherSubnetId,
            subnet.getVpcId(),
            subnet.getAvailabilityZone());
    String instanceId = "instanceId";
    Ip instanceIp = Ip.parse("1.1.1.1");
    Instance instanceInOtherSubnet =
        new Instance(
            instanceId,
            subnet.getVpcId(),
            otherSubnetId,
            ImmutableList.of(),
            ImmutableList.of(),
            instanceIp,
            ImmutableMap.of(),
            Status.RUNNING);

    // Create load balancer in subnet
    String lbArn = "lbArn";
    String lbDnsName = "lbDnsName";
    AvailabilityZone subnetAz = new AvailabilityZone(subnet.getId(), subnet.getAvailabilityZone());
    LoadBalancer loadBalancerInSubnet =
        new LoadBalancer(
            lbArn,
            ImmutableList.of(subnetAz),
            lbDnsName,
            "name",
            Scheme.INTERNAL,
            Type.NETWORK,
            subnet.getVpcId());

    String subnetHostname = Subnet.nodeName(subnet.getId());
    String vpcHostname = Vpc.nodeName(subnet.getVpcId());
    String nlbHostname = LoadBalancer.getNodeId(lbDnsName, subnet.getAvailabilityZone());
    String instanceHostname = Instance.instanceHostname(instanceId);

    IpAccessList ingressAcl = IpAccessList.builder().setName("ingress").build();
    IpAccessList egressAcl = IpAccessList.builder().setName("egress").build();

    // Subnet has an NLB, no instance targets: Should create VRFs on subnet and VPC, connect subnet
    // to NLB and VPC
    Map<String, Configuration> configs =
        createConfigs(
            subnet.getId(), subnet.getVpcId(), lbDnsName, subnet.getAvailabilityZone(), instanceId);
    Configuration subnetCfg = configs.get(subnetHostname);
    Configuration vpcCfg = configs.get(vpcHostname);
    Configuration nlbCfg = configs.get(nlbHostname);
    Configuration instanceCfg = configs.get(instanceHostname);
    ConvertedConfiguration awsConf =
        new ConvertedConfiguration(
            configs.values(),
            new HashSet<>(), // layer 1 edges
            ImmutableMultimap.of(otherSubnetId, instanceInOtherSubnet), // subnets to targets
            ImmutableMultimap.of(subnet.getId(), loadBalancerInSubnet), // subnets to NLBs
            ImmutableMultimap.of(lbArn, instanceInOtherSubnet)); // NLBs to targets
    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet, otherSubnetId, otherSubnet))
            .build();
    subnet.addNlbInstanceTargetInterfaces(
        awsConf, region, subnetCfg, vpcCfg, ingressAcl, egressAcl);

    // New VRFs created in subnet and VPC configs
    assertThat(subnetCfg.getVrfs(), hasKey(NLB_INSTANCE_TARGETS_VRF_NAME));
    assertThat(vpcCfg.getVrfs(), hasKey(NLB_INSTANCE_TARGETS_VRF_NAME));

    // Subnet should be connected to VPC on their new VRFs; subnet iface needs session info to send
    // return flow from instance target back to NLB (for instances outside the subnet)
    String subnetToVpcIfaceName = interfaceNameToRemote(vpcCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX);
    assertThat(
        subnetCfg.getAllInterfaces().get(subnetToVpcIfaceName),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(equalTo(ingressAcl)),
            hasOutgoingFilter(equalTo(egressAcl)),
            hasFirewallSessionInterfaceInfo(hasAcls(ingressAcl.getName(), egressAcl.getName()))));
    assertThat(
        vpcCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(nullValue())));

    // Subnet should be connected to NLB on subnet's new VRF, NLB's default VRF; NLB iface should
    // have session info
    assertThat(
        subnetCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(nlbCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(nullValue())));
    assertThat(
        nlbCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(
            hasVrfName(DEFAULT_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(hasNoAcls())));

    // Subnet should have static route to instance IP out the interface to VPC
    assertThat(
        subnetCfg,
        hasVrf(
            NLB_INSTANCE_TARGETS_VRF_NAME,
            hasStaticRoutes(
                contains(
                    toStaticRoute(
                        instanceIp.toPrefix(),
                        subnetToVpcIfaceName,
                        AwsConfiguration.LINK_LOCAL_IP)))));

    // Subnet should have static route to instance IP out the interface to VPC
    assertThat(
        subnetCfg,
        hasVrf(
            NLB_INSTANCE_TARGETS_VRF_NAME,
            hasStaticRoutes(
                contains(
                    toStaticRoute(
                        instanceIp.toPrefix(),
                        subnetToVpcIfaceName,
                        AwsConfiguration.LINK_LOCAL_IP)))));

    // VPC should have no static routes since it only gets routes for instance targets in the subnet
    assertThat(vpcCfg, hasVrf(NLB_INSTANCE_TARGETS_VRF_NAME, hasStaticRoutes(empty())));

    // Instance should be unaffected
    assertThat(instanceCfg.getAllInterfaces(), anEmptyMap());
  }

  @Test
  public void testAddNlbInstanceTargetInterfaces_subnetWithInstanceTarget() {
    Subnet subnet = _subnetList.get(0);

    // Create instance target in subnet
    String instanceId = "instanceId";
    Ip instanceIp = Ip.parse("1.1.1.1");
    Instance instanceInSubnet =
        new Instance(
            instanceId,
            subnet.getVpcId(),
            subnet.getId(),
            ImmutableList.of(),
            ImmutableList.of(),
            instanceIp,
            ImmutableMap.of(),
            Status.RUNNING);

    // Create load balancer not in subnet
    String lbArn = "lbArn";
    String lbDnsName = "lbDnsName";

    String subnetHostname = Subnet.nodeName(subnet.getId());
    String vpcHostname = Vpc.nodeName(subnet.getVpcId());
    String nlbHostname = LoadBalancer.getNodeId(lbDnsName, subnet.getAvailabilityZone());
    String instanceHostname = Instance.instanceHostname(instanceId);

    IpAccessList ingressAcl = IpAccessList.builder().setName("ingress").build();
    IpAccessList egressAcl = IpAccessList.builder().setName("egress").build();

    // Subnet has an instance target, no NLBs: Should create VRFs on subnet and VPC, connect subnet
    // to VPC and instance target
    Map<String, Configuration> configs =
        createConfigs(
            subnet.getId(), subnet.getVpcId(), lbDnsName, subnet.getAvailabilityZone(), instanceId);
    Configuration subnetCfg = configs.get(subnetHostname);
    Configuration vpcCfg = configs.get(vpcHostname);
    Configuration nlbCfg = configs.get(nlbHostname);
    Configuration instanceCfg = configs.get(instanceHostname);
    ConvertedConfiguration awsConf =
        new ConvertedConfiguration(
            configs.values(),
            new HashSet<>(), // layer 1 edges
            ImmutableMultimap.of(subnet.getId(), instanceInSubnet), // subnets to targets
            ImmutableMultimap.of(), // subnets to NLBs
            ImmutableMultimap.of(lbArn, instanceInSubnet)); // NLBs to targets
    Region region =
        Region.builder("region").setSubnets(ImmutableMap.of(subnet.getId(), subnet)).build();
    subnet.addNlbInstanceTargetInterfaces(
        awsConf, region, subnetCfg, vpcCfg, ingressAcl, egressAcl);

    // New VRFs created in subnet and VPC configs
    assertThat(subnetCfg.getVrfs(), hasKey(NLB_INSTANCE_TARGETS_VRF_NAME));
    assertThat(vpcCfg.getVrfs(), hasKey(NLB_INSTANCE_TARGETS_VRF_NAME));

    // Subnet should be connected to VPC on their new VRFs; both ifaces should have session info
    String vpcToSubnetIfaceName =
        interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX);
    assertThat(
        subnetCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(vpcCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(equalTo(ingressAcl)),
            hasOutgoingFilter(equalTo(egressAcl)),
            hasFirewallSessionInterfaceInfo(hasAcls(ingressAcl.getName(), egressAcl.getName()))));
    assertThat(
        vpcCfg.getAllInterfaces().get(vpcToSubnetIfaceName),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(hasNoAcls())));

    // Instance target should be connected to subnet on subnet's new VRF, instance's default VRF
    String subnetToInstanceIfaceName =
        interfaceNameToRemote(instanceCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX);
    assertThat(
        subnetCfg.getAllInterfaces().get(subnetToInstanceIfaceName),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(hasNoAcls())));
    assertThat(
        instanceCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(hasVrfName(DEFAULT_VRF_NAME), hasFirewallSessionInterfaceInfo(hasNoAcls())));

    // Subnet should have static route to instance IP out the interface to the instance target
    assertThat(
        subnetCfg,
        hasVrf(
            NLB_INSTANCE_TARGETS_VRF_NAME,
            hasStaticRoutes(
                contains(
                    toStaticRoute(
                        instanceIp.toPrefix(),
                        subnetToInstanceIfaceName,
                        AwsConfiguration.LINK_LOCAL_IP)))));

    // VPC should have static route to instance IP out the interface to subnet
    assertThat(
        vpcCfg,
        hasVrf(
            NLB_INSTANCE_TARGETS_VRF_NAME,
            hasStaticRoutes(
                contains(
                    toStaticRoute(
                        instanceIp.toPrefix(),
                        vpcToSubnetIfaceName,
                        AwsConfiguration.LINK_LOCAL_IP)))));

    // NLB should be unaffected
    assertThat(nlbCfg.getAllInterfaces(), anEmptyMap());
  }

  @Test
  public void testAddNlbInstanceTargetInterfaces_subnetWithNlbAndInstanceTarget() {
    Subnet subnet = _subnetList.get(0);

    // Create instance target in subnet
    String instanceId = "instanceId";
    Ip instanceIp = Ip.parse("1.1.1.1");
    Instance instanceInSubnet =
        new Instance(
            instanceId,
            subnet.getVpcId(),
            subnet.getId(),
            ImmutableList.of(),
            ImmutableList.of(),
            instanceIp,
            ImmutableMap.of(),
            Status.RUNNING);

    // Create load balancer in subnet
    String lbArn = "lbArn";
    String lbDnsName = "lbDnsName";
    AvailabilityZone subnetAz = new AvailabilityZone(subnet.getId(), subnet.getAvailabilityZone());
    LoadBalancer loadBalancerInSubnet =
        new LoadBalancer(
            lbArn,
            ImmutableList.of(subnetAz),
            lbDnsName,
            "name",
            Scheme.INTERNAL,
            Type.NETWORK,
            subnet.getVpcId());

    String subnetHostname = Subnet.nodeName(subnet.getId());
    String vpcHostname = Vpc.nodeName(subnet.getVpcId());
    String nlbHostname = LoadBalancer.getNodeId(lbDnsName, subnet.getAvailabilityZone());
    String instanceHostname = Instance.instanceHostname(instanceId);

    IpAccessList ingressAcl = IpAccessList.builder().setName("ingress").build();
    IpAccessList egressAcl = IpAccessList.builder().setName("egress").build();

    // Subnet has an instance target and its NLB: Should create VRFs on subnet and VPC, connect
    // subnet to instance target, NLB, and VPC
    Map<String, Configuration> configs =
        createConfigs(
            subnet.getId(), subnet.getVpcId(), lbDnsName, subnet.getAvailabilityZone(), instanceId);
    Configuration subnetCfg = configs.get(subnetHostname);
    Configuration vpcCfg = configs.get(vpcHostname);
    Configuration nlbCfg = configs.get(nlbHostname);
    Configuration instanceCfg = configs.get(instanceHostname);
    ConvertedConfiguration awsConf =
        new ConvertedConfiguration(
            configs.values(),
            new HashSet<>(), // layer 1 edges
            ImmutableMultimap.of(subnet.getId(), instanceInSubnet), // subnets to targets
            ImmutableMultimap.of(subnet.getId(), loadBalancerInSubnet), // subnets to NLBs
            ImmutableMultimap.of(lbArn, instanceInSubnet)); // NLBs to targets
    Region region =
        Region.builder("region").setSubnets(ImmutableMap.of(subnet.getId(), subnet)).build();
    subnet.addNlbInstanceTargetInterfaces(
        awsConf, region, subnetCfg, vpcCfg, ingressAcl, egressAcl);

    // New VRFs created in subnet and VPC configs
    assertThat(subnetCfg.getVrfs(), hasKey(NLB_INSTANCE_TARGETS_VRF_NAME));
    assertThat(vpcCfg.getVrfs(), hasKey(NLB_INSTANCE_TARGETS_VRF_NAME));

    // Subnet should be connected to VPC on their new VRFs; both ifaces should have session info
    String vpcToSubnetIfaceName =
        interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX);
    assertThat(
        subnetCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(vpcCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(equalTo(ingressAcl)),
            hasOutgoingFilter(equalTo(egressAcl)),
            hasFirewallSessionInterfaceInfo(hasAcls(ingressAcl.getName(), egressAcl.getName()))));
    assertThat(
        vpcCfg.getAllInterfaces().get(vpcToSubnetIfaceName),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(hasNoAcls())));

    // Subnet should be connected to NLB on subnet's new VRF, NLB's default VRF; NLB iface should
    // have session info
    assertThat(
        subnetCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(nlbCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(nullValue())));
    assertThat(
        nlbCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(
            hasVrfName(DEFAULT_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(hasNoAcls())));

    // Instance target should be connected to subnet on subnet's new VRF, instance's default VRF
    String subnetToInstanceIfaceName =
        interfaceNameToRemote(instanceCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX);
    assertThat(
        subnetCfg.getAllInterfaces().get(subnetToInstanceIfaceName),
        allOf(
            hasVrfName(NLB_INSTANCE_TARGETS_VRF_NAME),
            hasIncomingFilter(nullValue()),
            hasOutgoingFilter(nullValue()),
            hasFirewallSessionInterfaceInfo(hasNoAcls())));
    assertThat(
        instanceCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX)),
        allOf(hasVrfName(DEFAULT_VRF_NAME), hasFirewallSessionInterfaceInfo(hasNoAcls())));

    // Subnet should have static route to instance IP out the interface to the instance target
    assertThat(
        subnetCfg,
        hasVrf(
            NLB_INSTANCE_TARGETS_VRF_NAME,
            hasStaticRoutes(
                contains(
                    toStaticRoute(
                        instanceIp.toPrefix(),
                        subnetToInstanceIfaceName,
                        AwsConfiguration.LINK_LOCAL_IP)))));

    // VPC should have static route to instance IP out the interface to subnet
    assertThat(
        vpcCfg,
        hasVrf(
            NLB_INSTANCE_TARGETS_VRF_NAME,
            hasStaticRoutes(
                contains(
                    toStaticRoute(
                        instanceIp.toPrefix(),
                        vpcToSubnetIfaceName,
                        AwsConfiguration.LINK_LOCAL_IP)))));
  }

  @Test
  public void testAddNlbInstanceTargetInterfaces_appliesInstanceSecurityGroups() {
    // addNlbInstanceTargetInterfaces should apply security groups to instance's new interface
    Subnet subnet = _subnetList.get(0);

    String sgName = "sg";
    IpPermissions ipPermissions =
        new IpPermissions(
            "tcp",
            null,
            null,
            ImmutableList.of(new IpRange(Prefix.parse("1.1.1.0/24"))),
            ImmutableList.of(),
            ImmutableList.of());
    SecurityGroup sg =
        new SecurityGroup(
            sgName,
            sgName,
            ImmutableList.of(ipPermissions),
            ImmutableList.of(ipPermissions),
            subnet.getVpcId());

    // Create instance target in subnet
    String instanceId = "instanceId";
    Ip instanceIp = Ip.parse("1.1.1.1");
    Instance instanceInSubnet =
        new Instance(
            instanceId,
            subnet.getVpcId(),
            subnet.getId(),
            ImmutableList.of(sgName),
            ImmutableList.of(),
            instanceIp,
            ImmutableMap.of(),
            Status.RUNNING);

    // Create load balancer not in subnet
    String lbArn = "lbArn";
    String lbDnsName = "lbDnsName";

    String subnetHostname = Subnet.nodeName(subnet.getId());
    String vpcHostname = Vpc.nodeName(subnet.getVpcId());
    String instanceHostname = Instance.instanceHostname(instanceId);

    // Subnet has an instance target, no NLBs: Should create VRFs on subnet and VPC, connect subnet
    // to VPC and instance target
    Map<String, Configuration> configs =
        createConfigs(
            subnet.getId(), subnet.getVpcId(), lbDnsName, subnet.getAvailabilityZone(), instanceId);
    Configuration subnetCfg = configs.get(subnetHostname);
    Configuration vpcCfg = configs.get(vpcHostname);
    Configuration instanceCfg = configs.get(instanceHostname);
    ConvertedConfiguration awsConf =
        new ConvertedConfiguration(
            configs.values(),
            new HashSet<>(), // layer 1 edges
            ImmutableMultimap.of(subnet.getId(), instanceInSubnet), // subnets to targets
            ImmutableMultimap.of(), // subnets to NLBs
            ImmutableMultimap.of(lbArn, instanceInSubnet)); // NLBs to targets
    Region region =
        Region.builder("region")
            .setSecurityGroups(ImmutableMap.of(sgName, sg))
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .build();
    region.convertSecurityGroups(awsConf, new Warnings());
    subnet.addNlbInstanceTargetInterfaces(awsConf, region, subnetCfg, vpcCfg, null, null);

    // New interface on instance should filter with AclAclLines with ACL defined by security group
    String secGroupIngressAclName = sg.toAcl(region, true, new Warnings()).getName();
    String secGroupEgressAclName = sg.toAcl(region, false, new Warnings()).getName();
    Interface newInstanceIface =
        instanceCfg
            .getAllInterfaces()
            .get(interfaceNameToRemote(subnetCfg, NLB_INSTANCE_TARGETS_IFACE_SUFFIX));
    assertThat(
        newInstanceIface.getIncomingFilter().getLines(),
        contains(hasProperty("aclName", equalTo(secGroupIngressAclName))));
    assertThat(
        newInstanceIface.getOutgoingFilter().getLines(),
        contains(
            equalTo(Region.computeAntiSpoofingFilter(newInstanceIface)),
            hasProperty("aclName", equalTo(secGroupEgressAclName))));
  }

  /**
   * Creates configs for a VPC, subnet, NLB, and instance. Does not create any interfaces. Creates
   * default VRFs on NLB and instance configs.
   */
  private static Map<String, Configuration> createConfigs(
      String subnetId, String vpcId, String lbDnsName, String availabilityZone, String instanceId) {
    String subnetHostname = Subnet.nodeName(subnetId);
    String vpcHostname = Vpc.nodeName(vpcId);
    String nlbHostname = LoadBalancer.getNodeId(lbDnsName, availabilityZone);
    String instanceHostname = Instance.instanceHostname(instanceId);

    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.AWS);
    Configuration subnetCfg = cb.setHostname(subnetHostname).build();
    Configuration vpcCfg = cb.setHostname(vpcHostname).build();
    Configuration nlbCfg = cb.setHostname(nlbHostname).build();
    Configuration instanceCfg = cb.setHostname(instanceHostname).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(nlbCfg).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(instanceCfg).build();
    return ImmutableMap.of(
        subnetHostname,
        subnetCfg,
        vpcHostname,
        vpcCfg,
        nlbHostname,
        nlbCfg,
        instanceHostname,
        instanceCfg);
  }

  private static Matcher<FirewallSessionInterfaceInfo> hasNoAcls() {
    return hasAcls(null, null);
  }

  /** Creates a matcher for a {@link FirewallSessionInterfaceInfo} with the given ACLs */
  private static Matcher<FirewallSessionInterfaceInfo> hasAcls(String incoming, String outgoing) {
    return allOf(
        hasProperty("incomingAclName", incoming == null ? nullValue() : equalTo(incoming)),
        hasProperty("outgoingAclName", outgoing == null ? nullValue() : equalTo(outgoing)));
  }
}
