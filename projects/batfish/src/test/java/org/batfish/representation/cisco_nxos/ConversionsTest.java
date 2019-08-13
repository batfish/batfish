package org.batfish.representation.cisco_nxos;

import static org.batfish.representation.cisco_nxos.Conversions.toRouteTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.Warnings;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

/** Tests for {@link Conversions} */
public class ConversionsTest {

  @Test
  public void testToRouteTarget() {
    int adminField = 65000;
    long value = 101010101L;
    RouteDistinguisher rd = RouteDistinguisher.from(adminField, value);
    ExtendedCommunity ec = toRouteTarget(rd, new Warnings());
    assertThat(ec, equalTo(ExtendedCommunity.target(adminField, value)));
  }

  @Test
  public void testToRouteTargetOutOfRangeAsn() {
    int outOfRangeAdminField = 0xFFFFFF;
    int validAdminField =
    long value = 101010101L;
    RouteDistinguisher rd = RouteDistinguisher.from(outOfRangeAdminField, value);
    ExtendedCommunity ec = toRouteTarget(rd, new Warnings(true, true, true));
    assertThat(ec, equalTo(ExtendedCommunity.target(outOfRangeAdminField, value)));
  }
}
