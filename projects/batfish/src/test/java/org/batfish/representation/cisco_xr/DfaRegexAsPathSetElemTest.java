package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link DfaRegexAsPathSetElem}. */
@ParametersAreNonnullByDefault
public final class DfaRegexAsPathSetElemTest {

  @Test
  public void testSerialization() {
    DfaRegexAsPathSetElem obj = new DfaRegexAsPathSetElem("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    DfaRegexAsPathSetElem obj = new DfaRegexAsPathSetElem("a");
    new EqualsTester()
        .addEqualityGroup(obj, new DfaRegexAsPathSetElem("a"))
        .addEqualityGroup(new DfaRegexAsPathSetElem("b"))
        .testEquals();
  }
}
