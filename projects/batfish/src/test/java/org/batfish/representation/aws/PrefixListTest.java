package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link VpcPeeringConnection} */
public class PrefixListTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = readResource("org/batfish/representation/aws/PrefixListTest.json", UTF_8);

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
                    "pl-00a54069",
                    ImmutableList.of(Prefix.parse("52.94.28.0/23"), Prefix.parse("52.94.10.0/24")),
                    "com.amazonaws.us-west-2.dynamodb"))));
  }

  @Test
  public void testEquals() {
    Prefix prefix = Prefix.parse("1.1.1.1/32");
    new EqualsTester()
        .addEqualityGroup(
            new PrefixList("id", ImmutableList.of(prefix), "name"),
            new PrefixList("id", ImmutableList.of(prefix), "name"))
        .addEqualityGroup(new PrefixList("other", ImmutableList.of(prefix), "name"))
        .addEqualityGroup(
            new PrefixList("id", ImmutableList.of(Prefix.parse("2.2.2.2/32")), "name"))
        .addEqualityGroup(new PrefixList("id", ImmutableList.of(prefix), "other"))
        .testEquals();
  }
}
