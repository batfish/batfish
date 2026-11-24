package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link LessThanAstNode}. */
final class LessThanAstNodeTest {

  @Test
  void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(LessThanAstNode.instance()), equalTo(LessThanAstNode.instance()));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(LessThanAstNode.instance(), LessThanAstNode.instance())
        .testEquals();
  }
}
