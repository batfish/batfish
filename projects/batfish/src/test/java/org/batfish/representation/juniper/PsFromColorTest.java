package org.batfish.representation.juniper;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class PsFromColorTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(new PsFromColor(5), new PsFromColor(5))
        .addEqualityGroup(new PsFromColor(6))
        .testEquals();
  }
}
