package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link Uint16AstNode}. */
public final class Uint16AstNodeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testOfValid() {
    assertThat(Uint16AstNode.of("1"), equalTo(Uint16AstNode.of(1)));
  }

  @Test
  public void testOfInvalidNotNumber() {
    _thrown.expect(IllegalArgumentException.class);
    Uint16AstNode.of("foo");
  }

  @Test
  public void testOfInvalidLow() {
    _thrown.expect(IllegalArgumentException.class);
    Uint16AstNode.of("-1");
  }

  @Test
  public void testOfInvalidHigh() {
    _thrown.expect(IllegalArgumentException.class);
    Uint16AstNode.of("100000");
  }

  @Test
  public void testEquals() {
    Uint16AstNode obj = Uint16AstNode.of(1);
    new EqualsTester()
        .addEqualityGroup(obj, Uint16AstNode.of(1))
        .addEqualityGroup(Uint16AstNode.of(2))
        .testEquals();
  }
}
