package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.NatPacketLocation.interfaceLocation;
import static org.batfish.representation.juniper.NatPacketLocation.zoneLocation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.junit.Test;

/** Tests for {@link NatPacketLocation}. */
public class NatPacketLocationTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(interfaceLocation("1"), interfaceLocation("1"))
        .addEqualityGroup(interfaceLocation("2"))
        .addEqualityGroup(zoneLocation("1"))
        .testEquals();
  }

  @Test
  public void testComparator() {
    List<NatPacketLocation> locs =
        ImmutableList.of(
            new NatPacketLocation(),
            NatPacketLocation.interfaceLocation("i1"),
            NatPacketLocation.interfaceLocation("i2"),
            NatPacketLocation.zoneLocation("i1"),
            NatPacketLocation.zoneLocation("z1"),
            NatPacketLocation.routingInstanceLocation("i1"),
            NatPacketLocation.routingInstanceLocation("ri1"));
    assertThat(ImmutableSet.copyOf(locs), hasSize(locs.size()));
    assertThat(
        ImmutableSortedSet.copyOf(locs), equalTo(ImmutableSortedSet.copyOf(Lists.reverse(locs))));
  }
}
