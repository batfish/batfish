package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test for {@link org.batfish.representation.aws.CustomerGateway} */
public class CustomerGatewayTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/CustomerGatewayTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getCustomerGateways(),
        equalTo(
            ImmutableMap.of(
                "cgw-fb76ace5",
                new CustomerGateway(
                    "cgw-fb76ace5", Ip.parse("147.75.69.27"), "ipsec.1", "65301"))));
  }
}
