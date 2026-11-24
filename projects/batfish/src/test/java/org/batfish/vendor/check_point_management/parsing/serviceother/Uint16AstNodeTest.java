package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

/** Test of {@link Uint16AstNode}. */
final class Uint16AstNodeTest {

  @Test
  void testOfValid() {
    assertThat(Uint16AstNode.of("1"), equalTo(Uint16AstNode.of(1)));
  }

  @Test
  void testOfInvalidNotNumber() {
    assertThrows(IllegalArgumentException.class, () -> Uint16AstNode.of("foo"));
  }

  @Test
  void testOfInvalidLow() {
    assertThrows(IllegalArgumentException.class, () -> Uint16AstNode.of("-1"));
  }

  @Test
  void testOfInvalidHigh() {
    assertThrows(IllegalArgumentException.class, () -> Uint16AstNode.of("100000"));
  }

  @Test
  void testEquals() {
    Uint16AstNode obj = Uint16AstNode.of(1);
    new EqualsTester()
        .addEqualityGroup(obj, Uint16AstNode.of(1))
        .addEqualityGroup(Uint16AstNode.of(2))
        .testEquals();
  }
}
