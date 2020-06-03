package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.Resources;
import org.batfish.representation.aws.TransitGatewayAttachment.Association;
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;
import org.junit.Test;

/** Tests for {@link TransitGatewayAttachment} */
public class TransitGatewayAttachmentTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        Resources.readResource(
            "org/batfish/representation/aws/TransitGatewayAttachmentTest.json",
            StandardCharsets.UTF_8);

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
                    ResourceType.VPC,
                    "vpc-00a31ce9d0c06675c",
                    new Association("tgw-rtb-0fa40c8df355dce6e", "associated")),
                "tgw-attach-0ce5cf730c95980d9",
                new TransitGatewayAttachment(
                    "tgw-attach-0ce5cf730c95980d9",
                    "tgw-01e19888e3ba041ac",
                    ResourceType.VPC,
                    "vpc-0de868624d5f787db",
                    null))));
  }
}
