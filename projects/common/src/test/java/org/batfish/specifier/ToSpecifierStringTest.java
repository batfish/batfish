package org.batfish.specifier;

import static org.batfish.specifier.ToSpecifierString.toSpecifierString;
import static org.junit.Assert.assertEquals;

import org.batfish.common.util.isp.IspModelingUtils;
import org.batfish.specifier.parboiled.InternetLocationAstNode;
import org.junit.Test;

/** Test for {@link ToSpecifierString}. */
public final class ToSpecifierStringTest {
  @Test
  public void testInterfaceLocation() {
    assertEquals("n[i]", toSpecifierString(new InterfaceLocation("n", "i")));
    assertEquals(
        "\"001\"[\"/Common/Foo\"]", toSpecifierString(new InterfaceLocation("001", "/Common/Foo")));
  }

  @Test
  public void testInterfaceLinkLocation() {
    assertEquals("@enter(n[i])", toSpecifierString(new InterfaceLinkLocation("n", "i")));
    assertEquals(
        "@enter(\"001\"[\"/Common/Foo\"])",
        toSpecifierString(new InterfaceLinkLocation("001", "/Common/Foo")));
  }

  @Test
  public void testInternet() {
    // special case for the internet source location
    assertEquals(
        IspModelingUtils.INTERNET_HOST_NAME,
        toSpecifierString(InternetLocationAstNode.INTERNET_LOCATION));
  }
}
