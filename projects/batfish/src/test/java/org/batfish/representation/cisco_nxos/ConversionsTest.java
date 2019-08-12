package org.batfish.representation.cisco_nxos;

import static org.batfish.representation.cisco_nxos.Conversions.toRouteTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
    ExtendedCommunity ec = toRouteTarget(rd);
    assertThat(ec, equalTo(ExtendedCommunity.target(adminField, value)));
  }
}
