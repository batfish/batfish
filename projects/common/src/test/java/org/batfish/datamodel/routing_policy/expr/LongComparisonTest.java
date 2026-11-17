package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link LongComparison}. */
final class LongComparisonTest {

  private static final LongComparison OBJ =
      new LongComparison(IntComparator.EQ, new LiteralLong(1L));

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, LongComparison.class), equalTo(OBJ));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(OBJ, OBJ, new LongComparison(IntComparator.EQ, new LiteralLong(1L)))
        .addEqualityGroup(new LongComparison(IntComparator.GT, new LiteralLong(1L)))
        .addEqualityGroup(new LongComparison(IntComparator.GT, new LiteralLong(2L)))
        .testEquals();
  }
}
