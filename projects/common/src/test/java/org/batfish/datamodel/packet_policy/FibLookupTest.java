package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link FibLookup} */
class FibLookupTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new FibLookup(new LiteralVrfName("name")), new FibLookup(new LiteralVrfName("name")))
        .addEqualityGroup(new FibLookup(new LiteralVrfName("otherName")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    FibLookup fl = new FibLookup(new LiteralVrfName("name"));
    assertThat(SerializationUtils.clone(fl), equalTo(fl));
  }

  @Test
  void testJsonSerialization() {
    FibLookup fl = new FibLookup(new LiteralVrfName("name"));
    assertThat(BatfishObjectMapper.clone(fl, FibLookup.class), equalTo(fl));
  }

  @Test
  void testToString() {
    FibLookup fl = new FibLookup(new LiteralVrfName("xxxxxx"));
    assertTrue(fl.toString().contains(fl.getClass().getSimpleName()));
    assertTrue(fl.toString().contains("vrfName"));
    assertTrue(fl.toString().contains("xxxxxx"));
  }
}
