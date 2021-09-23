package org.batfish.vendor.check_point_management.parsing.parboiled;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link DportAstNode}. */
public final class DportAstNodeTest {

  @Test
  public void testEquals() {
    DportAstNode obj = new DportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1));
    new EqualsTester()
        .addEqualityGroup(obj, new DportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(new DportAstNode(LessThanOrEqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(new DportAstNode(EqualsAstNode.instance(), Uint16AstNode.of(2)))
        .testEquals();
  }
}
