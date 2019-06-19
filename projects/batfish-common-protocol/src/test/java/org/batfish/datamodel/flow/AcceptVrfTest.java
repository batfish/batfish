package org.batfish.datamodel.flow;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.junit.Test;

/** Test of {@link AcceptVrf}. */
@ParametersAreNonnullByDefault
public final class AcceptVrfTest {

  @Test
  public void testEquals() {
    AcceptVrf f = new AcceptVrf("a");
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(f, f, new AcceptVrf("a"))
        .addEqualityGroup(new AcceptVrf("b"))
        .testEquals();
  }
}
