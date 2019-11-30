package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.TransitGateway.TransitGatewayOptions;
import org.junit.Test;

/** Tests for {@link TransitGateway} */
public class TransitGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/TransitGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getTransitGateways(),
        equalTo(
            ImmutableMap.of(
                "tgw-044be4464fcc69aff",
                new TransitGateway(
                    "tgw-044be4464fcc69aff",
                    new TransitGatewayOptions(
                        64512L,
                        true,
                        "tgw-rtb-0fa40c8df355dce6e",
                        true,
                        "tgw-rtb-0fa40c8df355dce6e",
                        true)))));
  }
}
