package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.ip.Prefix;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.TransitGatewayRoute.Type;
import org.junit.Test;

/** Tests for {@link TransitGatewayStaticRoutes} */
public class TransitGatewayStaticRoutesTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource(
            "org/batfish/representation/aws/TransitGatewayStaticRoutesTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getTransitGatewayStaticRoutes(),
        equalTo(
            ImmutableMap.of(
                "tgw-rtb-08262a9ed03306288",
                new TransitGatewayStaticRoutes(
                    "tgw-rtb-08262a9ed03306288",
                    ImmutableList.of(
                        new TransitGatewayRouteV4(
                            Prefix.parse("1.1.1.1/32"),
                            State.ACTIVE,
                            Type.STATIC,
                            ImmutableList.of("tgw-attach-07f5ec59e9f540021")))),
                "tgw-rtb-0fa40c8df355dce6e",
                new TransitGatewayStaticRoutes("tgw-rtb-0fa40c8df355dce6e", ImmutableList.of()))));
  }
}
