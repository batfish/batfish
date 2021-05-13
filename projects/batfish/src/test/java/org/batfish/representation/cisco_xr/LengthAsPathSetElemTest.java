package org.batfish.representation.cisco_xr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.junit.Test;

/** Test of {@link org.batfish.representation.cisco_xr.LengthAsPathSetElem}. */
@ParametersAreNonnullByDefault
public final class LengthAsPathSetElemTest {

  @Test
  public void testSerialization() {
    LengthAsPathSetElem obj = new LengthAsPathSetElem(IntComparator.GT, 1, false);
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    LengthAsPathSetElem obj = new LengthAsPathSetElem(IntComparator.GT, 1, false);
    new EqualsTester()
        .addEqualityGroup(obj, new LengthAsPathSetElem(IntComparator.GT, 1, false))
        .addEqualityGroup(new LengthAsPathSetElem(IntComparator.EQ, 1, false))
        .addEqualityGroup(new LengthAsPathSetElem(IntComparator.GT, 2, false))
        .addEqualityGroup(new LengthAsPathSetElem(IntComparator.GT, 1, true))
        .testEquals();
  }
}
