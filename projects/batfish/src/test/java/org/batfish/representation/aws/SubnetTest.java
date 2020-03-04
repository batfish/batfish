package org.batfish.representation.aws;

import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SUBNETS;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;
import static org.batfish.representation.aws.InternetGateway.UNASSOCIATED_PRIVATE_IP_FILTER_NAME;
import static org.batfish.representation.aws.InternetGateway.computeUnassociatedPrivateIpFilter;
import static org.batfish.representation.aws.NetworkAcl.getAclName;
import static org.batfish.representation.aws.Subnet.findSubnetNetworkAcl;
import static org.batfish.representation.aws.Subnet.instancesInterfaceName;
import static org.batfish.representation.aws.Utils.interfaceNameToRemote;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.Vpc.nodeName;
import static org.batfish.representation.aws.Vpc.vrfNameForLink;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.Route.TargetType;
import org.batfish.representation.aws.RouteTable.Association;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link Subnet} */
public class SubnetTest {

  private List<Subnet> _subnetList;

  @Before
  public void setup() throws IOException {
    JsonNode json =
        BatfishObjectMapper.mapper()
            .readTree(CommonUtil.readResource("org/batfish/representation/aws/SubnetTest.json"));
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
                    "subnet-1",
                    "vpc-1",
                    "us-west-2c",
                    ImmutableMap.of()))));
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
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
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
            null);

    Subnet subnet = new Subnet(privatePrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());

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
        new ConvertedConfiguration(ImmutableMap.of(vpcConfig.getHostname(), vpcConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PRIVATE));

    // subnet should have interfaces to the instances and vpc
    assertThat(
        subnetCfg.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of(subnet.getId(), vpc.getId())));

    // the vpc should have gotten an interface pointed to the subnet
    assertThat(
        vpcConfig.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(subnet.getId())));

    // instance facing interface should have the right address
    assertThat(
        subnetCfg.getAllInterfaces().get(subnet.getId()).getConcreteAddress().getPrefix(),
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

  @Test
  public void testToConfigurationNodePrivateVgw() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    VpnGateway vgw = new VpnGateway("igw", ImmutableList.of(vpc.getId()), ImmutableMap.of());
    Configuration vgwConfig = Utils.newAwsConfiguration(vgw.getId(), "awstest");

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
            null);

    Subnet subnet = new Subnet(privatePrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());

    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setVpnGateways(ImmutableMap.of(vgw.getId(), vgw))
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
                            new RouteV4(privatePrefix, State.ACTIVE, "local", TargetType.Gateway),
                            new RouteV4(
                                Prefix.parse("0.0.0.0/0"),
                                State.ACTIVE,
                                vgw.getId(),
                                TargetType.Gateway)))))
            .build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(
            ImmutableMap.of(
                vpcConfig.getHostname(), vpcConfig, vgwConfig.getHostname(), vgwConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PRIVATE));

    // subnet should have interfaces to the instances, vpc, and vgw
    assertThat(
        subnetCfg.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of(subnet.getId(), vpc.getId(), vgw.getId())));

    // the vgw should have gotten an interface pointed to the subnet
    assertThat(
        vgwConfig.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(subnet.getId())));

    // the vgw router should have a static route to the private prefix
    assertThat(
        vgwConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    privatePrefix,
                    interfaceNameToRemote(subnetCfg),
                    Utils.getInterfaceLinkLocalIp(subnetCfg, vgw.getId())))));
  }

  @Test
  public void testToConfigurationNodePublic() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    InternetGateway igw =
        new InternetGateway("igw", ImmutableList.of(vpc.getId()), ImmutableMap.of());
    Configuration igwConfig = Utils.newAwsConfiguration(igw.getId(), "awstest");
    igwConfig.setIpAccessLists(
        ImmutableMap.of(
            UNASSOCIATED_PRIVATE_IP_FILTER_NAME,
            computeUnassociatedPrivateIpFilter(ImmutableSet.of())));

    Ip privateIp = Ip.parse("10.10.10.10");
    Prefix privatePrefix = Prefix.create(privateIp, 24);
    Ip publicIp = Ip.parse("1.1.1.1");

    NetworkInterface ni =
        new NetworkInterface(
            "ni",
            "subnet",
            vpc.getId(),
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, privateIp, publicIp)),
            "desc",
            null);

    Subnet subnet =
        new Subnet(
            privatePrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of(TAG_NAME, "subnet-name"));

    Region region =
        Region.builder("region")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setInternetGateways(ImmutableMap.of(igw.getId(), igw))
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
                            new RouteV4(privatePrefix, State.ACTIVE, "local", TargetType.Gateway),
                            new RouteV4(
                                Prefix.parse("0.0.0.0/0"),
                                State.ACTIVE,
                                igw.getId(),
                                TargetType.Gateway)))))
            .build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(
            ImmutableMap.of(
                vpcConfig.getHostname(), vpcConfig, igwConfig.getHostname(), igwConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg.getHumanName(), equalTo("subnet-name"));
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PUBLIC));

    // subnet should have interfaces to the instances, vpc, and igw
    assertThat(
        subnetCfg.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of(subnet.getId(), vpc.getId(), igw.getId())));

    // the igw should have gotten an interface pointed to the subnet
    assertThat(
        igwConfig.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(subnet.getId())));

    // the igw router should have a static route to the private ip
    assertThat(
        igwConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    interfaceNameToRemote(subnetCfg),
                    Utils.getInterfaceLinkLocalIp(subnetCfg, igw.getId())))));

    // igw's subnet facing interface should have the filter to block unassociated private IPs.
    assertThat(
        igwConfig
            .getAllInterfaces()
            .get(interfaceNameToRemote(subnetCfg))
            .getIncomingFilter()
            .getName(),
        equalTo(UNASSOCIATED_PRIVATE_IP_FILTER_NAME));

    // the subnet router should have routes from the table and to the public ip
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    Prefix.ZERO,
                    interfaceNameToRemote(igwConfig),
                    Utils.getInterfaceLinkLocalIp(igwConfig, subnet.getId())),
                toStaticRoute(
                    privatePrefix,
                    interfaceNameToRemote(vpcConfig),
                    Utils.getInterfaceLinkLocalIp(vpcConfig, subnet.getId())))));
  }

  /** Test that we do not create a link to the VPC's IGW if the subnet is private. */
  @Test
  public void testToConfigurationNodePrivateIgw() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    InternetGateway igw =
        new InternetGateway("igw", ImmutableList.of(vpc.getId()), ImmutableMap.of());
    Configuration igwConfig = Utils.newAwsConfiguration(igw.getId(), "awstest");

    Subnet subnet =
        new Subnet(Prefix.parse("1.1.1.1/32"), "subnet", vpc.getId(), "zone", ImmutableMap.of());

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
                                Prefix.parse("1.1.1.1/32"),
                                State.ACTIVE,
                                "local",
                                TargetType.Gateway)))))
            .build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(
            ImmutableMap.of(
                vpcConfig.getHostname(), vpcConfig, igwConfig.getHostname(), igwConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PRIVATE));

    // should have only two interfaces -- one toward instances and one toward the VPC router
    assertThat(
        subnetCfg.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(instancesInterfaceName("subnet"), interfaceNameToRemote(vpcConfig))));

    // the internet gateway should have any interfaces
    assertTrue(igwConfig.getAllInterfaces().isEmpty());
  }

  /** Tests that we do the right thing when processing a route for VPC peering connection. */
  @Test
  public void testProcessRouteVpcPeeringConnection() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");
    String connectionId = "peering";

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    RouteV4 route =
        new RouteV4(remotePrefix, State.ACTIVE, connectionId, TargetType.VpcPeeringConnection);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());

    // there should be a VRF on the VPC node
    String vrfName = vrfNameForLink(connectionId);
    assertThat(vpcCfg, hasVrf(vrfName, any(Vrf.class)));

    // there should be an interface on the Subnet pointed to the VPC node
    Interface subnetIface = Iterables.getOnlyElement(subnetCfg.getAllInterfaces().values());
    assertThat(subnetIface, hasName(Utils.interfaceNameToRemote(vpcCfg, connectionId)));

    // there should be an interface on the VPC node
    Interface vpcIface = Iterables.getOnlyElement(vpcCfg.getAllInterfaces().values());
    assertThat(vpcIface, hasName(Utils.interfaceNameToRemote(subnetCfg, connectionId)));
    assertThat(vpcIface, hasVrfName(vrfNameForLink(connectionId)));

    // right static routes on both sides
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    route.getDestinationCidrBlock(),
                    Utils.interfaceNameToRemote(vpcCfg, connectionId),
                    vpcIface.getLinkLocalAddress().getIp()))));
    assertThat(
        vpcCfg.getVrfs().get(vrfName).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.interfaceNameToRemote(subnetCfg, connectionId),
                    subnetIface.getLinkLocalAddress().getIp()))));
  }

  /** Tests that we do the right thing when processing a route for transit gateway. */
  @Test
  public void testProcessRouteTransitGateway() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());
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

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());

    // there should be a VRF on the VPC node
    String vrfName = vrfNameForLink(linkId);
    assertThat(vpcCfg, hasVrf(vrfName, any(Vrf.class)));

    // there should be an interface on the Subnet pointed to the VPC node
    Interface subnetIface = Iterables.getOnlyElement(subnetCfg.getAllInterfaces().values());
    assertThat(subnetIface, hasName(Utils.interfaceNameToRemote(vpcCfg, linkId)));

    // there should be an interface on the VPC node
    Interface vpcIface = Iterables.getOnlyElement(vpcCfg.getAllInterfaces().values());
    assertThat(vpcIface, hasName(Utils.interfaceNameToRemote(subnetCfg, linkId)));
    assertThat(vpcIface, hasVrfName(vrfNameForLink(linkId)));

    // right static routes on both sides
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    route.getDestinationCidrBlock(),
                    Utils.interfaceNameToRemote(vpcCfg, linkId),
                    vpcIface.getLinkLocalAddress().getIp()))));
    assertThat(
        vpcCfg.getVrfs().get(vrfName).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.interfaceNameToRemote(subnetCfg, linkId),
                    subnetIface.getLinkLocalAddress().getIp()))));
  }

  /**
   * Tests that we handle the case of the gateway not being connected to the subnet's availability
   * zone
   */
  @Test
  public void testProcessRouteTransitGatewayOutsideConnectedAzs() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    Subnet otherSubnet =
        new Subnet(
            Prefix.parse("0.0.0.0/0"), "otherSubnet", vpc.getId(), "otherZone", ImmutableMap.of());

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

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());

    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(toStaticRoute(route.getDestinationCidrBlock(), NULL_INTERFACE_NAME))));
  }

  /** Two subnets using the same VPC link */
  @Test
  public void testInitializeVpcLinkTwoSubnets() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnet1Prefix = Prefix.parse("10.10.10.0/24");
    Prefix subnet2Prefix = Prefix.parse("10.10.20.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");
    String linkId = "link";

    Subnet subnet1 = new Subnet(subnet1Prefix, "subnet1", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnet1Cfg = Utils.newAwsConfiguration(subnet1.getId(), "awstest");

    Subnet subnet2 = new Subnet(subnet2Prefix, "subnet2", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnet2Cfg = Utils.newAwsConfiguration(subnet1.getId(), "awstest");

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    StaticRoute.Builder sr =
        StaticRoute.builder()
            .setNetwork(remotePrefix)
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST);

    subnet1.initializeVpcLink(subnet1Cfg, vpcCfg, vpc, linkId, sr, awsConfiguration);
    subnet2.initializeVpcLink(subnet2Cfg, vpcCfg, vpc, linkId, sr, awsConfiguration);

    // the VPC should have static routes to both subnets in the VRF
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForLink(linkId)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet1.getCidrBlock(),
                    Utils.interfaceNameToRemote(subnet1Cfg, linkId),
                    Utils.getInterfaceLinkLocalIp(
                        subnet1Cfg, Utils.interfaceNameToRemote(vpcCfg, linkId))),
                toStaticRoute(
                    subnet2.getCidrBlock(),
                    Utils.interfaceNameToRemote(subnet2Cfg, linkId),
                    Utils.getInterfaceLinkLocalIp(
                        subnet2Cfg, Utils.interfaceNameToRemote(vpcCfg, linkId))))));
  }

  /** The subnet has two links */
  @Test
  public void testInitializeVpcLinkTwoLinks() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix1 = Prefix.parse("192.168.0.0/16");
    Prefix remotePrefix2 = Prefix.parse("192.169.0.0/16");
    String linkId1 = "peering1";
    String linkId2 = "peering2";

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    StaticRoute.Builder sr1 =
        StaticRoute.builder()
            .setNetwork(remotePrefix1)
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST);
    StaticRoute.Builder sr2 =
        StaticRoute.builder()
            .setNetwork(remotePrefix2)
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST);

    subnet.initializeVpcLink(subnetCfg, vpcCfg, vpc, linkId1, sr1, awsConfiguration);
    subnet.initializeVpcLink(subnetCfg, vpcCfg, vpc, linkId2, sr2, awsConfiguration);

    // there should be two VRFs on the VPC node
    assertThat(vpcCfg, hasVrf(vrfNameForLink(linkId1), any(Vrf.class)));
    assertThat(vpcCfg, hasVrf(vrfNameForLink(linkId2), any(Vrf.class)));

    // there should two interface on the Subnet pointed to the VPC node
    assertThat(
        subnetCfg,
        hasInterface(Utils.interfaceNameToRemote(vpcCfg, linkId1), any(Interface.class)));
    assertThat(
        subnetCfg,
        hasInterface(Utils.interfaceNameToRemote(vpcCfg, linkId2), any(Interface.class)));

    // there should be two interfaces on the VPC node
    assertThat(
        vpcCfg,
        hasInterface(Utils.interfaceNameToRemote(subnetCfg, linkId1), any(Interface.class)));
    assertThat(
        vpcCfg,
        hasInterface(Utils.interfaceNameToRemote(subnetCfg, linkId2), any(Interface.class)));

    // static routes
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    remotePrefix1,
                    Utils.interfaceNameToRemote(vpcCfg, linkId1),
                    Utils.getInterfaceLinkLocalIp(
                        vpcCfg, Utils.interfaceNameToRemote(subnetCfg, linkId1))),
                toStaticRoute(
                    remotePrefix2,
                    Utils.interfaceNameToRemote(vpcCfg, linkId2),
                    Utils.getInterfaceLinkLocalIp(
                        vpcCfg, Utils.interfaceNameToRemote(subnetCfg, linkId2))))));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForLink(linkId1)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.interfaceNameToRemote(subnetCfg, linkId1),
                    Utils.getInterfaceLinkLocalIp(
                        subnetCfg, Utils.interfaceNameToRemote(vpcCfg, linkId1))))));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForLink(linkId2)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.interfaceNameToRemote(subnetCfg, linkId2),
                    Utils.getInterfaceLinkLocalIp(
                        subnetCfg, Utils.interfaceNameToRemote(vpcCfg, linkId2))))));
  }

  /** The subnet has two routes going over the same connection */
  @Test
  public void testInitializeVpcLinkTwoRoutes() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix1 = Prefix.parse("192.168.0.0/16");
    Prefix remotePrefix2 = Prefix.parse("192.169.0.0/16");
    String linkId = "link";

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    StaticRoute.Builder sr1 =
        StaticRoute.builder()
            .setNetwork(remotePrefix1)
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST);
    StaticRoute.Builder sr2 =
        StaticRoute.builder()
            .setNetwork(remotePrefix2)
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet.initializeVpcLink(subnetCfg, vpcCfg, vpc, linkId, sr1, awsConfiguration);
    subnet.initializeVpcLink(subnetCfg, vpcCfg, vpc, linkId, sr2, awsConfiguration);

    // there should be two VRFs on the VPC node
    assertThat(vpcCfg, hasVrf(vrfNameForLink(linkId), any(Vrf.class)));

    // there should two interface on the Subnet pointed to the VPC node
    assertThat(
        subnetCfg, hasInterface(Utils.interfaceNameToRemote(vpcCfg, linkId), any(Interface.class)));

    // there should be two interfaces on the VPC node
    assertThat(
        vpcCfg, hasInterface(Utils.interfaceNameToRemote(subnetCfg, linkId), any(Interface.class)));

    // static routes
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    remotePrefix1,
                    Utils.interfaceNameToRemote(vpcCfg, linkId),
                    Utils.getInterfaceLinkLocalIp(
                        vpcCfg, Utils.interfaceNameToRemote(subnetCfg, linkId))),
                toStaticRoute(
                    remotePrefix2,
                    Utils.interfaceNameToRemote(vpcCfg, linkId),
                    Utils.getInterfaceLinkLocalIp(
                        vpcCfg, Utils.interfaceNameToRemote(subnetCfg, linkId))))));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForLink(linkId)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.interfaceNameToRemote(subnetCfg, linkId),
                    Utils.getInterfaceLinkLocalIp(
                        subnetCfg, Utils.interfaceNameToRemote(vpcCfg, linkId))))));
  }

  /** Test that network ACls are properly attached */
  @Test
  public void testToConfigurationNodeNetworkAcl() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId(), "zone", ImmutableMap.of());

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

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(nodeName(vpc.getId()), vpcCfg));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(subnetCfg, hasDeviceModel(DeviceModel.AWS_SUBNET_PRIVATE));

    Interface instancesInterface =
        subnetCfg.getAllInterfaces().get(instancesInterfaceName(subnet.getId()));

    assertThat(instancesInterface.getIncomingFilterName(), equalTo(getAclName(acl.getId(), true)));
    assertThat(instancesInterface.getOutgoingFilterName(), equalTo(getAclName(acl.getId(), false)));
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
}
