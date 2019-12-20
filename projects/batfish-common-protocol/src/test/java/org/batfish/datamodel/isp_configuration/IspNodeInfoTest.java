package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link IspNodeInfo} */
public class IspNodeInfoTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new IspNodeInfo(42, "n1"), new IspNodeInfo(42L, "n1"))
        .addEqualityGroup(new IspNodeInfo(42, "other"))
        .addEqualityGroup(new IspNodeInfo(24, "n1"))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    IspNodeInfo ispNodeInfo = new IspNodeInfo(42, "n1");

    assertThat(BatfishObjectMapper.clone(ispNodeInfo, IspNodeInfo.class), equalTo(ispNodeInfo));
  }
}
