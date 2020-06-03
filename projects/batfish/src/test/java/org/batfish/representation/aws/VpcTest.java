package org.batfish.representation.aws;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPCS;
import static org.batfish.representation.aws.Vpc.vrfNameForLink;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.Resources;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link Vpc} */
public class VpcTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        Resources.readResource(
            "org/batfish/representation/aws/VpcTest.json", StandardCharsets.UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_VPCS);
    List<Vpc> vpcs = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      vpcs.add(BatfishObjectMapper.mapper().convertValue(array.get(index), Vpc.class));
    }

    assertThat(
        vpcs,
        equalTo(
            ImmutableList.of(
                new Vpc(
                    "vpc-CCCCCC",
                    ImmutableSet.of(Prefix.parse("10.100.0.0/16"), Prefix.parse("10.200.0.0/16")),
                    ImmutableMap.of()))));
  }

  /** The default VRF is properly set up */
  @Test
  public void testToConfigurationNode_defaultVrf() {
    Set<Prefix> prefixes = ImmutableSet.of(Prefix.parse("1.1.1.1/32"), Prefix.parse("2.2.2.2/32"));
    Vpc vpc = new Vpc("vpc", prefixes, ImmutableMap.of());

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), new Region("r"), new Warnings());

    assertThat(
        vpcCfg.getDefaultVrf().getStaticRoutes(),
        equalTo(
            prefixes.stream()
                .map(Vpc::staticRouteToVpcPrefix)
                .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()))));
  }

  /** VRFs are in place for the right IGWs */
  @Test
  public void testToConfigurationNod_igwVrfs() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());

    String gatewayId = "gateway";
    Region region =
        Region.builder("r1")
            .setInternetGateways(
                ImmutableMap.of(
                    gatewayId,
                    new InternetGateway(
                        gatewayId, ImmutableList.of(vpc.getId()), ImmutableMap.of()),
                    "other",
                    new InternetGateway("other", ImmutableList.of("otherVpc"), ImmutableMap.of())))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    assertThat(
        vpcCfg.getVrfs().keySet(),
        equalTo(ImmutableSet.of(DEFAULT_VRF_NAME, vrfNameForLink(gatewayId))));
  }

  /** VRFs are in place for the right VGWs */
  @Test
  public void testToConfigurationNod_vgwVrfs() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());

    String gatewayId = "gateway";
    Region region =
        Region.builder("r1")
            .setVpnGateways(
                ImmutableMap.of(
                    gatewayId,
                    new VpnGateway(gatewayId, ImmutableList.of(vpc.getId()), ImmutableMap.of()),
                    "other",
                    new VpnGateway("other", ImmutableList.of("otherVpc"), ImmutableMap.of())))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    assertThat(
        vpcCfg.getVrfs().keySet(),
        equalTo(ImmutableSet.of(DEFAULT_VRF_NAME, vrfNameForLink(gatewayId))));
  }

  /** VRFs are in place for VPC Endpoint gateways */
  @Test
  public void testToConfigurationNod_vpceGateways() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());

    String gatewayId = "gateway";
    Region region =
        Region.builder("r1")
            .setVpcEndpoints(
                ImmutableMap.of(
                    gatewayId,
                    new VpcEndpointGateway(gatewayId, "service", vpc.getId(), ImmutableMap.of()),
                    "other",
                    new VpcEndpointGateway("other", "service", "otherVpc", ImmutableMap.of())))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    assertThat(
        vpcCfg.getVrfs().keySet(),
        equalTo(ImmutableSet.of(DEFAULT_VRF_NAME, vrfNameForLink(gatewayId))));
  }

  /** VRFs are in place for the right NAT gateways */
  @Test
  public void testToConfigurationNod_ngwVrfs() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());

    String gatewayId = "gateway";
    Region region =
        Region.builder("r1")
            .setNatGateways(
                ImmutableMap.of(
                    gatewayId,
                    new NatGateway(
                        gatewayId, "subnet", vpc.getId(), ImmutableList.of(), ImmutableMap.of()),
                    "other",
                    new NatGateway(
                        "other", "subnet", "otherVpc", ImmutableList.of(), ImmutableMap.of())))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    assertThat(
        vpcCfg.getVrfs().keySet(),
        equalTo(ImmutableSet.of(DEFAULT_VRF_NAME, vrfNameForLink(gatewayId))));
  }

  /** VRFs are in place for the right TGW attachments */
  @Test
  public void testToConfigurationNod_tgwAttachmentVrfs() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());

    String attachment = "attachment";
    Region region =
        Region.builder("r1")
            .setTransitGatewayVpcAttachments(
                ImmutableMap.of(
                    attachment,
                    new TransitGatewayVpcAttachment(
                        attachment, "tgw", vpc.getId(), ImmutableList.of()),
                    "other",
                    new TransitGatewayVpcAttachment(
                        "other",
                        "tgw", // same TGW different VPC
                        "otherVpc",
                        ImmutableList.of())))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    assertThat(
        vpcCfg.getVrfs().keySet(),
        equalTo(ImmutableSet.of(DEFAULT_VRF_NAME, vrfNameForLink(attachment))));
  }

  /** VRFs are in place for the right VPC peering connections */
  @Test
  public void testToConfigurationNod_vpcPeeringVrfs() {
    Vpc vpc = new Vpc("vpc", ImmutableSet.of(), ImmutableMap.of());

    String connectionRequester = "requester";
    String connectionAccepter = "accepter";
    Region region =
        Region.builder("r1")
            .setVpcPeerings(
                ImmutableMap.of(
                    connectionAccepter,
                    new VpcPeeringConnection(
                        connectionAccepter,
                        vpc.getId(),
                        ImmutableList.of(),
                        "requester",
                        ImmutableList.of()),
                    connectionRequester,
                    new VpcPeeringConnection(
                        connectionRequester,
                        "accepter",
                        ImmutableList.of(),
                        vpc.getId(),
                        ImmutableList.of()),
                    "other",
                    new VpcPeeringConnection(
                        "other", "accepter", ImmutableList.of(), "requested", ImmutableList.of())))
            .build();

    Configuration vpcCfg =
        vpc.toConfigurationNode(new ConvertedConfiguration(), region, new Warnings());

    assertThat(
        vpcCfg.getVrfs().keySet(),
        equalTo(
            ImmutableSet.of(
                DEFAULT_VRF_NAME,
                vrfNameForLink(connectionAccepter),
                vrfNameForLink(connectionRequester))));
  }
}
