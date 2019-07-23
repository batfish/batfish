package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NAT_GATEWAYS;
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
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link NatGateway} */
public class NatGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/NatGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NAT_GATEWAYS);
    List<NatGateway> gateways = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      gateways.add(BatfishObjectMapper.mapper().convertValue(array.get(index), NatGateway.class));
    }

    assertThat(
        gateways,
        equalTo(
            ImmutableList.of(
                new NatGateway(
                    "nat-05dba92075d71c408",
                    "subnet-847e4dc2",
                    "vpc-1a2b3c4d",
                    ImmutableList.of(
                        new NatGatewayAddress(
                            "eipalloc-89c620ec",
                            "eni-9dec76cd",
                            Ip.parse("10.0.0.149"),
                            Ip.parse("198.11.222.33")))))));
  }
}
