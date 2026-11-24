package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link IncomingAstNode}. */
final class IncomingAstNodeTest {

  @Test
  void testJavaSerialization() {
    IncomingAstNode obj = new IncomingAstNode("foo");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    IncomingAstNode obj = new IncomingAstNode("foo");
    new EqualsTester()
        .addEqualityGroup(obj, new IncomingAstNode("foo"))
        .addEqualityGroup(new IncomingAstNode("bar"))
        .testEquals();
  }
}
