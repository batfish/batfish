package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link OutgoingAstNode}. */
final class OutgoingAstNodeTest {

  @Test
  void testJavaSerialization() {
    OutgoingAstNode obj = new OutgoingAstNode("foo");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    OutgoingAstNode obj = new OutgoingAstNode("foo");
    new EqualsTester()
        .addEqualityGroup(obj, new OutgoingAstNode("foo"))
        .addEqualityGroup(new OutgoingAstNode("bar"))
        .testEquals();
  }
}
