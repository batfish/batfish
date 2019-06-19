package org.batfish.datamodel.flow;

import static org.batfish.datamodel.flow.FibLookup.INSTANCE;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.junit.Test;

/** Test of {@link FibLookup}. */
@ParametersAreNonnullByDefault
public final class FibLookupTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(INSTANCE, INSTANCE)
        .testEquals();
  }
}
