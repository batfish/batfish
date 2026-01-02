package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link LiteralVrfName} */
public class LiteralVrfNameTest {
  @Test
  public void testEquals() {
    LiteralVrfName lv = new LiteralVrfName("vrf");
    new EqualsTester()
        .addEqualityGroup(lv, lv, new LiteralVrfName("vrf"))
        .addEqualityGroup(new LiteralVrfName("vrf2"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    LiteralVrfName lv = new LiteralVrfName("vrf");
    assertThat(SerializationUtils.clone(lv), equalTo(lv));
  }

  @Test
  public void testJsonSerialization() {
    LiteralVrfName lv = new LiteralVrfName("vrf");
    assertThat(BatfishObjectMapper.clone(lv, LiteralVrfName.class), equalTo(lv));
  }
}
