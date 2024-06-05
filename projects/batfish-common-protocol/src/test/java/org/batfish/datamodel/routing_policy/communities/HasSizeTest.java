package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link HasSize}. */
public final class HasSizeTest {

  private static final HasSize EXPR =
      new HasSize(new IntComparison(IntComparator.EQ, new LiteralInt(5)));

  @Test
  public void testSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
    assertThat(BatfishObjectMapper.clone(EXPR, CommunitySetMatchExpr.class), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new HasSize(EXPR.getExpr()))
        .addEqualityGroup(new HasSize(new IntComparison(IntComparator.GT, new LiteralInt(5))))
        .testEquals();
  }
}
