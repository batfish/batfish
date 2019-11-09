package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.junit.Test;

/** Test of {@link RouteTargetExtendedCommunityExpr}. */
public final class RouteTargetExtendedCommunityExprTest {

  private static final RouteTargetExtendedCommunityExpr OBJ =
      new RouteTargetExtendedCommunityExpr(new LiteralLong(1L), new LiteralInt(1));

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(
        BatfishObjectMapper.clone(OBJ, RouteTargetExtendedCommunityExpr.class), equalTo(OBJ));
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
            OBJ, OBJ, new RouteTargetExtendedCommunityExpr(new LiteralLong(1L), new LiteralInt(1)))
        .addEqualityGroup(
            new RouteTargetExtendedCommunityExpr(new LiteralLong(1L), new LiteralInt(2)))
        .addEqualityGroup(
            new RouteTargetExtendedCommunityExpr(new LiteralLong(2L), new LiteralInt(2)))
        .testEquals();
  }
}
