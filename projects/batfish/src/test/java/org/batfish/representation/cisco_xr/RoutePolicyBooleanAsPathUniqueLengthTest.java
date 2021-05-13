package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.junit.Test;

/** Test of {@link RoutePolicyBooleanAsPathUniqueLength}. */
@ParametersAreNonnullByDefault
public final class RoutePolicyBooleanAsPathUniqueLengthTest {
  @Test
  public void testSerialization() {
    RoutePolicyBooleanAsPathUniqueLength obj =
        new RoutePolicyBooleanAsPathUniqueLength(IntComparator.GT, 1, false);
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    RoutePolicyBooleanAsPathUniqueLength obj =
        new RoutePolicyBooleanAsPathUniqueLength(IntComparator.GT, 1, false);
    new EqualsTester()
        .addEqualityGroup(obj, new RoutePolicyBooleanAsPathUniqueLength(IntComparator.GT, 1, false))
        .addEqualityGroup(new RoutePolicyBooleanAsPathUniqueLength(IntComparator.EQ, 1, false))
        .addEqualityGroup(new RoutePolicyBooleanAsPathUniqueLength(IntComparator.GT, 2, false))
        .addEqualityGroup(new RoutePolicyBooleanAsPathUniqueLength(IntComparator.GT, 1, true))
        .testEquals();
  }
}
