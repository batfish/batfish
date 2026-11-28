package org.batfish.representation.frr;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpCommunityListStandardLine} */
class BgpCommunityListStandardLineTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BgpCommunityListStandardLine(
                LineAction.PERMIT, ImmutableSet.of(StandardCommunity.of(65000, 1))),
            new BgpCommunityListStandardLine(
                LineAction.PERMIT, ImmutableSet.of(StandardCommunity.of(65000, 1))))
        .addEqualityGroup(
            new BgpCommunityListStandardLine(
                LineAction.DENY, ImmutableSet.of(StandardCommunity.of(65000, 1))))
        .addEqualityGroup(
            new BgpCommunityListStandardLine(
                LineAction.PERMIT, ImmutableSet.of(StandardCommunity.of(65000, 2))))
        .testEquals();
  }
}
