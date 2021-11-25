package org.batfish.vendor;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import org.junit.Test;

public class ParsingContextTest extends TestCase {

  private static class MockVendorParsingContext implements VendorParsingContext {}

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new ParsingContext(), new ParsingContext())
        .addEqualityGroup(new ParsingContext().setSonicConfigDbs(new MockVendorParsingContext()))
        .testEquals();
  }
}
