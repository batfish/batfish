package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link IntMatchAll}. */
public final class IntMatchAllTest {

  private static final IntMatchAll OBJ =
      IntMatchAll.of(new IntComparison(IntComparator.EQ, new LiteralInt(1)));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, IntMatchAll.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            OBJ,
            OBJ,
            IntMatchAll.of(new IntComparison(IntComparator.EQ, new LiteralInt(1))),
            IntMatchAll.of(ImmutableSet.of(new IntComparison(IntComparator.EQ, new LiteralInt(1)))),
            new IntMatchAll(
                ImmutableSet.of(new IntComparison(IntComparator.EQ, new LiteralInt(1)))))
        .addEqualityGroup(IntMatchAll.of())
        .testEquals();
  }
}
