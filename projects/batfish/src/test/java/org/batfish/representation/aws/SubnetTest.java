package org.batfish.representation.aws;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SUBNETS;
import static org.batfish.representation.aws.NetworkAcl.getAclName;
import static org.batfish.representation.aws.Subnet.findSubnetNetworkAcl;
import static org.batfish.representation.aws.Subnet.instancesInterfaceName;
import static org.batfish.representation.aws.Utils.connect;
import static org.batfish.representation.aws.Utils.getInterfaceLinkLocalIp;
import static org.batfish.representation.aws.Utils.interfaceNameToRemote;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.Vpc.nodeName;
import static org.batfish.representation.aws.Vpc.vrfNameForLink;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
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
        equalTo(ImmutableSet.of(instancesInterfaceName(subnet.getId()), vpc.getId())));

    // the vpc should have gotten an interface pointed to the subnet
    assertThat(
        vpcConfig.getAllInterfaces().values().stream()
            .map(i -> i.getName())
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
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Subnet subnet =
        new Subnet(Prefix.parse("10.10.10.0/24"), "subnet", vpc.getId(), "zone", ImmutableMap.of());
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
        new ConvertedConfiguration(ImmutableMap.of(vpcConfig.getHostname(), vpcConfig));

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
    Subnet subnet =
        new Subnet(subnetPrefix, "subnet", vpcCfg.getHostname(), "az", ImmutableMap.of());

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(vpcCfg.getHostname(), vpcCfg));

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

  /** Tests that we do the right thing when processing a route for an Internet gateway. */
  @Test
  public void testProcessRouteInternetGateway() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Subnet subnet =
        new Subnet(Prefix.parse("10.10.10.0/24"), "subnet", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    InternetGateway igw =
        new InternetGateway("igw", ImmutableList.of(vpc.getId()), ImmutableMap.of());

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    RouteV4 route =
        new RouteV4(Prefix.parse("192.168.0.0/16"), State.ACTIVE, igw.getId(), TargetType.Gateway);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

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
        subnetCfg, region, route, vpcCfg, igw, null, awsConfiguration, new Warnings());

    testProcessRouteHelper(route, igw.getId(), vpcCfg, subnetCfg);
  }

  /** Tests that we do the right thing when processing a route for an Internet gateway. */
  @Test
  public void testProcessRouteVpnGateway() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Subnet subnet =
        new Subnet(Prefix.parse("10.10.10.0/24"), "subnet", vpc.getId(), "zone", ImmutableMap.of());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    VpnGateway vgw = new VpnGateway("vgw", ImmutableList.of(vpc.getId()), ImmutableMap.of());

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    RouteV4 route =
        new RouteV4(Prefix.parse("192.168.0.0/16"), State.ACTIVE, vgw.getId(), TargetType.Gateway);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    vpcCfg
        .getVrfs()
        .put(
            vrfNameForLink(vgw.getId()),
            Vrf.builder().setOwner(vpcCfg).setName(vrfNameForLink(vgw.getId())).build());
    connect(
        awsConfiguration,
        subnetCfg,
        DEFAULT_VRF_NAME,
        vpcCfg,
        vrfNameForLink(vgw.getId()),
        vrfNameForLink(vgw.getId()));

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, null, vgw, awsConfiguration, new Warnings());

    testProcessRouteHelper(route, vgw.getId(), vpcCfg, subnetCfg);
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
        subnetCfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());

    testProcessRouteHelper(route, connectionId, vpcCfg, subnetCfg);
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
        subnetCfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());

    testProcessRouteHelper(route, linkId, vpcCfg, subnetCfg);
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

    // NACLs are installed on the configuration nodes
    assertThat(subnetCfg.getIpAccessLists(), hasKey(getAclName(acl.getId(), false)));
    assertThat(subnetCfg.getIpAccessLists(), hasKey(getAclName(acl.getId(), true)));

    // NACLs are not installed on the instances-facing interface
    Interface instancesInterface =
        subnetCfg.getAllInterfaces().get(instancesInterfaceName(subnet.getId()));
    assertThat(instancesInterface.getIncomingFilterName(), nullValue());
    assertThat(instancesInterface.getOutgoingFilterName(), nullValue());

    // NACLs are installed on the vpc-facing interface
    Interface ifaceToVpc = subnetCfg.getAllInterfaces().get(interfaceNameToRemote(vpcCfg));
    assertThat(ifaceToVpc.getIncomingFilterName(), equalTo(getAclName(acl.getId(), false)));
    assertThat(ifaceToVpc.getOutgoingFilterName(), equalTo(getAclName(acl.getId(), true)));
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
