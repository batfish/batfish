package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;

import com.google.common.testing.EqualsTester;
import java.util.Comparator;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link FibNextVrf}. */
public final class FibNextVrfTest {

  @Test
  public void testEquals() {
    Ip ip1 = Ip.parse("1.2.3.4");
    Ip ip2 = Ip.parse("5.6.7.8");
    new EqualsTester()
        .addEqualityGroup(FibNextVrf.of("foo", ip1))
        .addEqualityGroup(FibNextVrf.of("foo", ip2))
        .addEqualityGroup(FibNextVrf.of("bar", ip1))
        .addEqualityGroup(FibNextVrf.of("foo"), FibNextVrf.of("foo", null))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    FibNextVrf withIp = FibNextVrf.of("foo", Ip.parse("1.2.3.4"));
    assertThat(SerializationUtils.clone(withIp), equalTo(withIp));
    FibNextVrf withoutIp = FibNextVrf.of("foo");
    assertThat(SerializationUtils.clone(withoutIp), equalTo(withoutIp));
  }

  @Test
  public void testGetters() {
    Ip ip = Ip.parse("1.2.3.4");
    FibNextVrf withIp = FibNextVrf.of("foo", ip);
    assertThat(withIp.getNextVrf(), equalTo("foo"));
    assertThat(withIp.getIp(), equalTo(ip));

    FibNextVrf withoutIp = FibNextVrf.of("foo");
    assertThat(withoutIp.getNextVrf(), equalTo("foo"));
    assertThat(withoutIp.getIp(), nullValue());
  }

  @Test
  public void testInvalidIp() {
    assertThrows(IllegalArgumentException.class, () -> FibNextVrf.of("foo", Ip.AUTO));
    assertThrows(IllegalArgumentException.class, () -> FibNextVrf.of("foo", Ip.ZERO));
    assertThrows(IllegalArgumentException.class, () -> FibNextVrf.of("foo", Ip.MAX));
  }

  @Test
  public void testCaching() {
    FibNextVrf withoutIp1 = FibNextVrf.of("foo");
    FibNextVrf withoutIp2 = FibNextVrf.of("foo");
    assertThat(withoutIp1, sameInstance(withoutIp2));

    Ip ip = Ip.parse("1.2.3.4");
    FibNextVrf withIp1 = FibNextVrf.of("foo", ip);
    FibNextVrf withIp2 = FibNextVrf.of("foo", ip);
    assertThat(withIp1, sameInstance(withIp2));

    FibNextVrf deserialized = SerializationUtils.clone(withIp1);
    assertThat(deserialized, sameInstance(withIp1));
  }

  @Test
  public void testComparator() {
    Ip ip1 = Ip.parse("1.2.3.4");
    Ip ip2 = Ip.parse("5.6.7.8");
    Comparator<FibNextVrf> comparator =
        Comparator.<FibNextVrf, String>comparing(FibNextVrf::getNextVrf)
            .thenComparing(FibNextVrf::getIp, Comparator.nullsFirst(Comparator.naturalOrder()));

    // VRF name takes precedence
    assertThat(
        comparator.compare(FibNextVrf.of("aaa", ip1), FibNextVrf.of("bbb", ip1)), lessThan(0));
    assertThat(
        comparator.compare(FibNextVrf.of("bbb", ip1), FibNextVrf.of("aaa", ip1)), greaterThan(0));

    // IP compared second (null < non-null)
    assertThat(comparator.compare(FibNextVrf.of("foo"), FibNextVrf.of("foo", ip1)), lessThan(0));
    assertThat(comparator.compare(FibNextVrf.of("foo", ip1), FibNextVrf.of("foo")), greaterThan(0));
    assertThat(
        comparator.compare(FibNextVrf.of("foo", ip1), FibNextVrf.of("foo", ip2)), lessThan(0));
  }
}
