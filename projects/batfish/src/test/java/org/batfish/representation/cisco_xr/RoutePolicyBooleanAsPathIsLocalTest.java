package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link RoutePolicyBooleanAsPathIsLocal}. */
@ParametersAreNonnullByDefault
final class RoutePolicyBooleanAsPathIsLocalTest {
  @Test
  void testSerialization() {
    assertThat(
        SerializationUtils.clone(RoutePolicyBooleanAsPathIsLocal.instance()),
        equalTo(RoutePolicyBooleanAsPathIsLocal.instance()));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            RoutePolicyBooleanAsPathIsLocal.instance(), RoutePolicyBooleanAsPathIsLocal.instance())
        .testEquals();
  }
}
