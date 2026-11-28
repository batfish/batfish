package org.batfish.representation.frr;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.LineAction;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpCommunityListExpandedLine} */
class BgpCommunityListExpandedLineTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BgpCommunityListExpandedLine(LineAction.PERMIT, "^65000:1$"),
            new BgpCommunityListExpandedLine(LineAction.PERMIT, "^65000:1$"))
        .addEqualityGroup(new BgpCommunityListExpandedLine(LineAction.DENY, "^65000:1$"))
        .addEqualityGroup(new BgpCommunityListExpandedLine(LineAction.PERMIT, "^65000:2$"))
        .testEquals();
  }
}
