package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public final class SelfDescribingObjectTest {

  @Test
  public void deserialize() throws IOException {
    ObjectNode json = BatfishObjectMapper.mapper().createObjectNode();
    json.set(
        SelfDescribingObject.PROP_SCHEMA, BatfishObjectMapper.mapper().valueToTree(Schema.INTEGER));
    json.put(SelfDescribingObject.PROP_VALUE, 42);
    String strObject = json.toString();

    SelfDescribingObject sdObject =
        BatfishObjectMapper.mapper().readValue(strObject, SelfDescribingObject.class);

    assertThat(sdObject.getSchema(), equalTo(Schema.INTEGER));
    assertThat(sdObject.getValue(), equalTo(42));
  }

  @Test
  public void testEquals() {
    SelfDescribingObject group1Elem1 = new SelfDescribingObject(Schema.BOOLEAN, false);
    SelfDescribingObject group1Elem2 = new SelfDescribingObject(Schema.BOOLEAN, false);
    SelfDescribingObject group2Elem1 = new SelfDescribingObject(Schema.BOOLEAN, null);
    SelfDescribingObject group3Elem1 = new SelfDescribingObject(Schema.NODE, null);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .testEquals();
  }
}
