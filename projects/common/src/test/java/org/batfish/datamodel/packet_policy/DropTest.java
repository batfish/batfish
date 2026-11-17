package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link Drop} */
class DropTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(Drop.instance(), Drop.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(Drop.instance()), equalTo(Drop.instance()));
  }

  @Test
  void testJsonSerialization() {
    assertThat(BatfishObjectMapper.clone(Drop.instance(), Drop.class), equalTo(Drop.instance()));
  }

  @Test
  void testToString() {
    assertTrue(Drop.instance().toString().contains(Drop.class.getSimpleName()));
  }
}
