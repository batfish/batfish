package org.batfish.datamodel.route.nh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.NextHopComparator;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link NextHopVrf}. */
public final class NextHopVrfTest {

  @Test
  public void testJavaSerialiation() {
    NextHopVrf withoutIp = NextHopVrf.of("foo");
    assertThat(SerializationUtils.clone(withoutIp), equalTo(withoutIp));
    NextHopVrf withIp = NextHopVrf.of("foo", Ip.parse("1.2.3.4"));
    assertThat(SerializationUtils.clone(withIp), equalTo(withIp));
  }

  @Test
  public void testJacksonSerialization() {
    NextHopVrf withoutIp = NextHopVrf.of("foo");
    assertThat(BatfishObjectMapper.clone(withoutIp, NextHop.class), equalTo(withoutIp));
    NextHopVrf withIp = NextHopVrf.of("foo", Ip.parse("1.2.3.4"));
    assertThat(BatfishObjectMapper.clone(withIp, NextHop.class), equalTo(withIp));
  }

  @Test
  public void testEquals() {
    Ip ip1 = Ip.parse("1.2.3.4");
    Ip ip2 = Ip.parse("5.6.7.8");
    new EqualsTester()
        .addEqualityGroup(NextHopVrf.of("foo"), NextHopVrf.of("foo", null))
        .addEqualityGroup(NextHopVrf.of("bar"))
        .addEqualityGroup(NextHopVrf.of("foo", ip1))
        .addEqualityGroup(NextHopVrf.of("foo", ip2))
        .addEqualityGroup(NextHopVrf.of("bar", ip1))
        .testEquals();
  }

  @Test
  public void testGetters() {
    Ip ip = Ip.parse("1.2.3.4");
    NextHopVrf withIp = NextHopVrf.of("foo", ip);
    assertThat(withIp.getIp(), equalTo(ip));
    assertThat(withIp.getVrfName(), equalTo("foo"));

    NextHopVrf withoutIp = NextHopVrf.of("foo");
    assertThat(withoutIp.getIp(), nullValue());
    assertThat(withoutIp.getVrfName(), equalTo("foo"));
  }

  @Test
  public void testInvalidIp() {
    assertThrows(IllegalArgumentException.class, () -> NextHopVrf.of("foo", Ip.AUTO));
    assertThrows(IllegalArgumentException.class, () -> NextHopVrf.of("foo", Ip.ZERO));
    assertThrows(IllegalArgumentException.class, () -> NextHopVrf.of("foo", Ip.MAX));
  }

  @Test
  public void testCaching() {
    NextHopVrf withoutIp1 = NextHopVrf.of("foo");
    NextHopVrf withoutIp2 = NextHopVrf.of("foo");
    assertThat(withoutIp1, sameInstance(withoutIp2));

    Ip ip = Ip.parse("1.2.3.4");
    NextHopVrf withIp1 = NextHopVrf.of("foo", ip);
    NextHopVrf withIp2 = NextHopVrf.of("foo", ip);
    assertThat(withIp1, sameInstance(withIp2));

    NextHopVrf deserialized = SerializationUtils.clone(withIp1);
    assertThat(deserialized, sameInstance(withIp1));
  }

  @Test
  public void testComparator() {
    Ip ip1 = Ip.parse("1.2.3.4");
    Ip ip2 = Ip.parse("5.6.7.8");
    NextHopComparator comparator = NextHopComparator.instance();

    // VRF name takes precedence
    assertThat(
        comparator.compare(NextHopVrf.of("aaa", ip1), NextHopVrf.of("bbb", ip1)), lessThan(0));
    assertThat(
        comparator.compare(NextHopVrf.of("bbb", ip1), NextHopVrf.of("aaa", ip1)), greaterThan(0));

    // IP compared second (null < non-null)
    assertThat(comparator.compare(NextHopVrf.of("foo"), NextHopVrf.of("foo", ip1)), lessThan(0));
    assertThat(comparator.compare(NextHopVrf.of("foo", ip1), NextHopVrf.of("foo")), greaterThan(0));
    assertThat(
        comparator.compare(NextHopVrf.of("foo", ip1), NextHopVrf.of("foo", ip2)), lessThan(0));
  }
}
