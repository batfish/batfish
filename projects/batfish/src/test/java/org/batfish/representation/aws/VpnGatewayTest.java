package org.batfish.representation.aws;

import static org.batfish.common.util.IspModelingUtils.getAdvertiseStaticStatement;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPN_GATEWAYS;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.representation.aws.VpnGateway.ACCEPT_ALL_BGP;
import static org.batfish.representation.aws.VpnGateway.VGW_EXPORT_POLICY_NAME;
import static org.batfish.representation.aws.VpnGateway.VGW_IMPORT_POLICY_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.junit.Test;

/** Tests for {@link VpnGateway} */
public class VpnGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/VpnGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode gatewaysArray = (ArrayNode) json.get(JSON_KEY_VPN_GATEWAYS);
    List<VpnGateway> gateways = new LinkedList<>();

    for (int index = 0; index < gatewaysArray.size(); index++) {
      gateways.add(
          BatfishObjectMapper.mapper().convertValue(gatewaysArray.get(index), VpnGateway.class));
    }

    assertThat(
        gateways,
        equalTo(
            ImmutableList.of(new VpnGateway("vgw-81fd279f", ImmutableList.of("vpc-815775e7")))));
  }

  @Test
  public void testToConfigurationNodeNoBgp() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(Prefix.parse("10.0.0.0/16")));
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    VpnGateway vgw = new VpnGateway("vgw", ImmutableList.of(vpc.getId()));

    VpnConnection vpnConnection =
        new VpnConnection(
            false,
            "vpnConnectionId",
            "customerGatewayId",
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

    AwsConfiguration awsConfiguration =
        new AwsConfiguration(
            ImmutableMap.of(region.getName(), region),
            ImmutableMap.of(vpcConfig.getHostname(), vpcConfig));

    Configuration vgwConfig = vgw.toConfigurationNode(awsConfiguration, region, new Warnings());

    assertThat(vgwConfig.getDefaultVrf().getBgpProcess(), nullValue());
  }

  @Test
  public void testToConfigurationNodeBgp() {
    Prefix vpcPrefix = Prefix.parse("10.0.0.0/16");
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(vpcPrefix));
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    VpnGateway vgw = new VpnGateway("vgw", ImmutableList.of(vpc.getId()));

    VpnConnection vpnConnection =
        new VpnConnection(
            true,
            "vpnConnectionId",
            "customerGatewayId",
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

    AwsConfiguration awsConfiguration =
        new AwsConfiguration(
            ImmutableMap.of(region.getName(), region),
            ImmutableMap.of(vpcConfig.getHostname(), vpcConfig));

    Configuration vgwConfig = vgw.toConfigurationNode(awsConfiguration, region, new Warnings());

    // the loopback interface, bgp process, and static route should exist
    assertThat(vgwConfig.getDefaultVrf().getBgpProcess(), notNullValue());
    assertThat(vgwConfig.getAllInterfaces().keySet(), equalTo(ImmutableSet.of("loopbackBgp")));
    assertThat(
        vgwConfig.getDefaultVrf().getStaticRoutes(),
        equalTo(ImmutableSortedSet.of(toStaticRoute(vpcPrefix, NULL_INTERFACE_NAME))));

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
                    .build())));
  }
}
