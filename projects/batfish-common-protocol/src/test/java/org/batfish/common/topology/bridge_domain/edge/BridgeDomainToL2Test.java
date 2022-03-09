package org.batfish.common.topology.bridge_domain.edge;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Range;
import java.util.Optional;
import org.batfish.common.topology.bridge_domain.EthernetTag;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2.AccessMode;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2.Trunk;
import org.batfish.datamodel.IntegerSpace;
import org.junit.Test;

public class BridgeDomainToL2Test {
  @Test
  public void testAccessMode() {
    AccessMode testing = new AccessMode(5);
    assertThat(testing.receiveTag(EthernetTag.untagged()), equalTo(Optional.of(5)));
    assertThat(testing.receiveTag(EthernetTag.tagged(5)), equalTo(Optional.empty()));
    assertThat(testing.receiveTag(EthernetTag.tagged(6)), equalTo(Optional.empty()));
    assertThat(testing.sendFromVlan(5), equalTo(Optional.of(EthernetTag.untagged())));
    assertThat(testing.sendFromVlan(6), equalTo(Optional.empty()));
  }

  @Test
  public void testTrunk() {
    Trunk testing = new Trunk(IntegerSpace.of(Range.closed(4, 5)), 5);
    assertThat(testing.receiveTag(EthernetTag.tagged(4)), equalTo(Optional.of(4)));
    assertThat(testing.receiveTag(EthernetTag.untagged()), equalTo(Optional.of(5)));
    assertThat(testing.receiveTag(EthernetTag.tagged(5)), equalTo(Optional.empty()));
    assertThat(testing.receiveTag(EthernetTag.tagged(6)), equalTo(Optional.empty()));
    assertThat(testing.sendFromVlan(4), equalTo(Optional.of(EthernetTag.tagged(4))));
    assertThat(testing.sendFromVlan(5), equalTo(Optional.of(EthernetTag.untagged())));
    assertThat(testing.sendFromVlan(6), equalTo(Optional.empty()));

    Trunk noNative = new Trunk(IntegerSpace.of(Range.closed(4, 5)), null);
    assertThat(noNative.receiveTag(EthernetTag.untagged()), equalTo(Optional.empty()));
    assertThat(noNative.receiveTag(EthernetTag.tagged(4)), equalTo(Optional.of(4)));
    assertThat(noNative.receiveTag(EthernetTag.tagged(5)), equalTo(Optional.of(5)));
    assertThat(noNative.receiveTag(EthernetTag.tagged(6)), equalTo(Optional.empty()));
    assertThat(noNative.sendFromVlan(4), equalTo(Optional.of(EthernetTag.tagged(4))));
    assertThat(noNative.sendFromVlan(5), equalTo(Optional.of(EthernetTag.tagged(5))));
    assertThat(noNative.sendFromVlan(6), equalTo(Optional.empty()));

    Trunk nativeDisallowed = new Trunk(IntegerSpace.of(Range.closed(4, 4)), 5);
    assertThat(nativeDisallowed.receiveTag(EthernetTag.tagged(4)), equalTo(Optional.of(4)));
    assertThat(nativeDisallowed.receiveTag(EthernetTag.untagged()), equalTo(Optional.empty()));
    assertThat(nativeDisallowed.receiveTag(EthernetTag.tagged(5)), equalTo(Optional.empty()));
    assertThat(nativeDisallowed.receiveTag(EthernetTag.tagged(6)), equalTo(Optional.empty()));
    assertThat(nativeDisallowed.sendFromVlan(4), equalTo(Optional.of(EthernetTag.tagged(4))));
    assertThat(nativeDisallowed.sendFromVlan(5), equalTo(Optional.empty()));
    assertThat(nativeDisallowed.sendFromVlan(6), equalTo(Optional.empty()));
  }
}
