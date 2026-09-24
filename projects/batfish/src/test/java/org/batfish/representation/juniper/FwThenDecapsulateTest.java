package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class FwThenDecapsulateTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new FwThenDecapsulate(FwThenDecapsulate.Type.GRE, null),
            new FwThenDecapsulate(FwThenDecapsulate.Type.GRE, null))
        .addEqualityGroup(new FwThenDecapsulate(FwThenDecapsulate.Type.GRE, "DECAP-VRF"))
        .addEqualityGroup(new FwThenDecapsulate(FwThenDecapsulate.Type.GRE_IN_UDP, null))
        .testEquals();
  }

  @Test
  public void testProperties() {
    FwThenDecapsulate decapsulate =
        new FwThenDecapsulate(FwThenDecapsulate.Type.MPLS_IN_UDP, "DECAP-VRF");

    assertThat(decapsulate.getType(), equalTo(FwThenDecapsulate.Type.MPLS_IN_UDP));
    assertThat(decapsulate.getRoutingInstance(), equalTo("DECAP-VRF"));
    assertThat(
        new FwThenDecapsulate(FwThenDecapsulate.Type.GRE, null).getRoutingInstance(), nullValue());
  }
}
