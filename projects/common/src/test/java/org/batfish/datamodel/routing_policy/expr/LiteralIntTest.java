package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link LiteralInt}. */
final class LiteralIntTest {

  private static final LiteralInt OBJ = new LiteralInt(1);

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, LiteralInt.class), equalTo(OBJ));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(OBJ, OBJ, new LiteralInt(1))
        .addEqualityGroup(new LiteralInt(2))
        .testEquals();
  }
}
