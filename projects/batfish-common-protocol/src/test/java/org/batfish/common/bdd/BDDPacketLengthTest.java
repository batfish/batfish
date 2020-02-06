package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import net.sf.javabdd.BDD;
import org.junit.Test;

/** Test for {@link BDDPacketLength}. */
public final class BDDPacketLengthTest {
  private static final BDDPacket PKT = new BDDPacket();
  private static final BDDPacketLength PKT_LEN = PKT.getPacketLength();

  @Test
  public void testSatAssignment() {
    // unconstrained
    {
      BDD satAssignment = PKT.getFactory().one().fullSatOne();
      assertThat(PKT_LEN.satAssignmentToValue(satAssignment), equalTo(20L));
    }

    // constrained to a single value
    {
      BDD satAssignment = PKT_LEN.value(100).fullSatOne();
      assertThat(PKT_LEN.satAssignmentToValue(satAssignment), equalTo(100L));
    }

    // constrained to a range
    {
      BDD satAssignment = PKT_LEN.range(100, 200).fullSatOne();
      assertThat(PKT_LEN.satAssignmentToValue(satAssignment), equalTo(100L));
    }
  }
}
