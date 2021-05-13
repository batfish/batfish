package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link AsPathSetReference}. */
@ParametersAreNonnullByDefault
public final class AsPathSetReferenceTest {

  @Test
  public void testSerialization() {
    AsPathSetReference obj = new AsPathSetReference("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    AsPathSetReference obj = new AsPathSetReference("a");
    new EqualsTester()
        .addEqualityGroup(obj, new AsPathSetReference("a"))
        .addEqualityGroup(new AsPathSetReference("b"))
        .testEquals();
  }
}
