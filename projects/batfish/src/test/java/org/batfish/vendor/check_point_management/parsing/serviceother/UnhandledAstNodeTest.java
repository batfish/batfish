package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link UnhandledAstNode}. */
final class UnhandledAstNodeTest {

  @Test
  void testJavaSerialization() {
    UnhandledAstNode obj = UnhandledAstNode.of("foo");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    UnhandledAstNode obj = UnhandledAstNode.of("foo");
    new EqualsTester()
        .addEqualityGroup(obj, UnhandledAstNode.of("foo"))
        .addEqualityGroup(UnhandledAstNode.of("bar"))
        .testEquals();
  }
}
