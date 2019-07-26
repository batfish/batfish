package org.batfish.representation.aws;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_INTERNET_GATEWAYS;
import static org.batfish.representation.aws.InternetGateway.BACKBONE_EXPORT_POLICY_NAME;
import static org.batfish.representation.aws.InternetGateway.BACKBONE_INTERFACE_NAME;
import static org.batfish.representation.aws.InternetGateway.createBackboneConnection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.junit.Test;

/** Tests for {@link InternetGateway} */
public class InternetGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/InternetGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode gatewaysArray = (ArrayNode) json.get(JSON_KEY_INTERNET_GATEWAYS);
    List<InternetGateway> gateways = new LinkedList<>();

    for (int index = 0; index < gatewaysArray.size(); index++) {
      gateways.add(
          BatfishObjectMapper.mapper()
              .convertValue(gatewaysArray.get(index), InternetGateway.class));
    }

    assertThat(
        gateways,
        equalTo(
            ImmutableList.of(
                new InternetGateway("igw-fac5839d", ImmutableList.of("vpc-925131f4")))));
  }

  /** Test that the right interfaces are created on the Internet gateway */
  @Test
  public void testToConfigurationNodeInterfaces() {

    Vpc vpc = new Vpc("vpc", ImmutableSet.of());
    Configuration vpcConfig = Utils.newAwsConfiguration(vpc.getId(), "awstest");

    InternetGateway internetGateway = new InternetGateway("igw", ImmutableList.of(vpc.getId()));

    Region region =
        Region.builder("region")
            .setInternetGateways(ImmutableMap.of(internetGateway.getId(), internetGateway))
            .setVpcs(ImmutableMap.of(vpc.getId(), vpc))
            .build();

    AwsConfiguration awsConfiguration =
        new AwsConfiguration(
            ImmutableMap.of(region.getName(), region),
            ImmutableMap.of(vpcConfig.getHostname(), vpcConfig));

    Configuration igwConfig = internetGateway.toConfigurationNode(awsConfiguration, region);

    assertThat(
        igwConfig.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(BACKBONE_INTERFACE_NAME, vpc.getId())));

    assertThat(
        vpcConfig.getAllInterfaces().values().stream()
            .map(i -> i.getName())
            .collect(ImmutableList.toImmutableList()),
        equalTo(ImmutableList.of(internetGateway.getId())));

    assertThat(
        igwConfig.getAllInterfaces().get(vpc.getId()).getConcreteAddress().getPrefix(),
        equalTo(
            vpcConfig
                .getAllInterfaces()
                .get(internetGateway.getId())
                .getConcreteAddress()
                .getPrefix()));
  }

  @Test
  public void testCreateBackboneConnection() {
    Configuration cfgNode = Utils.newAwsConfiguration("igw", "awstest");
    Prefix prefix = Prefix.parse("10.10.10.10/24");

    createBackboneConnection(cfgNode, prefix);

    assertTrue(cfgNode.getAllInterfaces().containsKey(BACKBONE_INTERFACE_NAME));
    assertThat(cfgNode.getDefaultVrf().getBgpProcess().getRouterId(), equalTo(prefix.getStartIp()));

    BgpActivePeerConfig nbr =
        getOnlyElement(cfgNode.getDefaultVrf().getBgpProcess().getActiveNeighbors().values());
    assertThat(
        nbr.getAddressFamily(Type.IPV4_UNICAST).getExportPolicy(),
        equalTo(BACKBONE_EXPORT_POLICY_NAME));
  }
}
