package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link PassesThroughAsPathSetElem}. */
@ParametersAreNonnullByDefault
public final class PassesThroughAsPathSetElemTest {

  @Test
  public void testSerialization() {
    PassesThroughAsPathSetElem obj = new PassesThroughAsPathSetElem(false, Range.singleton(1L));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    PassesThroughAsPathSetElem obj = new PassesThroughAsPathSetElem(false, Range.singleton(1L));
    new EqualsTester()
        .addEqualityGroup(obj, new PassesThroughAsPathSetElem(false, Range.singleton(1L)))
        .addEqualityGroup(
            new PassesThroughAsPathSetElem(false, Range.singleton(1L), Range.singleton(2L)))
        .addEqualityGroup(new PassesThroughAsPathSetElem(true, Range.singleton(1L)))
        .testEquals();
  }
}
