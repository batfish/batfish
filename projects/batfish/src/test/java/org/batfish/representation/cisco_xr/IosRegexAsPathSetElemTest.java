package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link IosRegexAsPathSetElem}. */
@ParametersAreNonnullByDefault
public final class IosRegexAsPathSetElemTest {

  @Test
  public void testSerialization() {
    IosRegexAsPathSetElem obj = new IosRegexAsPathSetElem("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    IosRegexAsPathSetElem obj = new IosRegexAsPathSetElem("a");
    new EqualsTester()
        .addEqualityGroup(obj, new IosRegexAsPathSetElem("a"))
        .addEqualityGroup(new IosRegexAsPathSetElem("b"))
        .testEquals();
  }
}
