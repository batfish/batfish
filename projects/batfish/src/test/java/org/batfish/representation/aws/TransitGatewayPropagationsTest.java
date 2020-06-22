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
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;
import org.batfish.representation.aws.TransitGatewayPropagations.Propagation;
import org.junit.Test;

/** Tests for {@link TransitGatewayPropagations} */
public class TransitGatewayPropagationsTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/aws/TransitGatewayPropagationsTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getTransitGatewayPropagations(),
        equalTo(
            ImmutableMap.of(
                "tgw-rtb-08262a9ed03306288",
                new TransitGatewayPropagations(
                    "tgw-rtb-08262a9ed03306288",
                    ImmutableList.of(
                        new Propagation(
                            "tgw-attach-07f5ec59e9f540021",
                            ResourceType.VPC,
                            "vpc-0404e08ceddf7f650",
                            true),
                        new Propagation(
                            "tgw-attach-0ae2696617bb06fb4",
                            ResourceType.VPC,
                            "vpc-00a31ce9d0c06675c",
                            false),
                        new Propagation(
                            "tgw-attach-0dbe0fb191f73f405",
                            ResourceType.DIRECT_CONNECT_GATEWAY,
                            "a0cb33f1-da2c-4b16-94b9-09cf843d672f",
                            true))))));
  }
}
