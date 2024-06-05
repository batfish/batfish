package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link NeighborIsAsPathSetElem}. */
@ParametersAreNonnullByDefault
public final class NeighborIsAsPathSetElemTest {

  @Test
  public void testSerialization() {
    NeighborIsAsPathSetElem obj = new NeighborIsAsPathSetElem(false, Range.singleton(1L));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    NeighborIsAsPathSetElem obj = new NeighborIsAsPathSetElem(false, Range.singleton(1L));
    new EqualsTester()
        .addEqualityGroup(obj, new NeighborIsAsPathSetElem(false, Range.singleton(1L)))
        .addEqualityGroup(
            new NeighborIsAsPathSetElem(false, Range.singleton(1L), Range.singleton(2L)))
        .addEqualityGroup(new NeighborIsAsPathSetElem(true, Range.singleton(1L)))
        .testEquals();
  }
}
