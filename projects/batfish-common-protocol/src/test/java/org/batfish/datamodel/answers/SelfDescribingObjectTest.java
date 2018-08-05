package org.batfish.datamodel.answers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class SelfDescribingObjectTest {

  @Test
  public void deserialize() throws IOException {
    ObjectNode json = BatfishObjectMapper.mapper().createObjectNode();
    json.set(
        SelfDescribingObject.PROP_SCHEMA, BatfishObjectMapper.mapper().valueToTree(Schema.INTEGER));
    json.put(SelfDescribingObject.PROP_OBJECT, 42);
    String strObject = json.toString();

    SelfDescribingObject sdObject =
        BatfishObjectMapper.mapper().readValue(strObject, SelfDescribingObject.class);

    assertThat(sdObject.getSchema(), equalTo(Schema.INTEGER));
    assertThat(sdObject.getObject(), equalTo(42));
  }
}
