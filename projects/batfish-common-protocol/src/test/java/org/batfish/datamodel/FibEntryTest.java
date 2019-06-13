package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link FibEntry} */
public class FibEntryTest {

  private static final ImmutableList<AbstractRoute> RESOLUTION_STEPS =
      ImmutableList.of(new ConnectedRoute(Prefix.parse("2.2.2.2/31"), "Eth1"));
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new FibEntry(new FibForward(Ip.parse("1.1.1.1"), "Eth1"), RESOLUTION_STEPS),
            new FibEntry(new FibForward(Ip.parse("1.1.1.1"), "Eth1"), RESOLUTION_STEPS))
        .addEqualityGroup(
            new FibEntry(new FibForward(Ip.parse("1.1.1.2"), "Eth1"), RESOLUTION_STEPS))
        .addEqualityGroup(
            new FibEntry(new FibForward(Ip.parse("1.1.1.1"), "Eth2"), RESOLUTION_STEPS))
        .addEqualityGroup(
            new FibEntry(
                new FibForward(Ip.parse("1.1.1.1"), "Eth1"),
                ImmutableList.of(new ConnectedRoute(Prefix.parse("2.2.2.4/31"), "Eth100"))))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    FibEntry fe = new FibEntry(new FibForward(Ip.parse("1.1.1.1"), "Eth1"), RESOLUTION_STEPS);
    assertThat(SerializationUtils.clone(fe), equalTo(fe));
  }

  @Test
  public void requiresSteps() {
    thrown.expect(IllegalArgumentException.class);
    new FibEntry(new FibForward(Ip.parse("1.1.1.1"), "Eth1"), ImmutableList.of());
  }
}
