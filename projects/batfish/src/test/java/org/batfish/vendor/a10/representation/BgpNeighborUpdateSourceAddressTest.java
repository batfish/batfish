package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link BgpNeighborUpdateSourceAddress}. */
public class BgpNeighborUpdateSourceAddressTest {
  @Test
  public void testSerialization() {
    BgpNeighborUpdateSourceAddress obj =
        new BgpNeighborUpdateSourceAddress(Ip.parse("10.11.12.13"));
    BgpNeighborUpdateSourceAddress clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }

  @Test
  public void testEquality() {
    BgpNeighborUpdateSourceAddress obj = new BgpNeighborUpdateSourceAddress(Ip.ZERO);
    new EqualsTester()
        .addEqualityGroup(obj, new BgpNeighborUpdateSourceAddress(Ip.ZERO))
        .addEqualityGroup(new BgpNeighborUpdateSourceAddress(Ip.parse("10.11.12.13")))
        .testEquals();
  }
}
