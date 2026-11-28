package org.batfish.representation.frr;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.LineAction;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpAsPathAccessListLine} */
class BgpAsPathAccessListLineTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BgpAsPathAccessListLine(LineAction.PERMIT, "^1_"),
            new BgpAsPathAccessListLine(LineAction.PERMIT, "^1_"))
        .addEqualityGroup(new BgpAsPathAccessListLine(LineAction.DENY, "^1_"))
        .addEqualityGroup(new BgpAsPathAccessListLine(LineAction.PERMIT, "^2_"))
        .testEquals();
  }
}
