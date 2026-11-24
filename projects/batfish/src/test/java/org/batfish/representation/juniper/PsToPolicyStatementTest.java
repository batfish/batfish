package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

final class PsToPolicyStatementTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new PsToPolicyStatement("POLICY"), new PsToPolicyStatement("POLICY"))
        .addEqualityGroup(new PsToPolicyStatement("OTHER"))
        .testEquals();
  }
}
