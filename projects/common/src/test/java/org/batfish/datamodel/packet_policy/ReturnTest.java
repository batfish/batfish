package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class ReturnTest {

  @Test
  public void testEquals() {
    Return r = new Return(Drop.instance());
    new EqualsTester()
        .addEqualityGroup(r, r, new Return(Drop.instance()))
        .addEqualityGroup(new Return(new FibLookup(new LiteralVrfName("vrf"))))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Return r = new Return(Drop.instance());
    assertThat(SerializationUtils.clone(r), equalTo(r));
  }

  @Test
  public void testJsonSerialization() {
    Return r = new Return(Drop.instance());
    assertThat(BatfishObjectMapper.clone(r, Return.class), equalTo(r));
  }

  @Test
  public void testToString() {
    Return r = new Return(Drop.instance());
    assertTrue(r.toString().contains(Return.class.getSimpleName()));
    assertTrue(r.toString().contains("action"));
    assertTrue(r.toString().contains(r.getAction().toString()));
  }
}
