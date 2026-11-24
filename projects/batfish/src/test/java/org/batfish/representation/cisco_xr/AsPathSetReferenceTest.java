package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link AsPathSetReference}. */
@ParametersAreNonnullByDefault
final class AsPathSetReferenceTest {

  @Test
  void testSerialization() {
    AsPathSetReference obj = new AsPathSetReference("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    AsPathSetReference obj = new AsPathSetReference("a");
    new EqualsTester()
        .addEqualityGroup(obj, new AsPathSetReference("a"))
        .addEqualityGroup(new AsPathSetReference("b"))
        .testEquals();
  }
}
