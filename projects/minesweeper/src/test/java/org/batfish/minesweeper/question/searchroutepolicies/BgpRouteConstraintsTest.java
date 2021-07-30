package org.batfish.minesweeper.question.searchroutepolicies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import java.util.Set;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.LongSpace.Builder;
import org.batfish.datamodel.RoutingProtocol;
import org.junit.Test;

/** Tests for {@link BgpRouteConstraints}. */
public class BgpRouteConstraintsTest {

  @Test
  public void testEquals() {
    BgpRouteConstraints c1 = BgpRouteConstraints.builder().build();
    BgpRouteConstraints c2 =
        BgpRouteConstraints.builder().setLocalPreference(LongSpace.of(3)).build();
    BgpRouteConstraints c3 =
        BgpRouteConstraints.builder()
            .setLocalPreference(LongSpace.of(3))
            .setMed(LongSpace.of(3))
            .build();
    BgpRouteConstraints c4 =
        BgpRouteConstraints.builder()
            .setMed(LongSpace.of(3))
            .setLocalPreference(LongSpace.of(3))
            .build();
    BgpRouteConstraints c5 =
        BgpRouteConstraints.builder()
            .setCommunities(
                new RegexConstraints(ImmutableList.of(new RegexConstraint("foo", true))))
            .build();
    new EqualsTester()
        .addEqualityGroup(c1)
        .addEqualityGroup(c2)
        .addEqualityGroup(c3, c4)
        .addEqualityGroup(c5)
        .testEquals();
  }

  @Test
  public void testProcessBuilder() {

    Range<Long> include = Range.closed(4L, 44L);
    Range<Long> exclude = Range.singleton(43L);

    // b3 only has exclusions and so will be transformed by processBuilder; the others will not
    Builder b1 = null;
    Builder b2 = LongSpace.builder().including(include);
    Builder b3 = LongSpace.builder().excluding(exclude);
    Builder b4 = LongSpace.builder().including(include).excluding(exclude);

    LongSpace b3Expected =
        LongSpace.builder().including(Range.closed(0L, 4294967295L)).excluding(exclude).build();

    assertNull(BgpRouteConstraints.processBuilder(b1));
    assertEquals(b2.build(), BgpRouteConstraints.processBuilder(b2));
    assertEquals(b3Expected, BgpRouteConstraints.processBuilder(b3));
    assertEquals(b4.build(), BgpRouteConstraints.processBuilder(b4));
  }

  @Test
  public void testIs32BitRange() {
    LongSpace s1 = LongSpace.builder().including(Range.closed(4L, 44L)).build();
    // negative numbers are not allowed
    LongSpace s2 = LongSpace.builder().including(Range.closed(-4L, 44L)).build();
    // numbers higher than 2^32 - 1 are not allowed
    LongSpace s3 = LongSpace.builder().including(Range.singleton(4294967296L)).build();

    assertTrue(BgpRouteConstraints.is32BitRange(s1));
    assertFalse(BgpRouteConstraints.is32BitRange(s2));
    assertFalse(BgpRouteConstraints.is32BitRange(s3));
  }

  @Test
  public void testIsAllowedProtocolSet() {
    Set<RoutingProtocol> s1 = ImmutableSet.of();
    Set<RoutingProtocol> s2 =
        ImmutableSet.of(RoutingProtocol.AGGREGATE, RoutingProtocol.BGP, RoutingProtocol.IBGP);
    Set<RoutingProtocol> s3 = ImmutableSet.of(RoutingProtocol.BGP, RoutingProtocol.OSPF);
    assertTrue(BgpRouteConstraints.isBgpProtocol(s1));
    assertTrue(BgpRouteConstraints.isBgpProtocol(s2));
    assertFalse(BgpRouteConstraints.isBgpProtocol(s3));
  }
}
