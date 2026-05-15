package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link DirectConnectGateway} */
public class DirectConnectGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/aws/DirectConnectGatewayTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    // Only "available" gateways should be included
    assertThat(
        region.getDirectConnectGateways(),
        equalTo(
            ImmutableMap.of(
                "dxgw-12345678abcdef012",
                new DirectConnectGateway(
                    "dxgw-12345678abcdef012",
                    "my-dx-gateway",
                    64512L,
                    ImmutableMap.of("Name", "prod-dxgw")))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new DirectConnectGateway("id", "name", 64512L, ImmutableMap.of()),
            new DirectConnectGateway("id", "name", 64512L, ImmutableMap.of()))
        .addEqualityGroup(new DirectConnectGateway("other", "name", 64512L, ImmutableMap.of()))
        .addEqualityGroup(new DirectConnectGateway("id", "other", 64512L, ImmutableMap.of()))
        .addEqualityGroup(new DirectConnectGateway("id", "name", 99999L, ImmutableMap.of()))
        .addEqualityGroup(new DirectConnectGateway("id", "name", 64512L, ImmutableMap.of("k", "v")))
        .testEquals();
  }
}
