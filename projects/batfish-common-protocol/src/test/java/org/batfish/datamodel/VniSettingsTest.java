package org.batfish.datamodel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VniSettingsTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void checkAllAttrs() {
    SortedSet<Ip> bumTransportIps = ImmutableSortedSet.of(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3"));
    VniSettings vs =
        new VniSettings(
            bumTransportIps,
            BumTransportMethod.UNICAST_FLOOD_GROUP,
            Ip.parse("1.2.3.4"),
            2345,
            7,
            10007);
    assertThat(
        vs.getBumTransportIps(), containsInAnyOrder(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3")));
    assertThat(vs.getBumTransportMethod(), equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP));
    assertThat(vs.getSourceAddress(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(vs.getUdpPort(), equalTo(2345));
    assertThat(vs.getVlan(), equalTo(7));
    assertThat(vs.getVni(), equalTo(10007));
  }

  @Test
  public void checkSerialization() {
    SortedSet<Ip> bumTransportIps = ImmutableSortedSet.of(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3"));
    VniSettings vs =
        new VniSettings(
            bumTransportIps,
            BumTransportMethod.UNICAST_FLOOD_GROUP,
            Ip.parse("1.2.3.4"),
            2345,
            7,
            10007);
    try {
      VniSettings parsedObj = BatfishObjectMapper.clone(vs, VniSettings.class);
      assertThat(
          parsedObj.getBumTransportIps(),
          containsInAnyOrder(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3")));
      assertThat(
          parsedObj.getBumTransportMethod(), equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP));
      assertThat(parsedObj.getSourceAddress(), equalTo(Ip.parse("1.2.3.4")));
      assertThat(parsedObj.getUdpPort(), equalTo(2345));
      assertThat(parsedObj.getVlan(), equalTo(7));
      assertThat(parsedObj.getVni(), equalTo(10007));
    } catch (IOException e) {
      throw new BatfishException("Cannot parse the json to VniSettings object", e);
    }
  }
}
