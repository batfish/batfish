package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link AsPathSetVariable}. */
@ParametersAreNonnullByDefault
public final class AsPathSetVariableTest {

  @Test
  public void testSerialization() {
    AsPathSetVariable obj = new AsPathSetVariable("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    AsPathSetVariable obj = new AsPathSetVariable("a");
    new EqualsTester()
        .addEqualityGroup(obj, new AsPathSetVariable("a"))
        .addEqualityGroup(new AsPathSetVariable("b"))
        .testEquals();
  }
}
