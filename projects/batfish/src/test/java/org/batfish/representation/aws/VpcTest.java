package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPCS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link Vpc} */
public class VpcTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/VpcTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode array = (ArrayNode) json.get(JSON_KEY_VPCS);
    List<Vpc> vpcs = new LinkedList<>();

    for (int index = 0; index < array.size(); index++) {
      vpcs.add(BatfishObjectMapper.mapper().convertValue(array.get(index), Vpc.class));
    }

    assertThat(
        vpcs,
        equalTo(
            ImmutableList.of(
                new Vpc(
                    "vpc-CCCCCC",
                    ImmutableSet.of(
                        Prefix.parse("10.100.0.0/16"), Prefix.parse("10.200.0.0/16"))))));
  }
}
