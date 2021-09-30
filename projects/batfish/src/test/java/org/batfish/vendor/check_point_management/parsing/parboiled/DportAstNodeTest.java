package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link DportAstNode}. */
public final class DportAstNodeTest {

  @Test
  public void testJavaSerialization() {
    DportAstNode obj = new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    DportAstNode obj = new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1));
    new EqualsTester()
        .addEqualityGroup(
            obj, new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(new DportAstNode("bar", EqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(
            new DportAstNode("foo", LessThanOrEqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(new DportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(2)))
        .testEquals();
  }
}
