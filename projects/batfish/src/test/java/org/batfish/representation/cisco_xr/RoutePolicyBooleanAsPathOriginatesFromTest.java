package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link RoutePolicyBooleanAsPathOriginatesFrom}. */
@ParametersAreNonnullByDefault
public final class RoutePolicyBooleanAsPathOriginatesFromTest {

  @Test
  public void testSerialization() {
    RoutePolicyBooleanAsPathOriginatesFrom obj =
        new RoutePolicyBooleanAsPathOriginatesFrom(false, Range.singleton(1L));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    RoutePolicyBooleanAsPathOriginatesFrom obj =
        new RoutePolicyBooleanAsPathOriginatesFrom(false, Range.singleton(1L));
    new EqualsTester()
        .addEqualityGroup(
            obj, new RoutePolicyBooleanAsPathOriginatesFrom(false, Range.singleton(1L)))
        .addEqualityGroup(
            new RoutePolicyBooleanAsPathOriginatesFrom(
                false, Range.singleton(1L), Range.singleton(2L)))
        .addEqualityGroup(new RoutePolicyBooleanAsPathOriginatesFrom(true, Range.singleton(1L)))
        .testEquals();
  }
}
