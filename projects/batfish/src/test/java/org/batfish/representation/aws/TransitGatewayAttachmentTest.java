package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.representation.aws.TransitGatewayAttachment.Association;
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;
import org.junit.Test;

/** Tests for {@link TransitGatewayAttachment} */
public class TransitGatewayAttachmentTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/aws/TransitGatewayAttachmentTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getTransitGatewayAttachments(),
        equalTo(
            ImmutableMap.of(
                "tgw-attach-0ae2696617bb06fb4",
                new TransitGatewayAttachment(
                    "tgw-attach-0ae2696617bb06fb4",
                    "tgw-044be4464fcc69aff",
                    "554773406868",
                    ResourceType.VPC,
                    "vpc-00a31ce9d0c06675c",
                    "554773406868",
                    new Association("tgw-rtb-0fa40c8df355dce6e", "associated")),
                "tgw-attach-0ce5cf730c95980d9",
                new TransitGatewayAttachment(
                    "tgw-attach-0ce5cf730c95980d9",
                    "tgw-01e19888e3ba041ac",
                    "554773406868",
                    ResourceType.VPC,
                    "vpc-0de868624d5f787db",
                    "554773406868",
                    null),
                "tgw-attach-0dbe0fb191f73f405",
                new TransitGatewayAttachment(
                    "tgw-attach-0dbe0fb191f73f405",
                    "tgw-0984f045a02daba60",
                    "094218548868",
                    ResourceType.DIRECT_CONNECT_GATEWAY,
                    "a0cb33f1-da2c-4b16-94b9-09cf843d672f",
                    "094218548868",
                    new Association("tgw-rtb-0dce4e83c56c46fa9", "associated")),
                "tgw-attach-084b68f4a40b12383",
                new TransitGatewayAttachment(
                    "tgw-attach-084b68f4a40b12383",
                    "tgw-074cc11880f74d29a",
                    "951601349076",
                    ResourceType.PEERING,
                    "tgw-0cacbf5ed9fe034e7",
                    "028403472736",
                    new Association("tgw-rtb-000baae357fa2f403", "associated")))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "attach", "tgw", "my-acc", ResourceType.VPC, "resId", "res-acc", null),
            new TransitGatewayAttachment(
                "attach", "tgw", "my-acc", ResourceType.VPC, "resId", "res-acc", null))
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "other", "tgw", "my-acc", ResourceType.VPC, "resId", "res-acc", null))
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "attach", "other", "my-acc", ResourceType.VPC, "resId", "res-acc", null))
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "attach", "tgw", "other", ResourceType.VPC, "resId", "res-acc", null))
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "attach", "tgw", "my-acc", ResourceType.PEERING, "resId", "res-acc", null))
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "attach", "tgw", "my-acc", ResourceType.VPC, "other", "res-acc", null))
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "attach", "tgw", "my-acc", ResourceType.VPC, "resId", "other", null))
        .addEqualityGroup(
            new TransitGatewayAttachment(
                "attach",
                "tgw",
                "my-acc",
                ResourceType.VPC,
                "resId",
                "res-acc",
                new Association("rt", "state")))
        .testEquals();
  }
}
