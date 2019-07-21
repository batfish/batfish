package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NETWORK_INTERFACES;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Ip;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/** */
public class NetworkInterfaceTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/NetworkInterfaceTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NETWORK_INTERFACES);
    List<NetworkInterface> interfaces = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      interfaces.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), NetworkInterface.class));
    }

    HashMap<Ip, Ip> map1 = new HashMap<>();
    map1.put(Ip.parse("10.100.1.71"), null);

    HashMap<Ip, Ip> map2 = new HashMap<>();
    map2.put(Ip.parse("10.100.1.20"), null);

    MatcherAssert.assertThat(
        interfaces,
        equalTo(
            ImmutableList.of(
                new NetworkInterface(
                    "eni-bd11cc9d",
                    "subnet-9a0c48fc",
                    "vpc-815775e7",
                    ImmutableList.of("sg-adcd87d0"),
                    map1,
                    null,
                    "i-05f467abe21e9b883"),
                new NetworkInterface(
                    "eni-a9d44c8a",
                    "subnet-9a0c48fc",
                    "vpc-815775e7",
                    ImmutableList.of("sg-adcd87d0"),
                    map2,
                    null,
                    null))));
  }
}
