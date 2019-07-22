package org.batfish.representation.aws;

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
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.Route.TargetType;
import org.batfish.representation.aws.RouteTable.Association;
import org.junit.Test;

/** Tests for {@link RouteTable} */
public class RouteTableTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/RouteTableTest.json");

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
                        new Route(
                            Prefix.parse("10.100.0.0/16"),
                            State.ACTIVE,
                            "local",
                            TargetType.Gateway))))));
  }
}
