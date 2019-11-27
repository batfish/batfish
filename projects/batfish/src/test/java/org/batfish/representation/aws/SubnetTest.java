package org.batfish.representation.aws;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrfName;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SUBNETS;
import static org.batfish.representation.aws.Subnet.findMyNetworkAcl;
import static org.batfish.representation.aws.Utils.suffixedInterfaceName;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.Vpc.vrfNameForPeeeringConnection;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
        equalTo(ImmutableList.of(new Subnet(Prefix.parse("172.31.0.0/20"), "subnet-1", "vpc-1"))));
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
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
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

    Subnet subnet = new Subnet(privatePrefix, "subnet", vpc.getId());

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
                            new Route(privatePrefix, State.ACTIVE, "local", TargetType.Gateway)))))
            .build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableMap.of(vpcConfig.getHostname(), vpcConfig));

    Configuration subnetCfg = subnet.toConfigurationNode(awsConfiguration, region, new Warnings());

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
                    subnetCfg.getAllInterfaces().get(vpc.getId()).getConcreteAddress().getIp()))));

    // the subnet router should have routes from the table
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(privatePrefix, Utils.getInterfaceIp(vpcConfig, subnet.getId())))));
  }

  @Test
  public void testToConfigurationNodePrivateVgw() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    VpnGateway vgw = new VpnGateway("igw", ImmutableList.of(vpc.getId()));
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

    Subnet subnet = new Subnet(privatePrefix, "subnet", vpc.getId());

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
                            new Route(privatePrefix, State.ACTIVE, "local", TargetType.Gateway),
                            new Route(
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
                toStaticRoute(privatePrefix, Utils.getInterfaceIp(subnetCfg, vgw.getId())))));
  }

  @Test
  public void testToConfigurationNodePublic() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    InternetGateway igw = new InternetGateway("igw", ImmutableList.of(vpc.getId()));
    Configuration igwConfig = Utils.newAwsConfiguration(igw.getId(), "awstest");

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

    Subnet subnet = new Subnet(privatePrefix, "subnet", vpc.getId());

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
                            new Route(privatePrefix, State.ACTIVE, "local", TargetType.Gateway),
                            new Route(
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

    // the igw router should have a static route to the public ip
    assertThat(
        igwConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(publicIp, Utils.getInterfaceIp(subnetCfg, igw.getId())))));

    // the subnet router should have routes from the table and to the public ip
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(Prefix.ZERO, Utils.getInterfaceIp(igwConfig, subnet.getId())),
                toStaticRoute(privatePrefix, Utils.getInterfaceIp(vpcConfig, subnet.getId())),
                toStaticRoute(publicIp, subnetCfg.getAllInterfaces().get(subnet.getId())))));
  }

  /**
   * Tests that we do the right thing when processing a route for VPC peering connection. Other
   * route types are tested as part of tests for {@link Subnet#toConfigurationNode}
   */
  @Test
  public void testProcessRouteVpcPeeringConnection() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");
    String connectionId = "peering";

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    Route route =
        new Route(remotePrefix, State.ACTIVE, connectionId, TargetType.VpcPeeringConnection);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet.processRoute(
        subnetCfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());

    // there should be a VRF on the VPC node
    String vrfName = vrfNameForPeeeringConnection(connectionId);
    assertThat(vpcCfg, hasVrf(vrfName, any(Vrf.class)));

    // there should be an interface on the Subnet pointed to the VPC node
    Interface subnetIface = Iterables.getOnlyElement(subnetCfg.getAllInterfaces().values());
    assertThat(subnetIface, hasName(suffixedInterfaceName(vpcCfg, connectionId)));

    // there should be an interface on the VPC node
    Interface vpcIface = Iterables.getOnlyElement(vpcCfg.getAllInterfaces().values());
    assertThat(vpcIface, hasName(suffixedInterfaceName(subnetCfg, connectionId)));
    assertThat(vpcIface, hasVrfName(vrfNameForPeeeringConnection(connectionId)));

    // right static routes on both sides
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    route.getDestinationCidrBlock(), vpcIface.getConcreteAddress().getIp()))));
    assertThat(
        vpcCfg.getVrfs().get(vrfName).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(subnet.getCidrBlock(), subnetIface.getConcreteAddress().getIp()))));
  }

  /** Two subnets using the VPC connection */
  @Test
  public void testProcessRouteVpcPeeringConnectionTwoSubnets() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnet1Prefix = Prefix.parse("10.10.10.0/24");
    Prefix subnet2Prefix = Prefix.parse("10.10.20.0/24");
    Prefix remotePrefix = Prefix.parse("192.168.0.0/16");
    String connectionId = "peering";

    Subnet subnet1 = new Subnet(subnet1Prefix, "subnet1", vpc.getId());
    Configuration subnet1Cfg = Utils.newAwsConfiguration(subnet1.getId(), "awstest");

    Subnet subnet2 = new Subnet(subnet2Prefix, "subnet2", vpc.getId());
    Configuration subnet2Cfg = Utils.newAwsConfiguration(subnet1.getId(), "awstest");

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    Route route =
        new Route(remotePrefix, State.ACTIVE, connectionId, TargetType.VpcPeeringConnection);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet1.processRoute(
        subnet1Cfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());
    subnet2.processRoute(
        subnet1Cfg, region, route, vpcCfg, null, null, awsConfiguration, new Warnings());

    // the VPC should have static routes to both subnets in the VRF
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForPeeeringConnection(connectionId)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet1.getCidrBlock(),
                    Utils.getInterfaceIp(subnet1Cfg, suffixedInterfaceName(vpcCfg, connectionId))),
                toStaticRoute(
                    subnet1.getCidrBlock(),
                    Utils.getInterfaceIp(
                        subnet1Cfg, suffixedInterfaceName(vpcCfg, connectionId))))));
  }

  /** The subnet has two peering connections */
  @Test
  public void testProcessRouteVpcPeeringConnectionTwoConnections() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix1 = Prefix.parse("192.168.0.0/16");
    Prefix remotePrefix2 = Prefix.parse("192.169.0.0/16");
    String connectionId1 = "peering1";
    String connectionId2 = "peering2";

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    Route route1 =
        new Route(remotePrefix1, State.ACTIVE, connectionId1, TargetType.VpcPeeringConnection);
    Route route2 =
        new Route(remotePrefix2, State.ACTIVE, connectionId2, TargetType.VpcPeeringConnection);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet.processRoute(
        subnetCfg, region, route1, vpcCfg, null, null, awsConfiguration, new Warnings());

    subnet.processRoute(
        subnetCfg, region, route2, vpcCfg, null, null, awsConfiguration, new Warnings());

    // there should be two VRFs on the VPC node
    assertThat(vpcCfg, hasVrf(vrfNameForPeeeringConnection(connectionId1), any(Vrf.class)));
    assertThat(vpcCfg, hasVrf(vrfNameForPeeeringConnection(connectionId2), any(Vrf.class)));

    // there should two interface on the Subnet pointed to the VPC node
    assertThat(
        subnetCfg,
        hasInterface(suffixedInterfaceName(vpcCfg, connectionId1), any(Interface.class)));
    assertThat(
        subnetCfg,
        hasInterface(suffixedInterfaceName(vpcCfg, connectionId2), any(Interface.class)));

    // there should be two interfaces on the VPC node
    assertThat(
        vpcCfg,
        hasInterface(suffixedInterfaceName(subnetCfg, connectionId1), any(Interface.class)));
    assertThat(
        vpcCfg,
        hasInterface(suffixedInterfaceName(subnetCfg, connectionId2), any(Interface.class)));

    // static routes
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    route1.getDestinationCidrBlock(),
                    Utils.getInterfaceIp(vpcCfg, suffixedInterfaceName(subnetCfg, connectionId1))),
                toStaticRoute(
                    route2.getDestinationCidrBlock(),
                    Utils.getInterfaceIp(
                        vpcCfg, suffixedInterfaceName(subnetCfg, connectionId2))))));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForPeeeringConnection(connectionId1)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.getInterfaceIp(
                        subnetCfg, suffixedInterfaceName(vpcCfg, connectionId1))))));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForPeeeringConnection(connectionId2)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.getInterfaceIp(
                        subnetCfg, suffixedInterfaceName(vpcCfg, connectionId2))))));
  }

  /** The subnet has two routes going over the same connection */
  @Test
  public void testProcessRouteVpcPeeringConnectionTwoRoutes() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcCfg = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    Prefix subnetPrefix = Prefix.parse("10.10.10.0/24");
    Prefix remotePrefix1 = Prefix.parse("192.168.0.0/16");
    Prefix remotePrefix2 = Prefix.parse("192.169.0.0/16");
    String connectionId = "peering1";

    Subnet subnet = new Subnet(subnetPrefix, "subnet", vpc.getId());
    Configuration subnetCfg = Utils.newAwsConfiguration(subnet.getId(), "awstest");

    Region region = Region.builder("region").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();

    Route route1 =
        new Route(remotePrefix1, State.ACTIVE, connectionId, TargetType.VpcPeeringConnection);
    Route route2 =
        new Route(remotePrefix2, State.ACTIVE, connectionId, TargetType.VpcPeeringConnection);

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration(ImmutableMap.of());

    subnet.processRoute(
        subnetCfg, region, route1, vpcCfg, null, null, awsConfiguration, new Warnings());

    subnet.processRoute(
        subnetCfg, region, route2, vpcCfg, null, null, awsConfiguration, new Warnings());

    // there should be two VRFs on the VPC node
    assertThat(vpcCfg, hasVrf(vrfNameForPeeeringConnection(connectionId), any(Vrf.class)));

    // there should two interface on the Subnet pointed to the VPC node
    assertThat(
        subnetCfg, hasInterface(suffixedInterfaceName(vpcCfg, connectionId), any(Interface.class)));

    // there should be two interfaces on the VPC node
    assertThat(
        vpcCfg, hasInterface(suffixedInterfaceName(subnetCfg, connectionId), any(Interface.class)));

    // static routes
    assertThat(
        subnetCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    route1.getDestinationCidrBlock(),
                    Utils.getInterfaceIp(vpcCfg, suffixedInterfaceName(subnetCfg, connectionId))),
                toStaticRoute(
                    route2.getDestinationCidrBlock(),
                    Utils.getInterfaceIp(
                        vpcCfg, suffixedInterfaceName(subnetCfg, connectionId))))));
    assertThat(
        vpcCfg.getVrfs().get(vrfNameForPeeeringConnection(connectionId)).getStaticRoutes(),
        equalTo(
            ImmutableSet.of(
                toStaticRoute(
                    subnet.getCidrBlock(),
                    Utils.getInterfaceIp(
                        subnetCfg, suffixedInterfaceName(vpcCfg, connectionId))))));
  }

  @Test
  public void testFindMyNetworkAclNoMatchingVpc() {
    Map<String, NetworkAcl> networkAcls =
        ImmutableMap.of(
            "aclId",
            new NetworkAcl("aclId", "novpc", ImmutableList.of(), ImmutableList.of(), true));

    assertThat(findMyNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of()));
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
        findMyNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of(networkAcl)));
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
        findMyNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of(networkAcl)));
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
        findMyNetworkAcl(networkAcls, "vpc", "subnet"), equalTo(ImmutableList.of(networkAcl)));
  }
}
