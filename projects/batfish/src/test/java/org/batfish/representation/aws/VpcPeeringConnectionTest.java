package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTIONS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Prefix;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/** Tests for {@link VpcPeeringConnection} */
public class VpcPeeringConnectionTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/VpcPeeringConnectionsTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_VPC_PEERING_CONNECTIONS);
    List<VpcPeeringConnection> vpcPeeringConnections = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      vpcPeeringConnections.add(
          BatfishObjectMapper.mapper().convertValue(array.get(index), VpcPeeringConnection.class));
    }

    MatcherAssert.assertThat(
        vpcPeeringConnections,
        equalTo(
            ImmutableList.of(
                new VpcPeeringConnection(
                    "pcx-f754069e",
                    "vpc-f6c5c790",
                    Prefix.parse("10.199.100.0/24"),
                    "vpc-07acbc61",
                    Prefix.parse("10.130.0.0/16")))));
  }

  @Test
  public void testIgnoreDeleted() throws IOException {
    String text =
        CommonUtil.readResource(
            "org/batfish/representation/aws/VpcPeeringConnectionsTestIgnoreDeleted.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * We should have an entry for the vpc peering connection with status code "active", but not
     * for the one with status code "deleted".
     */
    assertThat(region.getVpcPeeringConnections(), hasKey("pcx-f754069e"));
    assertThat(region.getVpcPeeringConnections(), not(hasKey("pcx-4ee8b427")));
  }
}
