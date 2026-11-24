package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link UhDportAstNode}. */
final class UhDportAstNodeTest {

  @Test
  void testJavaSerialization() {
    UhDportAstNode obj = new UhDportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    UhDportAstNode obj = new UhDportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1));
    new EqualsTester()
        .addEqualityGroup(
            obj, new UhDportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(new UhDportAstNode("bar", EqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(
            new UhDportAstNode("foo", LessThanOrEqualsAstNode.instance(), Uint16AstNode.of(1)))
        .addEqualityGroup(new UhDportAstNode("foo", EqualsAstNode.instance(), Uint16AstNode.of(2)))
        .testEquals();
  }
}
