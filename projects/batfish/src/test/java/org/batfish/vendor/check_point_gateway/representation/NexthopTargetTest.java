package org.batfish.vendor.check_point_gateway.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link NexthopTarget} */
public class NexthopTargetTest {
  @Test
  public void testEquality() {
    new EqualsTester()
        .addEqualityGroup(NexthopReject.INSTANCE, NexthopReject.INSTANCE)
        .addEqualityGroup(NexthopBlackhole.INSTANCE, NexthopBlackhole.INSTANCE)
        .addEqualityGroup(
            new NexthopAddress(Ip.parse("1.2.3.4")), new NexthopAddress(Ip.parse("1.2.3.4")))
        .addEqualityGroup(
            new NexthopAddress(Ip.parse("1.2.3.5")), new NexthopAddress(Ip.parse("1.2.3.5")))
        .addEqualityGroup(new NexthopLogical("iface1"), new NexthopLogical("iface1"))
        .addEqualityGroup(new NexthopLogical("iface2"), new NexthopLogical("iface2"))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    NexthopTarget obj = new NexthopLogical("iface");
    NexthopTarget clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }
}
