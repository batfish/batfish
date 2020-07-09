package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NETWORK_INTERFACES;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** */
public class NetworkInterfaceTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/NetworkInterfaceTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_NETWORK_INTERFACES);
    List<NetworkInterface> interfaces = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      interfaces.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), NetworkInterface.class));
    }

    assertThat(
        interfaces,
        equalTo(
            ImmutableList.of(
                new NetworkInterface(
                    "eni-bd11cc9d",
                    "subnet-9a0c48fc",
                    "vpc-815775e7",
                    ImmutableList.of("sg-adcd87d0"),
                    ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.100.1.71"), null)),
                    "Primary network interface",
                    "i-05f467abe21e9b883",
                    ImmutableMap.of("Name", "primary", "ExampleTag", "ExampleValue")),
                new NetworkInterface(
                    "eni-a9d44c8a",
                    "subnet-9a0c48fc",
                    "vpc-815775e7",
                    ImmutableList.of("sg-adcd87d0"),
                    ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.100.1.20"), null)),
                    "Primary network interface",
                    null,
                    ImmutableMap.of()))));
  }
}
