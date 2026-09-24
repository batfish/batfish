package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class PsToPolicyStatementTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new PsToPolicyStatement("POLICY"), new PsToPolicyStatement("POLICY"))
        .addEqualityGroup(new PsToPolicyStatement("OTHER"))
        .testEquals();
  }
}
