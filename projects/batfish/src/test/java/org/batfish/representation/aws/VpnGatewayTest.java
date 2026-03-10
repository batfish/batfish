package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.common.util.isp.IspModelingUtils.getAdvertiseStaticStatement;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceModel;
import static org.batfish.representation.aws.AwsConfiguration.BACKBONE_FACING_INTERFACE_NAME;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.getTestVpc;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.Vpc.vrfNameForLink;
import static org.batfish.representation.aws.VpnConnection.EXPORT_CONNECTED_STATEMENT;
import static org.batfish.representation.aws.VpnConnection.VPN_TO_BACKBONE_EXPORT_POLICY_NAME;
import static org.batfish.representation.aws.VpnGateway.VGW_EXPORT_POLICY_NAME;
import static org.batfish.representation.aws.VpnGateway.VGW_IMPORT_POLICY_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Collections;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.representation.aws.VpnConnection.GatewayType;
import org.junit.Test;

/** Tests for {@link VpnGateway} */
public class VpnGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/VpnGatewayTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getVpnGateways(),
        equalTo(
            ImmutableMap.of(
                "vgw-81fd279f",
                new VpnGateway(
                    "vgw-81fd279f",
                    ImmutableList.of("vpc-815775e7"),
                    ImmutableMap.of(TAG_NAME, "lhr-aws-01"),
                    64666L))));
  }

  @Test
  public void testToConfigurationNodeNoBgp() {
    Vpc vpc = getTestVpc("vpc", ImmutableSet.of(Prefix.parse("10.0.0.0/16")));
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    VpnGateway vgw =
        new VpnGateway(
            "vgw", ImmutableList.of(vpc.getId()), ImmutableMap.of(TAG_NAME, "vgw-name"), 64666L);

    VpnConnection vpnConnection =
        new VpnConnection(
            false,
            "vpnConnectionId",
            "customerGatewayId",
            GatewayType.VPN,
            vgw.getId(),
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(),
            false);

    Region region =
        Region.builder("region")
            .setVpnGateways(ImmutableMap.of(vgw.getId(), vgw))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setVpnConnections(ImmutableMap.of(vpnConnection.getId(), vpnConnection))
            .build();

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableList.of(vpcConfig));

    String vrfNameOnVpc = vrfNameForLink(vgw.getId());
    vpcConfig
        .getVrfs()
        .put(vrfNameOnVpc, Vrf.builder().setName(vrfNameOnVpc).setOwner(vpcConfig).build());

    Configuration vgwConfig = vgw.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(vgwConfig, hasDeviceModel(DeviceModel.AWS_VPN_GATEWAY));
    assertThat(vgwConfig.getHumanName(), equalTo("vgw-name"));
    assertThat(vgwConfig.getDefaultVrf().getBgpProcess(), nullValue());

    // ensure the connection to VPC
    assertThat(
        vgwConfig.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(
                Utils.interfaceNameToRemote(vpcConfig), BACKBONE_FACING_INTERFACE_NAME)));
  }

  @Test
  public void testToConfigurationNodeBgp() {
    Prefix vpcPrefix = Prefix.parse("10.0.0.0/16");
    Vpc vpc = getTestVpc("vpc", ImmutableSet.of(vpcPrefix));
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    VpnGateway vgw =
        new VpnGateway("vgw", ImmutableList.of(vpc.getId()), ImmutableMap.of(), 64666L);

    VpnConnection vpnConnection =
        new VpnConnection(
            true,
            "vpnConnectionId",
            "customerGatewayId",
            GatewayType.VPN,
            vgw.getId(),
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(),
            false);

    Region region =
        Region.builder("region")
            .setVpnGateways(ImmutableMap.of(vgw.getId(), vgw))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .setVpnConnections(ImmutableMap.of(vpnConnection.getId(), vpnConnection))
            .build();

    String vrfNameOnVpc = vrfNameForLink(vgw.getId());
    vpcConfig
        .getVrfs()
        .put(vrfNameOnVpc, Vrf.builder().setName(vrfNameOnVpc).setOwner(vpcConfig).build());

    ConvertedConfiguration awsConfiguration =
        new ConvertedConfiguration(ImmutableList.of(vpcConfig));

    Configuration vgwConfig = vgw.toConfigurationNode(awsConfiguration, region, new Warnings());
    assertThat(vgwConfig, hasDeviceModel(DeviceModel.AWS_VPN_GATEWAY));

    // the loopback interface, bgp process, and static route should exist
    assertThat(vgwConfig.getDefaultVrf().getBgpProcess(), notNullValue());
    assertThat(
        vgwConfig.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(
                "loopbackBgp",
                Utils.interfaceNameToRemote(vpcConfig),
                BACKBONE_FACING_INTERFACE_NAME)));
    assertThat(
        vgwConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSortedSet.of(
                toStaticRoute(
                    vpcPrefix,
                    Utils.interfaceNameToRemote(vpcConfig),
                    Utils.getInterfaceLinkLocalIp(
                        vpcConfig, Utils.interfaceNameToRemote(vgwConfig))))));

    assertThat(
        vgwConfig.getRoutingPolicies(),
        equalTo(
            ImmutableMap.of(
                VGW_EXPORT_POLICY_NAME,
                RoutingPolicy.builder()
                    .setName(VGW_EXPORT_POLICY_NAME)
                    .setStatements(
                        Collections.singletonList(
                            getAdvertiseStaticStatement(
                                new PrefixSpace(PrefixRange.fromPrefix(vpcPrefix)))))
                    .build(),
                VGW_IMPORT_POLICY_NAME,
                RoutingPolicy.builder()
                    .setName(VGW_IMPORT_POLICY_NAME)
                    .setStatements(Collections.singletonList(ACCEPT_ALL_BGP))
                    .build(),
                VPN_TO_BACKBONE_EXPORT_POLICY_NAME,
                RoutingPolicy.builder()
                    .setName(VPN_TO_BACKBONE_EXPORT_POLICY_NAME)
                    .setStatements(Collections.singletonList(EXPORT_CONNECTED_STATEMENT))
                    .build())));
  }
}
