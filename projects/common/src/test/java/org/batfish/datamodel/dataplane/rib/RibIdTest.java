package org.batfish.datamodel.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link org.batfish.datamodel.dataplane.rib.RibId} */
class RibIdTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new RibId("h", "v", "r"), new RibId("h", "v", "r"))
        .addEqualityGroup(new RibId("h1", "v", "r"))
        .addEqualityGroup(new RibId("h", "v1", "r"))
        .addEqualityGroup(new RibId("h", "v", "r1"))
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    RibId ribId = new RibId("h", "v", "r");
    assertThat(SerializationUtils.clone(ribId), equalTo(ribId));
  }

  @Test
  void testJsonSerialization() {
    RibId ribId = new RibId("h", "v", "r");
    assertThat(BatfishObjectMapper.clone(ribId, RibId.class), equalTo(ribId));
  }
}
