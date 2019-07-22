package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_CUSTOMER_GATEWAYS;
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

/** Test for {@link org.batfish.representation.aws.CustomerGateway} */
public class CustomerGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/CustomerGatewayTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode gatewaysArray = (ArrayNode) json.get(JSON_KEY_CUSTOMER_GATEWAYS);
    List<CustomerGateway> gateways = new LinkedList<>();

    for (int index = 0; index < gatewaysArray.size(); index++) {
      gateways.add(
          BatfishObjectMapper.mapper()
              .convertValue(gatewaysArray.get(index), CustomerGateway.class));
    }

    assertThat(
        gateways,
        equalTo(
            ImmutableList.of(
                new CustomerGateway(
                    "cgw-fb76ace5", Ip.parse("147.75.69.27"), "ipsec.1", "65301"))));
  }
}
