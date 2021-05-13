package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link RoutePolicyBooleanAsPathIsLocal}. */
@ParametersAreNonnullByDefault
public final class RoutePolicyBooleanAsPathIsLocalTest {
  @Test
  public void testSerialization() {
    assertThat(
        SerializationUtils.clone(RoutePolicyBooleanAsPathIsLocal.instance()),
        equalTo(RoutePolicyBooleanAsPathIsLocal.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            RoutePolicyBooleanAsPathIsLocal.instance(), RoutePolicyBooleanAsPathIsLocal.instance())
        .testEquals();
  }
}
