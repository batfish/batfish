package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.junit.Test;

public class VpcEndpointTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/VpcEndpointTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /** should skip unavailable one in the file */
    assertThat(
        region.getVpcEndpoints(),
        equalTo(
            ImmutableMap.of(
                "vpce-0d4dd62ef8577fa8e",
                new VpcEndpointGateway("vpce-0d4dd62ef8577fa8e", "vpc-05575381cd519fdef"),
                "vpce-0dacb0be80d30b16e",
                new VpcEndpointInterface(
                    "vpce-0dacb0be80d30b16e",
                    "vpc-05575381cd519fdef",
                    ImmutableList.of("eni-01e50eacd921e923f", "eni-02c68bab6b43ddfe6"),
                    ImmutableList.of("subnet-0176606bd33f3c98b", "subnet-07ca689a49d471449")))));
  }
}
