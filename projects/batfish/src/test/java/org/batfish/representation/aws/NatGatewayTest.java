package org.batfish.representation.aws;

import static org.batfish.datamodel.DeviceModel.AWS_NAT_GATEWAY;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NAT_GATEWAYS;
import static org.batfish.representation.aws.NatGateway.UNSUPPORTED_PROTOCOL_FILTER_NAME;
import static org.batfish.representation.aws.NatGateway.computeNatTransformation;
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
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
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
    assertThat(ngwConfig.getIpAccessLists(), hasKey(UNSUPPORTED_PROTOCOL_FILTER_NAME));

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
        ifaceToSubnet.getIncomingTransformation(),
        equalTo(computeNatTransformation(ngw.getPrivateIp())));
    assertThat(
        ifaceToSubnet.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                false, ImmutableList.of(ifaceToSubnet.getName()), null, null)));
    assertThat(
        ifaceToSubnet.getPostTransformationIncomingFilter().getName(),
        equalTo(UNSUPPORTED_PROTOCOL_FILTER_NAME));

    // test that ngw was connected to the vpc
    assertThat(ngwConfig.getAllInterfaces(), hasKey(vpc.getId()));

    // test that the network ACLs were installed
    assertThat(ngwConfig.getIpAccessLists(), hasKey(nacl.getIngressAcl().getName()));
    assertThat(ngwConfig.getIpAccessLists(), hasKey(nacl.getEgressAcl().getName()));

    // is the interface to vpc properly configured?
    Interface ifaceToVpc = ngwConfig.getAllInterfaces().get(vpc.getId());
    assertThat(ifaceToVpc.getIncomingFilter().getName(), equalTo(nacl.getIngressAcl().getName()));
    assertThat(
        ifaceToVpc.getPostTransformationIncomingFilter().getName(),
        equalTo(UNSUPPORTED_PROTOCOL_FILTER_NAME));
    assertThat(
        ifaceToVpc.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                false, ImmutableList.of(), null, nacl.getEgressAcl().getName())));
  }
}
