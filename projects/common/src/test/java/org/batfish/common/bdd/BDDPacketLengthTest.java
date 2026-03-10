package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import net.sf.javabdd.BDD;
import org.junit.Test;

/** Test for {@link BDDPacketLength}. */
public final class BDDPacketLengthTest {
  @Test
  public void testSatAssignment() {
    BDDPacket pkt = new BDDPacket();
    BDDPacketLength pktLen = pkt.getPacketLength();

    // unconstrained
    {
      BDD satAssignment = pkt.getFactory().one().fullSatOne();
      assertThat(pktLen.satAssignmentToValue(satAssignment), equalTo(20));
    }

    // constrained to a single value
    {
      BDD satAssignment = pktLen.value(100).fullSatOne();
      assertThat(pktLen.satAssignmentToValue(satAssignment), equalTo(100));
    }

    // constrained to a range
    {
      BDD satAssignment = pktLen.range(100, 200).fullSatOne();
      assertThat(pktLen.satAssignmentToValue(satAssignment), equalTo(100));
    }
  }
}
