package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link VarInt}. */
final class VarIntTest {

  private static final VarInt OBJ = new VarInt("a");

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, VarInt.class), equalTo(OBJ));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(OBJ, OBJ, new VarInt("a"))
        .addEqualityGroup(new VarInt("b"))
        .testEquals();
  }
}
