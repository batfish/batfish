package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link IosRegexAsPathSetElem}. */
@ParametersAreNonnullByDefault
final class IosRegexAsPathSetElemTest {

  @Test
  void testSerialization() {
    IosRegexAsPathSetElem obj = new IosRegexAsPathSetElem("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    IosRegexAsPathSetElem obj = new IosRegexAsPathSetElem("a");
    new EqualsTester()
        .addEqualityGroup(obj, new IosRegexAsPathSetElem("a"))
        .addEqualityGroup(new IosRegexAsPathSetElem("b"))
        .testEquals();
  }
}
