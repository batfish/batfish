package org.batfish.representation.aws;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.DeviceModel.AWS_NAT_GATEWAY;
import static org.batfish.representation.aws.AwsLocationInfoUtils.INFRASTRUCTURE_LOCATION_INFO;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NAT_GATEWAYS;
import static org.batfish.representation.aws.NatGateway.ILLEGAL_PACKET_FILTER_NAME;
import static org.batfish.representation.aws.NatGateway.INCOMING_NAT_FILTER_NAME;
import static org.batfish.representation.aws.NatGateway.computePostTransformationIllegalPacketFilter;
import static org.batfish.representation.aws.NatGateway.installIncomingFilter;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.junit.Test;

/** Tests for {@link NatGateway} */
public class NatGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/NatGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NAT_GATEWAYS);
    List<NatGateway> gateways = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      gateways.add(BatfishObjectMapper.mapper().convertValue(array.get(index), NatGateway.class));
    }

    assertThat(
        gateways,
        equalTo(
            ImmutableList.of(
                new NatGateway(
                    "nat-05dba92075d71c408",
                    "subnet-847e4dc2",
                    "vpc-1a2b3c4d",
                    ImmutableList.of(
                        new NatGatewayAddress(
                            "eipalloc-89c620ec",
                            "eni-9dec76cd",
                            Ip.parse("10.0.0.149"),
                            Ip.parse("198.11.222.33"))),
                    ImmutableMap.of("Department", "IT")))));
  }

  @Test
  public void testToConfigurationNode() {
    NatGatewayAddress ngwAddress =
        new NatGatewayAddress(
            "allocationId", "netInterface", Ip.parse("10.10.10.10"), Ip.parse("1.1.1.1"));
    NatGateway ngw =
        new NatGateway("ngw", "subnet", "vpc", ImmutableList.of(ngwAddress), ImmutableMap.of());

    Vpc vpc = new Vpc(ngw.getVpcId(), ImmutableSet.of(), ImmutableMap.of());
    Subnet subnet =
        new Subnet(
            Prefix.parse("10.10.10.0/24"),
            ngw.getSubnetId(),
            ngw.getVpcId(),
            "zone",
            ImmutableMap.of());
    NetworkAcl nacl =
        new NetworkAcl(
            "netAcl",
            vpc.getId(),
            ImmutableList.of(new NetworkAclAssociation(subnet.getId())),
            ImmutableList.of(),
            true);
    NetworkInterface netInterface =
        new NetworkInterface(
            "netInterface",
            subnet.getId(),
            subnet.getVpcId(),
            ImmutableList.of(),
            ImmutableList.of(
                new PrivateIpAddress(true, ngwAddress.getPrivateIp(), ngw.getPublicIp())),
            "desc",
            null);
    Region region =
        Region.builder("r1")
            .setSubnets(ImmutableMap.of(subnet.getId(), subnet))
            .setNetworkInterfaces(ImmutableMap.of(netInterface.getId(), netInterface))
            .setNetworkAcls(ImmutableMap.of(nacl.getId(), nacl))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .build();

    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();
    Configuration vpcCfg = vpc.toConfigurationNode(awsConfiguration, region, new Warnings());
    awsConfiguration.getConfigurationNodes().put(vpcCfg.getHostname(), vpcCfg);

    Configuration ngwConfig = ngw.toConfigurationNode(awsConfiguration, region, new Warnings());

    // test the basics
    assertThat(ngwConfig.getDeviceModel(), equalTo(AWS_NAT_GATEWAY));
    assertThat(ngwConfig.getVendorFamily().getAws().getSubnetId(), equalTo(ngw.getSubnetId()));
    assertThat(ngwConfig.getVendorFamily().getAws().getVpcId(), equalTo(ngw.getVpcId()));
    assertThat(ngwConfig.getVendorFamily().getAws().getRegion(), equalTo(region.getName()));
    assertThat(ngwConfig.getIpAccessLists(), hasKey(ILLEGAL_PACKET_FILTER_NAME));

    // test that ngw was connected to the subnet
    assertThat(
        awsConfiguration.getLayer1Edges(),
        hasItem(
            new Layer1Edge(
                ngw.getId(),
                netInterface.getId(),
                Subnet.nodeName(subnet.getId()),
                Subnet.instancesInterfaceName(subnet.getId()))));

    // is the subnet interface properly configured?
    Interface ifaceToSubnet = ngwConfig.getAllInterfaces().get(netInterface.getId());
    assertThat(
        ifaceToSubnet.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                false, ImmutableList.of(ifaceToSubnet.getName()), null, null)));
    assertThat(
        ifaceToSubnet.getPostTransformationIncomingFilter().getName(),
        equalTo(ILLEGAL_PACKET_FILTER_NAME));

    // test that ngw was connected to the vpc
    assertThat(ngwConfig.getAllInterfaces(), hasKey(vpc.getId()));

    // test that the network ACLs were installed
    assertThat(ngwConfig.getIpAccessLists(), hasKey(nacl.getIngressAcl().getName()));
    assertThat(ngwConfig.getIpAccessLists(), hasKey(nacl.getEgressAcl().getName()));

    // is the interface to vpc properly configured?
    Interface ifaceToVpc = ngwConfig.getAllInterfaces().get(vpc.getId());
    assertThat(ifaceToVpc.getIncomingFilter().getName(), equalTo(INCOMING_NAT_FILTER_NAME));
    assertThat(
        ifaceToVpc.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                false,
                ImmutableList.of(ifaceToVpc.getName()),
                null,
                nacl.getEgressAcl().getName())));

    assertThat(
        ngwConfig.getLocationInfo(),
        equalTo(
            ImmutableMap.of(
                interfaceLocation(ifaceToSubnet),
                INFRASTRUCTURE_LOCATION_INFO,
                interfaceLinkLocation(ifaceToSubnet),
                INFRASTRUCTURE_LOCATION_INFO)));
  }

  @Test
  public void testConnectToVpc() {
    NatGateway ngw = new NatGateway("ngw", "subnet", "vpc", ImmutableList.of(), ImmutableMap.of());
    Configuration ngwConfig = new Configuration("c", ConfigurationFormat.AWS);
    ngwConfig.getVrfs().put(DEFAULT_VRF_NAME, new Vrf(DEFAULT_VRF_NAME));

    Vpc vpc = new Vpc(ngw.getVpcId(), ImmutableSet.of(), ImmutableMap.of());
    Region region = Region.builder("r1").setVpcs(ImmutableMap.of(vpc.getId(), vpc)).build();
    ConvertedConfiguration awsConfiguration = new ConvertedConfiguration();
    Configuration vpcCfg = vpc.toConfigurationNode(awsConfiguration, region, new Warnings());
    awsConfiguration.getConfigurationNodes().put(vpcCfg.getHostname(), vpcCfg);

    ngw.connectToVpc(ngwConfig, awsConfiguration, region, new Warnings());

    // new VRF on VPC
    assertThat(vpcCfg.getVrfs(), hasKey(Vpc.vrfNameForLink(ngw.getId())));

    // connection between NGW and VPC
    assertThat(
        awsConfiguration.getLayer1Edges(),
        hasItem(
            new Layer1Edge(
                ngwConfig.getHostname(),
                Utils.interfaceNameToRemote(vpcCfg),
                vpcCfg.getHostname(),
                Utils.interfaceNameToRemote(ngwConfig))));

    // static route on VPC
    assertThat(
        vpcCfg.getVrfs().get(Vpc.vrfNameForLink(ngw.getId())).getStaticRoutes(),
        hasItem(
            toStaticRoute(
                Prefix.ZERO,
                Utils.interfaceNameToRemote(ngwConfig),
                Utils.getInterfaceLinkLocalIp(ngwConfig, Utils.interfaceNameToRemote(vpcCfg)))));
  }

  @Test
  public void testComputePostTransformationIllegalPacketFilter() {
    Ip privateIp = Ip.parse("10.10.10.10");
    IpAccessList filter =
        computePostTransformationIllegalPacketFilter(privateIp, Prefix.parse("10.10.10.0/24"));

    // is in the NAT's subnet
    assertThat(
        filter
            .filter(
                Flow.builder()
                    .setIngressNode("a")
                    .setIpProtocol(IpProtocol.TCP)
                    .setSrcIp(Ip.parse("10.10.10.11"))
                    .setSrcPort(345)
                    .setDstIp(privateIp)
                    .setDstPort(80)
                    .build(),
                "a",
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));

    // has the same ip has the nat
    assertThat(
        filter
            .filter(
                Flow.builder()
                    .setIngressNode("a")
                    .setIpProtocol(IpProtocol.TCP)
                    .setSrcPort(345)
                    .setDstIp(privateIp)
                    .setDstPort(80)
                    .build(),
                "a",
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));

    // legal packet
    assertThat(
        filter
            .filter(
                Flow.builder()
                    .setIngressNode("a")
                    .setIpProtocol(IpProtocol.TCP)
                    .setSrcPort(345)
                    .setDstIp(Ip.parse("2.2.2.2"))
                    .setDstPort(80)
                    .build(),
                "a",
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.PERMIT));
  }

  @Test
  public void testInstallIncomingFilter() {
    Configuration cfg = new Configuration("cfg", ConfigurationFormat.AWS);
    Interface iface = Interface.builder().setName("test").build();

    Ip blockedIp = Ip.parse("8.8.8.8");
    IpAccessList nacl =
        IpAccessList.builder()
            .setName("test")
            .setLines(
                ExprAclLine.rejecting(
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(IpWildcard.create(blockedIp).toIpSpace())
                            .build())))
            .build();

    installIncomingFilter(cfg, iface, nacl);

    // filter was installed on the configuration node
    assertThat(cfg.getIpAccessLists(), hasKey(INCOMING_NAT_FILTER_NAME));
    IpAccessList filter = cfg.getIpAccessLists().get(INCOMING_NAT_FILTER_NAME);

    // filter was configured on the interface
    assertThat(iface.getIncomingFilter(), equalTo(filter));

    // blocks unsupported protocols
    assertThat(
        filter
            .filter(
                Flow.builder()
                    .setIngressNode("a")
                    .setIpProtocol(IpProtocol.AN)
                    .setDstIp(Ip.parse("1.1.1.1."))
                    .build(),
                "a",
                ImmutableMap.of(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));

    // consistent with nacl: blocks blocked IP
    assertThat(
        filter
            .filter(
                Flow.builder()
                    .setIngressNode("a")
                    .setIpProtocol(IpProtocol.TCP)
                    .setSrcIp(blockedIp)
                    .setSrcPort(345)
                    .setDstIp(Ip.parse("1.1.1.1"))
                    .setDstPort(80)
                    .build(),
                "a",
                cfg.getIpAccessLists(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));

    // consistent with nacl: allows other IPs
    assertThat(
        filter
            .filter(
                Flow.builder()
                    .setIngressNode("a")
                    .setIpProtocol(IpProtocol.TCP)
                    .setSrcIp(Ip.parse("2.2.2.2"))
                    .setSrcPort(345)
                    .setDstIp(Ip.parse("1.1.1.1"))
                    .setDstPort(80)
                    .build(),
                "a",
                cfg.getIpAccessLists(),
                ImmutableMap.of())
            .getAction(),
        equalTo(LineAction.DENY));
  }
}
