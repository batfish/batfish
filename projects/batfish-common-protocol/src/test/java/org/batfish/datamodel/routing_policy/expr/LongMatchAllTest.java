package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link LongMatchAll}. */
public final class LongMatchAllTest {

  private static final LongMatchAll OBJ =
      LongMatchAll.of(new LongComparison(IntComparator.EQ, new LiteralLong(1L)));

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(BatfishObjectMapper.clone(OBJ, LongMatchAll.class), equalTo(OBJ));
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
            LongMatchAll.of(new LongComparison(IntComparator.EQ, new LiteralLong(1L))),
            LongMatchAll.of(
                ImmutableSet.of(new LongComparison(IntComparator.EQ, new LiteralLong(1L)))),
            new LongMatchAll(
                ImmutableSet.of(new LongComparison(IntComparator.EQ, new LiteralLong(1L)))))
        .addEqualityGroup(LongMatchAll.of())
        .testEquals();
  }
}
