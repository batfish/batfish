package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link BgpNeighborIdAddress}. */
public class BgpNeighborIdAddressTest {
  @Test
  public void testSerialization() {
    BgpNeighborIdAddress obj = new BgpNeighborIdAddress(Ip.parse("10.11.12.13"));
    BgpNeighborIdAddress clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }

  @Test
  public void testEquality() {
    BgpNeighborIdAddress obj = new BgpNeighborIdAddress(Ip.ZERO);
    new EqualsTester()
        .addEqualityGroup(obj, new BgpNeighborIdAddress(Ip.ZERO))
        .addEqualityGroup(new BgpNeighborIdAddress(Ip.parse("10.11.12.13")))
        .testEquals();
  }
}
