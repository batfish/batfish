package org.batfish.datamodel.flow;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.junit.Test;

/** Test of {@link Accept}. */
@ParametersAreNonnullByDefault
public final class AcceptTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(Accept.INSTANCE, Accept.INSTANCE)
        .testEquals();
  }
}
