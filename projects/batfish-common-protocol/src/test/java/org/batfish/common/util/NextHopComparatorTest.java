package org.batfish.common.util;

import static org.batfish.common.util.NextHopComparator.instance;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.route.nh.NextHopVtep;
import org.junit.Test;

/** Test of {@link NextHopComparator}. */
public final class NextHopComparatorTest {

  @Test
  public void testCompareClass() {
    assertThat(
        instance().compare(NextHopDiscard.instance(), NextHopInterface.of("foo")), lessThan(0));
    assertThat(
        instance().compare(NextHopDiscard.instance(), NextHopIp.of(Ip.parse("1.1.1.1"))),
        lessThan(0));
    assertThat(instance().compare(NextHopDiscard.instance(), NextHopVrf.of("foo")), lessThan(0));
    assertThat(
        instance().compare(NextHopDiscard.instance(), NextHopVtep.of(5, Ip.parse("1.1.1.1"))),
        lessThan(0));

    assertThat(
        instance().compare(NextHopInterface.of("foo"), NextHopIp.of(Ip.parse("1.1.1.1"))),
        lessThan(0));
    assertThat(instance().compare(NextHopInterface.of("foo"), NextHopVrf.of("foo")), lessThan(0));
    assertThat(
        instance().compare(NextHopInterface.of("foo"), NextHopVtep.of(5, Ip.parse("1.1.1.1"))),
        lessThan(0));

    assertThat(
        instance().compare(NextHopIp.of(Ip.parse("1.1.1.1")), NextHopVrf.of("foo")), lessThan(0));
    assertThat(
        instance()
            .compare(NextHopIp.of(Ip.parse("1.1.1.1")), NextHopVtep.of(5, Ip.parse("1.1.1.1"))),
        lessThan(0));

    assertThat(
        instance().compare(NextHopVrf.of("foo"), NextHopVtep.of(5, Ip.parse("1.1.1.1"))),
        lessThan(0));
  }

  @Test
  public void testCompareNextHopDiscard() {
    assertThat(
        instance().compare(NextHopDiscard.instance(), NextHopDiscard.instance()), equalTo(0));
  }

  @Test
  public void testCompareNextHopInterface() {
    assertThat(
        instance().compare(NextHopInterface.of("foo"), NextHopInterface.of("foo")), equalTo(0));
    assertThat(instance().compare(NextHopInterface.of("a"), NextHopInterface.of("b")), lessThan(0));
    assertThat(
        instance().compare(NextHopInterface.of("Ethernet2"), NextHopInterface.of("Ethernet11")),
        lessThan(0));
    assertThat(
        instance()
            .compare(NextHopInterface.of("foo"), NextHopInterface.of("foo", Ip.parse("1.1.1.1"))),
        lessThan(0));
    assertThat(
        instance()
            .compare(NextHopInterface.of("foo"), NextHopInterface.of("bar", Ip.parse("1.1.1.1"))),
        lessThan(0));
    assertThat(
        instance()
            .compare(
                NextHopInterface.of("foo", Ip.parse("2.2.2.2")),
                NextHopInterface.of("foo", Ip.parse("11.0.0.0"))),
        lessThan(0));
    assertThat(
        instance()
            .compare(
                NextHopInterface.of("foo", Ip.parse("2.2.2.2")),
                NextHopInterface.of("foo", Ip.parse("2.2.2.2"))),
        equalTo(0));
    assertThat(
        instance()
            .compare(
                NextHopInterface.of("foo", Ip.parse("1.1.1.1")),
                NextHopInterface.of("bar", Ip.parse("2.2.2.2"))),
        lessThan(0));
  }

  @Test
  public void testCompareNextHopIp() {
    assertThat(
        instance().compare(NextHopIp.of(Ip.parse("1.1.1.1")), NextHopIp.of(Ip.parse("1.1.1.1"))),
        equalTo(0));
    assertThat(
        instance().compare(NextHopIp.of(Ip.parse("1.1.1.1")), NextHopIp.of(Ip.parse("1.1.1.2"))),
        lessThan(0));
    assertThat(
        instance().compare(NextHopIp.of(Ip.parse("2.2.2.2")), NextHopIp.of(Ip.parse("11.0.0.0"))),
        lessThan(0));
  }

  @Test
  public void testCompareNextHopVrf() {
    assertThat(instance().compare(NextHopVrf.of("foo"), NextHopVrf.of("foo")), equalTo(0));
    assertThat(instance().compare(NextHopVrf.of("foo"), NextHopVrf.of("foo1")), lessThan(0));
    assertThat(instance().compare(NextHopVrf.of("foo1"), NextHopVrf.of("foo2")), lessThan(0));
  }

  @Test
  public void testCompareNextHopVtep() {
    assertThat(
        instance()
            .compare(
                NextHopVtep.of(5, Ip.parse("1.1.1.1")), NextHopVtep.of(5, Ip.parse("1.1.1.1"))),
        equalTo(0));
    assertThat(
        instance()
            .compare(
                NextHopVtep.of(5, Ip.parse("1.1.1.1")), NextHopVtep.of(6, Ip.parse("1.1.1.1"))),
        lessThan(0));
    assertThat(
        instance()
            .compare(
                NextHopVtep.of(5, Ip.parse("2.2.2.2")), NextHopVtep.of(6, Ip.parse("1.1.1.1"))),
        lessThan(0));
    assertThat(
        instance()
            .compare(
                NextHopVtep.of(5, Ip.parse("1.1.1.1")), NextHopVtep.of(5, Ip.parse("2.2.2.2"))),
        lessThan(0));
  }
}
