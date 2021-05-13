package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.junit.Test;

/** Test of {@link RoutePolicyBooleanAsPathLength}. */
@ParametersAreNonnullByDefault
public final class RoutePolicyBooleanAsPathLengthTest {
  @Test
  public void testSerialization() {
    RoutePolicyBooleanAsPathLength obj =
        new RoutePolicyBooleanAsPathLength(IntComparator.GT, 1, false);
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    RoutePolicyBooleanAsPathLength obj =
        new RoutePolicyBooleanAsPathLength(IntComparator.GT, 1, false);
    new EqualsTester()
        .addEqualityGroup(obj, new RoutePolicyBooleanAsPathLength(IntComparator.GT, 1, false))
        .addEqualityGroup(new RoutePolicyBooleanAsPathLength(IntComparator.EQ, 1, false))
        .addEqualityGroup(new RoutePolicyBooleanAsPathLength(IntComparator.GT, 2, false))
        .addEqualityGroup(new RoutePolicyBooleanAsPathLength(IntComparator.GT, 1, true))
        .testEquals();
  }
}
