package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link HasMatchingRoute} */
@ParametersAreNonnullByDefault
public final class HasMatchingRouteTest {

  @Test
  public void testJavaSerialization() {
    HasMatchingRoute obj = new HasMatchingRoute(MainRibRoutes.instance(), BooleanExprs.TRUE);
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJsonSerialization() {
    HasMatchingRoute obj = new HasMatchingRoute(MainRibRoutes.instance(), BooleanExprs.TRUE);
    assertThat(BatfishObjectMapper.clone(obj, HasMatchingRoute.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new HasMatchingRoute(MainRibRoutes.instance(), BooleanExprs.TRUE))
        .addEqualityGroup(new HasMatchingRoute(MainRibRoutes.instance(), BooleanExprs.FALSE))
        .testEquals();
  }
}
