package org.batfish.specifier;

import static org.batfish.specifier.ToSpecifierString.toSpecifierString;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Test for {@link ToSpecifierString}. */
public final class ToSpecifierStringTest {
  @Test
  public void testInterfaceLocation() {
    assertEquals("n[i]", toSpecifierString(new InterfaceLocation("n", "i")));
  }

  @Test
  public void testInterfaceLinkLocation() {
    assertEquals("@enter(n[i])", toSpecifierString(new InterfaceLinkLocation("n", "i")));
  }
}
