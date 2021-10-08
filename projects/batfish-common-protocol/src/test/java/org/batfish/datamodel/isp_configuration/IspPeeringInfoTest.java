package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class IspPeeringInfoTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new IspPeeringInfo(1, 2), new IspPeeringInfo(1, 2))
        .addEqualityGroup(new IspPeeringInfo(10, 2))
        .addEqualityGroup(new IspPeeringInfo(1, 20))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IspPeeringInfo ispPeeringInfo = new IspPeeringInfo(1L, 2L);

    assertThat(
        BatfishObjectMapper.clone(ispPeeringInfo, IspPeeringInfo.class), equalTo(ispPeeringInfo));
  }
}
