package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link IntComparison}. */
public final class IntComparisonTest {

  private static final IntComparison OBJ = new IntComparison(IntComparator.EQ, new LiteralInt(1));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, IntComparison.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(OBJ, OBJ, new IntComparison(IntComparator.EQ, new LiteralInt(1)))
        .addEqualityGroup(new IntComparison(IntComparator.GT, new LiteralInt(1)))
        .addEqualityGroup(new IntComparison(IntComparator.GT, new LiteralInt(2)))
        .testEquals();
  }
}
