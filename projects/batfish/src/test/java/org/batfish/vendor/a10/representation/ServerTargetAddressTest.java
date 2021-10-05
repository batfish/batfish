package org.batfish.vendor.a10.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link ServerTargetAddress} */
public class ServerTargetAddressTest {
  @Test
  public void testEquality() {
    ServerTargetAddress obj = new ServerTargetAddress(Ip.ZERO);
    new EqualsTester()
        .addEqualityGroup(obj, new ServerTargetAddress(Ip.ZERO))
        .addEqualityGroup(new ServerTargetAddress(Ip.parse("10.10.10.10")))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    ServerTargetAddress obj = new ServerTargetAddress(Ip.ZERO);
    ServerTargetAddress clone = SerializationUtils.clone(obj);
    assertThat(obj, equalTo(clone));
  }
}
