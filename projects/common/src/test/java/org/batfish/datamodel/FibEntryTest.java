package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link FibEntry} */
class FibEntryTest {

  private static final ImmutableList<AbstractRoute> RESOLUTION_STEPS =
      ImmutableList.of(new ConnectedRoute(Prefix.parse("2.2.2.2/31"), "Eth1"));

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new FibEntry(FibForward.of(Ip.parse("1.1.1.1"), "Eth1"), RESOLUTION_STEPS),
            new FibEntry(FibForward.of(Ip.parse("1.1.1.1"), "Eth1"), RESOLUTION_STEPS))
        .addEqualityGroup(
            new FibEntry(FibForward.of(Ip.parse("1.1.1.2"), "Eth1"), RESOLUTION_STEPS))
        .addEqualityGroup(
            new FibEntry(FibForward.of(Ip.parse("1.1.1.1"), "Eth2"), RESOLUTION_STEPS))
        .addEqualityGroup(
            new FibEntry(
                FibForward.of(Ip.parse("1.1.1.1"), "Eth1"),
                ImmutableList.of(new ConnectedRoute(Prefix.parse("2.2.2.4/31"), "Eth100"))))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    FibEntry fe = new FibEntry(FibForward.of(Ip.parse("1.1.1.1"), "Eth1"), RESOLUTION_STEPS);
    assertThat(SerializationUtils.clone(fe), equalTo(fe));
  }

  @Test
  void requiresSteps() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new FibEntry(FibForward.of(Ip.parse("1.1.1.1"), "Eth1"), ImmutableList.of());
        });
  }
}
