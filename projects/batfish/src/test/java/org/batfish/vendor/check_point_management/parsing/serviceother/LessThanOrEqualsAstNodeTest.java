package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link LessThanOrEqualsAstNode}. */
final class LessThanOrEqualsAstNodeTest {

  @Test
  void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(LessThanOrEqualsAstNode.instance()),
        equalTo(LessThanOrEqualsAstNode.instance()));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(LessThanOrEqualsAstNode.instance(), LessThanOrEqualsAstNode.instance())
        .testEquals();
  }
}
