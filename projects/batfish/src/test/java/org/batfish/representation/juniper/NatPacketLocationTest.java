package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.NatPacketLocation.interfaceLocation;
import static org.batfish.representation.juniper.NatPacketLocation.zoneLocation;

import com.google.common.testing.EqualsTester;
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
}
