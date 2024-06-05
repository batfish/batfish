package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link UnimplementedBoolean}. */
@ParametersAreNonnullByDefault
public final class UnimplementedBooleanTest {
  @Test
  public void testSerialization() {
    assertThat(
        SerializationUtils.clone(UnimplementedBoolean.instance()),
        equalTo(UnimplementedBoolean.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(UnimplementedBoolean.instance(), UnimplementedBoolean.instance())
        .testEquals();
  }
}
