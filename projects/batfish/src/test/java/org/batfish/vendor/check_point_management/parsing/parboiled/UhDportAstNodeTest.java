package org.batfish.vendor.check_point_management.parsing.parboiled;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link UhDportAstNode}. */
public final class UhDportAstNodeTest {

  @Test
  public void testEquals() {
    UhDportAstNode obj = new UhDportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1));
    new EqualsTester()
        .addEqualityGroup(obj, new UhDportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(
            new UhDportAstNode(LessThanOrEqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(new UhDportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(2)))
        .testEquals();
  }
}
