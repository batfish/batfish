package org.batfish.specifier;

import static org.batfish.specifier.ToSpecifierString.toSpecifierString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.batfish.common.util.isp.IspModelingUtils;
import org.batfish.specifier.parse.InternetLocationAstNode;
import org.junit.jupiter.api.Test;

/** Test for {@link ToSpecifierString}. */
final class ToSpecifierStringTest {
  @Test
  void testInterfaceLocation() {
    assertEquals("n[i]", toSpecifierString(new InterfaceLocation("n", "i")));
    assertEquals(
        "\"001\"[\"/Common/Foo\"]", toSpecifierString(new InterfaceLocation("001", "/Common/Foo")));
  }

  @Test
  void testInterfaceLinkLocation() {
    assertEquals("@enter(n[i])", toSpecifierString(new InterfaceLinkLocation("n", "i")));
    assertEquals(
        "@enter(\"001\"[\"/Common/Foo\"])",
        toSpecifierString(new InterfaceLinkLocation("001", "/Common/Foo")));
  }

  @Test
  void testInternet() {
    // special case for the internet source location
    assertEquals(
        IspModelingUtils.INTERNET_HOST_NAME,
        toSpecifierString(InternetLocationAstNode.INTERNET_LOCATION));
  }
}
