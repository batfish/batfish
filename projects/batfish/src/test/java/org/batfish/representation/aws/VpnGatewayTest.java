package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPN_GATEWAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.junit.Test;

/** Tests for {@link VpnGateway} */
public class VpnGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/VpnGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode gatewaysArray = (ArrayNode) json.get(JSON_KEY_VPN_GATEWAYS);
    List<VpnGateway> gateways = new LinkedList<>();

    for (int index = 0; index < gatewaysArray.size(); index++) {
      gateways.add(
          BatfishObjectMapper.mapper().convertValue(gatewaysArray.get(index), VpnGateway.class));
    }

    assertThat(
        gateways,
        equalTo(
            ImmutableList.of(new VpnGateway("vgw-81fd279f", ImmutableList.of("vpc-815775e7")))));
  }
}
