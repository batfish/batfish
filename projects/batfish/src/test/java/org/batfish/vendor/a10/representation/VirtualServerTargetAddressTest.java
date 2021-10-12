package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link VirtualServerTargetAddress} */
public class VirtualServerTargetAddressTest {
  @Test
  public void testEquality() {
    VirtualServerTargetAddress obj = new VirtualServerTargetAddress(Ip.ZERO);
    new EqualsTester()
        .addEqualityGroup(obj, new VirtualServerTargetAddress(Ip.ZERO))
        .addEqualityGroup(new VirtualServerTargetAddress(Ip.parse("10.10.10.10")))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    VirtualServerTargetAddress obj = new VirtualServerTargetAddress(Ip.ZERO);
    VirtualServerTargetAddress clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }
}
