package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_ROUTE_TABLES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.Route.TargetType;
import org.batfish.representation.aws.RouteTable.Association;
import org.junit.Test;

/** Tests for {@link RouteTable} */
public class RouteTableTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/RouteTableTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_ROUTE_TABLES);
    List<RouteTable> tables = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      tables.add(BatfishObjectMapper.mapper().convertValue(array.get(index), RouteTable.class));
    }

    assertThat(
        tables,
        equalTo(
            ImmutableList.of(
                new RouteTable(
                    "rtb-296bf350",
                    "vpc-815775e7",
                    ImmutableList.of(new Association(true, null)),
                    ImmutableList.of(
                        new RouteV4(
                            Prefix.parse("10.100.0.0/16"),
                            State.ACTIVE,
                            "local",
                            TargetType.Gateway))))));
  }

  @Test
  public void testDeserializationV6() throws IOException {
    String text = readResource("org/batfish/representation/aws/RouteV6TableTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_ROUTE_TABLES);
    List<RouteTable> tables = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      tables.add(BatfishObjectMapper.mapper().convertValue(array.get(index), RouteTable.class));
    }

    assertThat(
        tables,
        equalTo(
            ImmutableList.of(
                new RouteTable(
                    "rtb-296bf350",
                    "vpc-815775e7",
                    ImmutableList.of(new Association(true, null)),
                    ImmutableList.of(
                        new RouteV6(
                            Prefix6.parse("2600:1f16:751:7800::/56"),
                            State.ACTIVE,
                            "local",
                            TargetType.Gateway))))));
  }
}
