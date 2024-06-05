package org.batfish.datamodel.routing_policy.as_path;

import static org.batfish.datamodel.routing_policy.expr.IntComparator.EQ;
import static org.batfish.datamodel.routing_policy.expr.IntComparator.GT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link HasAsPathLength}. */
public final class HasAsPathLengthTest {

  @Test
  public void testJavaSerialization() {
    HasAsPathLength obj = HasAsPathLength.of(new IntComparison(EQ, new LiteralInt(1)));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    HasAsPathLength obj = HasAsPathLength.of(new IntComparison(EQ, new LiteralInt(1)));
    assertThat(BatfishObjectMapper.clone(obj, HasAsPathLength.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    HasAsPathLength obj = HasAsPathLength.of(new IntComparison(EQ, new LiteralInt(1)));
    new EqualsTester()
        .addEqualityGroup(obj, HasAsPathLength.of(new IntComparison(EQ, new LiteralInt(1))))
        .addEqualityGroup(HasAsPathLength.of(new IntComparison(GT, new LiteralInt(1))))
        .testEquals();
  }
}
