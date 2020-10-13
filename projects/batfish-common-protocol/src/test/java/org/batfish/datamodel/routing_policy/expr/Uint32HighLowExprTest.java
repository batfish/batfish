package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link Uint32HighLowExpr}. */
public final class Uint32HighLowExprTest {

  private static final Uint32HighLowExpr OBJ =
      new Uint32HighLowExpr(new LiteralInt(1), new LiteralInt(1));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, Uint32HighLowExpr.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(OBJ, OBJ, new Uint32HighLowExpr(new LiteralInt(1), new LiteralInt(1)))
        .addEqualityGroup(new Uint32HighLowExpr(new LiteralInt(1), new LiteralInt(2)))
        .addEqualityGroup(new Uint32HighLowExpr(new LiteralInt(2), new LiteralInt(2)))
        .testEquals();
  }
}
