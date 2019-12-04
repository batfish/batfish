package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link VpcPeeringConnection} */
public class PrefixListTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/PrefixListTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /*
     * Only the available gateway should show up.
     */
    assertThat(
        region.getPrefixLists(),
        equalTo(
            ImmutableMap.of(
                "pl-00a54069",
                new PrefixList(
                    ImmutableList.of(Prefix.parse("52.94.28.0/23"), Prefix.parse("52.94.10.0/24")),
                    "pl-00a54069",
                    "com.amazonaws.us-west-2.dynamodb"))));
  }
}
