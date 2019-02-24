package org.batfish.dataplane.traceroute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests of {@link ExitPoint} */
public class ExitPointTest {
  @Test
  public void testEquals() {
    ExitPoint ep = new ExitPoint(Ip.parse("1.1.1.1"), "eth0");
    new EqualsTester()
        .addEqualityGroup(ep, ep, new ExitPoint(Ip.parse("1.1.1.1"), "eth0"))
        .addEqualityGroup(new ExitPoint(Ip.parse("1.1.1.2"), "eth0"))
        .addEqualityGroup(new ExitPoint(Ip.parse("1.1.1.1"), "eth1"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testFromFibEntry() {
    assertThat(
        ExitPoint.from(
            new FibEntry(
                Ip.parse("1.1.1.1"),
                "eth0",
                ImmutableList.of(
                    ConnectedRoute.builder()
                        .setNextHopInterface("eth0")
                        .setNetwork(Prefix.parse("1.1.1.0/24"))
                        .build()))),
        equalTo(new ExitPoint(Ip.parse("1.1.1.1"), "eth0")));
  }
}
