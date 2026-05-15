package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.DirectConnectGatewayAssociation.AssociatedGateway;
import org.batfish.representation.aws.DirectConnectGatewayAssociation.AssociatedGateway.GatewayType;
import org.junit.Test;

/** Tests for {@link DirectConnectGatewayAssociation} */
public class DirectConnectGatewayAssociationTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource(
            "org/batfish/representation/aws/DirectConnectGatewayAssociationTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    // Only the "associated" one should be included
    assertThat(
        region.getDirectConnectGatewayAssociations(),
        equalTo(
            ImmutableMap.of(
                "dxgw-assoc-001122aabbccddee0",
                new DirectConnectGatewayAssociation(
                    "dxgw-assoc-001122aabbccddee0",
                    "dxgw-12345678abcdef012",
                    new AssociatedGateway(
                        "tgw-0984f045a02daba60",
                        GatewayType.TRANSIT_GATEWAY,
                        "094218548868",
                        "us-east-1"),
                    ImmutableList.of(Prefix.parse("10.0.0.0/8"), Prefix.parse("172.16.0.0/12"))))));
  }

  @Test
  public void testEquals() {
    AssociatedGateway gw1 =
        new AssociatedGateway("tgw-1", GatewayType.TRANSIT_GATEWAY, "account", "us-east-1");
    AssociatedGateway gw2 =
        new AssociatedGateway("vgw-1", GatewayType.VIRTUAL_PRIVATE_GATEWAY, "account", "us-west-2");

    new EqualsTester()
        .addEqualityGroup(
            new DirectConnectGatewayAssociation(
                "assoc1", "dxgw-1", gw1, ImmutableList.of(Prefix.parse("10.0.0.0/8"))),
            new DirectConnectGatewayAssociation(
                "assoc1", "dxgw-1", gw1, ImmutableList.of(Prefix.parse("10.0.0.0/8"))))
        .addEqualityGroup(
            new DirectConnectGatewayAssociation(
                "assoc2", "dxgw-1", gw1, ImmutableList.of(Prefix.parse("10.0.0.0/8"))))
        .addEqualityGroup(
            new DirectConnectGatewayAssociation(
                "assoc1", "dxgw-2", gw1, ImmutableList.of(Prefix.parse("10.0.0.0/8"))))
        .addEqualityGroup(
            new DirectConnectGatewayAssociation(
                "assoc1", "dxgw-1", gw2, ImmutableList.of(Prefix.parse("10.0.0.0/8"))))
        .addEqualityGroup(
            new DirectConnectGatewayAssociation("assoc1", "dxgw-1", gw1, ImmutableList.of()))
        .testEquals();
  }
}
