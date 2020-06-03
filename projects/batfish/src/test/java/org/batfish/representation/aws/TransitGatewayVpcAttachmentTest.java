package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link TransitGatewayVpcAttachment} */
public class TransitGatewayVpcAttachmentTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/aws/TransitGatewayVpcAttachmentTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getTransitGatewayVpcAttachments(),
        equalTo(
            ImmutableMap.of(
                "tgw-attach-07f5ec59e9f540021",
                new TransitGatewayVpcAttachment(
                    "tgw-attach-07f5ec59e9f540021",
                    "tgw-044be4464fcc69aff",
                    "vpc-0404e08ceddf7f650",
                    ImmutableList.of("subnet-006a19c846f047bd7", "subnet-0b5b8ddd5a69fcfcd")))));
  }
}
