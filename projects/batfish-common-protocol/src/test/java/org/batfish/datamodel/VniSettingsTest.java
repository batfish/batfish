package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.SortedSet;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VniSettingsTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testAllAttrs() {
    SortedSet<Ip> bumTransportIps = ImmutableSortedSet.of(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3"));
    VniSettings vs =
        VniSettings.builder()
            .setBumTransportIps(bumTransportIps)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(Ip.parse("1.2.3.4"))
            .setUdpPort(2345)
            .setVlan(7)
            .setVni(10007)
            .build();
    assertThat(
        vs.getBumTransportIps(), containsInAnyOrder(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3")));
    assertThat(vs.getBumTransportMethod(), equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP));
    assertThat(vs.getSourceAddress(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(vs.getUdpPort(), equalTo(2345));
    assertThat(vs.getVlan(), equalTo(7));
    assertThat(vs.getVni(), equalTo(10007));
  }

  @Test
  public void testJavaSerialization() throws IOException {
    SortedSet<Ip> bumTransportIps = ImmutableSortedSet.of(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3"));
    VniSettings vs =
        VniSettings.builder()
            .setBumTransportIps(bumTransportIps)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(Ip.parse("1.2.3.4"))
            .setUdpPort(2345)
            .setVlan(7)
            .setVni(10007)
            .build();
    assertThat(SerializationUtils.clone(vs), equalTo(vs));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    SortedSet<Ip> bumTransportIps = ImmutableSortedSet.of(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3"));
    VniSettings vs =
        VniSettings.builder()
            .setBumTransportIps(bumTransportIps)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(Ip.parse("1.2.3.4"))
            .setUdpPort(2345)
            .setVlan(7)
            .setVni(10007)
            .build();
    assertThat(BatfishObjectMapper.clone(vs, VniSettings.class), equalTo(vs));
  }

  @Test
  public void testEquals() {
    VniSettings.Builder builder =
        VniSettings.builder()
            .setVni(1)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP);

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            builder.setBumTransportIps(ImmutableSortedSet.of(Ip.parse("2.2.2.2"))).build())
        .addEqualityGroup(builder.setBumTransportMethod(BumTransportMethod.MULTICAST_GROUP).build())
        .addEqualityGroup(builder.setSourceAddress(Ip.parse("1.1.1.1")).build())
        .addEqualityGroup(builder.setUdpPort(1234).build())
        .addEqualityGroup(builder.setVlan(1000).build())
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    SortedSet<Ip> bumTransportIps = ImmutableSortedSet.of(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3"));
    VniSettings vs =
        VniSettings.builder()
            .setBumTransportIps(bumTransportIps)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(Ip.parse("1.2.3.4"))
            .setUdpPort(2345)
            .setVlan(7)
            .setVni(10007)
            .build();
    assertThat(vs.toBuilder().build(), equalTo(vs));
  }

  @Test
  public void testAddToFloodList() {
    Ip ip = Ip.parse("2.2.2.2");
    VniSettings vs =
        VniSettings.builder()
            .setBumTransportIps(ImmutableSortedSet.of(ip))
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSourceAddress(Ip.parse("1.2.3.4"))
            .setUdpPort(2345)
            .setVlan(7)
            .setVni(10007)
            .build();
    Ip newIp = Ip.parse("9.9.9.9");
    assertThat(vs.addToFloodList(newIp).getBumTransportIps(), containsInAnyOrder(ip, newIp));
  }
}
