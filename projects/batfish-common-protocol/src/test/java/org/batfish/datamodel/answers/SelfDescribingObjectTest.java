package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public final class SelfDescribingObjectTest {

  @Test
  public void deserialize() throws IOException {
    ObjectNode json = BatfishObjectMapper.mapper().createObjectNode();
    json.put(SelfDescribingObject.PROP_NAME, "meaningoflife");
    json.set(
        SelfDescribingObject.PROP_SCHEMA, BatfishObjectMapper.mapper().valueToTree(Schema.INTEGER));
    json.put(SelfDescribingObject.PROP_VALUE, 42);
    String strObject = json.toString();

    SelfDescribingObject sdObject =
        BatfishObjectMapper.mapper().readValue(strObject, SelfDescribingObject.class);

    assertThat(sdObject.getName(), equalTo("meaningoflife"));
    assertThat(sdObject.getSchema(), equalTo(Schema.INTEGER));
    assertThat(sdObject.getValue(), equalTo(42));
  }

  @Test
  public void testEquals() {
    SelfDescribingObject group1Elem1 = new SelfDescribingObject("a", Schema.BOOLEAN, false);
    SelfDescribingObject group1Elem2 = new SelfDescribingObject("a", Schema.BOOLEAN, false);

    // different value
    SelfDescribingObject group2 = new SelfDescribingObject("a", Schema.BOOLEAN, true);

    // different schema
    SelfDescribingObject group3 = new SelfDescribingObject("a", Schema.NODE, null);

    // different name
    SelfDescribingObject group4 = new SelfDescribingObject("b", Schema.BOOLEAN, false);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2)
        .addEqualityGroup(group3)
        .addEqualityGroup(group4)
        .testEquals();
  }

  @Test
  public void testGetTypedValue() {
    Ip ip = Ip.parse("1.1.1.1");
    SelfDescribingObject ipObject = new SelfDescribingObject(Schema.IP, ip);
    assertThat(ipObject.getTypedValue(), equalTo(ip));
  }
}
